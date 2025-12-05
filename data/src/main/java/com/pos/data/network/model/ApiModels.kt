package com.pos.data.network.model

import kotlinx.serialization.Serializable

/**
 * API响应包装类
 */
@Serializable
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T? = null
)

/**
 * 商品响应
 */
@Serializable
data class ProductResponse(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val barcode: String,
    val imageUrl: String? = null,
    val stock: Int
)

/**
 * 支付请求
 */
@Serializable
data class PaymentRequest(
    val orderId: String,
    val amount: Double,
    val method: String, // CASH, CARD, MOBILE_PAY
    val reference: String? = null
)

/**
 * 支付响应
 */
@Serializable
data class PaymentResponse(
    val transactionId: String,
    val status: String, // SUCCESS, FAILED, PENDING
    val message: String,
    val amount: Double,
    val timestamp: Long
)

/**
 * 打印请求
 */
@Serializable
data class PrintRequest(
    val orderId: String,
    val content: String,
    val copies: Int = 1
)

/**
 * 打印响应
 */
@Serializable
data class PrintResponse(
    val jobId: String,
    val status: String,
    val message: String
)

/**
 * 数据同步请求
 */
@Serializable
data class SyncRequest(
    val lastSyncTime: Long
)

/**
 * 数据同步响应
 */
@Serializable
data class SyncResponse(
    val products: List<ProductResponse>,
    val lastSyncTime: Long
)
