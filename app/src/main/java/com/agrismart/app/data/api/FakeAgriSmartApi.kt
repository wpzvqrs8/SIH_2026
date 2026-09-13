package com.agrismart.app.data.api

import android.content.Context
import com.agrismart.app.data.api.model.CropsResponse
import com.agrismart.app.data.api.model.HealthResponse
import com.agrismart.app.data.api.model.PredictResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAgriSmartApi @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getHealth(): HealthResponse {
        delay(300)
        return HealthResponse(status = "ok", modelVersion = "model-v2")
    }

    suspend fun getCrops(): CropsResponse {
        delay(500)
        val content = context.assets.open("crops_fallback.json").bufferedReader().use { it.readText() }
        return json.decodeFromString(CropsResponse.serializer(), content)
    }

    suspend fun predict(cropId: String): PredictResponse {
        delay(1000)
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
                    crop = com.agrismart.app.data.api.model.NamedItemDto(id = cropId, name = cropName),
                    disease = com.agrismart.app.data.api.model.DiseaseDto(id = diseaseClass, name = diseaseName, label = diseaseName),
                    confidence = 0.92f,
                    confidenceLevel = "high",
                    coverageNote = cropDto?.coverageNote,
                    advice = com.agrismart.app.data.api.model.AdviceDto(
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
