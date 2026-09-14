package com.agrismart.app.data.api

import android.content.Context
import com.agrismart.app.data.api.model.AdviceDto
import com.agrismart.app.data.api.model.CropsResponse
import com.agrismart.app.data.api.model.DiseaseDto
import com.agrismart.app.data.api.model.HealthResponse
import com.agrismart.app.data.api.model.NamedItemDto
import com.agrismart.app.data.api.model.PredictResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAgriSmartApi @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiConfig: ApiConfig,
    private val okHttpClient: OkHttpClient
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val fastHealthClient by lazy {
        okHttpClient.newBuilder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    suspend fun getHealth(): HealthResponse = withContext(Dispatchers.IO) {
        try {
            val baseUrl = apiConfig.baseUrl
            val healthUrl = if (baseUrl.endsWith("/")) "${baseUrl}health" else "$baseUrl/health"
            val request = Request.Builder()
                .url(healthUrl)
                .header("User-Agent", "AgriSmartAndroid/1.0")
                .header("bypass-tunnel-reminder", "true")
                .header("Accept", "application/json")
                .get()
                .build()
            fastHealthClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val parsed = json.decodeFromString(JsonObject.serializer(), body)
                    val statusStr = parsed["status"]?.jsonPrimitive?.contentOrNull ?: "ok"
                    return@withContext HealthResponse(status = statusStr, modelVersion = "india_v1")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        HealthResponse(status = "offline", modelVersion = "india_v1")
    }

    suspend fun isApiValid(): Boolean = withContext(Dispatchers.IO) {
        try {
            val baseUrl = apiConfig.baseUrl.trim()
            if (baseUrl.isBlank()) return@withContext false
            val healthUrl = if (baseUrl.endsWith("/")) "${baseUrl}health" else "$baseUrl/health"
            val request = Request.Builder()
                .url(healthUrl)
                .header("User-Agent", "AgriSmartAndroid/1.0")
                .header("bypass-tunnel-reminder", "true")
                .header("Accept", "application/json")
                .get()
                .build()
            fastHealthClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    if (body.contains("status") || body.contains("online") || body.contains("ok") || body.contains("model_ready")) {
                        return@withContext true
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext false
    }

    suspend fun getCrops(): CropsResponse = withContext(Dispatchers.IO) {
        try {
            val content = context.assets.open("crops_fallback.json").bufferedReader().use { it.readText() }
            json.decodeFromString(CropsResponse.serializer(), content)
        } catch (e: Exception) {
            CropsResponse(crops = emptyList())
        }
    }

    suspend fun predict(
        cropId: String,
        imageFile: File? = null,
        imageUrl: String? = null
    ): PredictResponse = withContext(Dispatchers.IO) {
        // If online mode is active, attempt direct online prediction first
        if (!apiConfig.useOfflineModel) {
            val liveResult = tryPredictOnline(cropId, imageFile, imageUrl)
            if (liveResult != null) {
                return@withContext liveResult
            }
        }
        
        // Fallback local prediction if offline mode selected or API is unreachable
        return@withContext fallbackPredict(cropId)
    }

    private fun tryPredictOnline(
        cropId: String,
        imageFile: File?,
        imageUrl: String?
    ): PredictResponse? {
        try {
            val baseUrl = apiConfig.baseUrl
            val builder = Request.Builder()
                .header("User-Agent", "AgriSmartAndroid/1.0")
                .header("bypass-tunnel-reminder", "true")
                .header("Accept", "application/json")

            val request: Request = when {
                imageFile != null && imageFile.exists() -> {
                    val fileBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                    val multipart = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", imageFile.name, fileBody)
                        .addFormDataPart("crop", cropId)
                        .build()
                    builder.url(baseUrl + "predict/image").post(multipart).build()
                }
                !imageUrl.isNullOrBlank() -> {
                    val jsonBody = """{"url": "$imageUrl", "crop": "$cropId"}"""
                        .toRequestBody("application/json".toMediaTypeOrNull())
                    builder.url(baseUrl + "predict/url").post(jsonBody).build()
                }
                else -> {
                    val jsonBody = """{"url": "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTengOJgGoCt0T0QCW6SzRzqNVC1rZmiZCk4whV0wjxog&s=10", "crop": "$cropId"}"""
                        .toRequestBody("application/json".toMediaTypeOrNull())
                    builder.url(baseUrl + "predict/url").post(jsonBody).build()
                }
            }

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: return null
                    val jsonObj = json.decodeFromString(JsonObject.serializer(), bodyString)
                    
                    val cropName = jsonObj["crop"]?.jsonPrimitive?.contentOrNull ?: cropId.replaceFirstChar { it.uppercase() }
                    val diseaseName = jsonObj["disease"]?.jsonPrimitive?.contentOrNull ?: "Healthy"
                    val confidence = jsonObj["confidence"]?.jsonPrimitive?.floatOrNull ?: 0.92f
                    val isHealthy = jsonObj["is_healthy"]?.jsonPrimitive?.contentOrNull?.toBoolean() ?: ("healthy" in diseaseName.lowercase())

                    val treatmentsObj = jsonObj["treatments"]?.jsonObject
                    val organicList = treatmentsObj?.get("organic")?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
                    val chemicalList = treatmentsObj?.get("chemical")?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
                    val preventionList = treatmentsObj?.get("prevention")?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()

                    val adviceSteps = (organicList + chemicalList).ifEmpty {
                        listOf("Apply organic neem oil spray.", "Maintain crop hygiene.")
                    }

                    return PredictResponse(
                        testId = "live_${System.currentTimeMillis()}",
                        clientTestId = "client_${System.currentTimeMillis()}",
                        modelVersion = "india_v1_onnx",
                        status = if (isHealthy) "healthy" else "disease",
                        crop = NamedItemDto(id = cropId, name = cropName),
                        disease = DiseaseDto(id = diseaseName.lowercase().replace(" ", "_"), name = diseaseName, label = diseaseName),
                        confidence = confidence,
                        confidenceLevel = if (confidence > 0.7f) "high" else "moderate",
                        coverageNote = "Live diagnostic prediction from Render server",
                        advice = AdviceDto(
                            summary = "Recommended diagnostic actions for $diseaseName on $cropName.",
                            steps = adviceSteps,
                            prevention = preventionList,
                            source = "AgriSmart Cloud AI Engine (Render)"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private suspend fun fallbackPredict(cropId: String): PredictResponse {
        return try {
            val cropsResponse = getCrops()
            val cropDto = cropsResponse.crops.find { it.id.equals(cropId, ignoreCase = true) }
            val cropName = cropDto?.name ?: cropId.replace('_', ' ').replaceFirstChar { it.uppercase() }
            val classes = cropDto?.classes ?: emptyList()

            if (classes.isNotEmpty()) {
                val diseaseClass = classes.firstOrNull { !it.contains("healthy", ignoreCase = true) } ?: classes.first()
                val diseaseName = diseaseClass.replace('_', ' ').replaceFirstChar { it.uppercase() }
                PredictResponse(
                    testId = "test_${System.currentTimeMillis()}",
                    clientTestId = "client_${System.currentTimeMillis()}",
                    modelVersion = "india_v1",
                    status = if (diseaseClass.contains("healthy", ignoreCase = true)) "healthy" else "disease",
                    crop = NamedItemDto(id = cropId, name = cropName),
                    disease = DiseaseDto(id = diseaseClass, name = diseaseName, label = diseaseName),
                    confidence = 0.92f,
                    confidenceLevel = "high",
                    coverageNote = cropDto?.coverageNote,
                    advice = AdviceDto(
                        summary = "First-aid diagnostic recommendations for $diseaseName on $cropName.",
                        steps = listOf(
                            "Pick off affected leaves/parts and destroy them away from field.",
                            "Apply recommended bio-fungicide or copper spray (2g per litre of water).",
                            "Repeat treatment after 10-12 days if spots persist."
                        ),
                        prevention = listOf(
                            "Avoid over-watering and maintain proper air circulation between plants.",
                            "Rotate crop with non-host species in next planting season."
                        ),
                        source = "AgriSmart Extension Database"
                    )
                )
            } else {
                val content = context.assets.open("mock/predict_disease.json").bufferedReader().use { it.readText() }
                json.decodeFromString(PredictResponse.serializer(), content)
            }
        } catch (e: Exception) {
            val content = context.assets.open("mock/predict_disease.json").bufferedReader().use { it.readText() }
            json.decodeFromString(PredictResponse.serializer(), content)
        }
    }
}
