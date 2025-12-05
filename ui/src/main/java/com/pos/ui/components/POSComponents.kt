package com.pos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 商品卡片组件
 */
@Composable
fun ProductCard(
    name: String,
    price: Double,
    barcode: String,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "¥${String.format("%.2f", price)}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "条码: $barcode",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Button(
                onClick = onAddToCart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("加入购物车")
            }
        }
    }
}

/**
 * 购物车项目组件
 */
@Composable
fun CartItemRow(
    name: String,
    price: Double,
    quantity: Int,
    subtotal: Double,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "¥${String.format("%.2f", price)} x $quantity = ¥${String.format("%.2f", subtotal)}",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onQuantityChange(quantity - 1) },
                    modifier = Modifier
                        .width(40.dp)
                        .height(40.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("-", fontSize = 16.sp)
                }

                Text(
                    text = quantity.toString(),
                    modifier = Modifier.width(30.dp),
                    style = MaterialTheme.typography.labelMedium
                )

                OutlinedButton(
                    onClick = { onQuantityChange(quantity + 1) },
                    modifier = Modifier
                        .width(40.dp)
                        .height(40.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("+", fontSize = 16.sp)
                }

                OutlinedButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .width(50.dp)
                        .height(40.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("删除", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * 订单摘要卡片
 */
@Composable
fun OrderSummary(
    subtotal: Double,
    discountPercent: Double = 0.0,
    finalAmount: Double,
    itemCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("商品数量:", style = MaterialTheme.typography.labelMedium)
                Text("$itemCount 件", style = MaterialTheme.typography.labelMedium)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("小计:", style = MaterialTheme.typography.labelMedium)
                Text("¥${String.format("%.2f", subtotal)}", style = MaterialTheme.typography.labelMedium)
            }

            if (discountPercent > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("折扣 ($discountPercent%):", style = MaterialTheme.typography.labelMedium)
                    Text(
                        "-¥${String.format("%.2f", subtotal * discountPercent / 100)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.LightGray)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "总计:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "¥${String.format("%.2f", finalAmount)}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 加载进度条
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    message: String = "加载中..."
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = message,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

/**
 * 支付方法选择卡片
 */
@Composable
fun PaymentMethodCard(
    method: String,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                shape = RoundedCornerShape(8.dp)
            ),
        elevation = if (isSelected) CardDefaults.cardElevation(4.dp) else CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = method,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier,
            )
        }
    }
}

/**
 * 数字输入框
 */
@Composable
fun NumericInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.isEmpty() || newValue.toDoubleOrNull() != null) {
                onValueChange(newValue)
            }
        },
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}

/**
 * 操作按钮组
 */
@Composable
fun ActionButtonGroup(
    primaryButtonText: String,
    secondaryButtonText: String,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onSecondaryClick,
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
        ) {
            Text(secondaryButtonText)
        }

        Button(
            onClick = onPrimaryClick,
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
        ) {
            Text(primaryButtonText)
        }
    }
}
