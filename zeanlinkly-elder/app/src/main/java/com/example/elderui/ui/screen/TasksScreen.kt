package com.example.elderui.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.elderui.core.api.Task
import com.example.elderui.core.di.rememberAppViewModelFactory
import com.example.elderui.core.viewmodel.TaskViewModel
import com.example.elderui.ui.component.*

/**
 * 任务列表屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen() {
    val factory = rememberAppViewModelFactory()
    val viewModel: TaskViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val tasks by viewModel.tasks.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showPublishDialog by remember { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var taskPoints by remember { mutableStateOf("10") }

    LaunchedEffect(Unit) {
        viewModel.getMyTasks()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopAppBar(
            title = { Text("我的任务") }
        )

        if (error != null) {
            ErrorMessage(error!!) { viewModel.clearError() }
        }

        if (loading) {
            LoadingIndicator()
        } else if (tasks.isEmpty()) {
            EmptyState(
                message = "还没有任务",
                modifier = Modifier.weight(1f)
            ) {
                Button(onClick = { showPublishDialog = true }) {
                    Text("发布新任务")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tasks) { task ->
                    TaskCard(
                        task = task,
                        onConfirm = { taskId ->
                            viewModel.confirmTask(taskId)
                        }
                    )
                }

                item {
                    Button(
                        onClick = { showPublishDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "发布")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("发布新任务")
                    }
                }
            }
        }
    }

    if (showPublishDialog) {
        AlertDialog(
            onDismissRequest = { showPublishDialog = false },
            title = { Text("发布任务") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("任务标题") }
                    )
                    OutlinedTextField(
                        value = taskDescription,
                        onValueChange = { taskDescription = it },
                        label = { Text("任务描述") },
                        modifier = Modifier.height(100.dp)
                    )
                    OutlinedTextField(
                        value = taskPoints,
                        onValueChange = { taskPoints = it },
                        label = { Text("积分奖励") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.publishTask(taskTitle, taskDescription, taskPoints.toIntOrNull() ?: 10)
                        showPublishDialog = false
                        taskTitle = ""
                        taskDescription = ""
                        taskPoints = "10"
                    }
                ) {
                    Text("发布")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPublishDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun TaskCard(
    task: Task,
    onConfirm: (Long) -> Unit = {}
) {
    CardContainer {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(task.content.take(20), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                AssistChip(
                    onClick = {},
                    label = { Text(task.status, fontSize = 12.sp) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("奖励: ${task.pointsReward}分", color = MaterialTheme.colorScheme.primary)
                if (task.volunteer != null) {
                    Text("志愿者: ${task.volunteer.realName}")
                }
            }

            Text(task.createdAt.take(10), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // 如果任务状态是SUBMITTED（志愿者已提交证据），显示确认按钮
            if (task.status == "SUBMITTED") {
                Button(
                    onClick = { onConfirm(task.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "确认")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("确认完成")
                }
            }
        }
    }
}
