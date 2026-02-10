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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import com.example.elderui.core.di.rememberAppViewModelFactory
import com.example.elderui.core.viewmodel.UserViewModel
import com.example.elderui.ui.component.*

/**
 * 个人资料屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToEmergencyContacts: () -> Unit = {}
) {
    val factory = rememberAppViewModelFactory()
    val userViewModel: UserViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val context = LocalContext.current

    val userInfo by userViewModel.userInfo.collectAsState()
    val loading by userViewModel.loading.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var editRealName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editAddress by remember { mutableStateOf("") }
    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!isInitialized) {
            userViewModel.getUserInfo()
            isInitialized = true
        }
    }

    LaunchedEffect(userInfo) {
        userInfo?.let {
            editRealName = it.realName
            editPhone = it.phone
            editAddress = it.address ?: ""
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("我的") }
        )

        if (loading && userInfo == null) {
            // 显示加载状态，避免闪烁
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 用户信息卡片
                userInfo?.let {
                    CardContainer {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(it.realName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text("@${it.username}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(onClick = { showEditDialog = true }) {
                                    Text("编辑")
                                }
                            }

                            HorizontalDivider()

                            InfoRow("电话", it.phone)
                            InfoRow("地址", it.address ?: "未设置")
                            InfoRow("积分", it.points.toString())
                        }
                    }
                }

                // 快速操作
                // 快速操作
                Text("快速操作", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                QuickActionItem(
                    icon = Icons.Filled.Notifications,
                    label = "通知",
                    onClick = onNavigateToNotifications
                )

                QuickActionItem(
                    icon = Icons.Filled.Contacts,
                    label = "紧急联系人",
                    onClick = onNavigateToEmergencyContacts
                )

                QuickActionItem(
                    icon = Icons.Filled.Help,
                    label = "关于我们",
                    onClick = {
                        Toast.makeText(context, "关于我们\n智链邻里 v1.0\n老人服务平台", Toast.LENGTH_LONG).show()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 登出按钮
                Button(
                    onClick = { showLogoutConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("登出", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // 编辑信息对话框
    if (showEditDialog && userInfo != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("编辑个人信息") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editRealName,
                        onValueChange = { editRealName = it },
                        label = { Text("姓名") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("电话") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text("地址") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        userViewModel.updateUserInfo(editRealName, editPhone, editAddress)
                        showEditDialog = false
                    }
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 登出确认
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("确认登出") },
            text = { Text("确认要退出登录吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("登出")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun QuickActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String = "",
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = label)
                Text(label)
            }
            if (value.isNotEmpty()) {
                Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

