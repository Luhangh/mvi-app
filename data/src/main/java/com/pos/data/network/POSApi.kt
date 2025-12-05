package com.pos.data.network

import com.pos.data.network.model.ApiResponse
import com.pos.data.network.model.PaymentRequest
import com.pos.data.network.model.PaymentResponse
import com.pos.data.network.model.PrintRequest
import com.pos.data.network.model.PrintResponse
import com.pos.data.network.model.ProductResponse
import com.pos.data.network.model.SyncRequest
import com.pos.data.network.model.SyncResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface POSApi {

    /**
     * 获取所有商品
     */
    @GET("/api/products")
    suspend fun getProducts(
        @Query("category") category: String? = null
    ): ApiResponse<List<ProductResponse>>

    /**
     * 按条形码获取商品
     */
    @GET("/api/products/barcode/{barcode}")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): ApiResponse<ProductResponse>

    /**
     * 同步产品数据
     */
    @POST("/api/sync/products")
    suspend fun syncProducts(
        @Body request: SyncRequest
    ): ApiResponse<SyncResponse>

    /**
     * 支付
     */
    @POST("/api/payments")
    suspend fun processPayment(
        @Body request: PaymentRequest
    ): ApiResponse<PaymentResponse>

    /**
     * 获取支付状态
     */
    @GET("/api/payments/{transactionId}")
    suspend fun getPaymentStatus(
        @Path("transactionId") transactionId: String
    ): ApiResponse<PaymentResponse>

    /**
     * 提交打印任务
     */
    @POST("/api/print")
    suspend fun submitPrintJob(
        @Body request: PrintRequest
    ): ApiResponse<PrintResponse>

    /**
     * 获取打印任务状态
     */
    @GET("/api/print/{jobId}")
    suspend fun getPrintJobStatus(
        @Path("jobId") jobId: String
    ): ApiResponse<PrintResponse>

    /**
     * 健康检查
     */
    @GET("/api/health")
    suspend fun healthCheck(): ApiResponse<String>
}
