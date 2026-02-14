package com.example.elderui.ui.screen

import android.media.MediaPlayer
import java.io.File
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.elderui.core.api.AgentProcessResponse
import com.example.elderui.core.di.rememberAppViewModelFactory
import com.example.elderui.core.viewmodel.*
import com.example.elderui.ui.component.*
import com.example.elderui.core.utils.AudioRecorder
import com.example.elderui.core.utils.TtsManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.ui.platform.LocalContext

/**
 * 首页屏幕 - 老人端主界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("home_main") },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "首页") },
                    label = { Text("首页") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("tasks") },
                    icon = { Icon(Icons.Filled.Task, contentDescription = "任务") },
                    label = { Text("任务") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("points") },
                    icon = { Icon(Icons.Filled.Star, contentDescription = "积分") },
                    label = { Text("积分") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("profile") },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "我的") },
                    label = { Text("我的") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home_main",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home_main") {
                HomeMainScreen(
                    onShowVoiceInput = { showBottomSheet = true },
                    onNavigateToChat = { navController.navigate("chat") }
                )
            }
            composable("tasks") {
                TasksScreen()
            }
            composable("points") {
                PointsScreen()
            }
            composable("profile") {
                ProfileScreen(
                    onLogout = onLogout,
                    onNavigateToNotifications = { navController.navigate("notifications") },
                    onNavigateToEmergencyContacts = { navController.navigate("emergency_contacts") },
                    onNavigateToAboutUs = { navController.navigate("about_us") }
                )
            }
            composable("chat") {
                ChatScreen()
            }
            composable("notifications") {
                NotificationListScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("emergency_contacts") {
                EmergencyContactScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("about_us") {
                AboutUsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }

    if (showBottomSheet) {
        VoiceInputBottomSheet(
            onDismiss = { showBottomSheet = false }
        )
    }
}

/**
 * 首页主界面
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeMainScreen(
    onShowVoiceInput: () -> Unit = {},
    onNavigateToChat: () -> Unit = {}
) {
    val context = LocalContext.current
    val factory = rememberAppViewModelFactory()
    val agentViewModel: AgentViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val userViewModel: UserViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val emergencyViewModel: EmergencyViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)

    var textInput by remember { mutableStateOf("") }
    var showEmergencyConfirm by remember { mutableStateOf(false) }

    val agentResult by agentViewModel.result.collectAsState()
    val agentLoading by agentViewModel.loading.collectAsState()
    val agentError by agentViewModel.error.collectAsState()

    // 初始化 TTS (文字转语音)
    val ttsManager = remember { TtsManager(context) }

    // 当收到新的 AI 回复时，自动朗读
    LaunchedEffect(agentResult) {
        agentResult?.aiResponse?.let { response ->
            if (response.isNotBlank()) {
                ttsManager.speak(response)
            }
        }
    }

    // 页面销毁时释放 TTS 资源
    DisposableEffect(Unit) {
        onDispose {
            ttsManager.shutdown()
        }
    }

    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
        )
    )

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 欢迎信息
        WelcomeCard()

        // 快速操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ActionButton(
                icon = Icons.Filled.Mic,
                label = "语音输入",
                onClick = onShowVoiceInput,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                icon = Icons.Filled.Phone,
                label = "紧急报警",
                onClick = { showEmergencyConfirm = true },
                modifier = Modifier.weight(1f)
            )
        }

        // 文字输入区域
        CardContainer {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("文字输入", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("告诉我你需要什么帮助") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                Button(
                    onClick = { agentViewModel.processText(textInput) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = textInput.isNotBlank() && !agentLoading
                ) {
                    Text("发送")
                }
            }
        }

        // 错误显示
        agentError?.let {
            ErrorMessage(it) { agentViewModel.clearError() }
        }

        // 结果显示
        if (agentLoading) {
            LoadingIndicator()
        } else if (agentResult != null) {
            AgentResultCard(agentResult!!)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // 紧急报警确认
    ConfirmDialog(
        title = "确认紧急报警",
        message = "确认要触发紧急报警吗？系统将尝试获取您当前的位置信息并一同发送给管理员和您的紧急联系人。",
        confirmText = "确认报警",
        onConfirm = {
            if (locationPermissionsState.allPermissionsGranted) {
                // 权限已授予，可以安全地调用
                emergencyViewModel.triggerEmergencyWithLocation()
            } else {
                // 权限被拒绝，不带位置信息报警
                emergencyViewModel.triggerEmergency()
            }
            showEmergencyConfirm = false
        },
        onDismiss = { showEmergencyConfirm = false },
        isVisible = showEmergencyConfirm
    )
}

@Composable
fun WelcomeCard() {
    val factory = rememberAppViewModelFactory()
    val userViewModel: UserViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val userInfo by userViewModel.userInfo.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.getUserInfo()
    }

    CardContainer {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 优化：只在用户信息加载完成后才显示名字，避免闪烁
            if (userInfo?.realName != null) {
                Text(
                    text = "欢迎，${userInfo?.realName}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "欢迎",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "请问您现在有任何需要吗？",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(32.dp))
            Text(label, fontSize = 14.sp)
        }
    }
}

@Composable
fun AgentResultCard(result: com.example.elderui.core.api.AgentProcessResponse) {
    CardContainer {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("识别结果", fontWeight = FontWeight.Bold)
            Text("意图：${result.intentDescription}", color = MaterialTheme.colorScheme.primary)
            Text("理解：${result.userInput}")

            if (result.tasks.isNotEmpty()) {
                Text("任务信息：", fontWeight = FontWeight.SemiBold)
                result.tasks.forEach { task ->
                    Text("• ${task.typeDescription}: ${task.description}")
                }
            }

            if (result.createdTasks?.isNotEmpty() == true) {
                Text("状态：${result.message}", color = MaterialTheme.colorScheme.secondary)
            }

            result.aiResponse?.let {
                Text("AI回复：$it", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun VoiceInputBottomSheet(
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val factory = rememberAppViewModelFactory()
    val agentViewModel: AgentViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)

    // 录音权限状态
    val audioPermissionState = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)

    // 音频录制工具
    val audioRecorder = remember { AudioRecorder(context) }

    // 状态管理
    var isRecording by remember { mutableStateOf(false) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    // 播放器
    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    // 播放录音
    fun playRecording() {
        recordedFile?.let { file ->
            if (file.exists()) {
                try {
                    if (isPlaying) {
                        mediaPlayer.stop()
                        isPlaying = false
                    } else {
                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(file.absolutePath)
                        mediaPlayer.prepare()
                        mediaPlayer.start()
                        isPlaying = true
                        mediaPlayer.setOnCompletionListener {
                            isPlaying = false
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    isPlaying = false
                }
            }
        }
    }

    // 处理停止录音
    fun stopRecording() {
        if (!isRecording) return
        isRecording = false
        recordedFile = audioRecorder.stopRecording()
    }

    // 发送录音
    fun sendRecording() {
        recordedFile?.let { file ->
            if (file.exists()) {
                val base64 = audioRecorder.getFileBase64(file)
                agentViewModel.processVoice(base64)
                onDismiss()
            }
        }
    }

    ModalBottomSheet(onDismissRequest = {
        if (isRecording) {
            audioRecorder.stopRecording()
        }
        if (isPlaying) {
            mediaPlayer.stop()
        }
        onDismiss()
    }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "语音输入",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // 状态显示图标
            if (isRecording) {
                Icon(
                    Icons.Filled.Mic,
                    contentDescription = "录音中",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text("正在录音...", color = MaterialTheme.colorScheme.error)
            } else if (recordedFile != null) {
                 Icon(
                    if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    contentDescription = "播放",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(if (isPlaying) "正在播放..." else "录音完成，请确认", color = MaterialTheme.colorScheme.primary)
            } else {
                Icon(
                    Icons.Filled.Mic,
                    contentDescription = "未开始录音",
                    modifier = Modifier.size(80.dp)
                )
                Text("点击开始录音")
            }

            // 按钮区域
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (recordedFile == null) {
                    // 录音阶段
                    Button(
                        onClick = {
                            if (isRecording) {
                                stopRecording()
                            } else {
                                if (audioPermissionState.status.isGranted) {
                                    if (audioRecorder.startRecording()) {
                                        isRecording = true
                                    }
                                } else {
                                    audioPermissionState.launchPermissionRequest()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (isRecording) "停止录音" else "开始录音")
                    }
                } else {
                    // 确认阶段
                    Button(
                        onClick = { playRecording() },
                        modifier = Modifier.weight(1f),
                         colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text(if (isPlaying) "停止播放" else "试听")
                    }

                    Button(
                        onClick = { sendRecording() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("发送")
                    }
                }
            }

            // 取消/重录按钮
            if (recordedFile != null) {
                TextButton(onClick = {
                    recordedFile = null
                    if (isPlaying) {
                        mediaPlayer.stop()
                        isPlaying = false
                    }
                }) {
                    Text("删除并重录")
                }
            } else {
                Button(
                    onClick = {
                        if (isRecording) {
                            audioRecorder.stopRecording()
                            isRecording = false
                        }
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("取消")
                }
            }

            if (!audioPermissionState.status.isGranted) {
                Text(
                    "需要录音权限才能使用语音输入功能",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
