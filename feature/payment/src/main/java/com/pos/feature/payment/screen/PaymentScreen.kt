package com.pos.feature.payment.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pos.feature.payment.viewmodel.PaymentViewModel
import com.pos.feature.payment.mvi.PaymentIntent
import com.pos.feature.payment.mvi.PaymentEffect
import com.pos.feature.payment.mvi.PaymentMethod
import com.pos.feature.payment.mvi.PaymentStatus
import com.pos.feature.payment.mvi.PaymentItem
import com.pos.ui.components.ActionButtonGroup
import com.pos.ui.components.LoadingIndicator
import com.pos.ui.components.OrderSummary
import com.pos.ui.components.PaymentMethodCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    orderId: String,
    totalAmount: Double,
    items: List<PaymentItem> = emptyList(),
    viewModel: PaymentViewModel = hiltViewModel(),
    onNavigateToPrinting: () -> Unit = { },
    onNavigateBack: () -> Unit = { }
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.handleIntent(
            PaymentIntent.InitializePayment(orderId, totalAmount, items)
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PaymentEffect.PaymentSuccess -> {
                    snackbarHostState.showSnackbar(
                        "支付成功！交易号: ${effect.transactionId}",
                        duration = SnackbarDuration.Long
                    )
                    onNavigateToPrinting()
                }
                is PaymentEffect.PaymentFailed -> {
                    snackbarHostState.showSnackbar(
                        effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is PaymentEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is PaymentEffect.NavigateToPrinting -> {
                    onNavigateToPrinting()
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("支付") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        if (state.isProcessing) {
            LoadingIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                message = "处理支付中..."
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 订单明细
                Text(
                    "订单明细",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.items) { item ->
                        Text(
                            "${item.name} x${item.quantity} = ¥${String.format("%.2f", item.subtotal)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // 订单摘要
                OrderSummary(
                    subtotal = state.totalAmount,
                    finalAmount = state.totalAmount,
                    itemCount = state.items.size
                )

                // 支付方式选择
                Text(
                    "选择支付方式",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentMethod.values().forEach { method ->
                        PaymentMethodCard(
                            method = when (method) {
                                PaymentMethod.CASH -> "现金支付"
                                PaymentMethod.CARD -> "银行卡"
                                PaymentMethod.MOBILE_PAY -> "移动支付"
                                PaymentMethod.CHEQUE -> "支票"
                            },
                            isSelected = state.selectedMethod == method,
                            onClick = {
                                viewModel.handleIntent(PaymentIntent.SelectPaymentMethod(method))
                            }
                        )
                    }
                }

                // 操作按钮
                when (state.paymentStatus) {
                    PaymentStatus.PENDING, PaymentStatus.FAILED -> {
                        ActionButtonGroup(
                            primaryButtonText = "确认支付",
                            secondaryButtonText = "返回",
                            onPrimaryClick = {
                                if (state.selectedMethod != null) {
                                    viewModel.handleIntent(
                                        PaymentIntent.ProcessPayment(
                                            state.selectedMethod!!,
                                            state.totalAmount
                                        )
                                    )
                                }
                            },
                            onSecondaryClick = onNavigateBack
                        )
                    }
                    PaymentStatus.PROCESSING -> {
                        LoadingIndicator(message = "处理支付中...")
                    }
                    PaymentStatus.SUCCESS -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "✓ 支付成功",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Green,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            if (state.transactionId != null) {
                                Text(
                                    "交易号: ${state.transactionId}",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            Button(
                                onClick = onNavigateToPrinting,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("打印收据")
                            }
                        }
                    }
                    PaymentStatus.CANCELLED -> {
                        Text(
                            "支付已取消",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}
