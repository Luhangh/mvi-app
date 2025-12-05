package com.pos.data.di

import android.content.Context
import androidx.room.Room
import com.pos.data.db.POSDatabase
import com.pos.data.db.dao.CartItemDao
import com.pos.data.db.dao.OrderDao
import com.pos.data.db.dao.OrderItemDao
import com.pos.data.db.dao.PrintJobDao
import com.pos.data.db.dao.ProductDao
import com.pos.data.db.dao.TransactionDao
import com.pos.data.network.POSApi
import com.pos.data.repository.CartRepository
import com.pos.data.repository.OrderRepository
import com.pos.data.repository.PaymentRepository
import com.pos.data.repository.PrintRepository
import com.pos.data.repository.ProductRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    /**
     * 提供Room数据库实例
     */
    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): POSDatabase {
        return Room.databaseBuilder(
            context,
            POSDatabase::class.java,
            "pos_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * 提供ProductDao
     */
    @Singleton
    @Provides
    fun provideProductDao(database: POSDatabase): ProductDao {
        return database.productDao()
    }

    /**
     * 提供CartItemDao
     */
    @Singleton
    @Provides
    fun provideCartItemDao(database: POSDatabase): CartItemDao {
        return database.cartItemDao()
    }

    /**
     * 提供OrderDao
     */
    @Singleton
    @Provides
    fun provideOrderDao(database: POSDatabase): OrderDao {
        return database.orderDao()
    }

    /**
     * 提供OrderItemDao
     */
    @Singleton
    @Provides
    fun provideOrderItemDao(database: POSDatabase): OrderItemDao {
        return database.orderItemDao()
    }

    /**
     * 提供TransactionDao
     */
    @Singleton
    @Provides
    fun provideTransactionDao(database: POSDatabase): TransactionDao {
        return database.transactionDao()
    }

    /**
     * 提供PrintJobDao
     */
    @Singleton
    @Provides
    fun providePrintJobDao(database: POSDatabase): PrintJobDao {
        return database.printJobDao()
    }

    /**
     * 提供OkHttpClient
     */
    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * 提供JSON序列化器
     */
    @Singleton
    @Provides
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    /**
     * 提供Retrofit实例
     */
    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("http://api.pos.local/") // 替换为实际API地址
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    /**
     * 提供POSApi
     */
    @Singleton
    @Provides
    fun providePOSApi(retrofit: Retrofit): POSApi {
        return retrofit.create(POSApi::class.java)
    }

    /**
     * 提供ProductRepository
     */
    @Singleton
    @Provides
    fun provideProductRepository(
        productDao: ProductDao,
        api: POSApi
    ): ProductRepository {
        return ProductRepository(productDao, api)
    }

    /**
     * 提供CartRepository
     */
    @Singleton
    @Provides
    fun provideCartRepository(
        cartItemDao: CartItemDao
    ): CartRepository {
        return CartRepository(cartItemDao)
    }

    /**
     * 提供OrderRepository
     */
    @Singleton
    @Provides
    fun provideOrderRepository(
        orderDao: OrderDao,
        orderItemDao: OrderItemDao
    ): OrderRepository {
        return OrderRepository(orderDao, orderItemDao)
    }

    /**
     * 提供PaymentRepository
     */
    @Singleton
    @Provides
    fun providePaymentRepository(
        transactionDao: TransactionDao,
        api: POSApi
    ): PaymentRepository {
        return PaymentRepository(transactionDao, api)
    }

    /**
     * 提供PrintRepository
     */
    @Singleton
    @Provides
    fun providePrintRepository(
        printJobDao: PrintJobDao,
        api: POSApi
    ): PrintRepository {
        return PrintRepository(printJobDao, api)
    }
}
