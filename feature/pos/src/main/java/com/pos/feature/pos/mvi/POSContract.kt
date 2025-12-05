package com.pos.feature.pos.mvi

import com.pos.core.mvi.MviIntent
import com.pos.core.mvi.MviState
import com.pos.core.mvi.MviEffect
import com.pos.data.db.entity.ProductEntity
import com.pos.data.db.entity.CartItemEntity

/**
 * POS屏幕的Intent（用户操作）
 */
sealed class POSIntent : MviIntent {
    object LoadProducts : POSIntent()
    data class ScanBarcode(val barcode: String) : POSIntent()
    data class AddToCart(val product: ProductEntity) : POSIntent()
    data class UpdateCartQuantity(val cartItem: CartItemEntity, val quantity: Int) : POSIntent()
    data class RemoveFromCart(val cartItem: CartItemEntity) : POSIntent()
    object ClearCart : POSIntent()
    object ProceedToCheckout : POSIntent()
    data class ApplyDiscount(val discountPercent: Double) : POSIntent()
    object CancelOrder : POSIntent()
}

/**
 * POS屏幕的State（UI状态）
 */
data class POSState(
    val products: List<ProductEntity> = emptyList(),
    val cartItems: List<CartItemEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalAmount: Double = 0.0,
    val discountPercent: Double = 0.0,
    val finalAmount: Double = 0.0,
    val itemCount: Int = 0
) : MviState {
    val subtotal: Double
        get() = cartItems.sumOf { it.price * it.quantity }

    val discountAmount: Double
        get() = subtotal * (discountPercent / 100)

    val total: Double
        get() = subtotal - discountAmount
}

/**
 * POS屏幕的Effect（一次性事件）
 */
sealed class POSEffect : MviEffect {
    data class ShowError(val message: String) : POSEffect()
    data class ShowSuccess(val message: String) : POSEffect()
    object NavigateToPayment : POSEffect()
    object NavigateToScanner : POSEffect()
    data class ProductAdded(val product: ProductEntity) : POSEffect()
}
