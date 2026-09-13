# AgriSmart AI Backend Service

Production-ready FastAPI backend for plant disease recognition. Serves both Android and Web apps via REST API endpoints.

---

## Features

- **High Accuracy & Fast CPU Inference**: Uses PyTorch + `timm` pre-loaded in RAM (~100ms per prediction).
- **Dual Prediction Endpoints**:
  - `POST /predict/image` - Multipart image file upload (Camera or Gallery).
  - `POST /predict/url` - Web image URL prediction (`http://` or `https://`).
- **Comprehensive Diagnosis**: Returns confidence score, top-5 predictions, organic remedies, chemical treatments, and prevention measures.
- **CORS Enabled**: Configured with `allow_origins=["*"]` for Android, Web app, and third-party API consumers.
- **Interactive Documentation**: Auto-generated Swagger UI at `/docs`.

---

## Local Setup & Testing

### 1. Install Dependencies
```bash
cd backend
pip install -r requirements.txt
```

### 2. Run Server
```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```
Open your browser at `http://localhost:8000/docs` to test endpoints interactively.

---

## API Examples

### 1. Health Check
`GET http://localhost:8000/health`

### 2. Predict from Web Image URL
`POST http://localhost:8000/predict/url`

**Request Body:**
```json
{
  "url": "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTengOJgGoCt0T0QCW6SzRzqNVC1rZmiZCk4whV0wjxog&s=10",
  "crop": "apple",
  "top_k": 5
}
```

**Response:**
```json
{
  "success": true,
  "crop": "Apple",
  "disease": "Apple Scab",
  "raw_label": "Apple___Apple_scab",
  "confidence": 0.85,
  "confidence_percentage": "85.0%",
  "is_healthy": false,
  "treatments": {
    "severity": "Moderate",
    "organic": ["Apply neem oil spray weekly.", "Prune infected leaves."],
    "chemical": ["Apply copper hydroxide or captan fungicide."],
    "prevention": ["Avoid overhead irrigation; water at soil level."]
  },
  "top_predictions": [
    {
      "raw_label": "Apple___Apple_scab",
      "crop": "Apple",
      "disease": "Apple Scab",
      "probability": 0.85,
      "percentage": "85.0%"
    }
  ]
}
```

### 3. Predict from Image File Upload
`POST http://localhost:8000/predict/image`

**cURL command:**
```bash
curl -X POST "http://localhost:8000/predict/image" \
  -H "accept: application/json" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@leaf.jpg" \
  -F "crop=tomato"
```

---

## Deploy to Render or Railway

### Deploy on Render
1. Create a new **Web Service** on [Render](https://render.com/).
2. Connect your Git repository.
3. Set **Root Directory** to `backend`.
4. Set **Build Command** to `pip install -r requirements.txt`.
5. Set **Start Command** to `uvicorn app.main:app --host 0.0.0.0 --port $PORT`.

### Deploy on Railway
1. Create a new project on [Railway](https://railway.app/).
2. Select **Deploy from GitHub repo**.
3. Railway automatically detects `Dockerfile` or `Procfile` and deploys your service.
