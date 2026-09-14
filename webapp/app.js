/**
 * AgriSmart AI - Plant Disease Testing Web Application Logic
 */

// Application State
let currentMode = 'camera'; // 'camera' | 'storage'
let mediaStream = null;
let facingMode = 'environment'; // 'user' | 'environment'
let currentImageBlob = null;
let currentPredictionResult = null;
let isSpeaking = false;

// Configurable API Endpoint Base
const API_BASE = window.location.origin;

// Sample Local Storage / Preset Images
const SAMPLE_IMAGES = [
  { name: 'Tomato Late Blight', path: '/samples/tomato_late_blight_1.jpg', crop: 'Tomato' },
  { name: 'Potato Early Blight', path: '/samples/potato_early_blight_1.jpg', crop: 'Potato' },
  { name: 'Corn Common Rust', path: '/samples/corn_common_rust_1.jpg', crop: 'Corn' },
  { name: 'Tomato Healthy', path: '/samples/tomato_healthy_1.jpg', crop: 'Tomato' }
];

// Initialize application on DOM ready
document.addEventListener('DOMContentLoaded', () => {
  initCropsCatalog();
  initDragAndDrop();
  initPresetsGrid();
  startCamera();
});

/**
 * Fetch supported crops catalog from backend to populate select dropdown
 */
async function initCropsCatalog() {
  const cropSelect = document.getElementById('cropSelect');
  try {
    const resp = await fetch(`${API_BASE}/crops`);
    if (resp.ok) {
      const data = await resp.json();
      if (data.success && data.crops) {
        Object.keys(data.crops).sort().forEach(crop => {
          const opt = document.createElement('option');
          opt.value = crop.toLowerCase();
          opt.textContent = `${crop} (${data.crops[crop].length} diseases)`;
          cropSelect.appendChild(opt);
        });
      }
    }
  } catch (err) {
    console.warn('[AgriSmart UI] Backend crops catalog load warning:', err);
  }
}

/**
 * Mode Switching (Camera vs Storage)
 */
function switchMode(mode) {
  currentMode = mode;
  const tabCamera = document.getElementById('tabCamera');
  const tabStorage = document.getElementById('tabStorage');
  const cameraSection = document.getElementById('cameraSection');
  const storageSection = document.getElementById('storageSection');

  if (mode === 'camera') {
    tabCamera.classList.add('active');
    tabStorage.classList.remove('active');
    cameraSection.style.display = 'block';
    storageSection.style.display = 'none';
    startCamera();
  } else {
    tabStorage.classList.add('active');
    tabCamera.classList.remove('active');
    cameraSection.style.display = 'none';
    storageSection.style.display = 'block';
    stopCamera();
  }
}

/**
 * Camera Stream Management using WebRTC
 */
async function startCamera() {
  stopCamera();
  const video = document.getElementById('cameraVideo');
  const preview = document.getElementById('capturedPreview');
  const overlay = document.getElementById('cameraOverlay');
  const captureBtn = document.getElementById('captureBtn');

  video.style.display = 'block';
  preview.style.display = 'none';
  overlay.style.display = 'flex';
  captureBtn.innerHTML = '📸 Take Direct Photo';

  try {
    mediaStream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: facingMode, width: { ideal: 1280 }, height: { ideal: 960 } },
      audio: false
    });
    video.srcObject = mediaStream;
    document.getElementById('apiStatusText').textContent = 'Camera Ready & Backend Online';
  } catch (err) {
    console.error('[AgriSmart UI] Camera access error:', err);
    showToast('Camera access blocked or unavailable. Switching to File Upload mode.');
    switchMode('storage');
  }
}

function stopCamera() {
  if (mediaStream) {
    mediaStream.getTracks().forEach(track => track.stop());
    mediaStream = null;
  }
}

function toggleCameraFacing() {
  facingMode = (facingMode === 'environment') ? 'user' : 'environment';
  startCamera();
}

/**
 * Capture Photo directly from Camera Video Feed
 */
function captureCameraPhoto() {
  const video = document.getElementById('cameraVideo');
  const preview = document.getElementById('capturedPreview');
  const overlay = document.getElementById('cameraOverlay');
  const flash = document.getElementById('shutterFlash');

  if (!video.srcObject) {
    showToast('Camera stream not active');
    return;
  }

  // Visual Shutter Flash Effect
  flash.classList.add('flash');
  setTimeout(() => flash.classList.remove('flash'), 300);

  // Capture frame to Canvas
  const canvas = document.createElement('canvas');
  canvas.width = video.videoWidth || 640;
  canvas.height = video.videoHeight || 480;
  const ctx = canvas.getContext('2d');
  ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

  canvas.toBlob(blob => {
    currentImageBlob = blob;
    const url = URL.createObjectURL(blob);
    preview.src = url;
    video.style.display = 'none';
    preview.style.display = 'block';
    overlay.style.display = 'none';
    stopCamera();

    // Directly trigger backend analysis
    analyzeCurrentImage();
  }, 'image/jpeg', 0.92);
}

/**
 * Drag and Drop & Local File Selection
 */
function initDragAndDrop() {
  const dropzone = document.getElementById('dropzone');

  ['dragenter', 'dragover'].forEach(eventName => {
    dropzone.addEventListener(eventName, (e) => {
      e.preventDefault();
      dropzone.classList.add('dragover');
    }, false);
  });

  ['dragleave', 'drop'].forEach(eventName => {
    dropzone.addEventListener(eventName, (e) => {
      e.preventDefault();
      dropzone.classList.remove('dragover');
    }, false);
  });

  dropzone.addEventListener('drop', (e) => {
    const files = e.dataTransfer.files;
    if (files && files.length > 0) {
      processFile(files[0]);
    }
  });
}

function triggerFileInput() {
  document.getElementById('fileInput').click();
}

function handleFileSelect(event) {
  const file = event.target.files[0];
  if (file) {
    processFile(file);
  }
}

function processFile(file) {
  if (!file.type.startsWith('image/')) {
    showToast('Please select a valid image file (JPG, PNG, WEBP)');
    return;
  }

  currentImageBlob = file;
  const reader = new FileReader();
  reader.onload = (e) => {
    const preview = document.getElementById('filePreview');
    const dropContent = document.getElementById('dropzoneContent');
    const analyzeBtn = document.getElementById('analyzeStorageBtn');

    preview.src = e.target.result;
    preview.style.display = 'block';
    dropContent.style.display = 'none';
    analyzeBtn.disabled = false;
  };
  reader.readAsDataURL(file);
}

/**
 * Populate Sample Local Storage Grid
 */
function initPresetsGrid() {
  const grid = document.getElementById('presetsGrid');
  grid.innerHTML = '';

  SAMPLE_IMAGES.forEach(sample => {
    const div = document.createElement('div');
    div.className = 'preset-thumb';
    div.title = `Test with ${sample.name}`;
    div.innerHTML = `
      <img src="${sample.path}" alt="${sample.name}" onerror="this.src='data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 100 100%22><text y=%22.9em%22 font-size=%2250%22>🍃</text></svg>'">
      <span>${sample.name}</span>
    `;
    div.onclick = () => loadPresetSample(sample);
    grid.appendChild(div);
  });
}

async function loadPresetSample(sample) {
  try {
    const resp = await fetch(sample.path);
    const blob = await resp.blob();
    currentImageBlob = blob;

    const preview = document.getElementById('filePreview');
    const dropContent = document.getElementById('dropzoneContent');
    const analyzeBtn = document.getElementById('analyzeStorageBtn');

    preview.src = URL.createObjectURL(blob);
    preview.style.display = 'block';
    dropContent.style.display = 'none';
    analyzeBtn.disabled = false;

    showToast(`Loaded preset sample: ${sample.name}`);
    analyzeCurrentImage();
  } catch (err) {
    console.error('Error loading preset:', err);
    showToast('Failed to load sample image');
  }
}

/**
 * Send Image to FastAPI Backend `/predict/image`
 */
async function analyzeCurrentImage() {
  if (!currentImageBlob) {
    showToast('Please select or capture an image first');
    return;
  }

  const loadingOverlay = document.getElementById('loadingOverlay');
  loadingOverlay.style.display = 'flex';

  const selectedCrop = document.getElementById('cropSelect').value;
  const formData = new FormData();
  formData.append('file', currentImageBlob, 'leaf_sample.jpg');
  if (selectedCrop) {
    formData.append('crop', selectedCrop);
  }
  formData.append('top_k', 5);

  try {
    const resp = await fetch(`${API_BASE}/predict/image`, {
      method: 'POST',
      body: formData
    });

    if (!resp.ok) {
      const errJson = await resp.json().catch(() => ({ detail: 'Prediction HTTP error' }));
      throw new Error(errJson.detail || `Server returned ${resp.status}`);
    }

    const result = await resp.json();
    currentPredictionResult = result;
    renderDiagnosisResult(result);
  } catch (err) {
    console.error('[AgriSmart UI] Prediction error:', err);
    showToast(`Analysis failed: ${err.message}`);
  } finally {
    loadingOverlay.style.display = 'none';
  }
}

/**
 * Render Diagnostic Results View
 */
function renderDiagnosisResult(res) {
  document.getElementById('placeholderResult').style.display = 'none';
  const diagContent = document.getElementById('diagnosisContent');
  diagContent.style.display = 'flex';

  // Crop & Disease Names
  document.getElementById('cropNameLabel').textContent = res.crop || 'Plant Sample';
  document.getElementById('diseaseTitle').textContent = res.disease || 'Unknown Condition';

  // Status Badge (Healthy vs Diseased)
  const badge = document.getElementById('statusBadge');
  if (res.is_healthy) {
    badge.className = 'status-badge healthy';
    badge.textContent = '✓ Healthy Crop';
  } else {
    badge.className = 'status-badge diseased';
    badge.textContent = '⚠️ Disease Detected';
  }

  // Confidence Progress Bar
  const confVal = res.confidence_percentage || `${Math.round((res.confidence || 0) * 100)}%`;
  document.getElementById('confidencePercent').textContent = confVal;
  document.getElementById('meterFill').style.width = confVal;

  // Treatment & Care Guide
  const causeText = document.getElementById('causeText');
  const treatmentList = document.getElementById('treatmentList');
  treatmentList.innerHTML = '';

  if (res.treatments) {
    causeText.textContent = res.treatments.cause || res.treatments.symptoms || 'Detailed agronomic advisory available below.';

    const steps = [
      ...(res.treatments.organic || []),
      ...(res.treatments.chemical || []),
      ...(res.treatments.prevention || [])
    ];

    if (steps.length > 0) {
      steps.forEach(step => {
        const li = document.createElement('li');
        li.textContent = step;
        treatmentList.appendChild(li);
      });
    } else {
      const li = document.createElement('li');
      li.textContent = 'Maintain standard crop care, adequate soil hydration, and regular inspection.';
      treatmentList.appendChild(li);
    }
  }

  // Top Candidates List
  const topPredsList = document.getElementById('topPredsList');
  topPredsList.innerHTML = '';

  if (res.top_predictions && res.top_predictions.length > 0) {
    res.top_predictions.forEach(item => {
      const div = document.createElement('div');
      div.className = 'pred-item';
      div.innerHTML = `
        <span class="pred-name">${item.crop} - ${item.disease}</span>
        <span class="pred-val">${item.percentage}</span>
      `;
      topPredsList.appendChild(div);
    });
  }

  // Scroll smooth into view on mobile
  document.getElementById('resultsCard').scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

/**
 * Save Scan Result to LocalStorage History
 */
function saveScanToLocalStorage() {
  if (!currentPredictionResult) {
    showToast('No active scan to save');
    return;
  }

  try {
    const history = JSON.parse(localStorage.getItem('agri_scans_history') || '[]');
    const newEntry = {
      timestamp: new Date().toISOString(),
      crop: currentPredictionResult.crop,
      disease: currentPredictionResult.disease,
      confidence: currentPredictionResult.confidence_percentage,
      is_healthy: currentPredictionResult.is_healthy
    };
    history.unshift(newEntry);
    localStorage.setItem('agri_scans_history', JSON.stringify(history.slice(0, 20))); // Keep last 20
    showToast('Saved diagnosis scan to Local Storage!');
  } catch (err) {
    console.error('LocalStorage write error:', err);
    showToast('Could not save to local storage');
  }
}

/**
 * Text-To-Speech (Web Speech API)
 */
function toggleVoiceSpeech() {
  if (!currentPredictionResult) return;

  if (isSpeaking) {
    window.speechSynthesis.cancel();
    isSpeaking = false;
    document.getElementById('ttsBtnText').textContent = 'Read Diagnosis Aloud';
    document.getElementById('ttsBtn').classList.remove('active');
    return;
  }

  if (!('speechSynthesis' in window)) {
    showToast('Text-to-speech is not supported in this browser');
    return;
  }

  const textToRead = `Diagnosis for ${currentPredictionResult.crop}. Result: ${currentPredictionResult.disease}. Confidence score is ${currentPredictionResult.confidence_percentage}. ${currentPredictionResult.is_healthy ? 'Your plant is healthy.' : 'Treatments include: ' + (currentPredictionResult.treatments?.organic?.join('. ') || 'Consult local agricultural extension.')}`;

  const utterance = new SpeechSynthesisUtterance(textToRead);
  utterance.rate = 0.95;
  utterance.onend = () => {
    isSpeaking = false;
    document.getElementById('ttsBtnText').textContent = 'Read Diagnosis Aloud';
    document.getElementById('ttsBtn').classList.remove('active');
  };

  window.speechSynthesis.speak(utterance);
  isSpeaking = true;
  document.getElementById('ttsBtnText').textContent = 'Stop Reading';
  document.getElementById('ttsBtn').classList.add('active');
}

/**
 * Toast Notification Utility
 */
function showToast(msg) {
  const toast = document.getElementById('toastMsg');
  toast.textContent = msg;
  toast.classList.add('show');
  setTimeout(() => {
    toast.classList.remove('show');
  }, 3500);
}
