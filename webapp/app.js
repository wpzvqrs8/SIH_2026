/**
 * AgriSmart AI - Plant Disease Testing Web Application Logic
 */

// Multilingual Translations Dictionary
const TRANSLATIONS = {
  en: {
    brand_sub: "Plant Disease Testing & Health Diagnostics",
    backend_online: "AI Online",
    hero_title: "Plant Health Scanner",
    hero_subtitle: "Capture or upload a leaf photo to receive real-time disease diagnosis & treatment guide.",
    input_title: "Select Photo Input Mode",
    tab_camera: "Direct Camera",
    tab_storage: "File / Local Storage",
    crop_select_label: "Filter by Crop (Optional):",
    auto_detect_all: "Auto-Detect All Crops (38 Classes)",
    btn_take_photo: "Take Direct Photo",
    btn_flip: "Flip",
    cam_connecting: "Connecting camera...",
    file_loading: "Processing image...",
    dropzone_title: "Click or Drag Image Here",
    dropzone_sub: "Supports JPG, PNG, WEBP (Local Storage or Disk)",
    btn_browse_files: "Browse Files",
    btn_analyze: "Analyze Uploaded Image",
    loading_title: "Analyzing Leaf Sample...",
    loading_sub: "Executing ONNX CPU Neural Model",
    placeholder_title: "No Image Analyzed Yet",
    placeholder_desc: "Take a photo with your camera or select an image from local storage to run plant disease detection.",
    conf_label: "Detection Confidence",
    btn_read_aloud: "Read Diagnosis Aloud",
    btn_stop_reading: "Stop Reading",
    btn_save_storage: "Save to Local Storage",
    treatment_header: "🌿 Recommended Treatment & Care Guide",
    diag_context_label: "Diagnosis Context:",
    actionable_steps_label: "Actionable Steps:",
    alt_predictions_header: "📊 Alternative Candidate Predictions",
    healthy_badge: "✓ Healthy Crop",
    diseased_badge: "⚠️ Disease Detected"
  },
  hi: {
    brand_sub: "पौधों के रोग की जांच और स्वास्थ्य निदान",
    backend_online: "एआई ऑनलाइन",
    hero_title: "पौधा स्वास्थ्य स्कैनर",
    hero_subtitle: "वास्तविक समय में बीमारी के निदान और उपचार गाइड प्राप्त करने के लिए पत्ती की फोटो लें या अपलोड करें।",
    input_title: "फोटो इनपुट मोड चुनें",
    tab_camera: "प्रत्यक्ष कैमरा",
    tab_storage: "फ़ाइल / लोकल स्टोरेज",
    crop_select_label: "फसल के अनुसार फ़िल्टर करें (वैकल्पिक):",
    auto_detect_all: "सभी फसलों का स्वतः पता लगाएं (38 श्रेणियां)",
    btn_take_photo: "सीधे फोटो लें",
    btn_flip: "कैमरा बदलें",
    cam_connecting: "कैमरा कनेक्ट हो रहा है...",
    file_loading: "इमेज प्रोसेस हो रही है...",
    dropzone_title: "यहाँ इमेज क्लिक करें या ड्रैग करें",
    dropzone_sub: "JPG, PNG, WEBP सपोर्टेड",
    btn_browse_files: "फ़ाइलें खोजें",
    btn_analyze: "अपलोड की गई इमेज का विश्लेषण करें",
    loading_title: "पत्ती के नमूने का विश्लेषण किया जा रहा है...",
    loading_sub: "न्यूरल मॉडल निष्पादित किया जा रहा है",
    placeholder_title: "अभी तक किसी इमेज का विश्लेषण नहीं किया गया",
    placeholder_desc: "पौधों की बीमारी का पता लगाने के लिए अपने कैमरे से एक फोटो लें या लोकल स्टोरेज से एक इमेज चुनें।",
    conf_label: "जांच की सटीकता",
    btn_read_aloud: "निदान जोर से सुनें",
    btn_stop_reading: "पढ़ना बंद करें",
    btn_save_storage: "लोकल स्टोरेज में सहेजें",
    treatment_header: "🌿 अनुशंसित उपचार और देखभाल गाइड",
    diag_context_label: "निदान संदर्भ:",
    actionable_steps_label: "कार्रवाई योग्य कदम:",
    alt_predictions_header: "📊 वैकल्पिक उम्मीदवार भविष्यवाणियां",
    healthy_badge: "✓ स्वस्थ फसल",
    diseased_badge: "⚠️ बीमारी पाई गई"
  },
  mr: {
    brand_sub: "वनस्पती रोग तपासणी आणि आरोग्य निदान",
    backend_online: "एआय बॅकएंड ऑनलाइन",
    hero_title: "वनस्पती आरोग्य स्कॅनर",
    hero_subtitle: "रोगाचे निदान आणि उपचार मार्गदर्शक मिळवण्यासाठी पानाचा फोटो घ्या किंवा अपलोड करा.",
    input_title: "फोटो इनपुट मोड निवडा",
    tab_camera: "थेट कॅमेरा",
    tab_storage: "फाइल / लोकल स्टोरेज",
    crop_select_label: "पिकांनुसार फिल्टर करा (पर्यायी):",
    auto_detect_all: "सर्व पिकांचा शोध घ्या",
    btn_take_photo: "थेट फोटो घ्या",
    btn_flip: "कॅमेरा बदला",
    cam_connecting: "कॅमेरा जोडत आहे...",
    file_loading: "फोटो प्रक्रियेत आहे...",
    dropzone_title: "इथे फोटो क्लिक करा किंवा ड्रॅग करा",
    dropzone_sub: "JPG, PNG, WEBP सपोर्टेड",
    btn_browse_files: "फाइल्स निवडा",
    btn_analyze: "अपलोड केलेल्या फोटोचे विश्लेषण करा",
    loading_title: "पानाच्या नमुन्याचे विश्लेषण चालू आहे...",
    loading_sub: "न्यूरल मॉडेल प्रक्रियेत आहे",
    placeholder_title: "अद्याप कोणत्याही फोटोचे विश्लेषण झालेले नाही",
    placeholder_desc: "रोग शोधण्यासाठी कॅमेऱ्याने फोटो घ्या किंवा लोकल स्टोरेजमधून इमेज निवडा.",
    conf_label: "तपासणी अचूकता",
    btn_read_aloud: "निदान मोठ्याने ऐका",
    btn_stop_reading: "वाचन थांबवा",
    btn_save_storage: "लोकल स्टोरेजमध्ये जतन करा",
    treatment_header: "🌿 शिफारस केलेले उपचार आणि काळजी मार्गदर्शक",
    diag_context_label: "निदान संदर्भ:",
    actionable_steps_label: "कृती करावयाचे टप्पे:",
    alt_predictions_header: "📊 पर्यायी संभाव्य अंदाज",
    healthy_badge: "✓ निरोगी पीक",
    diseased_badge: "⚠️ रोग आढळला"
  },
  ta: {
    brand_sub: "பயிர் நோய் பரிசோதனை & சுகாதார கண்டறிதல்",
    backend_online: "AI இயங்குகிறது",
    hero_title: "பயிர் சுகாதார ஸ்கேனர்",
    hero_subtitle: "நோயறிதல் மற்றும் சிகிச்சை பெற இலை புகைப்படத்தைப் பிடிக்கவும் அல்லது பதிவேற்றவும்.",
    input_title: "புகைப்பட பயன்முறையைத் தேர்ந்தெடுக்கவும்",
    tab_camera: "நேரடி கேமரா",
    tab_storage: "கோப்பு / உள்ளூர் சேமிப்பகம்",
    crop_select_label: "பயிரின் படி வடிகட்டவும் (விருப்பத்தேர்வு):",
    auto_detect_all: "அனைத்து பயிர்களையும் தானாகக் கண்டறிக",
    btn_take_photo: "நேரடி புகைப்படம் எடுக்கவும்",
    btn_flip: "கேமராவை மாற்று",
    cam_connecting: "கேமரா இணைக்கிறது...",
    file_loading: "படம் செயலாக்கப்படுகிறது...",
    dropzone_title: "படத்தை கிளிக் செய்யவும் அல்லது இழுக்கவும்",
    dropzone_sub: "JPG, PNG, WEBP ஆதரிக்கப்படுகிறது",
    btn_browse_files: "கோப்புகளை உலாவு",
    btn_analyze: "படத்தை பகுப்பாய்வு செய்",
    loading_title: "இலை மாதிரியை பகுப்பாய்வு செய்கிறது...",
    loading_sub: "நரம்பியல் மாதிரி இயங்குகிறது",
    placeholder_title: "இன்னும் பகுப்பாய்வு செய்யப்படவில்லை",
    placeholder_desc: "நோயைக் கண்டறிய கேமராவைப் பயன்படுத்தவும் அல்லது சேமிப்பகத்திலிருந்து படத்தைத் தேர்ந்தெடுக்கவும்.",
    conf_label: "துல்லியம்",
    btn_read_aloud: "நோயறிதலை வாசிக்கவும்",
    btn_stop_reading: "வாசிப்பதை நிறுத்து",
    btn_save_storage: "சேமிப்பகத்தில் சேமி",
    treatment_header: "🌿 பரிந்துரைக்கப்பட்ட சிகிச்சை மற்றும் பராமரிப்பு",
    diag_context_label: "நோயறிதல் சூழல்:",
    actionable_steps_label: "செயல்பாட்டு படிகள்:",
    alt_predictions_header: "📊 மாற்று கணிப்புகள்",
    healthy_badge: "✓ ஆரோக்கியமான பயிர்",
    diseased_badge: "⚠️ நோய் கண்டறியப்பட்டது"
  },
  te: {
    brand_sub: "మొక్కల వ్యాధి పరీక్ష & ఆరోగ్య నిర్ధారణ",
    backend_online: "AI ఆన్‌లైన్",
    hero_title: "మొక్కల ఆరోగ్య స్కానర్",
    hero_subtitle: "రియల్ టైమ్ వ్యాధి నిర్ధారణ మరియు చికిత్స మార్గదర్శినిని పొందడానికి ఆకు ఫోటో తీయండి.",
    input_title: "ఫోటో ఇన్‌పుట్ మోడ్‌ను ఎంచుకోండి",
    tab_camera: "నేరుగా కెమెరా",
    tab_storage: "ఫైల్ / లోకల్ స్టోరేజ్",
    crop_select_label: "పంటల వారీగా ఫిల్టర్ చేయండి:",
    auto_detect_all: "అన్ని పంటలను గుర్తించండి",
    btn_take_photo: "నేరుగా ఫోటో తీయండి",
    btn_flip: "కెమెరా మార్చు",
    cam_connecting: "కెమెరా కనెక్ట్ అవుతోంది...",
    file_loading: "చిత్రం ప్రాసెస్ చేయబడుతోంది...",
    dropzone_title: "ఫోటోను క్లిక్ చేయండి లేదా డ్రాగ్ చేయండి",
    dropzone_sub: "JPG, PNG, WEBP సపోర్ట్ చేస్తుంది",
    btn_browse_files: "ఫైళ్లను ఎంచుకోండి",
    btn_analyze: "చిత్రాన్ని విశ్లేషించండి",
    loading_title: "విశ్లేషిస్తోంది...",
    loading_sub: "మోడల్ నడుస్తోంది",
    placeholder_title: "ఇంకా విశ్లేషించబడలేదు",
    placeholder_desc: "వ్యాధిని గుర్తించడానికి కెమెరా ద్వారా ఫోటో తీయండి లేదా గ్యాలరీ నుండి ఎంచుకోండి.",
    conf_label: "ఖచ్చితత్వం",
    btn_read_aloud: "నిర్ధారణను వినండి",
    btn_stop_reading: "ఆపివేయి",
    btn_save_storage: "సేవ్ చేయండి",
    treatment_header: "🌿 సూచించిన చికిత్స మరియు సంరక్షణ",
    diag_context_label: "నిర్ధారణ వివరాలు:",
    actionable_steps_label: "చేయవలసిన పనులు:",
    alt_predictions_header: "📊 ఇతర అవకాశాలు",
    healthy_badge: "✓ ఆరోగ్యకరమైన పంట",
    diseased_badge: "⚠️ వ్యాధి గుర్తించబడింది"
  },
  gu: {
    brand_sub: "છોડના રોગની તપાસ અને નિદાન",
    backend_online: "AI બેકએન્ડ ઓનલાઇન",
    hero_title: "છોડ સ્વાસ્થ્ય સ્કેનર",
    hero_subtitle: "રોગ નિદાન અને સારવાર માર્ગદર્શિકા મેળવવા માટે પાંદડાનો ફોટો લો અથવા અપલોડ કરો.",
    input_title: "ફોટો ઇનપુટ મોડ પસંદ કરો",
    tab_camera: "ડાયરેક્ટ કેમેરા",
    tab_storage: "ફાઇલ / લોકલ સ્ટોરેજ",
    crop_select_label: "પાક અનુસાર ફિલ્ટર કરો:",
    auto_detect_all: "તમામ પાક ઓટો-ડિટેક્ટ કરો",
    btn_take_photo: "ડાયરેક્ટ ફોટો લો",
    btn_flip: "કેમેરા બદલો",
    cam_connecting: "કેમેરા કનેક્ટ થઈ રહ્યો છે...",
    file_loading: "ઈમેજ પ્રોસેસ થઈ રહી છે...",
    dropzone_title: "અહીં ફોટો ક્લિક કરો અથવા ડ્રેગ કરો",
    dropzone_sub: "JPG, PNG, WEBP સપોર્ટેડ",
    btn_browse_files: "ફાઇલો બ્રાઉઝ કરો",
    btn_analyze: "ફોટોનું વિશ્લેષણ કરો",
    loading_title: "વિશ્લેષણ થઈ રહ્યું છે...",
    loading_sub: "ન્યુરલ મોડેલ એક્ઝિક્યુટ થઈ રહ્યું છે",
    placeholder_title: "હજી સુધી કોઈ ફોટો વિશ્લેષિત થયો નથી",
    placeholder_desc: "રોગ શોધવા માટે તમારા કેમેરાથી ફોટો લો અથવા સ્ટોરેજમાંથી ફોટો પસંદ કરો.",
    conf_label: "ચોકસાઈ",
    btn_read_aloud: "નિદાન મોટેથી સાંભળો",
    btn_stop_reading: "વાંચવાનું બંધ કરો",
    btn_save_storage: "સેવ કરો",
    treatment_header: "🌿 ભલામણ કરેલ સારવાર અને સંભાળ",
    diag_context_label: "નિદાન વિગત:",
    actionable_steps_label: "પગલાં:",
    alt_predictions_header: "📊 અન્ય શક્યતાઓ",
    healthy_badge: "✓ સ્વસ્થ પાક",
    diseased_badge: "⚠️ રોગ જણાયો"
  },
  bn: {
    brand_sub: "উদ্ভিদের রোগ নির্ণয় ও স্বাস্থ্য পরীক্ষা",
    backend_online: "AI ব্যাকএন্ড অনলাইন",
    hero_title: "উদ্ভিদ স্বাস্থ্য স্ক্যানার",
    hero_subtitle: "রোগ নির্ণয় ও চিকিৎসার নির্দেশিকা পেতে পাতার ছবি তুলুন বা আপলোড করুন।",
    input_title: "ফটো ইনপুট মোড নির্বাচন করুন",
    tab_camera: "সরাসরি ক্যামেরা",
    tab_storage: "ফাইল / লোকাল স্টোরেজ",
    crop_select_label: "ফসল অনুযায়ী ফিল্টার করুন:",
    auto_detect_all: "সকল ফসল সনাক্ত করুন",
    btn_take_photo: "ছবি তুলুন",
    btn_flip: "ক্যামেরা পরিবর্তন করুন",
    cam_connecting: "ক্যামেরা যুক্ত হচ্ছে...",
    file_loading: "ছবি প্রসেস হচ্ছে...",
    dropzone_title: "ছবি নির্বাচন করুন বা ড্র্যাগ করুন",
    dropzone_sub: "JPG, PNG, WEBP সমর্থিত",
    btn_browse_files: "ফাইল ব্রাউজ করুন",
    btn_analyze: "ছবি বিশ্লেষণ করুন",
    loading_title: "বিশ্লেষণ করা হচ্ছে...",
    loading_sub: "নিউরাল মডেল চলছে",
    placeholder_title: "এখনও কোন ছবি বিশ্লেষণ করা হয়নি",
    placeholder_desc: "রোগ সনাক্ত করতে আপনার ক্যামেরা ব্যবহার করুন বা ফাইল নির্বাচন করুন।",
    conf_label: "সঠিকতার হার",
    btn_read_aloud: "পড়ে শুনুন",
    btn_stop_reading: "পড়া বন্ধ করুন",
    btn_save_storage: "সংরক্ষণ করুন",
    treatment_header: "🌿 প্রস্তাবিত চিকিৎসা ও পরিচর্যা",
    diag_context_label: "রোগের বিবরণ:",
    actionable_steps_label: "করণীয় পদক্ষেপ:",
    alt_predictions_header: "📊 অন্যান্য সম্ভাবনা",
    healthy_badge: "✓ সুস্থ ফসল",
    diseased_badge: "⚠️ রোগ ধরা পড়েছে"
  },
  kn: {
    brand_sub: "ಸಸ್ಯ ರೋಗ ಪರೀಕ್ಷೆ ಮತ್ತು ಆರೋಗ್ಯ ರೋಗನಿರ್ಣಯ",
    backend_online: "AI ಬ್ಯಾಕೆಂಡ್ ಆನ್‌ಲೈನ್",
    hero_title: "ಸಸ್ಯ ಆರೋಗ್ಯ ಸ್ಕ್ಯಾನರ್",
    hero_subtitle: "ನೈಜ ಸಮಯದಲ್ಲಿ ರೋಗ ನಿರ್ಣಯ ಮತ್ತು ಚಿಕಿತ್ಸಾ ಮಾರ್ಗದರ್ಶಿ ಪಡೆಯಲು ಎಲೆಯ ಫೋಟೋ ತೆಗೆಯಿರಿ.",
    input_title: "ಫೋಟೋ ಇನ್‌ಪುಟ್ ಮೋಡ್ ಆಯ್ಕೆಮಾಡಿ",
    tab_camera: "ನೇರ ಕ್ಯಾಮೆರಾ",
    tab_storage: "ಫೈಲ್ / ಲೋಕಲ್ ಸ್ಟೋರೇಜ್",
    crop_select_label: "ಬೆಳೆಯ ಪ್ರಕಾರ ಫಿಲ್ಟರ್ ಮಾಡಿ:",
    auto_detect_all: "ಎಲ್ಲಾ ಬೆಳೆಗಳನ್ನು ಪತ್ತೆ ಮಾಡಿ",
    btn_take_photo: "ನೇರ ಫೋಟೋ ತೆಗೆಯಿರಿ",
    btn_flip: "ಕ್ಯಾಮೆರಾ ಬದಲಾಯಿಸಿ",
    cam_connecting: "ಕ್ಯಾಮೆರಾ ಸಂಪರ್ಕಗೊಳ್ಳುತ್ತಿದೆ...",
    file_loading: "ಚಿತ್ರ ಪ್ರಕ್ರಿಯೆಯಲ್ಲಿದೆ...",
    dropzone_title: "ಚಿತ್ರವನ್ನು ಕ್ಲಿಕ್ ಮಾಡಿ ಅಥವಾ ಡ್ರಾಗ್ ಮಾಡಿ",
    dropzone_sub: "JPG, PNG, WEBP ಬೆಂಬಲಿತವಾಗಿದೆ",
    btn_browse_files: "ಫೈಲ್‌ಗಳನ್ನು ಆಯ್ಕೆ ಮಾಡಿ",
    btn_analyze: "ಚಿತ್ರವನ್ನು ವಿಶ್ಲೇಷಿಸಿ",
    loading_title: "ವಿಶ್ಲೇಷಿಸಲಾಗುತ್ತಿದೆ...",
    loading_sub: "ಮಾಡೆಲ್ ಚಾಲನೆಯಲ್ಲಿದೆ",
    placeholder_title: "ಇನ್ನೂ ಯಾವುದೇ ಚಿತ್ರ ವಿಶ್ಲೇಷಿಸಲ್ಪಟ್ಟಿಲ್ಲ",
    placeholder_desc: "ರೋಗ ಪತ್ತೆಹಚ್ಚಲು ಕ್ಯಾಮೆರಾ ಬಳಸಿ ಅಥವಾ ಗ್ಯಾಲರಿಯಿಂದ ಆಯ್ಕೆಮಾಡಿ.",
    conf_label: "ನಿಖರತೆ",
    btn_read_aloud: "ಧ್ವನಿಯಲ್ಲಿ ಕೇಳಿ",
    btn_stop_reading: "ನಿಲ್ಲಿಸಿ",
    btn_save_storage: "ಉಳಿಸಿ",
    treatment_header: "🌿 ಶಿಫಾರಸು ಮಾಡಿದ ಚಿಕಿತ್ಸೆ ಮತ್ತು ಆರೈಕೆ",
    diag_context_label: "ರೋಗನಿರ್ಣಯದ ವಿವರ:",
    actionable_steps_label: "ಮಾಡಬೇಕಾದ ಕ್ರಮಗಳು:",
    alt_predictions_header: "📊 ಇತರ ಸಾಧ್ಯತೆಗಳು",
    healthy_badge: "✓ ಅರೋಗ್ಯಕರ ಬೆಳೆ",
    diseased_badge: "⚠️ ರೋಗ ಪತ್ತೆಯಾಗಿದೆ"
  }
};

// Current Application State
let currentLang = 'en';
let currentMode = 'camera'; // 'camera' | 'storage'
let mediaStream = null;
let facingMode = 'environment'; // 'user' | 'environment'
let currentImageBlob = null;
let currentPredictionResult = null;
let isSpeaking = false;

// Configurable API Endpoint Base
const API_BASE = window.location.origin;

// Initialize application on DOM ready
document.addEventListener('DOMContentLoaded', () => {
  initCropsCatalog();
  initDragAndDrop();
  startCamera();
});

/**
 * Switch Active Display Language
 */
function changeLanguage(langKey) {
  if (!TRANSLATIONS[langKey]) langKey = 'en';
  currentLang = langKey;

  const dict = TRANSLATIONS[langKey];
  document.querySelectorAll('[data-i18n]').forEach(el => {
    const key = el.getAttribute('data-i18n');
    if (dict[key]) {
      el.textContent = dict[key];
    }
  });

  if (currentPredictionResult) {
    renderDiagnosisResult(currentPredictionResult);
  }

  showToast(`Language set to ${document.querySelector(`#langSelect option[value="${langKey}"]`).textContent}`);
}

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
 * Camera Stream Management using WebRTC with Connecting Loader
 */
async function startCamera() {
  stopCamera();
  const video = document.getElementById('cameraVideo');
  const preview = document.getElementById('capturedPreview');
  const overlay = document.getElementById('cameraOverlay');
  const cameraLoader = document.getElementById('cameraLoader');

  video.style.display = 'block';
  preview.style.display = 'none';
  overlay.style.display = 'flex';
  cameraLoader.style.display = 'flex';

  try {
    mediaStream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: facingMode, width: { ideal: 1280 }, height: { ideal: 960 } },
      audio: false
    });
    video.srcObject = mediaStream;
    
    video.onloadeddata = () => {
      cameraLoader.style.display = 'none';
    };

    document.getElementById('apiStatusText').textContent = TRANSLATIONS[currentLang].backend_online || 'AI Online';
  } catch (err) {
    console.error('[AgriSmart UI] Camera access error:', err);
    cameraLoader.style.display = 'none';
    showToast('Camera access unavailable. Switched to File Upload.');
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

    // Trigger backend analysis
    analyzeCurrentImage();
  }, 'image/jpeg', 0.92);
}

/**
 * Drag and Drop & Local File Selection with File Loader
 */
function initDragAndDrop() {
  const dropzone = document.getElementById('dropzone');
  if (!dropzone) return;

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

  const fileLoader = document.getElementById('fileLoader');
  fileLoader.style.display = 'flex';

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
    fileLoader.style.display = 'none';
  };
  reader.readAsDataURL(file);
}

/**
 * Send Image to FastAPI Backend `/predict/image` with Multi-Stage Progress Loader
 */
async function analyzeCurrentImage() {
  if (!currentImageBlob) {
    showToast('Please select or capture an image first');
    return;
  }

  const loadingOverlay = document.getElementById('loadingOverlay');
  const loadingStepTitle = document.getElementById('loadingStepTitle');
  const loadingStepSub = document.getElementById('loadingStepSub');
  
  loadingOverlay.style.display = 'flex';
  loadingStepTitle.textContent = TRANSLATIONS[currentLang].loading_title || "Analyzing Leaf Sample...";
  loadingStepSub.textContent = "1/3 Resizing & normalizing leaf pixels...";

  const selectedCrop = document.getElementById('cropSelect').value;
  const formData = new FormData();
  formData.append('file', currentImageBlob, 'leaf_sample.jpg');
  if (selectedCrop) {
    formData.append('crop', selectedCrop);
  }
  formData.append('top_k', 5);

  setTimeout(() => {
    if (loadingOverlay.style.display === 'flex') {
      loadingStepSub.textContent = "2/3 Executing ONNX CPU Neural Engine...";
    }
  }, 400);

  try {
    const resp = await fetch(`${API_BASE}/predict/image`, {
      method: 'POST',
      body: formData
    });

    if (!resp.ok) {
      const errJson = await resp.json().catch(() => ({ detail: 'Prediction HTTP error' }));
      throw new Error(errJson.detail || `Server returned ${resp.status}`);
    }

    loadingStepSub.textContent = "3/3 Fetching agronomic treatment advisory...";

    const result = await resp.json();
    currentPredictionResult = result;
    
    setTimeout(() => {
      renderDiagnosisResult(result);
    }, 200);

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

  const dict = TRANSLATIONS[currentLang] || TRANSLATIONS.en;

  // Crop & Disease Names
  document.getElementById('cropNameLabel').textContent = res.crop || 'Plant Sample';
  document.getElementById('diseaseTitle').textContent = res.disease || 'Unknown Condition';

  // Status Badge (Healthy vs Diseased)
  const badge = document.getElementById('statusBadge');
  if (res.is_healthy) {
    badge.className = 'status-badge healthy';
    badge.textContent = dict.healthy_badge || '✓ Healthy Crop';
  } else {
    badge.className = 'status-badge diseased';
    badge.textContent = dict.diseased_badge || '⚠️ Disease Detected';
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
    localStorage.setItem('agri_scans_history', JSON.stringify(history.slice(0, 20)));
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

  const dict = TRANSLATIONS[currentLang] || TRANSLATIONS.en;
  const ttsBtnText = document.getElementById('ttsBtnText');

  if (isSpeaking) {
    window.speechSynthesis.cancel();
    isSpeaking = false;
    ttsBtnText.textContent = dict.btn_read_aloud || 'Read Diagnosis Aloud';
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
  
  const langCodeMap = { en: 'en-US', hi: 'hi-IN', mr: 'mr-IN', ta: 'ta-IN', te: 'te-IN', gu: 'gu-IN', bn: 'bn-IN', kn: 'kn-IN' };
  utterance.lang = langCodeMap[currentLang] || 'en-US';

  utterance.onend = () => {
    isSpeaking = false;
    ttsBtnText.textContent = dict.btn_read_aloud || 'Read Diagnosis Aloud';
    document.getElementById('ttsBtn').classList.remove('active');
  };

  window.speechSynthesis.speak(utterance);
  isSpeaking = true;
  ttsBtnText.textContent = dict.btn_stop_reading || 'Stop Reading';
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
