package com.pos.data.mock

import com.pos.data.db.dao.ProductDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据库初始化器
 * 负责在应用首次启动时填充模拟数据
 *
 * @property productDao 商品数据访问对象
 * @author POS Team
 * @since 1.0.0
 */
@Singleton
class DatabaseInitializer @Inject constructor(
    private val productDao: ProductDao
) {

    /**
     * 初始化数据库
     * 检查数据库是否为空，如果为空则插入模拟数据
     *
     * @return 初始化是否成功
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            // 检查数据库是否已有数据
            val existingProducts = productDao.getAllProductsOnce()

            if (existingProducts.isEmpty()) {
                Timber.d("数据库为空，开始插入模拟数据...")

                // 获取模拟商品数据
                val mockProducts = MockDataProvider.getMockProducts()

                // 批量插入商品
                mockProducts.forEach { product ->
                    productDao.insert(product)
                }

                Timber.i("成功插入 ${mockProducts.size} 个模拟商品")
                true
            } else {
                Timber.d("数据库已有 ${existingProducts.size} 个商品，跳过初始化")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "数据库初始化失败")
            false
        }
    }

    /**
     * 重置数据库
     * 清空所有商品并重新插入模拟数据
     * 注意：此操作会删除所有现有数据
     *
     * @return 重置是否成功
     */
    suspend fun reset(): Boolean = withContext(Dispatchers.IO) {
        try {
            Timber.w("开始重置数据库...")

            // 删除所有商品
            productDao.deleteAll()

            // 重新插入模拟数据
            val mockProducts = MockDataProvider.getMockProducts()
            mockProducts.forEach { product ->
                productDao.insert(product)
            }

            Timber.i("数据库重置成功，插入 ${mockProducts.size} 个商品")
            true
        } catch (e: Exception) {
            Timber.e(e, "数据库重置失败")
            false
        }
    }

    /**
     * 添加单个商品
     *
     * @param product 要添加的商品
     * @return 添加是否成功
     */
    suspend fun addProduct(product: com.pos.data.db.entity.ProductEntity): Boolean =
        withContext(Dispatchers.IO) {
            try {
                productDao.insert(product)
                Timber.d("成功添加商品: ${product.name}")
                true
            } catch (e: Exception) {
                Timber.e(e, "添加商品失败: ${product.name}")
                false
            }
        }

    /**
     * 批量添加商品
     *
     * @param products 要添加的商品列表
     * @return 成功添加的商品数量
     */
    suspend fun addProducts(products: List<com.pos.data.db.entity.ProductEntity>): Int =
        withContext(Dispatchers.IO) {
            var successCount = 0
            products.forEach { product ->
                try {
                    productDao.insert(product)
                    successCount++
                } catch (e: Exception) {
                    Timber.e(e, "添加商品失败: ${product.name}")
                }
            }
            Timber.i("批量添加完成: $successCount/${products.size}")
            successCount
        }

    /**
     * 获取数据库统计信息
     *
     * @return 统计信息字符串
     */
    suspend fun getStatistics(): String = withContext(Dispatchers.IO) {
        try {
            val products = productDao.getAllProductsOnce()
            val categories = products.map { it.category }.distinct()
            val totalStock = products.sumOf { it.stock }
            val activeProducts = products.count { it.isActive }

            buildString {
                appendLine("=== 数据库统计 ===")
                appendLine("商品总数: ${products.size}")
                appendLine("激活商品: $activeProducts")
                appendLine("商品分类: ${categories.size} 个")
                appendLine("总库存: $totalStock 件")
                appendLine("分类详情:")
                categories.forEach { category ->
                    val count = products.count { it.category == category }
                    appendLine("  - $category: $count 个商品")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "获取统计信息失败")
            "统计信息获取失败: ${e.message}"
        }
    }
}
