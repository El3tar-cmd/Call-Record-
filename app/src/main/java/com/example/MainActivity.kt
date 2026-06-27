package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.Recording
import com.example.data.gemini.GeminiClient
import com.example.ui.theme.CellularColor
import com.example.ui.theme.HighDensityBg
import com.example.ui.theme.HighDensityText
import com.example.ui.theme.HighDensityPrimary
import com.example.ui.theme.HighDensityAccentContainer
import com.example.ui.theme.HighDensityOnAccentContainer
import com.example.ui.theme.HighDensityBorder
import com.example.ui.theme.HighDensitySubText
import com.example.ui.theme.InboundCallColor
import com.example.ui.theme.MessengerColor
import com.example.ui.theme.MicRecordingColor
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OutboundCallColor
import com.example.ui.theme.SoftGray
import com.example.ui.theme.WhatsAppColor
import com.example.ui.theme.WhiteIce
import com.example.ui.viewmodel.CallRecorderViewModel
import com.example.utils.AudioPlayerManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Force RTL Layout because the main app language is Arabic
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = HighDensityBg
                    ) { innerPadding ->
                        SajilAppMainScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SajilAppMainScreen(
    modifier: Modifier = Modifier,
    viewModel: CallRecorderViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val prefs = remember { context.getSharedPreferences("sajil_prefs", Context.MODE_PRIVATE) }
    var geminiApiKey by remember { mutableStateOf(prefs.getString("gemini_api_key", "") ?: "") }

    LaunchedEffect(geminiApiKey) {
        prefs.edit().putString("gemini_api_key", geminiApiKey).apply()
        com.example.data.gemini.GeminiClient.apiKey = geminiApiKey
    }

    // Collect variables from ViewModel
    val activeSimulatedCall by viewModel.activeSimulatedCall.collectAsState()
    val isRecordingActive by viewModel.isRecordingActive.collectAsState()
    val aiOperationState by viewModel.aiOperationState.collectAsState()
    val selectedRecording by viewModel.selectedRecording.collectAsState()

    var activeDetailsRecording by remember { mutableStateOf<Recording?>(null) }
    LaunchedEffect(selectedRecording) {
        if (selectedRecording != null) {
            activeDetailsRecording = selectedRecording
        }
    }

    // Collect background CallStateTracker variables
    val isServiceRecording by com.example.services.CallStateTracker.isRecording.collectAsState()
    val serviceCallerName by com.example.services.CallStateTracker.callerName.collectAsState()
    val serviceDurationSec by com.example.services.CallStateTracker.durationSec.collectAsState()
    val serviceAmplitudeList by com.example.services.CallStateTracker.amplitudeList.collectAsState()
    val servicePlatform by com.example.services.CallStateTracker.platform.collectAsState()
    val serviceDirection by com.example.services.CallStateTracker.direction.collectAsState()

    // Permissions check
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    var hasPhoneStatePermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasCallLogPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasMicPermission = permissions[Manifest.permission.RECORD_AUDIO] ?: hasMicPermission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: hasNotificationPermission
        }
        hasPhoneStatePermission = permissions[Manifest.permission.READ_PHONE_STATE] ?: hasPhoneStatePermission
        hasCallLogPermission = permissions[Manifest.permission.READ_CALL_LOG] ?: hasCallLogPermission
    }

    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
            } else {
                true
            }
        )
    }

    val checkBatteryOptimization = {
        isIgnoringBatteryOptimizations = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
    }

    var hasOverlayPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        )
    }

    var hasAccessibilityPermission by remember {
        mutableStateOf(com.example.services.CallRecordingAccessibilityService.isServiceEnabled)
    }

    val checkAccessibilityPermission = {
        hasAccessibilityPermission = com.example.services.CallRecordingAccessibilityService.isServiceEnabled
    }

    val requestAccessibilityPermission = {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Ignore
        }
    }

    val requestBatteryOptimizationExemption = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                try {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    context.startActivity(intent)
                } catch (ex: Exception) {
                    android.util.Log.e("MainActivity", "Failed to launch battery settings", ex)
                }
            }
        }
    }

    val requestOverlayPermission = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    LaunchedEffect(Unit) {
        checkBatteryOptimization()
        checkAccessibilityPermission()
        val permissionsToRequest = mutableListOf<String>()
        if (!hasMicPermission) permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (!hasPhoneStatePermission) permissionsToRequest.add(Manifest.permission.READ_PHONE_STATE)
        if (!hasCallLogPermission) permissionsToRequest.add(Manifest.permission.READ_CALL_LOG)
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    // Display Toast for AI Success/Error
    LaunchedEffect(aiOperationState) {
        when (val state = aiOperationState) {
            is CallRecorderViewModel.AiOpState.Success -> {
                Toast.makeText(context, state.msg, Toast.LENGTH_LONG).show()
                viewModel.clearAiState()
            }
            is CallRecorderViewModel.AiOpState.Error -> {
                Toast.makeText(context, state.msg, Toast.LENGTH_LONG).show()
                viewModel.clearAiState()
            }
            else -> {}
        }
    }

    Box(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            // High Tech glowing top header
            TopSajilHeader(
                isRecordingActive = isRecordingActive || activeSimulatedCall != null
            )

            // Dynamic Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = com.example.ui.theme.HighDensityCardBg,
                contentColor = HighDensitySubText,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = HighDensityPrimary,
                        height = 3.dp
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "المكالمات المسجلة",
                            fontSize = 14.sp,
                            color = if (selectedTab == 0) HighDensityPrimary else HighDensitySubText,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_recordings")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "التحكم والتسجيل",
                            fontSize = 14.sp,
                            color = if (selectedTab == 1) HighDensityPrimary else HighDensitySubText,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_simulator")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "دليل الصلاحيات",
                            fontSize = 14.sp,
                            color = if (selectedTab == 2) HighDensityPrimary else HighDensitySubText,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_guide")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                when (selectedTab) {
                    0 -> RecordingsListTab(viewModel)
                    1 -> RecorderAndSimTab(viewModel, hasMicPermission) {
                        // Launch permission request if needed
                        permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                    }
                    2 -> PermissionsGuideTab(
                        hasMicPermission = hasMicPermission,
                        hasNotificationPermission = hasNotificationPermission,
                        hasPhoneStatePermission = hasPhoneStatePermission,
                        hasCallLogPermission = hasCallLogPermission,
                        isIgnoringBatteryOptimizations = isIgnoringBatteryOptimizations,
                        hasOverlayPermission = hasOverlayPermission,
                        hasAccessibilityPermission = hasAccessibilityPermission,
                        geminiApiKey = geminiApiKey,
                        onApiKeyChange = { geminiApiKey = it },
                        onRequestPermissions = {
                            val permissions = mutableListOf(
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.READ_CALL_LOG
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            permissionLauncher.launch(permissions.toTypedArray())
                        },
                        onRequestBatteryExemption = {
                            requestBatteryOptimizationExemption()
                        },
                        onRequestOverlayPermission = {
                            requestOverlayPermission()
                        },
                        onRequestAccessibilityPermission = {
                            requestAccessibilityPermission()
                        }
                    )
                }
            }
        }

        // Expanded recording detail dialog/sheet
        AnimatedVisibility(
            visible = selectedRecording != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.fillMaxSize()
        ) {
            activeDetailsRecording?.let { rec ->
                RecordingDetailsPanel(
                    recording = rec,
                    viewModel = viewModel,
                    onClose = { viewModel.selectRecording(null) }
                )
            }
        }

        // Unified Call Overlay (for both background service and local test calls)
        AnimatedVisibility(
            visible = isServiceRecording || activeSimulatedCall != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            if (isServiceRecording) {
                CallActiveOverlay(
                    callerName = serviceCallerName,
                    platform = servicePlatform,
                    direction = serviceDirection,
                    durationSec = serviceDurationSec,
                    amplitudeList = serviceAmplitudeList,
                    onEndCall = {
                        // Stop the background service
                        val serviceIntent = Intent(context, com.example.services.CallRecordingService::class.java).apply {
                            action = com.example.services.CallRecordingService.ACTION_STOP_RECORDING
                        }
                        context.startService(serviceIntent)
                    }
                )
            } else {
                activeSimulatedCall?.let { call ->
                    CallActiveOverlay(
                        callerName = call.callerName,
                        platform = call.platform,
                        direction = if (call.isInbound) "INBOUND" else "OUTBOUND",
                        durationSec = viewModel.activeRecordDurationSec.collectAsState().value,
                        amplitudeList = viewModel.amplitudeList.collectAsState().value,
                        onEndCall = { viewModel.endSimulatedCall() }
                    )
                }
            }
        }
    }
}

@Composable
fun TopSajilHeader(
    isRecordingActive: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Brand bee icon container matching the beautiful generated app logo
            Image(
                painter = painterResource(id = R.drawable.sajil_app_icon_1782550631592),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Column {
                Text(
                    text = "سجل الذكي",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = HighDensityPrimary
                )
                Text(
                    text = "مسجل مكالمات احترافي وتفريغ ذكي",
                    fontSize = 11.sp,
                    color = HighDensitySubText
                )
            }
        }

        if (isRecordingActive) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MicRecordingColor.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(MicRecordingColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "جاري التسجيل...",
                    color = MicRecordingColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(HighDensityAccentContainer)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Premium Mode",
                    tint = HighDensityPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "نسخة مفعلة",
                    color = HighDensityPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// TAB 1: RECORDINGS LIST
// ---------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingsListTab(viewModel: CallRecorderViewModel) {
    val recordings by viewModel.recordings.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedSourceFilter by viewModel.selectedSourceFilter.collectAsState()

    val sourceFilters = listOf(
        FilterItem("الكل", "ALL"),
        FilterItem("الهاتف", "CELLULAR"),
        FilterItem("واتساب", "WHATSAPP"),
        FilterItem("ماسينجر", "MESSENGER"),
        FilterItem("الميكروفون", "MIC")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Search TextField
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("ابحث عن جهة اتصال، ملاحظة، أو تفريغ نصي...", color = HighDensitySubText.copy(alpha = 0.7f), fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = HighDensitySubText) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = HighDensitySubText)
                    }
                }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = com.example.ui.theme.HighDensityCardBg,
                unfocusedContainerColor = com.example.ui.theme.HighDensityCardBg,
                focusedBorderColor = HighDensityPrimary,
                unfocusedBorderColor = HighDensityBorder,
                focusedTextColor = HighDensityText,
                unfocusedTextColor = HighDensityText
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal filter tags
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sourceFilters.forEach { filter ->
                val isSelected = selectedSourceFilter == filter.value
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isSelected) HighDensityPrimary else com.example.ui.theme.HighDensityCardBg)
                        .then(
                            if (!isSelected) Modifier.border(1.dp, HighDensityBorder, RoundedCornerShape(24.dp))
                            else Modifier
                        )
                        .clickable { viewModel.updateSourceFilter(filter.value) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = filter.label,
                        color = if (isSelected) com.example.ui.theme.HighDensityOnPrimary else HighDensitySubText,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Service Status Card (Directly matches "Service Status Card" in Design HTML)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = HighDensityAccentContainer),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(HighDensityPrimary)
                        )
                        Text(
                            text = "الخدمة نشطة",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityPrimary
                        )
                    }
                    Text(
                        text = "نظام التسجيل الذكي يعمل",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityOnAccentContainer
                    )
                    Text(
                        text = "تم منح صلاحيات Accessibility و Overlay بنجاح",
                        fontSize = 11.sp,
                        color = HighDensityOnAccentContainer.copy(alpha = 0.7f)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(com.example.ui.theme.HighDensityCardBg.copy(alpha = 0.6f))
                        .padding(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active",
                        tint = HighDensityPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Recording List
        if (recordings.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "No recordings",
                        tint = HighDensitySubText,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "لا توجد أي تسجيلات مكالمات تطابق بحثك",
                        color = HighDensitySubText,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            val context = LocalContext.current
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(recordings, key = { it.id }) { rec ->
                    RecordingItemCard(
                        recording = rec,
                        onClick = { viewModel.selectRecording(rec) },
                        onDelete = { viewModel.deleteRecording(rec) },
                        onShare = {
                            val file = java.io.File(rec.filePath)
                            if (file.exists()) {
                                try {
                                    val uri = androidx.core.content.FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        file
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "audio/*"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "مشاركة التسجيل"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "حدث خطأ أثناء المشاركة", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "الملف غير موجود", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RecordingItemCard(
    recording: Recording,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val platformLabel = when (recording.source) {
        "WHATSAPP" -> "واتساب"
        "MESSENGER" -> "ماسينجر"
        "CELLULAR" -> "الهاتف"
        else -> "مسجل صوتي"
    }

    val platformColor = when (recording.source) {
        "WHATSAPP" -> WhatsAppColor
        "MESSENGER" -> MessengerColor
        "CELLULAR" -> CellularColor
        else -> MicRecordingColor
    }

    val directionLabel = when (recording.direction) {
        "INBOUND" -> "واردة"
        "OUTBOUND" -> "صادرة"
        else -> "مذكرة"
    }

    val directionColor = when (recording.direction) {
        "INBOUND" -> InboundCallColor
        "OUTBOUND" -> OutboundCallColor
        else -> SoftGray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, HighDensityBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("recording_item_${recording.id}"),
        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.HighDensityCardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App platform Icon indicator
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(platformColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (recording.source) {
                        "MIC" -> Icons.Default.Mic
                        else -> Icons.Default.Call
                    },
                    contentDescription = platformLabel,
                    tint = platformColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = recording.title,
                        color = HighDensityText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatDuration(recording.durationSec),
                        color = HighDensityPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Source and direction details
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$platformLabel • ",
                            color = HighDensitySubText,
                            fontSize = 11.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(directionColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = directionLabel,
                                color = directionColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Transcription/Sentiment badges
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (recording.isTranscribed) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(HighDensityAccentContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "تفريغ متاح",
                                    color = HighDensityPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        recording.sentiment?.let { sentiment ->
                            val sentColor = when (sentiment) {
                                "إيجابي" -> OutboundCallColor
                                "سلبي" -> MicRecordingColor
                                else -> InboundCallColor
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(sentColor)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = sentiment,
                                    color = sentColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Date
                Text(
                    text = formatTimestamp(recording.timestamp),
                    color = HighDensitySubText.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Share action button
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = HighDensitySubText.copy(alpha = 0.5f)
                    )
                }

                // Delete action button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = HighDensitySubText.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// TAB 2: RECORDER AND MOCK SIMULATOR
// ---------------------------------------------------------------------------------
@Composable
fun RecorderAndSimTab(
    viewModel: CallRecorderViewModel,
    hasMicPermission: Boolean,
    onRequestMicPermission: () -> Unit
) {
    val isRecordingActive by viewModel.isRecordingActive.collectAsState()
    val activeRecordDurationSec by viewModel.activeRecordDurationSec.collectAsState()
    val amplitudeList by viewModel.amplitudeList.collectAsState()

    val isServiceRecording by com.example.services.CallStateTracker.isRecording.collectAsState()
    val serviceCallerName by com.example.services.CallStateTracker.callerName.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section A: Micro recording box (Manual Voice Memos)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.HighDensityCardBg),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HighDensityBorder, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "مسجل صوتي سريع ومذكرات",
                        color = HighDensityText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "سجل أي فكرة أو حوار عابر في البيئة المحيطة بك فوراً باستخدام الميكروفون.",
                        color = HighDensitySubText,
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    if (isRecordingActive) {
                        // Live waveform simulation
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                amplitudeList.takeLast(25).forEach { amp ->
                                    val barHeight = (amp * 50.dp.value).coerceAtLeast(3f)
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(barHeight.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(MicRecordingColor)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Pulse elapsed timer
                        Text(
                            text = formatDuration(activeRecordDurationSec),
                            color = MicRecordingColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.stopMicRecording() },
                            colors = ButtonDefaults.buttonColors(containerColor = MicRecordingColor),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Stop", tint = com.example.ui.theme.HighDensityOnPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إيقاف وحفظ التسجيل", color = com.example.ui.theme.HighDensityOnPrimary, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Inactive, trigger record
                        IconButton(
                            onClick = {
                                if (hasMicPermission) {
                                    viewModel.startMicRecording()
                                } else {
                                    onRequestMicPermission()
                                }
                            },
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(HighDensityPrimary.copy(alpha = 0.15f))
                                .border(2.dp, HighDensityPrimary, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Start mic recording",
                                tint = HighDensityPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "انقر للبدء الفوري بالتسجيل اليدوي",
                            color = HighDensitySubText,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Section B: Automatic background recording controller status indicator & tips
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.HighDensityCardBg),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HighDensityBorder, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "نظام المراقبة والتسجيل التلقائي",
                        color = HighDensityText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "يقوم النظام بمراقبة خطوط الهاتف بالخلفية لتبدأ عملية التسجيل التلقائي بمجرد استلام أو بدء أي مكالمة حقيقية.",
                        color = HighDensitySubText,
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status Indicator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(InboundCallColor.copy(alpha = 0.08f))
                            .border(1.dp, InboundCallColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Pulsing green dot
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(InboundCallColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isServiceRecording) "جاري تسجيل مكالمة جارية الآن: $serviceCallerName" else "الخدمة نشطة بالخلفية وبانتظار مكالمة جديدة...",
                                color = HighDensityText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Golden Tip Card for Recording Both Sides on Android 13/14
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF9E6))
                            .border(1.dp, Color(0xFFFFE599), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Tip Icon",
                                    tint = Color(0xFFD48806),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تنبيه هام لأجهزة أندرويد 13 و 14",
                                    color = Color(0xFFD48806),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "لضمان تسجيل صوت الطرفين بوضوح تام، يرجى تشغيل مكبر الصوت (الاسبيكر / Speaker) أثناء المكالمة. القيود الأمنية في إصدارات أندرويد الحديثة تمنع التقاط صوت الطرف الآخر مباشرة، والاسبيكر هو الحل المثالي والآمن.",
                                color = Color(0xFF595959),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Quick automated test trigger
                    val contextLocal = LocalContext.current
                    Button(
                        onClick = {
                            if (!hasMicPermission) {
                                onRequestMicPermission()
                                return@Button
                            }
                            viewModel.initiateQuickTestCall()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("start_quick_test_button")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Launch Test", tint = com.example.ui.theme.HighDensityOnPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تشغيل مكالمة اختبارية وتجربة التسجيل",
                            color = com.example.ui.theme.HighDensityOnPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// TAB 3: PERMISSIONS GUIDE
// ---------------------------------------------------------------------------------
@Composable
fun PermissionsGuideTab(
    hasMicPermission: Boolean,
    hasNotificationPermission: Boolean,
    hasPhoneStatePermission: Boolean,
    hasCallLogPermission: Boolean,
    isIgnoringBatteryOptimizations: Boolean,
    hasOverlayPermission: Boolean,
    hasAccessibilityPermission: Boolean,
    geminiApiKey: String,
    onApiKeyChange: (String) -> Unit,
    onRequestPermissions: () -> Unit,
    onRequestBatteryExemption: () -> Unit,
    onRequestOverlayPermission: () -> Unit,
    onRequestAccessibilityPermission: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Checkers
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.HighDensityCardBg),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HighDensityBorder, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "صلاحيات نظام الاندرويد الحديث",
                        color = HighDensityText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "لضمان التقاط الصوت بدقة عالية وتسجيل المكالمات بوضوح تطلب الخدمة الصلاحيات التالية:",
                        color = HighDensitySubText,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Permission 1: Microphone
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Mic",
                                tint = if (hasMicPermission) InboundCallColor else MicRecordingColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("صلاحية الميكروفون", color = HighDensityText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("التقاط تدفق الصوت والمكالمات", color = HighDensitySubText, fontSize = 11.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (hasMicPermission) InboundCallColor.copy(alpha = 0.15f) else MicRecordingColor.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (hasMicPermission) "مفعلة" else "غير مفعلة",
                                color = if (hasMicPermission) InboundCallColor else MicRecordingColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Permission 2: Phone State
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Phone State",
                                tint = if (hasPhoneStatePermission) InboundCallColor else MicRecordingColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("صلاحية حالة الهاتف", color = HighDensityText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("التقاط إشارة الاتصال للبدء التلقائي", color = HighDensitySubText, fontSize = 11.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (hasPhoneStatePermission) InboundCallColor.copy(alpha = 0.15f) else MicRecordingColor.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (hasPhoneStatePermission) "مفعلة" else "غير مفعلة",
                                color = if (hasPhoneStatePermission) InboundCallColor else MicRecordingColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Permission 3: Call Log
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Call Log",
                                tint = if (hasCallLogPermission) InboundCallColor else MicRecordingColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("صلاحية سجل المكالمات", color = HighDensityText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("التعرف على هوية ورقم المتصل تلقائياً", color = HighDensitySubText, fontSize = 11.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (hasCallLogPermission) InboundCallColor.copy(alpha = 0.15f) else MicRecordingColor.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (hasCallLogPermission) "مفعلة" else "غير مفعلة",
                                color = if (hasCallLogPermission) InboundCallColor else MicRecordingColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Permission 4: Notifications
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Notifications",
                                tint = if (hasNotificationPermission) InboundCallColor else HighDensitySubText,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("صلاحية الإشعارات", color = HighDensityText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("إبقاء محرك التسجيل يعمل بالخلفية", color = HighDensitySubText, fontSize = 11.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (hasNotificationPermission) InboundCallColor.copy(alpha = 0.15f) else HighDensitySubText.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (hasNotificationPermission) "مفعلة" else "غير مفعلة",
                                color = if (hasNotificationPermission) InboundCallColor else HighDensitySubText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Permission 5: Battery Optimization Exemption
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Battery Optimization",
                                tint = if (isIgnoringBatteryOptimizations) InboundCallColor else MicRecordingColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("تجاوز تحسين البطارية", color = HighDensityText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("منع النظام من إيقاف الخدمة تلقائياً", color = HighDensitySubText, fontSize = 11.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isIgnoringBatteryOptimizations) InboundCallColor.copy(alpha = 0.15f) else MicRecordingColor.copy(alpha = 0.15f))
                                .clickable { onRequestBatteryExemption() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isIgnoringBatteryOptimizations) "مفعلة" else "تفعيل الآن",
                                color = if (isIgnoringBatteryOptimizations) InboundCallColor else MicRecordingColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Permission 6: Overlay (System Alert Window)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Overlay Permission",
                                tint = if (hasOverlayPermission) InboundCallColor else MicRecordingColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("الظهور فوق التطبيقات", color = HighDensityText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("فتح التطبيق تلقائيا لتسجيل المكالمات بنجاح", color = HighDensitySubText, fontSize = 11.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (hasOverlayPermission) InboundCallColor.copy(alpha = 0.15f) else MicRecordingColor.copy(alpha = 0.15f))
                                .clickable { onRequestOverlayPermission() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (hasOverlayPermission) "مفعلة" else "تفعيل الآن",
                                color = if (hasOverlayPermission) InboundCallColor else MicRecordingColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Permission 7: Accessibility Service
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Accessibility Permission",
                                tint = if (hasAccessibilityPermission) InboundCallColor else MicRecordingColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("إمكانية الوصول (Accessibility)", color = HighDensityText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("ضرورية لتسجيل الصوت أثناء المكالمات بكفاءة", color = HighDensitySubText, fontSize = 11.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (hasAccessibilityPermission) InboundCallColor.copy(alpha = 0.15f) else MicRecordingColor.copy(alpha = 0.15f))
                                .clickable { onRequestAccessibilityPermission() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (hasAccessibilityPermission) "مفعلة" else "تفعيل الآن",
                                color = if (hasAccessibilityPermission) InboundCallColor else MicRecordingColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (!hasMicPermission || !hasNotificationPermission || !hasPhoneStatePermission || !hasCallLogPermission) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onRequestPermissions,
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("منح الصلاحيات المطلوبة الآن", color = com.example.ui.theme.HighDensityOnPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Explanation text card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.HighDensityCardBg),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HighDensityBorder, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "كيف يعمل مسجل المكالمات Sajil Pro؟",
                        color = HighDensityText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "قامت جوجل منذ نظام أندرويد 10 بفرض حظر أمني شامل يمنع التطبيقات الخارجية من تسجيل الصوت الصادر من خط الهاتف بشكل مباشر للحفاظ على خصوصية المستخدمين.\n\n" +
                                "تطبيق سجل المبتكر يتجاوز هذه القيود بالطرق الاحترافية التالية:\n" +
                                "1. التقاط الصوت الخارجي والداخلي المتسرب عبر الميكروفون المتقدم (يفضل تشغيل مكبر الصوت/الاسبيكر في المكالمات لنتائج مذهلة).\n" +
                                "2. محاكي المكالمات المدمج: يتيح لك محاكاة الاتصال وتفعيل التسجيل التلقائي فوراً لاختبار جودة الصوت وحفظ المكالمات بالتنصيف والاسم.\n" +
                                "3. النسخ الذكي: استخدام نموذج الذكاء الاصطناعي الأكثر تقدماً Gemini 3.5 Flash لتحويل التسجيلات إلى نص وقراءتها فوراً مع التلخيص الذكي وتحديد اتجاه ونبرة الاتصال.",
                        color = HighDensitySubText,
                        fontSize = 11.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Gemini API Key Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.HighDensityCardBg),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HighDensityBorder, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "إعدادات الذكاء الاصطناعي (Gemini)",
                        color = HighDensityText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "قم بإضافة مفتاح API الخاص بك لتفعيل التلخيص الحقيقي والتفريغ الذكي للمكالمات بدلا من التلخيص الوهمي.",
                        color = HighDensitySubText,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    androidx.compose.material3.OutlinedTextField(
                        value = geminiApiKey,
                        onValueChange = onApiKeyChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("أدخل Gemini API Key هنا...", color = HighDensitySubText.copy(alpha = 0.6f), fontSize = 11.sp) },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HighDensityPrimary,
                            unfocusedBorderColor = HighDensityBorder,
                            focusedTextColor = HighDensityText,
                            unfocusedTextColor = HighDensityText,
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Developer Info Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.HighDensityCardBg),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HighDensityBorder, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "مطور التطبيق",
                        color = HighDensityText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Eng Abdelrahman Mahmoud",
                        color = HighDensityPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "DevHive",
                        color = HighDensitySubText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "DevHive040@gmail.com",
                        color = HighDensitySubText,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ---------------------------------------------------------------------------------
// DETAILED RECORDING VIEW PANEL (BOTTOM DRAWER-STYLE EXPANDABLE DIALOG)
// ---------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingDetailsPanel(
    recording: Recording,
    viewModel: CallRecorderViewModel,
    onClose: () -> Unit
) {
    val playbackState by viewModel.playbackState.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val aiOperationState by viewModel.aiOperationState.collectAsState()

    var notesText by remember(recording.id) { mutableStateOf(recording.notes ?: "") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onClose() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .align(Alignment.BottomCenter)
                .clickable(enabled = false) {}
                .testTag("details_panel"),
            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.HighDensityCardBg),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Drag handle / Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_details")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HighDensityPrimary)
                    }
                    Text(
                        text = "تفاصيل وتفريغ المكالمة",
                        color = HighDensityText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { viewModel.deleteRecording(recording) },
                        modifier = Modifier.testTag("delete_recording")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MicRecordingColor)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable details content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Item Title / Info card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = HighDensityBg),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.border(1.dp, HighDensityBorder, RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = recording.title,
                                    color = HighDensityText,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "المصدر: ${getPlatformArabicName(recording.source)} • ${getDirectionArabicName(recording.direction)}",
                                        color = HighDensitySubText,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = formatTimestamp(recording.timestamp),
                                        color = HighDensitySubText,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Audio player section
                    item {
                        AudioPlayerSection(
                            recording = recording,
                            playbackState = playbackState,
                            playbackSpeed = playbackSpeed,
                            onPlay = { viewModel.playRecording(recording) },
                            onPause = { viewModel.pausePlayback() },
                            onResume = { viewModel.resumePlayback() },
                            onSeek = { viewModel.seekPlaybackTo(it) },
                            onSpeedChange = { viewModel.updatePlaybackSpeed(it) }
                        )
                    }

                    // Notes Section
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.HighDensityCardBg),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.border(1.dp, HighDensityBorder, RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "ملاحظات المستخدم:",
                                    color = HighDensityText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = notesText,
                                    onValueChange = { notesText = it },
                                    placeholder = { Text("أضف ملاحظات شخصية عن هذه المكالمة هنا...", color = HighDensitySubText.copy(alpha = 0.6f), fontSize = 11.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = HighDensityPrimary,
                                        unfocusedBorderColor = HighDensityBorder,
                                        focusedTextColor = HighDensityText,
                                        unfocusedTextColor = HighDensityText,
                                        focusedContainerColor = com.example.ui.theme.HighDensityCardBg,
                                        unfocusedContainerColor = com.example.ui.theme.HighDensityCardBg
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 2,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.updateNotes(recording.id, notesText) },
                                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("حفظ الملاحظة", color = com.example.ui.theme.HighDensityOnPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Advanced AI Transcription / Summarization State block
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.HighDensityCardBg),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.border(1.dp, HighDensityBorder, RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "التفريغ والتحليل الذكي (Gemini AI):",
                                        color = HighDensityText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    if (recording.isTranscribed) {
                                        IconButton(
                                            onClick = { viewModel.transcribeRecording(recording) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Retry",
                                                tint = HighDensityPrimary
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val opState = aiOperationState
                                if (opState is CallRecorderViewModel.AiOpState.Transcribing && opState.id == recording.id) {
                                    // Transcription progress indicator
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        CircularProgressIndicator(color = HighDensityPrimary)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "جاري قراءة ملف الصوت وترجمته إلى نص بالذكاء الاصطناعي...",
                                            color = HighDensitySubText,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else if (opState is CallRecorderViewModel.AiOpState.Analyzing && opState.id == recording.id) {
                                    // Analysis progress indicator
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        CircularProgressIndicator(color = HighDensityPrimary)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "جاري استنباط ملخص المكالمة والنقاط الهامة وتحليل النبرة العاطفية...",
                                            color = HighDensitySubText,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else if (!recording.isTranscribed) {
                                    // Trigger AI Transcription button
                                    Button(
                                        onClick = { viewModel.transcribeRecording(recording) },
                                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("ai_transcribe_button")
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = "AI", tint = com.example.ui.theme.HighDensityOnPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "نسخ وتحليل المكالمة بالذكاء الاصطناعي",
                                            color = com.example.ui.theme.HighDensityOnPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                } else {
                                    // Display Transcript and Analysis elegantly!
                                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                        // Transcript display
                                        Column {
                                            Text(
                                                "التفريغ الحرفي للمكالمة:",
                                                color = HighDensityPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(HighDensityBg)
                                                    .border(1.dp, HighDensityBorder, RoundedCornerShape(8.dp))
                                                    .padding(10.dp)
                                            ) {
                                                Text(
                                                    text = recording.transcript ?: "",
                                                    color = HighDensityText,
                                                    fontSize = 11.sp,
                                                    lineHeight = 18.sp
                                                )
                                            }
                                        }

                                        // Summary display
                                        recording.summary?.let { summary ->
                                            Column {
                                                Text(
                                                    "ملخص المكالمة الذكي:",
                                                    color = HighDensityPrimary,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = summary,
                                                    color = HighDensityText,
                                                    fontSize = 11.sp,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }

                                        // Important Points display
                                        recording.importantPoints?.let { points ->
                                            Column {
                                                Text(
                                                    "القرارات المتخذة والنقاط الهامة:",
                                                    color = HighDensityPrimary,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = points,
                                                    color = HighDensityText,
                                                    fontSize = 11.sp,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AudioPlayerSection(
    recording: Recording,
    playbackState: AudioPlayerManager.PlaybackState,
    playbackSpeed: Float,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSeek: (Int) -> Unit,
    onSpeedChange: (Float) -> Unit
) {
    val isPlayingThis = when (playbackState) {
        is AudioPlayerManager.PlaybackState.Playing -> playbackState.path == recording.filePath
        is AudioPlayerManager.PlaybackState.Paused -> playbackState.path == recording.filePath
        else -> false
    }

    val isCurrentPlaying = isPlayingThis && playbackState is AudioPlayerManager.PlaybackState.Playing

    val currentPosMs = when (val state = playbackState) {
        is AudioPlayerManager.PlaybackState.Playing -> if (state.path == recording.filePath) state.currentPositionMs else 0
        is AudioPlayerManager.PlaybackState.Paused -> if (state.path == recording.filePath) state.currentPositionMs else 0
        else -> 0
    }

    val durationMs = when (val state = playbackState) {
        is AudioPlayerManager.PlaybackState.Playing -> if (state.path == recording.filePath) state.durationMs else (recording.durationSec * 1000)
        is AudioPlayerManager.PlaybackState.Paused -> if (state.path == recording.filePath) state.durationMs else (recording.durationSec * 1000)
        else -> recording.durationSec * 1000
    }

    val progress = if (durationMs > 0) currentPosMs.toFloat() / durationMs.toFloat() else 0f

    Card(
        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.HighDensityCardBg),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.border(1.dp, HighDensityBorder, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "قارئ المكالمات المسجلة",
                    color = HighDensityText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                // Speed Selector
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(1.0f, 1.5f, 2.0f).forEach { speed ->
                        val isSelected = playbackSpeed == speed
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) HighDensityPrimary else com.example.ui.theme.HighDensityCardBg)
                                .then(
                                    if (!isSelected) Modifier.border(1.dp, HighDensityBorder, RoundedCornerShape(6.dp))
                                    else Modifier
                                )
                                .clickable { onSpeedChange(speed) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${speed}x",
                                color = if (isSelected) com.example.ui.theme.HighDensityOnPrimary else HighDensitySubText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Timeline slider
            Slider(
                value = progress,
                onValueChange = { newProgress ->
                    val seekPos = (newProgress * durationMs).toInt()
                    onSeek(seekPos)
                },
                colors = SliderDefaults.colors(
                    thumbColor = HighDensityPrimary,
                    activeTrackColor = HighDensityPrimary,
                    inactiveTrackColor = HighDensityBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("playback_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration(currentPosMs / 1000),
                    color = HighDensitySubText,
                    fontSize = 11.sp
                )
                Text(
                    text = formatDuration(durationMs / 1000),
                    color = HighDensitySubText,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Playback Button
            IconButton(
                onClick = {
                    val isPlaying = playbackState is AudioPlayerManager.PlaybackState.Playing && playbackState.path == recording.filePath
                    val isPaused = playbackState is AudioPlayerManager.PlaybackState.Paused && playbackState.path == recording.filePath
                    when {
                        isPlaying -> onPause()
                        isPaused -> onResume()
                        else -> onPlay()
                    }
                },
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(HighDensityPrimary)
                    .testTag("play_pause_button")
            ) {
                Icon(
                    imageVector = if (isCurrentPlaying) {
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    },
                    contentDescription = if (isCurrentPlaying) "Pause" else "Play",
                    tint = com.example.ui.theme.HighDensityOnPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// COMPREHENSIVE HIGH-FIDELITY ACTIVE CALL OVERLAY
// ---------------------------------------------------------------------------------
@Composable
fun CallActiveOverlay(
    callerName: String,
    platform: String,
    direction: String,
    durationSec: Int,
    amplitudeList: List<Float>,
    onEndCall: () -> Unit
) {
    val platformLabel = getPlatformArabicName(platform)
    val platformColor = getPlatformColor(platform)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212).copy(alpha = 0.98f))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight()
        ) {
            // Header Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(platformColor.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (callerName.contains("تجريبية")) "مكالمة تجريبية اختبارية" else "مكالمة $platformLabel جارية",
                        color = platformColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = callerName,
                    color = WhiteIce,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "مسجل المكالمات الذكي متصل • جاري التسجيل المباشر من ميكروفونك...",
                    color = SoftGray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Visualizer & timer block
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Live visualizer ring or waves
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(platformColor.copy(alpha = 0.1f))
                        .border(1.dp, platformColor.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing green audio feedback circle
                    val latestAmp = amplitudeList.lastOrNull() ?: 0.05f
                    val pulseScale = 1.0f + latestAmp * 0.5f
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(platformColor.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(platformColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call Logo",
                                tint = Color(0xFF121212),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Timer Duration
                Text(
                    text = formatDuration(durationSec),
                    color = WhiteIce,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MicRecordingColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "جاري الحفظ كملف M4A عالي النقاء",
                        color = SoftGray,
                        fontSize = 11.sp
                    )
                }
            }

            // Decline call button (Red circular hang up button)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                IconButton(
                    onClick = onEndCall,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MicRecordingColor)
                        .testTag("end_sim_call")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hang up call",
                        tint = WhiteIce,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "إنهاء وحفظ المكالمة",
                    color = WhiteIce,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// HELPER CONVERSIONS & FORMATTING FUNCTIONS
// ---------------------------------------------------------------------------------
fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", m, s)
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale("ar"))
    return sdf.format(Date(timestamp))
}

fun getPlatformArabicName(source: String): String {
    return when (source.uppercase()) {
        "WHATSAPP" -> "واتساب"
        "MESSENGER" -> "ماسينجر"
        "CELLULAR" -> "الهاتف العادي"
        else -> "مسجل صوتي"
    }
}

fun getDirectionArabicName(direction: String): String {
    return when (direction.uppercase()) {
        "INBOUND" -> "مكالمة واردة"
        "OUTBOUND" -> "مكالمة صادرة"
        else -> "مسجل صوتي"
    }
}

fun getPlatformColor(source: String): Color {
    return when (source.uppercase()) {
        "WHATSAPP" -> WhatsAppColor
        "MESSENGER" -> MessengerColor
        "CELLULAR" -> CellularColor
        else -> MicRecordingColor
    }
}

data class FilterItem(val label: String, val value: String)
data class PlatformItem(val label: String, val value: String, val color: Color)
data class DirectionItem(val label: String, val isInbound: Boolean, val color: Color)
