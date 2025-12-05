package com.pos.data.repository

import com.pos.core.result.Result
import com.pos.data.db.dao.CartItemDao
import com.pos.data.db.dao.OrderDao
import com.pos.data.db.dao.OrderItemDao
import com.pos.data.db.dao.ProductDao
import com.pos.data.db.dao.PrintJobDao
import com.pos.data.db.dao.TransactionDao
import com.pos.data.db.entity.CartItemEntity
import com.pos.data.db.entity.OrderEntity
import com.pos.data.db.entity.OrderItemEntity
import com.pos.data.db.entity.PrintJobEntity
import com.pos.data.db.entity.ProductEntity
import com.pos.data.db.entity.TransactionEntity
import com.pos.data.network.POSApi
import com.pos.data.network.model.PaymentRequest
import com.pos.data.network.model.PrintRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 商品Repository
 */
@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao,
    private val api: POSApi
) {
    fun getProductByBarcode(barcode: String): Flow<Result<ProductEntity>> = flow {
        emit(Result.Loading)
        try {
            // 先从本地数据库查询
            val localProduct = productDao.getProductByBarcode(barcode).collect { local ->
                if (local != null) {
                    emit(Result.Success(local))
                } else {
                    // 如果本地没有，从网络查询
                    try {
                        val response = api.getProductByBarcode(barcode)
                        if (response.code == 200 && response.data != null) {
                            val entity = response.data.toEntity()
                            productDao.insertProduct(entity)
                            emit(Result.Success(entity))
                        } else {
                            emit(Result.Error(Exception(response.message)))
                        }
                    } catch (e: Exception) {
                        emit(Result.Error(e))
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting product by barcode: $barcode")
            emit(Result.Error(e))
        }
    }.catch { e ->
        emit(Result.Error(e as Exception))
    }

    fun getAllProducts(): Flow<Result<List<ProductEntity>>> = flow {
        emit(Result.Loading)
        try {
            productDao.getAllProducts().collect { products ->
                if (products.isNotEmpty()) {
                    emit(Result.Success(products))
                } else {
                    // 如果本地为空，从网络同步
                    try {
                        val response = api.getProducts()
                        if (response.code == 200 && response.data != null) {
                            val entities = response.data.map { it.toEntity() }
                            productDao.insertProducts(entities)
                            emit(Result.Success(entities))
                        } else {
                            emit(Result.Error(Exception(response.message)))
                        }
                    } catch (e: Exception) {
                        emit(Result.Error(e))
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting all products")
            emit(Result.Error(e))
        }
    }.catch { e ->
        emit(Result.Error(e as Exception))
    }

    fun searchProducts(query: String): Flow<Result<List<ProductEntity>>> = flow {
        emit(Result.Loading)
        try {
            productDao.searchProducts("%$query%").collect { results ->
                emit(Result.Success(results))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching products: $query")
            emit(Result.Error(e))
        }
    }.catch { e ->
        emit(Result.Error(e as Exception))
    }

    suspend fun syncProducts() {
        try {
            val response = api.syncProducts(
                com.pos.data.network.model.SyncRequest(System.currentTimeMillis())
            )
            if (response.code == 200 && response.data != null) {
                val entities = response.data.products.map { it.toEntity() }
                productDao.insertProducts(entities)
                Timber.d("Synced ${entities.size} products")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error syncing products")
        }
    }
}

/**
 * 购物车Repository
 */
@Singleton
class CartRepository @Inject constructor(
    private val cartItemDao: CartItemDao
) {
    fun getCartItems(cartId: String): Flow<List<CartItemEntity>> =
        cartItemDao.getCartItems(cartId)

    suspend fun addToCart(item: CartItemEntity) {
        val existing = cartItemDao.getCartItem(item.cartId, item.productId)
        existing.collect { existing ->
            if (existing != null) {
                cartItemDao.updateCartItem(
                    existing.copy(quantity = existing.quantity + item.quantity)
                )
            } else {
                cartItemDao.insertCartItem(item)
            }
        }
    }

    suspend fun updateCartItem(item: CartItemEntity) {
        cartItemDao.updateCartItem(item)
    }

    suspend fun removeFromCart(item: CartItemEntity) {
        cartItemDao.deleteCartItem(item)
    }

    suspend fun clearCart(cartId: String) {
        cartItemDao.clearCart(cartId)
    }
}

/**
 * 订单Repository
 */
@Singleton
class OrderRepository @Inject constructor(
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao
) {
    fun getOrderById(id: String): Flow<OrderEntity?> = orderDao.getOrderById(id)

    fun getRecentOrders(): Flow<List<OrderEntity>> = orderDao.getRecentOrders()

    fun getOrdersByStatus(status: String): Flow<List<OrderEntity>> =
        orderDao.getOrdersByStatus(status)

    fun getTotalSalesByDateRange(startTime: Long, endTime: Long): Flow<Double> =
        orderDao.getTotalSalesByDateRange(startTime, endTime)

    suspend fun createOrder(order: OrderEntity, items: List<OrderItemEntity>) {
        orderDao.insertOrder(order)
        orderItemDao.insertOrderItems(items)
    }

    suspend fun updateOrder(order: OrderEntity) {
        orderDao.updateOrder(order)
    }

    fun getOrderItems(orderId: String): Flow<List<OrderItemEntity>> =
        orderItemDao.getOrderItems(orderId)
}

/**
 * 支付Repository
 */
@Singleton
class PaymentRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val api: POSApi
) {
    fun processPayment(orderId: String, amount: Double, method: String):
            Flow<Result<String>> = flow {
        emit(Result.Loading)
        try {
            val response = api.processPayment(
                PaymentRequest(orderId, amount, method)
            )
            if (response.code == 200 && response.data != null) {
                val transaction = TransactionEntity(
                    id = response.data.transactionId,
                    orderId = orderId,
                    amount = amount,
                    method = method,
                    status = response.data.status,
                    createdAt = response.data.timestamp
                )
                transactionDao.insertTransaction(transaction)
                emit(Result.Success(response.data.transactionId))
            } else {
                emit(Result.Error(Exception(response.message)))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error processing payment for order: $orderId")
            emit(Result.Error(e))
        }
    }.catch { e ->
        emit(Result.Error(e as Exception))
    }

    fun getTransactionByOrderId(orderId: String): Flow<TransactionEntity?> =
        transactionDao.getTransactionByOrderId(orderId)

    fun getRecentTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.getRecentTransactions()
}

/**
 * 打印Repository
 */
@Singleton
class PrintRepository @Inject constructor(
    private val printJobDao: PrintJobDao,
    private val api: POSApi
) {
    fun submitPrintJob(orderId: String, content: String, copies: Int = 1):
            Flow<Result<String>> = flow {
        emit(Result.Loading)
        try {
            val response = api.submitPrintJob(
                PrintRequest(orderId, content, copies)
            )
            if (response.code == 200 && response.data != null) {
                val job = PrintJobEntity(
                    id = response.data.jobId,
                    orderId = orderId,
                    content = content,
                    status = response.data.status,
                    copies = copies
                )
                printJobDao.insertPrintJob(job)
                emit(Result.Success(response.data.jobId))
            } else {
                emit(Result.Error(Exception(response.message)))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error submitting print job for order: $orderId")
            emit(Result.Error(e))
        }
    }.catch { e ->
        emit(Result.Error(e as Exception))
    }

    fun getPendingPrintJobs(): Flow<List<PrintJobEntity>> =
        printJobDao.getPendingPrintJobs()

    suspend fun updatePrintJob(job: PrintJobEntity) {
        printJobDao.updatePrintJob(job)
    }
}

// 扩展函数：将API模型转换为数据库实体
private fun com.pos.data.network.model.ProductResponse.toEntity() = ProductEntity(
    id = id,
    name = name,
    description = description,
    price = price,
    category = category,
    barcode = barcode,
    imageUrl = imageUrl,
    stock = stock
)
