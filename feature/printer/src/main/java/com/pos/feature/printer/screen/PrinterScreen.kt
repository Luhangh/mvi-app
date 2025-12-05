package com.pos.feature.printer.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pos.feature.printer.viewmodel.PrinterViewModel
import com.pos.feature.printer.mvi.PrinterIntent
import com.pos.feature.printer.mvi.PrinterEffect
import com.pos.feature.printer.mvi.PrinterStatus
import com.pos.feature.printer.mvi.PrintJobStatus
import com.pos.ui.components.ActionButtonGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrinterScreen(
    orderId: String,
    viewModel: PrinterViewModel = hiltViewModel(),
    onNavigateToOrderHistory: () -> Unit = { },
    onNavigateBack: () -> Unit = { }
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var expandedPrinterMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!orderId.isNullOrEmpty()) {
            viewModel.handleIntent(
                PrinterIntent.PrintReceipt(
                    orderId = orderId,
                    orderNumber = "", // TODO: 获取订单号
                    items = emptyList(), // TODO: 获取订单项
                    totalAmount = 0.0, // TODO: 获取总金额
                    paymentMethod = "" // TODO: 获取支付方式
                )
            )
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PrinterEffect.PrintSuccess -> {
                    snackbarHostState.showSnackbar(
                        "打印成功！任务ID: ${effect.jobId}",
                        duration = SnackbarDuration.Short
                    )
                }
                is PrinterEffect.PrintFailed -> {
                    snackbarHostState.showSnackbar(
                        effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is PrinterEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is PrinterEffect.PrintersDiscovered -> {
                    snackbarHostState.showSnackbar(
                        "发现${effect.printers.size}台打印机",
                        duration = SnackbarDuration.Short
                    )
                }
                is PrinterEffect.NavigateToOrderHistory -> {
                    onNavigateToOrderHistory()
                }
                is PrinterEffect.PrinterNotFound -> {
                    snackbarHostState.showSnackbar(
                        "未找到可用的打印机",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("打印") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 打印机状态卡片
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "打印机状态",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "状态: ${
                                    when (state.printerStatus) {
                                        PrinterStatus.READY -> "就绪"
                                        PrinterStatus.BUSY -> "忙碌"
                                        PrinterStatus.ERROR -> "错误"
                                        PrinterStatus.OFFLINE -> "离线"
                                        PrinterStatus.NOT_FOUND -> "未找到"
                                    }
                                }",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (state.selectedPrinter != null) {
                                Text(
                                    "选中: ${state.selectedPrinter}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.handleIntent(PrinterIntent.CheckPrinterStatus) }
                        ) {
                            Text("刷新")
                        }
                    }
                }
            }

            // 打印机选择
            if (state.availablePrinters.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "选择打印机",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { expandedPrinterMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(state.selectedPrinter ?: "选择打印机")
                    }

                    DropdownMenu(
                        expanded = expandedPrinterMenu,
                        onDismissRequest = { expandedPrinterMenu = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        state.availablePrinters.forEach { printer ->
                            DropdownMenuItem(
                                text = { Text(printer) },
                                onClick = {
                                    viewModel.handleIntent(PrinterIntent.SelectPrinter(printer))
                                    expandedPrinterMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // 打印份数设置
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "打印份数: ${state.copies}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (state.copies > 1) {
                                viewModel.handleIntent(PrinterIntent.SetCopies(state.copies - 1))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("-")
                    }

                    Text(
                        state.copies.toString(),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall
                    )

                    OutlinedButton(
                        onClick = {
                            if (state.copies < 10) {
                                viewModel.handleIntent(PrinterIntent.SetCopies(state.copies + 1))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+")
                    }
                }
            }

            // 打印状态显示
            when (state.printJobStatus) {
                PrintJobStatus.PENDING -> {
                    Text(
                        "准备打印...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                PrintJobStatus.PRINTING -> {
                    Column(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("打印中...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                PrintJobStatus.SUCCESS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "✓ 打印成功",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Green
                        )
                        if (state.jobId != null) {
                            Text(
                                "任务ID: ${state.jobId}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                PrintJobStatus.FAILED -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "✗ 打印失败",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        if (!state.error.isNullOrEmpty()) {
                            Text(
                                state.error!!,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Red
                            )
                        }
                    }
                }
                PrintJobStatus.CANCELLED -> {
                    Text(
                        "打印已取消",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            // 操作按钮
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                when (state.printJobStatus) {
                    PrintJobStatus.FAILED -> {
                        ActionButtonGroup(
                            primaryButtonText = "重试",
                            secondaryButtonText = "跳过",
                            onPrimaryClick = {
                                viewModel.handleIntent(PrinterIntent.RetryPrint)
                            },
                            onSecondaryClick = {
                                viewModel.handleIntent(PrinterIntent.SkipPrinting)
                            }
                        )
                    }
                    PrintJobStatus.SUCCESS -> {
                        ActionButtonGroup(
                            primaryButtonText = "完成",
                            secondaryButtonText = "再次打印",
                            onPrimaryClick = onNavigateToOrderHistory,
                            onSecondaryClick = {
                                viewModel.handleIntent(PrinterIntent.RetryPrint)
                            }
                        )
                    }
                    else -> {
                        ActionButtonGroup(
                            primaryButtonText = "开始打印",
                            secondaryButtonText = "跳过",
                            onPrimaryClick = {
                                if (!orderId.isNullOrEmpty()) {
                                    viewModel.handleIntent(
                                        PrinterIntent.PrintReceipt(
                                            orderId = orderId,
                                            orderNumber = "", // TODO: 获取订单号
                                            items = emptyList(), // TODO: 获取订单项
                                            totalAmount = 0.0, // TODO: 获取总金额
                                            paymentMethod = "" // TODO: 获取支付方式
                                        )
                                    )
                                }
                            },
                            onSecondaryClick = {
                                viewModel.handleIntent(PrinterIntent.SkipPrinting)
                            }
                        )
                    }
                }
            }
        }
    }
}