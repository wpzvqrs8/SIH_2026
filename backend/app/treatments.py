"""AgriSmart Treatment & Remedy Database."""

DEFAULT_TREATMENTS = {
    "scab": {
        "severity": "Moderate",
        "organic": ["Apply neem oil spray weekly.", "Prune infected leaves and clear fallen debris."],
        "chemical": ["Apply copper hydroxide or captan fungicide at first sign of infection."],
        "prevention": ["Avoid overhead irrigation; water at soil level.", "Ensure proper tree spacing for sunlight and airflow."]
    },
    "blight": {
        "severity": "High",
        "organic": ["Spray bio-fungicides containing Bacillus subtilis.", "Remove and destroy blighted plant tissue immediately."],
        "chemical": ["Apply Mancozeb or Chlorothalonil fungicide every 7-10 days during wet weather."],
        "prevention": ["Use certified disease-free seeds.", "Rotate crops every 2-3 years."]
    },
    "rust": {
        "severity": "Moderate",
        "organic": ["Apply sulfur-based dust or spray.", "Remove alternate host plants in proximity."],
        "chemical": ["Use propiconazole or tebuconazole systemic fungicide."],
        "prevention": ["Plant rust-resistant varieties.", "Maintain optimal plant spacing."]
    },
    "rot": {
        "severity": "High",
        "organic": ["Improve soil drainage and add Trichoderma viride to root zone."],
        "chemical": ["Apply Metalaxyl or Ridomil Gold soil drench."],
        "prevention": ["Avoid waterlogging and overwatering.", "Use raised plant beds."]
    },
    "spot": {
        "severity": "Low to Moderate",
        "organic": ["Baking soda spray (1 tbsp baking soda + 1 tsp liquid soap per gallon water)."],
        "chemical": ["Apply copper sulfate or Dithane M-45."],
        "prevention": ["Rotate crops annually.", "Disinfect gardening tools after use."]
    },
    "healthy": {
        "severity": "None",
        "organic": ["Maintain regular balanced organic fertilization (vermicompost)."],
        "chemical": ["No chemical treatment needed."],
        "prevention": ["Continue good irrigation, weeding, and nutrient management."]
    }
}

def get_treatment_info(disease_name: str) -> dict:
    d_lower = disease_name.lower()
    if "healthy" in d_lower:
        return DEFAULT_TREATMENTS["healthy"]
    
    for key, treatment in DEFAULT_TREATMENTS.items():
        if key in d_lower:
            return treatment
            
    # Default fallback treatment for unspecified diseases
    return {
        "severity": "Moderate",
        "organic": ["Spray neem oil extract (5ml/L) and remove affected foliage."],
        "chemical": ["Consult local agricultural officer for recommended broad-spectrum fungicide."],
        "prevention": ["Maintain good field hygiene, field ventilation, and crop rotation."]
    }
