package com.example.elderui.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.elderui.core.di.rememberAppViewModelFactory
import com.example.elderui.core.utils.ImageUtils
import com.example.elderui.core.utils.LoginMethod
import com.example.elderui.core.viewmodel.AuthViewModel
import com.example.elderui.core.viewmodel.LoginState
import com.example.elderui.ui.component.CommonTextField
import com.example.elderui.ui.component.ErrorMessage
import com.example.elderui.ui.component.LoadingIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image

/**
 * 登录屏幕
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = rememberAppViewModelFactory()
    ),
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // 使用 ViewModel 中的 LoginMethod
    val currentLoginMethod by viewModel.loginMethod.collectAsState()

    // 检查是否有相机权限
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // 拍照功能 Results
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val base64 = ImageUtils.bitmapToBase64(bitmap)
            viewModel.loginByCard(base64)
        }
    }

    // 权限请求 Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasCameraPermission = isGranted
        if (isGranted) {
            cameraLauncher.launch()
        } else {
            Toast.makeText(context, "需要相机权限才能拍照登录", Toast.LENGTH_SHORT).show()
        }
    }

    // 相册选择 Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64 = ImageUtils.uriToBase64(context, uri)
            if (base64 != null) {
                viewModel.loginByCard(base64)
            } else {
                Toast.makeText(context, "图片处理失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // 标题
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "智链邻里",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "老人服务平台",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 错误信息
        (loginState as? LoginState.Error)?.let {
            ErrorMessage(it.message)
        }

        if (loginState is LoginState.Loading) {
            LoadingIndicator(message = "登录中...")
        } else {
            // 登录表单
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 登录方式选择
                TabRow(selectedTabIndex = if (currentLoginMethod == LoginMethod.CARD) 0 else 1) {
                    Tab(
                        selected = currentLoginMethod == LoginMethod.CARD,
                        onClick = { viewModel.setLoginMethod(LoginMethod.CARD) },
                        text = { Text("刷卡/拍照登录") }
                    )
                    Tab(
                        selected = currentLoginMethod == LoginMethod.ACCOUNT,
                        onClick = { viewModel.setLoginMethod(LoginMethod.ACCOUNT) },
                        text = { Text("账号密码登录") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (currentLoginMethod == LoginMethod.CARD) {
                    // 卡片登录 UI
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                if (hasCameraPermission) {
                                    cameraLauncher.launch()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.size(160.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("点击拍照", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("扫描身份证/社区卡", fontSize = 12.sp)
                            }
                        }
                    }
                    Text(
                        text = "请将身份证或社区卡正面朝向镜头",
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 从相册选择按钮
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("从相册选择照片")
                    }
                } else {
                    // 账号密码登录 UI
                    CommonTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "用户名"
                    )

                    CommonTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "密码",
                        isPassword = true
                    )

                    // 登录按钮
                    Button(
                        onClick = {
                            if (username.isNotBlank() && password.isNotBlank()) {
                                viewModel.login(username, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = username.isNotBlank() && password.isNotBlank() && loginState !is LoginState.Loading,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "登录",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
