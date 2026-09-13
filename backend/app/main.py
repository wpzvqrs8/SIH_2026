"""AgriSmart AI FastAPI Main Service Application."""
from typing import Optional
from fastapi import FastAPI, File, Form, HTTPException, UploadFile, status
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, HttpUrl

from app.predictor import get_crops_catalog, load_model, locate_weights, predict

app = FastAPI(
    title="AgriSmart AI Disease Recognition API",
    description="Production REST API for crop disease recognition using deep learning (PyTorch/timm). Supports both image upload and image URL predictions.",
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

class PredictUrlRequest(BaseModel):
    url: str
    crop: Optional[str] = None
    top_k: Optional[int] = 5

@app.on_event("startup")
def startup_event():
    """Pre-warm model into RAM on server boot."""
    try:
        load_model()
        print(f"[AgriSmart API] Successfully pre-loaded weights from {locate_weights()}")
    except Exception as e:
        print(f"[AgriSmart API] Warning: Failed to pre-warm model on boot: {e}")

@app.get("/", tags=["Health"])
@app.get("/health", tags=["Health"])
def health_check():
    """Verify backend API status and model readiness."""
    try:
        _, _, labels, crop_classes, backbone, _ = load_model()
        return {
            "status": "online",
            "service": "AgriSmart AI API",
            "model_ready": True,
            "total_classes": len(labels),
            "total_crops": len(crop_classes),
            "backbone": backbone,
            "weights_location": str(locate_weights())
        }
    except Exception as e:
        return {
            "status": "degraded",
            "service": "AgriSmart AI API",
            "model_ready": False,
            "error": str(e)
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

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
