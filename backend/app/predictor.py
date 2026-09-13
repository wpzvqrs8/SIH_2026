"""PyTorch Model Predictor Engine for AgriSmart Backend."""
import io
import urllib.request
from functools import lru_cache
from pathlib import Path
from typing import Dict, List, Optional, Tuple, Union

import timm
import torch
from PIL import Image

from app.treatments import get_treatment_info

# Base repository root directory relative to backend/app/predictor.py
REPO_ROOT = Path(__file__).resolve().parent.parent.parent

def locate_weights() -> Path:
    candidates = [
        Path(__file__).resolve().parent.parent / "model.pt",
        REPO_ROOT / "model" / "model.pt",
        REPO_ROOT / "model" / "india" / "model.pt",
        Path("model.pt"),
    ]
    for candidate in candidates:
        if candidate.exists():
            return candidate.resolve()
    # Fallback to default expected path
    return REPO_ROOT / "model" / "model.pt"

WEIGHTS_PATH = locate_weights()

@lru_cache(maxsize=1)
def load_model(weights_path: str = str(WEIGHTS_PATH)):
    p = Path(weights_path)
    if not p.exists():
        url = "https://github.com/wpzvqrs8/SIH_2026/releases/download/india-model-v1/model.pt"
        print(f"[AgriSmart Predictor] Model file missing at {p}. Downloading from {url}...")
        p.parent.mkdir(parents=True, exist_ok=True)
        urllib.request.urlretrieve(url, p)
        print("[AgriSmart Predictor] Model weights download complete!")

    try:
        pkg = torch.load(p, map_location="cpu", weights_only=True)
    except Exception:
        pkg = torch.load(p, map_location="cpu", weights_only=False)

    backbone = pkg.get("backbone", "resnet34")
    labels = pkg["labels"]
    img_size = pkg.get("img_size", 224)
    mean = pkg.get("mean", [0.485, 0.456, 0.406])
    std = pkg.get("std", [0.229, 0.224, 0.225])

    kwargs = {"img_size": img_size} if "vit" in backbone else {}
    model = timm.create_model(backbone, pretrained=False, num_classes=len(labels), **kwargs)
    model.load_state_dict(pkg["state_dict"])
    model.eval()

    tf = timm.data.create_transform(
        input_size=img_size,
        interpolation="bicubic",
        mean=mean,
        std=std,
        crop_pct=0.9
    )

    # Dynamic crop_classes construction
    if "crop_classes" in pkg:
        crop_classes = pkg["crop_classes"]
    else:
        crop_classes = {}
        for idx, label in enumerate(labels):
            if "::" in label:
                crop = label.split("::", 1)[0].lower()
            elif "___" in label:
                crop = label.split("___", 1)[0].lower()
            else:
                crop = "general"
            crop_classes.setdefault(crop, []).append(idx)

    return model, tf, labels, crop_classes, backbone, img_size

def process_image(image_input: Union[Image.Image, bytes, str]) -> Image.Image:
    if isinstance(image_input, Image.Image):
        return image_input.convert("RGB")
    if isinstance(image_input, bytes):
        return Image.open(io.BytesIO(image_input)).convert("RGB")
    if isinstance(image_input, str) and (image_input.startswith("http://") or image_input.startswith("https://")):
        req = urllib.request.Request(image_input, headers={"User-Agent": "AgriSmart/1.0"})
        with urllib.request.urlopen(req) as resp:
            return Image.open(io.BytesIO(resp.read())).convert("RGB")
    return Image.open(image_input).convert("RGB")

def format_label_details(raw_label: str) -> Tuple[str, str]:
    if "::" in raw_label:
        parts = raw_label.split("::", 1)
        crop_raw, disease_raw = parts[0], parts[1]
    elif "___" in raw_label:
        parts = raw_label.split("___", 1)
        crop_raw, disease_raw = parts[0], parts[1]
    else:
        crop_raw, disease_raw = "General", raw_label

    crop_clean = crop_raw.replace("_", " ").strip().title()
    disease_clean = disease_raw.replace("_", " ").replace("  ", " ").strip().title()
    return crop_clean, disease_clean

@torch.no_grad()
def predict(
    image_input: Union[Image.Image, bytes, str],
    crop_filter: Optional[str] = None,
    top_k: int = 5
) -> dict:
    model, tf, labels, crop_classes, backbone, img_size = load_model()
    pil_img = process_image(image_input)
    tensor_img = tf(pil_img).unsqueeze(0)

    logits = model(tensor_img)
    probs = torch.softmax(logits, dim=1)[0]

    # Test-time augmentation (horizontal flip)
    flipped_tensor = tf(pil_img.transpose(Image.FLIP_LEFT_RIGHT)).unsqueeze(0)
    flipped_probs = torch.softmax(model(flipped_tensor), dim=1)[0]
    probs = (probs + flipped_probs) / 2.0

    # Apply crop filtering if specified
    if crop_filter:
        c_lower = crop_filter.strip().lower()
        if c_lower in crop_classes:
            mask = torch.zeros_like(probs)
            mask[crop_classes[c_lower]] = 1.0
            probs = probs * mask
            total = probs.sum()
            if total > 0:
                probs = probs / total

    top_probs, top_indices = torch.topk(probs, min(top_k, len(labels)))

    top_predictions = []
    for p, idx in zip(top_probs.tolist(), top_indices.tolist()):
        raw_lbl = labels[idx]
        c_name, d_name = format_label_details(raw_lbl)
        top_predictions.append({
            "raw_label": raw_lbl,
            "crop": c_name,
            "disease": d_name,
            "probability": round(p, 4),
            "percentage": f"{round(p * 100, 2)}%"
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
            "backbone": backbone,
            "image_resolution": f"{img_size}x{img_size}",
            "weights_file": WEIGHTS_PATH.name
        }
    }

def get_crops_catalog() -> dict:
    model, _, labels, crop_classes, _, _ = load_model()
    catalog = {}
    for crop_key, idxs in crop_classes.items():
        crop_display = crop_key.replace("_", " ").title()
        diseases = set()
        for idx in idxs:
            _, d_name = format_label_details(labels[idx])
            diseases.add(d_name)
        catalog[crop_display] = sorted(list(diseases))
    return catalog
