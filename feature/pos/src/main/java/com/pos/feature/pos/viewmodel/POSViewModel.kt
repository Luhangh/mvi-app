package com.pos.feature.pos.viewmodel

import com.pos.core.mvi.MviViewModel
import com.pos.data.repository.CartRepository
import com.pos.data.repository.ProductRepository
import com.pos.feature.pos.mvi.POSIntent
import com.pos.feature.pos.mvi.POSState
import com.pos.feature.pos.mvi.POSEffect
import com.pos.data.db.entity.CartItemEntity
import com.pos.data.db.entity.ProductEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class POSViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository
) : MviViewModel<POSIntent, POSState, POSEffect>(
    initialState = POSState()
) {

    private var currentCartId = UUID.randomUUID().toString()

    init {
        handleIntent(POSIntent.LoadProducts)
    }

    override suspend fun processIntent(intent: POSIntent) {
        when (intent) {
            is POSIntent.LoadProducts -> loadProducts()
            is POSIntent.ScanBarcode -> scanBarcode(intent.barcode)
            is POSIntent.AddToCart -> addToCart(intent.product)
            is POSIntent.UpdateCartQuantity -> updateCartQuantity(intent.cartItem, intent.quantity)
            is POSIntent.RemoveFromCart -> removeFromCart(intent.cartItem)
            is POSIntent.ClearCart -> clearCart()
            is POSIntent.ProceedToCheckout -> proceedToCheckout()
            is POSIntent.ApplyDiscount -> applyDiscount(intent.discountPercent)
            is POSIntent.CancelOrder -> cancelOrder()
        }
    }

    private suspend fun loadProducts() {
        try {
            updateState { it.copy(isLoading = true, error = null) }
            productRepository.getAllProducts().collectLatest { result ->
                result.fold(
                    onSuccess = { products ->
                        updateState { state ->
                            state.copy(
                                products = products,
                                isLoading = false
                            )
                        }
                        Timber.d("Loaded ${products.size} products")
                    },
                    onError = { error ->
                        updateState { state ->
                            state.copy(
                                isLoading = false,
                                error = error.message ?: "Unknown error"
                            )
                        }
                        sendEffect(POSEffect.ShowError(error.message ?: "加载商品失败"))
                        Timber.e(error, "Failed to load products")
                    },
                    onLoading = {
                        updateState { it.copy(isLoading = true) }
                    }
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading products")
            updateState { state ->
                state.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    private suspend fun scanBarcode(barcode: String) {
        try {
            updateState { it.copy(isLoading = true) }
            productRepository.getProductByBarcode(barcode).collectLatest { result ->
                result.fold(
                    onSuccess = { product ->
                        updateState { it.copy(isLoading = false) }
                        addToCart(product)
                    },
                    onError = { error ->
                        updateState { it.copy(isLoading = false) }
                        sendEffect(POSEffect.ShowError("未找到商品: $barcode"))
                        Timber.w("Product not found for barcode: $barcode")
                    },
                    onLoading = {
                        updateState { it.copy(isLoading = true) }
                    }
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error scanning barcode: $barcode")
            updateState { it.copy(isLoading = false) }
            sendEffect(POSEffect.ShowError("扫描失败: ${e.message}"))
        }
    }

    private suspend fun addToCart(product: ProductEntity) {
        try {
            val cartItem = CartItemEntity(
                id = UUID.randomUUID().toString(),
                productId = product.id,
                productName = product.name,
                price = product.price,
                quantity = 1,
                cartId = currentCartId
            )
            cartRepository.addToCart(cartItem)
            sendEffect(POSEffect.ProductAdded(product))
            Timber.d("Added product to cart: ${product.name}")
            loadCartItems()
        } catch (e: Exception) {
            Timber.e(e, "Error adding product to cart")
            sendEffect(POSEffect.ShowError("添加购物车失败: ${e.message}"))
        }
    }

    private suspend fun updateCartQuantity(cartItem: CartItemEntity, quantity: Int) {
        try {
            if (quantity <= 0) {
                removeFromCart(cartItem)
            } else {
                cartRepository.updateCartItem(cartItem.copy(quantity = quantity))
                loadCartItems()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating cart quantity")
            sendEffect(POSEffect.ShowError("更新数量失败: ${e.message}"))
        }
    }

    private suspend fun removeFromCart(cartItem: CartItemEntity) {
        try {
            cartRepository.removeFromCart(cartItem)
            loadCartItems()
            Timber.d("Removed item from cart: ${cartItem.productName}")
        } catch (e: Exception) {
            Timber.e(e, "Error removing item from cart")
            sendEffect(POSEffect.ShowError("删除失败: ${e.message}"))
        }
    }

    private suspend fun clearCart() {
        try {
            cartRepository.clearCart(currentCartId)
            currentCartId = UUID.randomUUID().toString()
            updateState { state ->
                state.copy(
                    cartItems = emptyList(),
                    discountPercent = 0.0,
                    totalAmount = 0.0,
                    itemCount = 0
                )
            }
            sendEffect(POSEffect.ShowSuccess("购物车已清空"))
        } catch (e: Exception) {
            Timber.e(e, "Error clearing cart")
            sendEffect(POSEffect.ShowError("清空购物车失败: ${e.message}"))
        }
    }

    private suspend fun loadCartItems() {
        try {
            cartRepository.getCartItems(currentCartId).collectLatest { items ->
                updateState { state ->
                    state.copy(
                        cartItems = items,
                        itemCount = items.size,
                        totalAmount = items.sumOf { it.price * it.quantity }
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading cart items")
        }
    }

    private suspend fun applyDiscount(discountPercent: Double) {
        try {
            if (discountPercent < 0 || discountPercent > 100) {
                sendEffect(POSEffect.ShowError("折扣百分比必须在0-100之间"))
                return
            }
            updateState { state ->
                state.copy(discountPercent = discountPercent)
            }
            sendEffect(POSEffect.ShowSuccess("折扣已应用: $discountPercent%"))
        } catch (e: Exception) {
            Timber.e(e, "Error applying discount")
            sendEffect(POSEffect.ShowError("应用折扣失败: ${e.message}"))
        }
    }

    private suspend fun proceedToCheckout() {
        try {
            if (currentState.cartItems.isEmpty()) {
                sendEffect(POSEffect.ShowError("购物车为空"))
                return
            }
            sendEffect(POSEffect.NavigateToPayment)
        } catch (e: Exception) {
            Timber.e(e, "Error proceeding to checkout")
            sendEffect(POSEffect.ShowError("结账失败: ${e.message}"))
        }
    }

    private suspend fun cancelOrder() {
        try {
            clearCart()
            sendEffect(POSEffect.ShowSuccess("订单已取消"))
        } catch (e: Exception) {
            Timber.e(e, "Error canceling order")
            sendEffect(POSEffect.ShowError("取消失败: ${e.message}"))
        }
    }

    fun getCartId(): String = currentCartId
}
