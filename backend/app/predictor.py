"""Ultra-lightweight ONNX Runtime Predictor Engine for AgriSmart Backend."""
import io
import json
import urllib.request
from pathlib import Path
from typing import Optional, Tuple, Union

import numpy as np
import onnxruntime as ort
from PIL import Image

from app.treatments import get_treatment_info

REPO_ROOT = Path(__file__).resolve().parent.parent.parent
BACKEND_DIR = Path(__file__).resolve().parent.parent

import zipfile

ZIP_URL = "https://github.com/wpzvqrs8/SIH_2026/releases/download/offline-app-v1/AgriSmart-offline-windows.zip"

def locate_onnx_model() -> Path:
    candidates = [
        BACKEND_DIR / "india_v1.onnx",
        REPO_ROOT / "offline" / "web" / "models" / "india_v1.onnx",
        REPO_ROOT / "offline" / "android" / "app" / "src" / "main" / "assets" / "web" / "models" / "india_v1.onnx",
        Path("india_v1.onnx"),
    ]
    for c in candidates:
        if c.is_file() and c.stat().st_size > 1000000:
            return c.resolve()
    import tempfile
    return Path(tempfile.gettempdir()) / "india_v1.onnx"

def locate_models_json() -> Path:
    candidates = [
        BACKEND_DIR / "models.json",
        REPO_ROOT / "offline" / "web" / "models" / "models.json",
        REPO_ROOT / "offline" / "android" / "app" / "src" / "main" / "assets" / "web" / "models" / "models.json",
        Path("models.json"),
    ]
    for c in candidates:
        if c.is_file() and c.stat().st_size > 100:
            return c.resolve()
    import tempfile
    return Path(tempfile.gettempdir()) / "models.json"

_SESSION_CACHE = None
_META_CACHE = None

def load_session():
    global _SESSION_CACHE, _META_CACHE
    if _SESSION_CACHE is not None and _META_CACHE is not None:
        return _SESSION_CACHE, _META_CACHE

    onnx_path = locate_onnx_model()
    json_path = locate_models_json()

    if not (onnx_path.is_file() and onnx_path.stat().st_size > 1000000) or not (json_path.is_file() and json_path.stat().st_size > 100):
        print(f"[AgriSmart ONNX Engine] Downloading model release zip from {ZIP_URL}...")
        onnx_path.parent.mkdir(parents=True, exist_ok=True)
        json_path.parent.mkdir(parents=True, exist_ok=True)

        req = urllib.request.Request(ZIP_URL, headers={"User-Agent": "Mozilla/5.0"})
        with urllib.request.urlopen(req) as resp:
            zip_bytes = io.BytesIO(resp.read())
            with zipfile.ZipFile(zip_bytes) as z:
                for name in z.namelist():
                    if name.endswith("india_v1.onnx"):
                        with z.open(name) as src, open(onnx_path, "wb") as dst:
                            dst.write(src.read())
                    elif name.endswith("models.json"):
                        with z.open(name) as src, open(json_path, "wb") as dst:
                            dst.write(src.read())
        print("[AgriSmart ONNX Engine] Extracted india_v1.onnx and models.json successfully!")

    with open(json_path, "r", encoding="utf-8") as f:
        meta_data = json.load(f)

    # Use first model definition (india_v1)
    model_meta = meta_data["models"][0]

    # Create ONNX Runtime Inference Session (CPU execution)
    opts = ort.SessionOptions()
    opts.intra_op_num_threads = 1
    opts.inter_op_num_threads = 1
    session = ort.InferenceSession(str(onnx_path), sess_options=opts, providers=["CPUExecutionProvider"])

    _SESSION_CACHE = session
    _META_CACHE = model_meta
    return _SESSION_CACHE, _META_CACHE

def process_image(image_input: Union[Image.Image, bytes, str]) -> Image.Image:
    if isinstance(image_input, Image.Image):
        return image_input.convert("RGB")
    if isinstance(image_input, bytes):
        return Image.open(io.BytesIO(image_input)).convert("RGB")
    if isinstance(image_input, str) and (image_input.startswith("http://") or image_input.startswith("https://")):
        req = urllib.request.Request(image_input, headers={"User-Agent": "Mozilla/5.0"})
        with urllib.request.urlopen(req) as resp:
            return Image.open(io.BytesIO(resp.read())).convert("RGB")
    return Image.open(image_input).convert("RGB")

def preprocess_tensor(img: Image.Image) -> np.ndarray:
    resized = img.resize((224, 224), Image.BICUBIC)
    arr = np.array(resized, dtype=np.float32) / 255.0
    mean = np.array([0.485, 0.456, 0.406], dtype=np.float32)
    std = np.array([0.229, 0.224, 0.225], dtype=np.float32)
    arr = (arr - mean) / std
    arr = arr.transpose(2, 0, 1)
    return arr[np.newaxis, ...]

def predict(
    image_input: Union[Image.Image, bytes, str],
    crop_filter: Optional[str] = None,
    top_k: int = 5
) -> dict:
    session, meta = load_session()
    pil_img = process_image(image_input)
    tensor = preprocess_tensor(pil_img)

    # Standard forward pass
    logits = session.run(None, {"pixels": tensor})[0][0]

    # Test-time augmentation (horizontal flip)
    flipped_pil = pil_img.transpose(Image.FLIP_LEFT_RIGHT)
    flipped_tensor = preprocess_tensor(flipped_pil)
    flipped_logits = session.run(None, {"pixels": flipped_tensor})[0][0]

    # Softmax probabilities
    exp_a = np.exp(logits - np.max(logits))
    probs_a = exp_a / np.sum(exp_a)

    exp_b = np.exp(flipped_logits - np.max(flipped_logits))
    probs_b = exp_b / np.sum(exp_b)

    probs = (probs_a + probs_b) / 2.0

    labels = meta["labels"]
    crop_classes = meta["crop_classes"]

    # Filter by crop if specified
    if crop_filter:
        c_lower = crop_filter.strip().lower().replace(" ", "_")
        if c_lower in crop_classes:
            allowed_indices = set(crop_classes[c_lower])
            mask = np.zeros_like(probs)
            for idx in allowed_indices:
                mask[idx] = 1.0
            probs = probs * mask
            total = np.sum(probs)
            if total > 0:
                probs = probs / total

    top_indices = np.argsort(-probs)[:min(top_k, len(labels))]

    top_predictions = []
    for idx in top_indices:
        item = labels[idx]
        c_name = item["crop"].replace("_", " ").title()
        d_name = item["label"].replace("_", " ").title()
        p_val = float(probs[idx])
        top_predictions.append({
            "raw_label": f"{item['crop']}::${item['label']}",
            "crop": c_name,
            "disease": d_name,
            "probability": round(p_val, 4),
            "percentage": f"{round(p_val * 100, 2)}%"
        })

    best = top_predictions[0]
    is_healthy = "healthy" in best["disease"].lower()
    treatments = get_treatment_info(best["disease"])

    return {
        "success": True,
        "crop": best["crop"],
        "disease": best["disease"],
        "raw_label": best["raw_label"],
        "confidence": best["probability"],
        "confidence_percentage": best["percentage"],
        "is_healthy": is_healthy,
        "treatments": treatments,
        "top_predictions": top_predictions,
        "model_metadata": {
            "engine": "ONNX Runtime (CPU)",
            "model_id": meta["id"],
            "total_crops": len(crop_classes),
            "total_classes": len(labels),
            "ram_footprint": "~45 MB"
        }
    }

def get_crops_catalog() -> dict:
    _, meta = load_session()
    crop_classes = meta["crop_classes"]
    labels = meta["labels"]
    catalog = {}
    for crop_key, idxs in crop_classes.items():
        crop_display = crop_key.replace("_", " ").title()
        diseases = set()
        for idx in idxs:
            diseases.add(labels[idx]["label"].replace("_", " ").title())
        catalog[crop_display] = sorted(list(diseases))
    return catalog
