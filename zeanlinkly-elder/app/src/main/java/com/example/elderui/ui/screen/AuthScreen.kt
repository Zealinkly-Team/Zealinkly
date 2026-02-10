package com.example.elderui.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.elderui.core.di.rememberAppViewModelFactory
import com.example.elderui.core.viewmodel.AuthViewModel
import com.example.elderui.core.viewmodel.LoginState
import com.example.elderui.core.viewmodel.RegisterState
import com.example.elderui.ui.component.CommonTextField
import com.example.elderui.ui.component.ErrorMessage
import com.example.elderui.ui.component.LoadingIndicator

/**
 * 登录屏幕
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = rememberAppViewModelFactory()
    ),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var useCardLogin by remember { mutableStateOf(false) }

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
                CommonTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "用户名",
                    enabled = !useCardLogin
                )

                if (!useCardLogin) {
                    CommonTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "密码",
                        isPassword = true
                    )
                }

                // 登录按钮
                Button(
                    onClick = {
                        if (!useCardLogin && username.isNotBlank() && password.isNotBlank()) {
                            viewModel.login(username, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !useCardLogin && username.isNotBlank() && password.isNotBlank() && loginState !is LoginState.Loading,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "登录",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 卡片登录选项
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = useCardLogin,
                        onCheckedChange = { useCardLogin = it }
                    )
                    Text("使用社区卡登录")
                }

                // 注册链接
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("还没有账户？")
                    TextButton(onClick = onNavigateToRegister) {
                        Text("立即注册")
                    }
                }
            }
        }
    }
}

/**
 * 注册屏幕
 */
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = rememberAppViewModelFactory()
    ),
    onRegisterSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var realName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val registerState by viewModel.registerState.collectAsState()

    LaunchedEffect(registerState) {
        if (registerState is com.example.elderui.core.viewmodel.RegisterState.Success) {
            onRegisterSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 标题
        Text(
            text = "注册账户",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        (registerState as? com.example.elderui.core.viewmodel.RegisterState.Error)?.let {
            ErrorMessage(it.message)
        }

        if (registerState is com.example.elderui.core.viewmodel.RegisterState.Loading) {
            LoadingIndicator(message = "注册中...")
        } else {
            CommonTextField(value = username, onValueChange = { username = it }, label = "用户名")
            CommonTextField(value = realName, onValueChange = { realName = it }, label = "姓名")
            CommonTextField(value = phone, onValueChange = { phone = it }, label = "电话")
            CommonTextField(value = password, onValueChange = { password = it }, label = "密码", isPassword = true)
            CommonTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = "确认密码", isPassword = true)

            Button(
                onClick = {
                    if (password == confirmPassword && username.isNotBlank() && realName.isNotBlank()) {
                        viewModel.register(username, password, realName, phone)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = registerState !is com.example.elderui.core.viewmodel.RegisterState.Loading
            ) {
                Text("注册")
            }

            TextButton(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
                Text("返回登录")
            }
        }
    }
}
