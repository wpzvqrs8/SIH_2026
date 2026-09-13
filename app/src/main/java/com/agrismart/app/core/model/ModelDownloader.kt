package com.agrismart.app.core.model

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

sealed interface ModelDownloadStatus {
    data object NotDownloaded : ModelDownloadStatus
    data class Downloading(val bytesDownloaded: Long, val totalBytes: Long, val progressPercent: Int) : ModelDownloadStatus
    data object VerifyingIntegrity : ModelDownloadStatus
    data object Ready : ModelDownloadStatus
    data class Error(val message: String) : ModelDownloadStatus
}

@Singleton
class ModelDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    companion object {
        const val RELEASE_ZIP_URL = "https://github.com/wpzvqrs8/SIH_2026/releases/download/offline-app-v1/AgriSmart-offline-windows.zip"
        const val EXPECTED_SIZE_BYTES = 203741824L
    }

    fun isModelReady(): Boolean {
        return try {
            val assetList = context.assets.list("web/models")
            val dataList = context.assets.list("web/data")
            val hasAssets = !assetList.isNullOrEmpty() && (assetList.contains("india_v1.onnx") || assetList.contains("india_v2.onnx")) && !dataList.isNullOrEmpty()
            if (hasAssets) return true

            val targetDir = File(context.filesDir, "web/models")
            targetDir.exists() && (File(targetDir, "india_v1.onnx").exists() || File(targetDir, "india_v2.onnx").exists())
        } catch (e: Exception) {
            false
        }
    }

    fun downloadModel(): Flow<ModelDownloadStatus> = flow {
        if (isModelReady()) {
            emit(ModelDownloadStatus.Ready)
            return@flow
        }

        val targetZip = File(context.cacheDir, "offline_assets.zip")

        try {
            emit(ModelDownloadStatus.Downloading(0L, EXPECTED_SIZE_BYTES, 0))

            val request = Request.Builder()
                .url(RELEASE_ZIP_URL)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                emit(ModelDownloadStatus.Error("Server returned HTTP ${response.code}"))
                return@flow
            }

            val body = response.body
            if (body == null) {
                emit(ModelDownloadStatus.Error("Empty response body"))
                return@flow
            }

            val contentLength = if (body.contentLength() > 0) body.contentLength() else EXPECTED_SIZE_BYTES

            body.byteStream().use { input ->
                FileOutputStream(targetZip).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalRead = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        val percent = ((totalRead * 100) / contentLength).toInt().coerceIn(0, 100)
                        emit(ModelDownloadStatus.Downloading(totalRead, contentLength, percent))
                    }
                    output.flush()
                }
            }

            emit(ModelDownloadStatus.VerifyingIntegrity)
            val extractDir = File(context.filesDir, "web")
            if (!extractDir.exists()) extractDir.mkdirs()

            // Extract zip
            java.util.zip.ZipInputStream(targetZip.inputStream()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val file = File(extractDir, entry.name.removePrefix("AgriSmart-offline/web/").removePrefix("web/"))
                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.parentFile?.mkdirs()
                        FileOutputStream(file).use { out -> zip.copyTo(out) }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
            targetZip.delete()
            emit(ModelDownloadStatus.Ready)
        } catch (e: Exception) {
            if (targetZip.exists()) targetZip.delete()
            emit(ModelDownloadStatus.Error(e.localizedMessage ?: "Network error during model download"))
        }
    }.flowOn(Dispatchers.IO)
}
