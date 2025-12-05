package com.pos.feature.payment.viewmodel

import com.pos.core.mvi.MviViewModel
import com.pos.data.db.entity.OrderEntity
import com.pos.data.db.entity.OrderItemEntity
import com.pos.data.repository.OrderRepository
import com.pos.data.repository.PaymentRepository
import com.pos.feature.payment.mvi.PaymentIntent
import com.pos.feature.payment.mvi.PaymentState
import com.pos.feature.payment.mvi.PaymentEffect
import com.pos.feature.payment.mvi.PaymentMethod
import com.pos.feature.payment.mvi.PaymentStatus
import com.pos.feature.payment.mvi.PaymentItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val orderRepository: OrderRepository
) : MviViewModel<PaymentIntent, PaymentState, PaymentEffect>(
    initialState = PaymentState()
) {

    override suspend fun processIntent(intent: PaymentIntent) {
        when (intent) {
            is PaymentIntent.InitializePayment -> initializePayment(intent)
            is PaymentIntent.SelectPaymentMethod -> selectPaymentMethod(intent.method)
            is PaymentIntent.ProcessPayment -> processPayment(intent.method, intent.amount)
            is PaymentIntent.ConfirmPayment -> confirmPayment()
            is PaymentIntent.CancelPayment -> cancelPayment()
            is PaymentIntent.RetryPayment -> retryPayment()
            is PaymentIntent.UpdatePaymentStatus -> updatePaymentStatus(intent.status)
        }
    }

    private suspend fun initializePayment(intent: PaymentIntent.InitializePayment) {
        try {
            updateState { state ->
                state.copy(
                    orderId = intent.orderId,
                    totalAmount = intent.totalAmount,
                    items = intent.items,
                    paymentStatus = PaymentStatus.PENDING
                )
            }
            Timber.d("Payment initialized for order: ${intent.orderId}, Amount: ${intent.totalAmount}")
        } catch (e: Exception) {
            Timber.e(e, "Error initializing payment")
            sendEffect(PaymentEffect.ShowError("初始化支付失败: ${e.message}"))
        }
    }

    private suspend fun selectPaymentMethod(method: PaymentMethod) {
        try {
            updateState { state ->
                state.copy(selectedMethod = method)
            }
            Timber.d("Payment method selected: $method")
        } catch (e: Exception) {
            Timber.e(e, "Error selecting payment method")
            sendEffect(PaymentEffect.ShowError("选择支付方式失败: ${e.message}"))
        }
    }

    private suspend fun processPayment(method: PaymentMethod, amount: Double) {
        try {
            val orderId = currentState.orderId ?: run {
                sendEffect(PaymentEffect.ShowError("订单ID为空"))
                return
            }

            updateState { state ->
                state.copy(
                    isProcessing = true,
                    paymentStatus = PaymentStatus.PROCESSING,
                    selectedMethod = method
                )
            }

            paymentRepository.processPayment(orderId, amount, method.name)
                .collectLatest { result ->
                    result.fold(
                        onSuccess = { transactionId ->
                            updateState { state ->
                                state.copy(
                                    transactionId = transactionId,
                                    paymentStatus = PaymentStatus.SUCCESS,
                                    isProcessing = false
                                )
                            }
                            saveOrder()
                            sendEffect(PaymentEffect.PaymentSuccess(transactionId))
                            Timber.d("Payment successful: $transactionId")
                        },
                        onError = { error ->
                            updateState { state ->
                                state.copy(
                                    paymentStatus = PaymentStatus.FAILED,
                                    isProcessing = false,
                                    error = error.message
                                )
                            }
                            sendEffect(PaymentEffect.PaymentFailed(error.message ?: "支付失败"))
                            Timber.e(error, "Payment failed")
                        },
                        onLoading = {
                            updateState { state ->
                                state.copy(isProcessing = true)
                            }
                        }
                    )
                }
        } catch (e: Exception) {
            Timber.e(e, "Error processing payment")
            updateState { state ->
                state.copy(
                    isProcessing = false,
                    paymentStatus = PaymentStatus.FAILED,
                    error = e.message
                )
            }
            sendEffect(PaymentEffect.ShowError("处理支付失败: ${e.message}"))
        }
    }

    private suspend fun confirmPayment() {
        try {
            if (currentState.paymentStatus != PaymentStatus.SUCCESS) {
                sendEffect(PaymentEffect.ShowError("支付未成功，无法确认"))
                return
            }
            sendEffect(PaymentEffect.NavigateToPrinting)
        } catch (e: Exception) {
            Timber.e(e, "Error confirming payment")
            sendEffect(PaymentEffect.ShowError("确认支付失败: ${e.message}"))
        }
    }

    private suspend fun cancelPayment() {
        try {
            updateState { state ->
                state.copy(paymentStatus = PaymentStatus.CANCELLED)
            }
            Timber.d("Payment cancelled for order: ${currentState.orderId}")
        } catch (e: Exception) {
            Timber.e(e, "Error canceling payment")
            sendEffect(PaymentEffect.ShowError("取消支付失败: ${e.message}"))
        }
    }

    private suspend fun retryPayment() {
        try {
            if (currentState.selectedMethod == null) {
                sendEffect(PaymentEffect.ShowError("请先选择支付方式"))
                return
            }
            val method = currentState.selectedMethod ?: return
            processPayment(method, currentState.totalAmount)
        } catch (e: Exception) {
            Timber.e(e, "Error retrying payment")
            sendEffect(PaymentEffect.ShowError("重试支付失败: ${e.message}"))
        }
    }

    private suspend fun updatePaymentStatus(status: PaymentStatus) {
        try {
            updateState { state ->
                state.copy(paymentStatus = status)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating payment status")
        }
    }

    private suspend fun saveOrder() {
        try {
            val orderId = currentState.orderId ?: return
            val order = OrderEntity(
                id = orderId,
                orderNumber = generateOrderNumber(),
                totalAmount = currentState.totalAmount,
                paymentMethod = currentState.selectedMethod?.name ?: "UNKNOWN",
                orderStatus = "COMPLETED",
                itemCount = currentState.items.size
            )

            val orderItems = currentState.items.map { item ->
                OrderItemEntity(
                    id = UUID.randomUUID().toString(),
                    orderId = orderId,
                    productId = item.id,
                    productName = item.name,
                    price = item.price,
                    quantity = item.quantity,
                    subtotal = item.subtotal
                )
            }

            orderRepository.createOrder(order, orderItems)
            Timber.d("Order saved: $orderId")
        } catch (e: Exception) {
            Timber.e(e, "Error saving order")
            sendEffect(PaymentEffect.ShowError("保存订单失败: ${e.message}"))
        }
    }

    private fun generateOrderNumber(): String {
        return "ORD-${System.currentTimeMillis()}-${(1000..9999).random()}"
    }

    fun calculateChange(receivedAmount: Double): Double {
        val change = receivedAmount - currentState.totalAmount
        if (change >= 0) {
            return change
        }
        return 0.0
    }
}
