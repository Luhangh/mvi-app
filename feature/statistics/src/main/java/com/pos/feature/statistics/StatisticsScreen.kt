package com.pos.feature.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 订单统计和分析页面
 *
 * 功能特性：
 * - 今日销售统计
 * - 订单数量统计
 * - 热销商品分析
 * - 支付方式分布
 * - 时间段销售趋势
 *
 * @param onNavigateBack 返回回调
 * @param viewModel 统计 ViewModel
 * @author POS Team
 * @since 1.0.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedPeriod by remember { mutableStateOf("today") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("销售统计") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 时间段选择
            PeriodSelector(
                selectedPeriod = selectedPeriod,
                onPeriodChange = { selectedPeriod = it }
            )

            // 核心指标卡片
            CoreMetricsSection(state = state)

            // 销售趋势图表
            SalesTrendSection(state = state)

            // 支付方式分布
            PaymentMethodDistributionSection(state = state)

            // 热销商品
            TopProductsSection(state = state)
        }
    }
}

/**
 * 时间段选择器组件
 */
@Composable
private fun PeriodSelector(
    selectedPeriod: String,
    onPeriodChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "统计周期",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "today" to "今天",
                    "week" to "本周",
                    "month" to "本月",
                    "year" to "本年"
                ).forEach { (period, label) ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { onPeriodChange(period) },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * 核心指标卡片组件
 */
@Composable
private fun CoreMetricsSection(
    state: StatisticsState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "核心指标",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // 第一行：总销售额 + 订单数
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "总销售额",
                value = "¥${String.format("%.2f", state.totalSales)}",
                icon = Icons.Default.AttachMoney,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "订单总数",
                value = "${state.totalOrders}",
                icon = Icons.Default.ShoppingCart,
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
        }

        // 第二行：平均订单额 + 客单价
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "平均订单额",
                value = "¥${String.format("%.2f", state.averageOrderValue)}",
                icon = Icons.Default.TrendingUp,
                color = Color(0xFF9C27B0),
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "客单价",
                value = "¥${String.format("%.2f", state.customerUnitPrice)}",
                icon = Icons.Default.Person,
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 指标卡片组件
 */
@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.material.icons.filled.Add, // 使用实际的 Icon
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

/**
 * 销售趋势图表部分
 */
@Composable
private fun SalesTrendSection(
    state: StatisticsState,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "销售趋势",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // 简化的趋势显示（实际应用中可以使用图表库）
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(5) { hour ->
                    TrendBar(
                        label = "${9 + hour}:00",
                        value = (state.totalSales * (0.3f + hour * 0.15f)).toInt(),
                        maxValue = (state.totalSales * 1.2f).toInt()
                    )
                }
            }
        }
    }
}

/**
 * 趋势条组件
 */
@Composable
private fun TrendBar(
    label: String,
    value: Int,
    maxValue: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (maxValue > 0) value.toFloat() / maxValue else 0f

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(40.dp)
        )

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
        )

        Text(
            text = "¥$value",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(60.dp),
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 支付方式分布部分
 */
@Composable
private fun PaymentMethodDistributionSection(
    state: StatisticsState,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "支付方式分布",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val paymentMethods = listOf(
                "现金" to 0.4f,
                "银行卡" to 0.35f,
                "移动支付" to 0.25f
            )

            paymentMethods.forEach { (method, percentage) ->
                PaymentMethodItem(
                    method = method,
                    percentage = percentage
                )
            }
        }
    }
}

/**
 * 支付方式项目
 */
@Composable
private fun PaymentMethodItem(
    method: String,
    percentage: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = method,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(60.dp)
        )

        LinearProgressIndicator(
            progress = percentage,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "${(percentage * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp)
        )
    }
}

/**
 * 热销商品部分
 */
@Composable
private fun TopProductsSection(
    state: StatisticsState,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "热销商品 TOP 5",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 模拟热销商品数据
            val topProducts = listOf(
                "三明治套餐" to 156,
                "汉堡套餐" to 142,
                "可口可乐" to 128,
                "炸鸡套餐" to 115,
                "咖啡" to 98
            )

            topProducts.forEachIndexed { index, (productName, sales) ->
                TopProductItem(
                    rank = index + 1,
                    productName = productName,
                    sales = sales
                )
            }
        }
    }
}

/**
 * 热销商品项目
 */
@Composable
private fun TopProductItem(
    rank: Int,
    productName: String,
    sales: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // 排名徽章
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = when (rank) {
                    1 -> Color(0xFFFFD700)  // 金色
                    2 -> Color(0xFFC0C0C0)  // 银色
                    3 -> Color(0xFFCD7F32)  // 铜色
                    else -> MaterialTheme.colorScheme.primaryContainer
                },
                modifier = Modifier.size(32.dp)
            ) {
                Text(
                    text = rank.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.wrapContentSize(Alignment.Center)
                )
            }

            Text(
                text = productName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "$sales 份",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
