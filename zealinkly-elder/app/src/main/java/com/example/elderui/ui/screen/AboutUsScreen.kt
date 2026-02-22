package com.example.elderui.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.elderui.ui.component.CommonTopBar

/**
 * 关于我们 - 详细页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(
    onBackClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 顶部栏
        CommonTopBar(
            title = "关于我们",
            onBackClick = onBackClick
        )

        // 内容区域
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 应用信息卡片
            ApplicationInfoCard()

            // 功能介绍
            FeaturesSection()

            // 技术信息
            TechInfoSection()

            // 联系我们
            ContactSection()

            // 隐私和条款
            LinksSection()

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * 应用信息卡片
 */
@Composable
fun ApplicationInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 应用名称
            Text(
                text = "智链邻里",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // 版本号
            Text(
                text = "v1.0.0",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // 描述
            Text(
                text = "老人社区服务平台",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            // 发布日期
            Text(
                text = "发布于 2026年2月",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * 功能介绍区
 */
@Composable
fun FeaturesSection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "主要功能",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        listOf(
            "🎤 语音输入" to "自然语言处理，支持语音转文字和意图识别",
            "📋 互助任务" to "发布和接受社区互助任务，获得积分奖励",
            "🚨 紧急报警" to "一键紧急报警，自动通知紧急联系人",
            "⭐ 积分系统" to "完成任务获得积分，兑换社区服务",
            "🔔 通知管理" to "及时接收任务和系统通知",
            "👥 联系人管理" to "管理紧急联系人，确保及时联系"
        ).forEach { (title, desc) ->
            FeatureItem(title, desc)
        }
    }
}

/**
 * 功能项
 */
@Composable
fun FeatureItem(title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * 技术信息区
 */
@Composable
fun TechInfoSection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "技术信息",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AboutInfoRow("开发语言", "Kotlin")
                AboutInfoRow("UI框架", "Jetpack Compose")
                AboutInfoRow("最低系统", "Android 8.0 (API 26)")
                AboutInfoRow("目标系统", "Android 14+ (API 34)")
                AboutInfoRow("存储", "50 MB 可用空间")
            }
        }
    }
}

/**
 * 信息行 - 关于我们页面特用
 */
@Composable
fun AboutInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 联系我们区
 */
@Composable
fun ContactSection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "联系我们",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "如遇到问题或有改进建议，欢迎联系我们：",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                listOf(
                    "📧 邮箱：support@zealinkly.com",
                    "📞 客服热线：400-800-8888",
                    "💬 微信公众号：智链邻里",
                    "🌐 官网：www.zealinkly.com"
                ).forEach { contact ->
                    Text(
                        text = contact,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 链接区（隐私、条款等）
 */
@Composable
fun LinksSection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "其他",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        listOf(
            "隐私政策",
            "用户协议",
            "意见反馈"
        ).forEach { link ->
            LinkButton(link)
        }

        Text(
            text = "感谢您的使用！",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

/**
 * 链接按钮
 */
@Composable
fun LinkButton(text: String) {
    TextButton(
        onClick = { /* 处理链接点击 */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}



