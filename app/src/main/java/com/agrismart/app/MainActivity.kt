package com.agrismart.app

import com.agrismart.app.R
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.agrismart.app.core.designsystem.components.MockBanner
import com.agrismart.app.core.designsystem.theme.AgriSmartTheme
import com.agrismart.app.core.model.ModelDownloader
import com.agrismart.app.data.api.ApiConfig
import com.agrismart.app.data.repo.CropRepository
import com.agrismart.app.data.repo.PredictionRepository
import com.agrismart.app.domain.model.PredictionResult
import com.agrismart.app.feature.help.HelpAboutScreen
import com.agrismart.app.feature.history.RecentTestsScreen
import com.agrismart.app.feature.home.HomeScreen
import com.agrismart.app.feature.onboarding.LanguageScreen
import com.agrismart.app.feature.onboarding.ModelDownloadScreen
import com.agrismart.app.feature.scan.AddPhotoScreen
import com.agrismart.app.feature.scan.CheckingScreen
import com.agrismart.app.feature.scan.ChoosePlantScreen
import com.agrismart.app.feature.scan.LiveCameraScreen
import com.agrismart.app.feature.scan.ResultScreen
import com.agrismart.app.feature.settings.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var apiConfig: ApiConfig

    @Inject
    lateinit var modelDownloader: ModelDownloader

    @Inject
    lateinit var cropRepository: CropRepository

    @Inject
    lateinit var predictionRepository: PredictionRepository

    private var tts: TextToSpeech? = null
    private var ttsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
        }
        setContent {
            AgriSmartTheme {
                AgriSmartAppContent(
                    apiConfig = apiConfig,
                    modelDownloader = modelDownloader,
                    cropRepository = cropRepository,
                    predictionRepository = predictionRepository,
                    activity = this
                )
            }
        }
    }

    fun speakText(text: String, lang: String): Boolean {
        if (!ttsReady || tts == null) return false
        val locale = Locale(lang, "IN")
        tts?.language = locale
        return tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utt_id") == TextToSpeech.SUCCESS
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    override fun onDestroy() {
        tts?.shutdown()
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgriSmartAppContent(
    apiConfig: ApiConfig,
    modelDownloader: ModelDownloader,
    cropRepository: CropRepository,
    predictionRepository: PredictionRepository,
    activity: MainActivity? = null
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    val isFullscreenRoute = currentRoute == "language" || currentRoute == "model_download" || currentRoute == "offline_engine"

    var currentResult by remember { mutableStateOf<PredictionResult?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isFullscreenRoute,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_check_plant)) },
                        selected = currentRoute == "home" || currentRoute == "choose_plant",
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(painterResource(id = R.drawable.ic_wb_sunny), contentDescription = null) },
                        label = { Text("Offline AI Engine (59 Crops)") },
                        selected = currentRoute == "offline_engine",
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("offline_engine")
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(painterResource(id = R.drawable.ic_photo_camera), contentDescription = null) },
                        label = { Text("Live Camera Scan") },
                        selected = currentRoute == "live_camera",
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("live_camera")
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Info, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_recent_tests)) },
                        selected = currentRoute == "history",
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("history")
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_settings)) },
                        selected = currentRoute == "settings",
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("settings")
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Info, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_help_about)) },
                        selected = currentRoute == "help",
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("help")
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "Version 1.0.0 (model-v2)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (!isFullscreenRoute) {
                    Column {
                        TopAppBar(
                            title = {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = stringResource(R.string.tour_menu)
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        if (!apiConfig.isConfigured) {
                            MockBanner()
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "language",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("language") {
                    LanguageScreen(
                        onLanguageSelected = { tag ->
                            navController.navigate("model_download")
                        }
                    )
                }

                composable("model_download") {
                    ModelDownloadScreen(
                        modelDownloader = modelDownloader,
                        onDownloadComplete = {
                            navController.navigate("home") {
                                popUpTo("language") { inclusive = true }
                            }
                        }
                    )
                }

                composable("offline_engine") {
                    OfflineEngineScreen(
                        activity = activity,
                        onBackClick = { navController.navigate("home") }
                    )
                }

                composable("home") {
                    HomeScreen(
                        onLiveCameraClick = { navController.navigate("live_camera") },
                        onImageTestingClick = { navController.navigate("choose_plant") },
                        onRecentTestsClick = { navController.navigate("history") }
                    )
                }

                composable("live_camera") {
                    LiveCameraScreen(
                        predictionRepository = predictionRepository,
                        onResultConfirmed = { result ->
                            currentResult = result
                            navController.navigate("result")
                        },
                        onBackClick = { navController.navigate("home") }
                    )
                }

                composable("choose_plant") {
                    ChoosePlantScreen(
                        cropRepository = cropRepository,
                        onCropSelected = { cropId ->
                            navController.navigate("add_photo/$cropId")
                        },
                        onBackClick = { navController.navigate("home") }
                    )
                }

                composable(
                    route = "add_photo/{cropId}",
                    arguments = listOf(navArgument("cropId") { type = NavType.StringType })
                ) { backStack ->
                    val cropId = backStack.arguments?.getString("cropId") ?: "tomato"
                    AddPhotoScreen(
                        cropId = cropId,
                        onPhotoCaptured = { navController.navigate("checking/$cropId") },
                        onPhotoSelected = { navController.navigate("checking/$cropId") },
                        onBackClick = { navController.navigate("choose_plant") }
                    )
                }

                composable(
                    route = "checking/{cropId}",
                    arguments = listOf(navArgument("cropId") { type = NavType.StringType })
                ) { backStack ->
                    val cropId = backStack.arguments?.getString("cropId") ?: "tomato"
                    CheckingScreen(
                        cropId = cropId,
                        predictionRepository = predictionRepository,
                        onPredictionComplete = { result ->
                            currentResult = result
                            navController.navigate("result") {
                                popUpTo("choose_plant") { inclusive = true }
                            }
                        }
                    )
                }

                composable("result") {
                    currentResult?.let { res ->
                        ResultScreen(
                            result = res,
                            onCheckAnotherClick = { navController.navigate("choose_plant") }
                        )
                    }
                }

                composable("history") {
                    RecentTestsScreen(
                        predictionRepository = predictionRepository,
                        onTestClick = { }
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        onLanguageClick = { navController.navigate("language") },
                        apiConfig = apiConfig
                    )
                }

                composable("help") {
                    HelpAboutScreen()
                }
            }
        }
    }
}

@Composable
fun OfflineEngineScreen(
    activity: MainActivity?,
    onBackClick: () -> Unit
) {
    val host = "appassets.androidplatform.net"
    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_right),
                        contentDescription = "Back to Main Menu",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Offline AI Scanner (59 Crops)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        AndroidView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            factory = { ctx ->
                WebView(ctx).apply {
                    setBackgroundColor(android.graphics.Color.parseColor("#f3f5ef"))
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        allowFileAccess = false
                        allowContentAccess = true
                    }
                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun share(text: String) {
                            activity?.runOnUiThread {
                                val send = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                activity.startActivity(Intent.createChooser(send, null))
                            }
                        }

                        @JavascriptInterface
                        fun speak(text: String, lang: String, id: String): Boolean {
                            return activity?.speakText(text, lang) ?: false
                        }

                        @JavascriptInterface
                        fun hasVoice(lang: String): Boolean {
                            return true
                        }

                        @JavascriptInterface
                        fun stopSpeaking() {
                            activity?.stopSpeaking()
                        }
                    }, "AgriSmartAndroid")

                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            val url = request?.url ?: return null
                            if (host != url.host) return null

                            var path = url.path ?: "/index.html"
                            if (path == "/" || path.isEmpty()) path = "/index.html"
                            if (path.contains("..")) return null

                            val assetPath = "web$path"
                            val headers = hashMapOf(
                                "Cross-Origin-Opener-Policy" to "same-origin",
                                "Cross-Origin-Embedder-Policy" to "credentialless",
                                "Cross-Origin-Resource-Policy" to "same-origin",
                                "Cache-Control" to "no-cache"
                            )
                            return try {
                                val stream: InputStream = try {
                                    ctx.assets.open(assetPath)
                                } catch (e: Exception) {
                                    val file = File(ctx.filesDir, assetPath)
                                    if (file.exists() && file.length() > 0) FileInputStream(file) else throw e
                                }
                                val mime = when {
                                    assetPath.endsWith(".html") -> "text/html"
                                    assetPath.endsWith(".js") || assetPath.endsWith(".mjs") -> "text/javascript"
                                    assetPath.endsWith(".css") -> "text/css"
                                    assetPath.endsWith(".json") -> "application/json"
                                    assetPath.endsWith(".wasm") -> "application/wasm"
                                    assetPath.endsWith(".ogg") -> "audio/ogg"
                                    assetPath.endsWith(".svg") -> "image/svg+xml"
                                    assetPath.endsWith(".png") -> "image/png"
                                    else -> "application/octet-stream"
                                }
                                val charset = if (mime.startsWith("text/") || mime.endsWith("javascript") || mime.endsWith("json")) "utf-8" else null
                                WebResourceResponse(mime, charset, 200, "OK", headers, stream)
                            } catch (e: Exception) {
                                WebResourceResponse("text/plain", "utf-8", 404, "Not Found", headers, ByteArrayInputStream(ByteArray(0)))
                            }
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onPermissionRequest(request: PermissionRequest?) {
                            activity?.runOnUiThread {
                                request?.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
                            }
                        }
                    }

                    loadUrl("https://$host/index.html")
                }
            }
        )
    }
}
