package com.pos.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pos.data.db.dao.ProductDao
import com.pos.data.db.dao.CartItemDao
import com.pos.data.db.dao.OrderDao
import com.pos.data.db.dao.OrderItemDao
import com.pos.data.db.dao.TransactionDao
import com.pos.data.db.dao.PrintJobDao
import com.pos.data.db.entity.ProductEntity
import com.pos.data.db.entity.CartItemEntity
import com.pos.data.db.entity.OrderEntity
import com.pos.data.db.entity.OrderItemEntity
import com.pos.data.db.entity.TransactionEntity
import com.pos.data.db.entity.PrintJobEntity

@Database(
    entities = [
        ProductEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        TransactionEntity::class,
        PrintJobEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class POSDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartItemDao(): CartItemDao
    abstract fun orderDao(): OrderDao
    abstract fun orderItemDao(): OrderItemDao
    abstract fun transactionDao(): TransactionDao
    abstract fun printJobDao(): PrintJobDao
}
