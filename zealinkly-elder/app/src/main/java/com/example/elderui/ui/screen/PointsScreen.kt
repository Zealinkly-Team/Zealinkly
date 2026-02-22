package com.example.elderui.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.elderui.core.api.PointsRecord
import com.example.elderui.core.di.rememberAppViewModelFactory
import com.example.elderui.core.viewmodel.PointsViewModel
import com.example.elderui.ui.component.CardContainer
import com.example.elderui.ui.component.LoadingIndicator
import com.example.elderui.ui.component.ErrorMessage

/**
 * 积分屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointsScreen() {
    val factory = rememberAppViewModelFactory()
    val viewModel: PointsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val total by viewModel.total.collectAsState()
    val history by viewModel.history.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getPointsTotal()
        viewModel.getPointsHistory()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("我的积分") }
        )

        if (error != null) {
            ErrorMessage(error!!) { viewModel.clearError() }
        }

        if (loading) {
            LoadingIndicator()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    PointsSummaryCard(total = total)
                }

                item {
                    Text(
                        "积分明细",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                items(history) { record ->
                    PointsRecordCard(record)
                }
            }
        }
    }
}

@Composable
fun PointsSummaryCard(total: Int) {
    CardContainer {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("总积分", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = total.toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text("可用于社区商城兑换礼品", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PointsRecordCard(record: com.example.elderui.core.api.PointsRecord) {
    CardContainer {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(record.reasonDescription, fontWeight = FontWeight.Bold)
                Text(record.createdAt.take(10), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${if (record.amount > 0) "+" else ""}${record.amount}",
                    fontWeight = FontWeight.Bold,
                    color = if (record.amount > 0) Color.Green else Color.Red
                )
                Text(
                    "余额: ${record.balanceAfter}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
