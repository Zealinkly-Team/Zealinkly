package com.example.elderui.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.elderui.core.api.ChatHistory
import com.example.elderui.core.di.rememberAppViewModelFactory
import com.example.elderui.core.viewmodel.ChatViewModel
import com.example.elderui.ui.component.CardContainer
import com.example.elderui.ui.component.ErrorMessage
import com.example.elderui.ui.component.LoadingIndicator

/**
 * AI聊天屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    val factory = rememberAppViewModelFactory()
    val viewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val history by viewModel.history.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.getChatHistory()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("AI聊天") }
        )

        if (error != null) {
            ErrorMessage(error!!) { viewModel.clearError() }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            reverseLayout = true
        ) {
            items(history.reversed()) { chat ->
                ChatMessageItem(chat)
            }
        }

        // 输入框
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("问我任何问题...") },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(24.dp)
            )
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.ask(inputText)
                        inputText = ""
                    }
                },
                enabled = inputText.isNotBlank() && !loading
            ) {
                Icon(Icons.Filled.Send, contentDescription = "发送")
            }
        }
    }
}

@Composable
fun ChatMessageItem(chat: com.example.elderui.core.api.ChatHistory) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 用户提问
        Box(
            modifier = Modifier
                .align(Alignment.End)
                .fillMaxWidth(0.8f)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = chat.question,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // AI回答
        Box(
            modifier = Modifier
                .align(Alignment.Start)
                .fillMaxWidth(0.8f)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = chat.answer,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = chat.createdAt.take(16),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
