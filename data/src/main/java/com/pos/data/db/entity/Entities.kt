package com.pos.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 商品实体
 */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val barcode: String,
    val imageUrl: String? = null,
    val stock: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 购物车项实体
 */
@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey
    val id: String,
    val productId: String,
    val productName: String,
    val price: Double,
    val quantity: Int,
    val cartId: String,
    val addedAt: Long = System.currentTimeMillis()
)

/**
 * 订单实体
 */
@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    val id: String,
    val orderNumber: String,
    val totalAmount: Double,
    val discountAmount: Double = 0.0,
    val paymentMethod: String, // CASH, CARD, MOBILE
    val orderStatus: String, // PENDING, COMPLETED, CANCELLED
    val itemCount: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val printedAt: Long? = null
)

/**
 * 订单明细项实体
 */
@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey
    val id: String,
    val orderId: String,
    val productId: String,
    val productName: String,
    val price: Double,
    val quantity: Int,
    val subtotal: Double,
    val addedAt: Long = System.currentTimeMillis()
)

/**
 * 交易记录实体
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val orderId: String,
    val amount: Double,
    val method: String, // CASH, CARD, MOBILE_PAY
    val status: String, // SUCCESS, FAILED, PENDING
    val referenceNumber: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 打印记录实体
 */
@Entity(tableName = "print_jobs")
data class PrintJobEntity(
    @PrimaryKey
    val id: String,
    val orderId: String,
    val content: String,
    val status: String, // PENDING, PRINTING, SUCCESS, FAILED
    val copies: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val printedAt: Long? = null,
    val errorMessage: String? = null
)
