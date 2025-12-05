package com.pos.feature.payment.mvi

import com.pos.core.mvi.MviIntent
import com.pos.core.mvi.MviState
import com.pos.core.mvi.MviEffect

/**
 * 支付屏幕的Intent
 */
sealed class PaymentIntent : MviIntent {
    data class InitializePayment(
        val orderId: String,
        val totalAmount: Double,
        val items: List<PaymentItem>
    ) : PaymentIntent()

    data class SelectPaymentMethod(val method: PaymentMethod) : PaymentIntent()
    data class ProcessPayment(val method: PaymentMethod, val amount: Double) : PaymentIntent()
    object ConfirmPayment : PaymentIntent()
    object CancelPayment : PaymentIntent()
    object RetryPayment : PaymentIntent()
    data class UpdatePaymentStatus(val status: PaymentStatus) : PaymentIntent()
}

/**
 * 支付方法枚举
 */
enum class PaymentMethod {
    CASH, CARD, MOBILE_PAY, CHEQUE
}

/**
 * 支付状态枚举
 */
enum class PaymentStatus {
    PENDING, PROCESSING, SUCCESS, FAILED, CANCELLED
}

/**
 * 支付项目数据类
 */
data class PaymentItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val price: Double,
    val subtotal: Double
)

/**
 * 支付屏幕的State
 */
data class PaymentState(
    val orderId: String? = null,
    val totalAmount: Double = 0.0,
    val items: List<PaymentItem> = emptyList(),
    val selectedMethod: PaymentMethod? = null,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val isProcessing: Boolean = false,
    val transactionId: String? = null,
    val error: String? = null,
    val changeAmount: Double = 0.0
) : MviState

/**
 * 支付屏幕的Effect
 */
sealed class PaymentEffect : MviEffect {
    data class PaymentSuccess(val transactionId: String) : PaymentEffect()
    data class PaymentFailed(val message: String) : PaymentEffect()
    data class ShowError(val message: String) : PaymentEffect()
    data class ShowChangeAmount(val amount: Double) : PaymentEffect()
    object NavigateToPrinting : PaymentEffect()
    object NavigateToOrderHistory : PaymentEffect()
}
