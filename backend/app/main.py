"""AgriSmart AI FastAPI Main Service Application."""
from pathlib import Path
from typing import Optional
from fastapi import FastAPI, File, Form, HTTPException, UploadFile, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse, HTMLResponse
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel, HttpUrl

try:
    from app.predictor import get_crops_catalog, load_session, predict
except ModuleNotFoundError:
    from backend.app.predictor import get_crops_catalog, load_session, predict


app = FastAPI(
    title="AgriSmart AI Disease Recognition API",
    description="Production REST API for crop disease recognition using ONNX Runtime CPU Engine (~45MB RAM footprint).",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# Enable CORS for Android client app and Web client app
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

def locate_webapp_dir() -> Path:
    """Robust resolution to locate webapp/index.html across local and container environments."""
    this_file = Path(__file__).resolve()
    
    # 1. Direct candidates (backend/webapp first, then root/webapp)
    candidates = [
        this_file.parent.parent / "webapp",        # backend/webapp
        this_file.parent.parent.parent / "webapp", # root/webapp
        Path.cwd() / "webapp",
        Path.cwd() / "backend" / "webapp",
    ]
    for c in candidates:
        if c.is_dir() and (c / "index.html").is_file():
            print(f"[AgriSmart API] Found webapp directory at: {c.resolve()}")
            return c.resolve()
    
    # 2. Glob search across project tree
    for parent in [this_file.parent.parent.parent, this_file.parent.parent, Path.cwd()]:
        try:
            for child in parent.glob("**/webapp/index.html"):
                if child.is_file():
                    print(f"[AgriSmart API] Found webapp directory via glob at: {child.parent.resolve()}")
                    return child.parent.resolve()
        except Exception:
            pass

    # 3. Fallback
    fallback = this_file.parent.parent / "webapp"
    print(f"[AgriSmart API] Using default webapp path: {fallback}")
    return fallback

def locate_samples_dir() -> Path:
    this_file = Path(__file__).resolve()
    candidates = [
        this_file.parent.parent / "samples",
        this_file.parent.parent.parent / "samples",
        Path.cwd() / "samples",
        Path.cwd() / "backend" / "samples",
    ]
    for c in candidates:
        if c.is_dir():
            return c.resolve()
    return (this_file.parent.parent.parent / "samples").resolve()

WEBAPP_DIR = locate_webapp_dir()
SAMPLES_DIR = locate_samples_dir()

if SAMPLES_DIR.is_dir():
    app.mount("/samples", StaticFiles(directory=str(SAMPLES_DIR)), name="samples")

class PredictUrlRequest(BaseModel):
    url: str
    crop: Optional[str] = None
    top_k: Optional[int] = 5

@app.on_event("startup")
def startup_event():
    """Pre-warm ONNX model into memory on boot."""
    try:
        load_session()
        print("[AgriSmart API] Successfully pre-loaded ONNX model session into memory (~45MB RAM)")
    except Exception as e:
        print(f"[AgriSmart API] Warning: Failed to pre-warm ONNX model on boot: {e}")

@app.get("/health", tags=["Health"])
def health_check():
    """Verify backend API status and model readiness."""
    try:
        session, meta = load_session()
        return {
            "status": "online",
            "service": "AgriSmart AI API",
            "model_ready": True,
            "engine": "ONNX Runtime (CPU)",
            "model_id": meta["id"],
            "total_classes": len(meta["labels"]),
            "total_crops": len(meta["crop_classes"]),
            "ram_footprint": "~45 MB"
        }
    except Exception as e:
        import traceback
        return {
            "status": "degraded",
            "service": "AgriSmart AI API",
            "model_ready": False,
            "error": str(e),
            "traceback": traceback.format_exc()
        }

@app.get("/crops", tags=["Catalog"])
def list_crops():
    """Get catalog of supported crops and their respective disease classes."""
    try:
        catalog = get_crops_catalog()
        return {
            "success": True,
            "total_crops": len(catalog),
            "crops": catalog
        }
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to fetch crops catalog: {str(e)}"
        )

@app.post("/predict/image", tags=["Prediction"])
async def predict_image(
    file: UploadFile = File(..., description="Image leaf file (JPG, PNG, WEBP)"),
    crop: Optional[str] = Form(None, description="Optional crop filter name (e.g. tomato, apple)"),
    top_k: int = Form(5, description="Number of top predictions to return")
):
    """Predict crop disease from uploaded image file."""
    if not file.content_type.startswith("image/"):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid file format. Please upload a valid image (JPEG/PNG/WEBP)."
        )
    
    try:
        contents = await file.read()
        result = predict(image_input=contents, crop_filter=crop, top_k=top_k)
        return result
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Image prediction failed: {str(e)}"
        )

@app.post("/predict/url", tags=["Prediction"])
def predict_url(payload: PredictUrlRequest):
    """Predict crop disease directly from a public web image URL."""
    if not (payload.url.startswith("http://") or payload.url.startswith("https://")):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid URL protocol. Must start with http:// or https://"
        )

    try:
        result = predict(image_input=payload.url, crop_filter=payload.crop, top_k=payload.top_k or 5)
        return result
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"URL prediction failed: {str(e)}"
        )

# Web Application Frontend Routes
@app.get("/", tags=["Frontend"])
async def serve_frontend_index():
    """Serve Plant Disease Testing Web App homepage."""
    target_dir = locate_webapp_dir()
    index_file = target_dir / "index.html"
    if index_file.is_file():
        return FileResponse(index_file)
    
    # Secondary check in root webapp
    root_index = Path.cwd() / "webapp" / "index.html"
    if root_index.is_file():
        return FileResponse(root_index)

    raise HTTPException(
        status_code=status.HTTP_404_NOT_FOUND,
        detail=f"Plant Disease Testing WebApp index.html not found at {index_file}"
    )

@app.get("/{file_path:path}", include_in_schema=False)
async def serve_webapp_static_file(file_path: str):
    """Serve static webapp files (styles.css, app.js, icons, etc)."""
    target_dir = locate_webapp_dir()
    target = (target_dir / file_path).resolve()
    if target.is_file() and str(target).startswith(str(target_dir)):
        return FileResponse(target)
    
    # Fallback to root webapp
    root_target = (Path.cwd() / "webapp" / file_path).resolve()
    if root_target.is_file():
        return FileResponse(root_target)

    raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Not Found")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
