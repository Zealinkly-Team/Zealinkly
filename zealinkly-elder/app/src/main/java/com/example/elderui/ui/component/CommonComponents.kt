package com.example.elderui.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 通用UI组件 - 三端共用
 */

/**
 * 加载中指示器
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    message: String = "加载中..."
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(message)
        }
    }
}

/**
 * 错误显示
 */
@Composable
fun ErrorMessage(
    error: String,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    }
}

/**
 * 状态容器
 */
@Composable
fun StateContainer(
    isLoading: Boolean = false,
    error: String? = null,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> LoadingIndicator()
            error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ErrorMessage(error)
                    Button(onClick = onRetry) {
                        Text("重试")
                    }
                }
            }
            else -> content()
        }
    }
}

/**
 * 卡片容器
 */
@Composable
fun CardContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        content()
    }
}

/**
 * 按钮样式 - 主按钮
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 按钮样式 - 次按钮
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 输入框
 */
/**
 * 通用文本输入框 - 完全支持中文输入（虚拟键盘和物理键盘）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    enabled: Boolean = true
) {
    // 使用 BasicTextField 以获得更好的中文输入支持
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // 直接传递所有输入，包括中文字符
            onValueChange(newValue)
        },
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        visualTransformation = if (isPassword) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        )
    )
}

/**
 * 标题栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        navigationIcon = if (onBackClick != null) {
            {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "返回"
                    )
                }
            }
        } else {
            { }
        },
        actions = actions
    )
}

/**
 * 空状态显示
 */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        action?.invoke()
    }
}

/**
 * 对话框
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "确认",
    dismissText: String = "取消",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isVisible: Boolean
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(dismissText)
                }
            }
        )
    }
}

