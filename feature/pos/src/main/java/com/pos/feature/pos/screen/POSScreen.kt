package com.pos.feature.pos.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pos.feature.pos.viewmodel.POSViewModel
import com.pos.feature.pos.mvi.POSIntent
import com.pos.feature.pos.mvi.POSEffect
import com.pos.ui.components.ActionButtonGroup
import com.pos.ui.components.CartItemRow
import com.pos.ui.components.LoadingIndicator
import com.pos.ui.components.OrderSummary
import com.pos.ui.components.ProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POSScreen(
    viewModel: POSViewModel = hiltViewModel(),
    onNavigateToPayment: (String, Double, List<Any>) -> Unit = { _, _, _ -> },
    onNavigateToScanner: () -> Unit = { }
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is POSEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is POSEffect.ShowSuccess -> {
                    snackbarHostState.showSnackbar(
                        effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is POSEffect.NavigateToPayment -> {
                    // 触发支付导航
                    onNavigateToPayment(viewModel.getCartId(), state.total, emptyList())
                }
                is POSEffect.NavigateToScanner -> {
                    onNavigateToScanner()
                }
                is POSEffect.ProductAdded -> {
                    snackbarHostState.showSnackbar(
                        "已添加: ${effect.product.name}",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("POS 收银系统") },
                actions = {
                    IconButton(onClick = {
                        viewModel.handleIntent(POSIntent.LoadProducts)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 左侧：商品列表
            Column(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("搜索商品") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (state.isLoading) {
                    LoadingIndicator(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val displayedProducts = if (searchQuery.isEmpty()) {
                            state.products
                        } else {
                            state.products.filter { it.name.contains(searchQuery, ignoreCase = true) }
                        }

                        items(displayedProducts) { product ->
                            ProductCard(
                                name = product.name,
                                price = product.price,
                                barcode = product.barcode,
                                onAddToCart = {
                                    viewModel.handleIntent(POSIntent.AddToCart(product))
                                }
                            )
                        }
                    }
                }
            }

            // 右侧：购物车和结账
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .padding(8.dp)
            ) {
                Text(
                    "购物车",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (state.cartItems.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("购物车为空", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(state.cartItems) { cartItem ->
                            CartItemRow(
                                name = cartItem.productName,
                                price = cartItem.price,
                                quantity = cartItem.quantity,
                                subtotal = cartItem.price * cartItem.quantity,
                                onQuantityChange = { newQty ->
                                    viewModel.handleIntent(
                                        POSIntent.UpdateCartQuantity(cartItem, newQty)
                                    )
                                },
                                onRemove = {
                                    viewModel.handleIntent(POSIntent.RemoveFromCart(cartItem))
                                }
                            )
                        }
                    }
                }

                // 订单摘要
                OrderSummary(
                    subtotal = state.subtotal,
                    discountPercent = state.discountPercent,
                    finalAmount = state.total,
                    itemCount = state.itemCount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                // 操作按钮
                ActionButtonGroup(
                    primaryButtonText = "结 账",
                    secondaryButtonText = "清 空",
                    onPrimaryClick = {
                        if (state.cartItems.isNotEmpty()) {
                            viewModel.handleIntent(POSIntent.ProceedToCheckout)
                        }
                    },
                    onSecondaryClick = {
                        viewModel.handleIntent(POSIntent.ClearCart)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { onNavigateToScanner() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("扫 码", fontSize = 16.sp)
                }
            }
        }
    }
}
