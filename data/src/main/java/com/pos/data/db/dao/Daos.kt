package com.pos.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.pos.data.db.entity.ProductEntity
import com.pos.data.db.entity.CartItemEntity
import com.pos.data.db.entity.OrderEntity
import com.pos.data.db.entity.OrderItemEntity
import com.pos.data.db.entity.TransactionEntity
import com.pos.data.db.entity.PrintJobEntity
import kotlinx.coroutines.flow.Flow

/**
 * 商品DAO
 */
@Dao
interface ProductDao {
    @Insert
    suspend fun insertProduct(product: ProductEntity)

    @Insert
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: String): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE barcode = :barcode")
    fun getProductByBarcode(barcode: String): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE category = :category AND isActive = 1")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY updatedAt DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE name LIKE :query AND isActive = 1")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Insert
    suspend fun insert(product: ProductEntity)

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY updatedAt DESC")
    suspend fun getAllProductsOnce(): List<ProductEntity>

    @Query("DELETE FROM products")
    suspend fun deleteAll()
}

/**
 * 购物车DAO
 */
@Dao
interface CartItemDao {
    @Insert
    suspend fun insertCartItem(item: CartItemEntity)

    @Update
    suspend fun updateCartItem(item: CartItemEntity)

    @Query("SELECT * FROM cart_items WHERE cartId = :cartId")
    fun getCartItems(cartId: String): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE cartId = :cartId AND productId = :productId")
    fun getCartItem(cartId: String, productId: String): Flow<CartItemEntity?>

    @Query("DELETE FROM cart_items WHERE cartId = :cartId")
    suspend fun clearCart(cartId: String)

    @Delete
    suspend fun deleteCartItem(item: CartItemEntity)
}

/**
 * 订单DAO
 */
@Dao
interface OrderDao {
    @Insert
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("SELECT * FROM orders WHERE id = :id")
    fun getOrderById(id: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE orderNumber = :orderNumber")
    fun getOrderByNumber(orderNumber: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentOrders(limit: Int = 10): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderStatus = :status ORDER BY createdAt DESC")
    fun getOrdersByStatus(status: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE createdAt >= :startTime AND createdAt <= :endTime")
    fun getOrdersByDateRange(startTime: Long, endTime: Long): Flow<List<OrderEntity>>

    @Query("SELECT COUNT(*) FROM orders WHERE createdAt >= :startTime AND createdAt <= :endTime")
    fun getOrderCountByDateRange(startTime: Long, endTime: Long): Flow<Int>

    @Query("SELECT SUM(totalAmount) FROM orders WHERE createdAt >= :startTime AND createdAt <= :endTime")
    fun getTotalSalesByDateRange(startTime: Long, endTime: Long): Flow<Double>

    @Delete
    suspend fun deleteOrder(order: OrderEntity)
}

/**
 * 订单明细DAO
 */
@Dao
interface OrderItemDao {
    @Insert
    suspend fun insertOrderItem(item: OrderItemEntity)

    @Insert
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getOrderItems(orderId: String): Flow<List<OrderItemEntity>>

    @Delete
    suspend fun deleteOrderItem(item: OrderItemEntity)
}

/**
 * 交易DAO
 */
@Dao
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getTransactionById(id: String): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions WHERE orderId = :orderId")
    fun getTransactionByOrderId(orderId: String): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions WHERE status = :status")
    fun getTransactionsByStatus(status: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 20): Flow<List<TransactionEntity>>
}

/**
 * 打印任务DAO
 */
@Dao
interface PrintJobDao {
    @Insert
    suspend fun insertPrintJob(job: PrintJobEntity)

    @Update
    suspend fun updatePrintJob(job: PrintJobEntity)

    @Query("SELECT * FROM print_jobs WHERE id = :id")
    fun getPrintJobById(id: String): Flow<PrintJobEntity?>

    @Query("SELECT * FROM print_jobs WHERE status = :status ORDER BY createdAt DESC")
    fun getPrintJobsByStatus(status: String): Flow<List<PrintJobEntity>>

    @Query("SELECT * FROM print_jobs WHERE orderId = :orderId")
    fun getPrintJobsByOrderId(orderId: String): Flow<List<PrintJobEntity>>

    @Query("SELECT * FROM print_jobs WHERE status = 'PENDING' ORDER BY createdAt ASC")
    fun getPendingPrintJobs(): Flow<List<PrintJobEntity>>

    @Delete
    suspend fun deletePrintJob(job: PrintJobEntity)
}
