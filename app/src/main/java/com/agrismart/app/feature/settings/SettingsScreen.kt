package com.agrismart.app.feature.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agrismart.app.R
import com.agrismart.app.core.model.ModelDownloadStatus
import com.agrismart.app.core.model.ModelDownloader
import com.agrismart.app.data.api.ApiConfig
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onLanguageClick: () -> Unit,
    apiConfig: ApiConfig? = null,
    fakeApi: com.agrismart.app.data.api.FakeAgriSmartApi? = null,
    modelDownloader: ModelDownloader? = null,
    modifier: Modifier = Modifier
) {
    var readAloud by remember { mutableStateOf(true) }
    var serverUrlInput by remember { mutableStateOf(apiConfig?.serverUrl ?: "https://sih-2026-u4kv.onrender.com/") }
    var saveStatus by remember { mutableStateOf("") }
    var apiValidationStatus by remember { mutableStateOf("") }
    var isValidatingApi by remember { mutableStateOf(false) }
    var useOffline by remember { mutableStateOf(apiConfig?.useOfflineModel ?: false) }

    val scope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0) }
    var downloadStatusText by remember {
        mutableStateOf(
            if (modelDownloader?.isModelReady() == true) "Offline Model Ready (Installed)" else "Offline Model Not Downloaded"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = stringResource(R.string.nav_settings),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Language Selection Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                .clickable(onClick = onLanguageClick),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_translate), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(R.string.nav_language), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = "Change App Language (English, Hindi, Bengali, etc.)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Icon(painter = painterResource(id = R.drawable.ic_chevron_right), contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. AI Model Selection Mode Card (Online Cloud vs Offline Local)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.ic_wb_sunny), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "AI Diagnostic Model Selection", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Choose your preferred AI prediction engine mode:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Option A: Online Cloud Model (Render)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            useOffline = false
                            apiConfig?.useOfflineModel = false
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !useOffline,
                        onClick = {
                            useOffline = false
                            apiConfig?.useOfflineModel = false
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "🌐 Online Cloud Model (Render Backend)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = "Fastest, real-time cloud diagnostic engine. Zero storage required.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Option B: Offline Local ONNX Model
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            useOffline = true
                            apiConfig?.useOfflineModel = true
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = useOffline,
                        onClick = {
                            useOffline = true
                            apiConfig?.useOfflineModel = true
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "📱 Offline Local Model (On-device ONNX)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = "Runs 100% offline without internet. Requires downloading model package once.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Download Offline Model Section (Inside Settings)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.ic_storage), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Download Offline Model Package", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(text = downloadStatusText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (isDownloading) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { downloadProgress / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Downloading offline model assets: $downloadProgress%", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    enabled = !isDownloading && modelDownloader != null,
                    onClick = {
                        if (modelDownloader != null) {
                            scope.launch {
                                isDownloading = true
                                modelDownloader.downloadModel().collect { status ->
                                    when (status) {
                                        is ModelDownloadStatus.Downloading -> {
                                            downloadProgress = status.progressPercent
                                            downloadStatusText = "Downloading (${status.progressPercent}%)"
                                        }
                                        is ModelDownloadStatus.VerifyingIntegrity -> {
                                            downloadStatusText = "Verifying package integrity..."
                                        }
                                        is ModelDownloadStatus.Ready -> {
                                            isDownloading = false
                                            downloadStatusText = "Offline Model Ready (Installed)"
                                        }
                                        is ModelDownloadStatus.Error -> {
                                            isDownloading = false
                                            downloadStatusText = "Error: ${status.message}"
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(painter = painterResource(id = R.drawable.ic_storage), contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (modelDownloader?.isModelReady() == true) "Re-download Offline Model" else "Download Offline Model Assets (~200MB)")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Read Aloud Voice Toggle
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_volume_up), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Read results aloud", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = "Speak result titles and advice automatically", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Switch(checked = readAloud, onCheckedChange = { readAloud = it })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Custom Server URL Settings Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.ic_wb_sunny), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Custom Cloud Backend URL", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Configure custom backend deployment endpoint.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = serverUrlInput,
                    onValueChange = { 
                        serverUrlInput = it 
                        saveStatus = ""
                    },
                    label = { Text("Server Base URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        enabled = !isValidatingApi,
                        onClick = {
                            apiConfig?.serverUrl = serverUrlInput
                            saveStatus = "Saved!"
                            scope.launch {
                                isValidatingApi = true
                                apiValidationStatus = "Checking API health..."
                                val valid = fakeApi?.isApiValid() ?: false
                                isValidatingApi = false
                                apiValidationStatus = if (valid) "✅ API Valid & Online" else "⚠️ Saved, but API is unreachable"
                            }
                        }
                    ) {
                        Text("Save & Validate URL")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedButton(
                        enabled = !isValidatingApi,
                        onClick = {
                            scope.launch {
                                isValidatingApi = true
                                apiValidationStatus = "Testing API health..."
                                val valid = fakeApi?.isApiValid() ?: false
                                isValidatingApi = false
                                apiValidationStatus = if (valid) "✅ API Valid & Online" else "❌ API Unreachable or Invalid"
                            }
                        }
                    ) {
                        Text("Test API")
                    }
                }
                if (apiValidationStatus.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = apiValidationStatus,
                        color = if (apiValidationStatus.startsWith("✅")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
