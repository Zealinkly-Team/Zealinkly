package com.example.elderui.ui.screen

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
                    onNavigateToEmergencyContacts = { navController.navigate("emergency_contacts") }
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
@Composable
fun HomeMainScreen(
    onShowVoiceInput: () -> Unit = {},
    onNavigateToChat: () -> Unit = {}
) {
    val factory = rememberAppViewModelFactory()
    val agentViewModel: AgentViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val userViewModel: UserViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val emergencyViewModel: EmergencyViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)

    var textInput by remember { mutableStateOf("") }
    var showEmergencyConfirm by remember { mutableStateOf(false) }

    val agentResult by agentViewModel.result.collectAsState()
    val agentLoading by agentViewModel.loading.collectAsState()
    val agentError by agentViewModel.error.collectAsState()

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
        message = "确认要触发紧急报警吗？报警信息将立即发送给管理员和您的紧急联系人。",
        confirmText = "确认报警",
        onConfirm = {
            emergencyViewModel.triggerEmergency()
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
                text = "您有丰富的社区生活，现在有任何需要吗？",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceInputBottomSheet(
    onDismiss: () -> Unit = {}
) {
    val factory = rememberAppViewModelFactory()
    val agentViewModel: AgentViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    var isRecording by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
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

            if (isRecording) {
                Icon(
                    Icons.Filled.Mic,
                    contentDescription = "录音中",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text("正在录音...", color = MaterialTheme.colorScheme.error)
            } else {
                Icon(
                    Icons.Filled.Mic,
                    contentDescription = "未开始录音",
                    modifier = Modifier.size(80.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { isRecording = !isRecording },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (isRecording) "停止录音" else "开始录音")
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("关闭")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
