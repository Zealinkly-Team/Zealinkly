package com.example.elderui.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.elderui.core.api.Notification
import com.example.elderui.core.di.rememberAppViewModelFactory
import com.example.elderui.core.viewmodel.NotificationViewModel
import com.example.elderui.ui.component.*

/**
 * 通知列表界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationListScreen(
    onBack: () -> Unit = {}
) {
    val factory = rememberAppViewModelFactory()
    val viewModel: NotificationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val notifications by viewModel.notifications.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showMarkAllDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getNotifications()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("通知") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (notifications.any { !it.isRead }) {
                        TextButton(onClick = { showMarkAllDialog = true }) {
                            Text("全部已读")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (error != null) {
                ErrorMessage(error!!) { viewModel.clearError() }
            }

            if (loading && notifications.isEmpty()) {
                LoadingIndicator()
            } else if (notifications.isEmpty()) {
                EmptyState(
                    message = "暂无通知",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }

                    items(notifications) { notification ->
                        NotificationCard(
                            notification = notification,
                            onMarkAsRead = {
                                if (!notification.isRead) {
                                    viewModel.markAsRead(notification.id)
                                }
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    // 全部标记为已读确认对话框
    if (showMarkAllDialog) {
        AlertDialog(
            onDismissRequest = { showMarkAllDialog = false },
            title = { Text("标记全部已读") },
            text = { Text("确认将所有未读通知标记为已读吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.markAllAsRead()
                        showMarkAllDialog = false
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMarkAllDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 通知卡片组件
 */
@Composable
fun NotificationCard(
    notification: Notification,
    onMarkAsRead: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (notification.isRead) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        },
        onClick = onMarkAsRead
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 未读标识
            if (!notification.isRead) {
                Surface(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.Top)
                        .padding(top = 6.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {}
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }

            // 通知内容
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = notification.title,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = notification.message,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Text(
                    text = formatNotificationTime(notification.createdAt),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }

            // 图标
            Icon(
                imageVector = when {
                    notification.title.contains("任务") -> Icons.Filled.Task
                    notification.title.contains("积分") -> Icons.Filled.Star
                    notification.title.contains("紧急") -> Icons.Filled.Warning
                    else -> Icons.Filled.Notifications
                },
                contentDescription = null,
                tint = if (notification.isRead) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }
    }
}

/**
 * 格式化通知时间显示
 */
fun formatNotificationTime(time: String): String {
    return try {
        // 简单处理：只显示日期和时间
        val parts = time.split("T")
        if (parts.size >= 2) {
            val date = parts[0]
            val timePart = parts[1].split(".")[0]
            "$date $timePart"
        } else {
            time.take(16)
        }
    } catch (e: Exception) {
        time.take(16)
    }
}


