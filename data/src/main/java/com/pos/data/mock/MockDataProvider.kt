package com.pos.data.mock

import com.pos.data.db.entity.ProductEntity
import java.util.UUID

/**
 * 模拟数据提供者
 * 用于开发和测试环境，提供预设的商品数据
 *
 * @author POS Team
 * @since 1.0.0
 */
object MockDataProvider {

    /**
     * 商品分类枚举
     */
    enum class Category(val displayName: String) {
        FOOD("食品"),
        BEVERAGE("饮料"),
        SNACK("零食"),
        DAILY("日用品"),
        ELECTRONICS("电子产品"),
        STATIONERY("文具")
    }

    /**
     * 获取所有模拟商品数据
     *
     * @return 包含30个预设商品的列表
     */
    fun getMockProducts(): List<ProductEntity> {
        val currentTime = System.currentTimeMillis()

        return listOf(
            // 食品类 (10个)
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "三明治套餐",
                description = "火腿芝士三明治配薯条",
                price = 25.00,
                category = Category.FOOD.displayName,
                barcode = "6901234567890",
                imageUrl = null,
                stock = 50,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "汉堡套餐",
                description = "牛肉汉堡配可乐",
                price = 35.00,
                category = Category.FOOD.displayName,
                barcode = "6901234567891",
                imageUrl = null,
                stock = 40,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "炸鸡套餐",
                description = "香脆炸鸡5块装",
                price = 42.00,
                category = Category.FOOD.displayName,
                barcode = "6901234567892",
                imageUrl = null,
                stock = 35,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "披萨（9寸）",
                description = "意式薄底披萨",
                price = 58.00,
                category = Category.FOOD.displayName,
                barcode = "6901234567893",
                imageUrl = null,
                stock = 25,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "意大利面",
                description = "番茄肉酱意面",
                price = 32.00,
                category = Category.FOOD.displayName,
                barcode = "6901234567894",
                imageUrl = null,
                stock = 30,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "寿司拼盘",
                description = "综合寿司12贯",
                price = 68.00,
                category = Category.FOOD.displayName,
                barcode = "6901234567895",
                imageUrl = null,
                stock = 20,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "沙拉碗",
                description = "凯撒鸡肉沙拉",
                price = 28.00,
                category = Category.FOOD.displayName,
                barcode = "6901234567896",
                imageUrl = null,
                stock = 45,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "牛排套餐",
                description = "西冷牛排配黑椒汁",
                price = 88.00,
                category = Category.FOOD.displayName,
                barcode = "6901234567897",
                imageUrl = null,
                stock = 15,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "拉面",
                description = "日式豚骨拉面",
                price = 38.00,
                category = Category.FOOD.displayName,
                barcode = "6901234567898",
                imageUrl = null,
                stock = 40,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "炒饭套餐",
                description = "扬州炒饭配汤",
                price = 22.00,
                category = Category.FOOD.displayName,
                barcode = "6901234567899",
                imageUrl = null,
                stock = 60,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),

            // 饮料类 (8个)
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "可口可乐",
                description = "经典可乐 500ml",
                price = 5.00,
                category = Category.BEVERAGE.displayName,
                barcode = "6901234568001",
                imageUrl = null,
                stock = 200,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "雪碧",
                description = "柠檬味汽水 500ml",
                price = 5.00,
                category = Category.BEVERAGE.displayName,
                barcode = "6901234568002",
                imageUrl = null,
                stock = 180,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "橙汁",
                description = "鲜榨橙汁 350ml",
                price = 12.00,
                category = Category.BEVERAGE.displayName,
                barcode = "6901234568003",
                imageUrl = null,
                stock = 100,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "矿泉水",
                description = "天然矿泉水 550ml",
                price = 3.00,
                category = Category.BEVERAGE.displayName,
                barcode = "6901234568004",
                imageUrl = null,
                stock = 300,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "奶茶",
                description = "珍珠奶茶 大杯",
                price = 15.00,
                category = Category.BEVERAGE.displayName,
                barcode = "6901234568005",
                imageUrl = null,
                stock = 80,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "咖啡",
                description = "美式咖啡 中杯",
                price = 18.00,
                category = Category.BEVERAGE.displayName,
                barcode = "6901234568006",
                imageUrl = null,
                stock = 90,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "酸奶",
                description = "原味酸奶 200ml",
                price = 8.00,
                category = Category.BEVERAGE.displayName,
                barcode = "6901234568007",
                imageUrl = null,
                stock = 120,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "绿茶",
                description = "无糖绿茶 500ml",
                price = 6.00,
                category = Category.BEVERAGE.displayName,
                barcode = "6901234568008",
                imageUrl = null,
                stock = 150,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),

            // 零食类 (6个)
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "薯片",
                description = "原味薯片 70g",
                price = 8.50,
                category = Category.SNACK.displayName,
                barcode = "6901234569001",
                imageUrl = null,
                stock = 150,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "巧克力",
                description = "德芙牛奶巧克力",
                price = 12.00,
                category = Category.SNACK.displayName,
                barcode = "6901234569002",
                imageUrl = null,
                stock = 100,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "饼干",
                description = "奥利奥夹心饼干",
                price = 10.00,
                category = Category.SNACK.displayName,
                barcode = "6901234569003",
                imageUrl = null,
                stock = 120,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "坚果",
                description = "每日坚果混合装",
                price = 18.00,
                category = Category.SNACK.displayName,
                barcode = "6901234569004",
                imageUrl = null,
                stock = 80,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "口香糖",
                description = "益达无糖口香糖",
                price = 5.50,
                category = Category.SNACK.displayName,
                barcode = "6901234569005",
                imageUrl = null,
                stock = 200,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "糖果",
                description = "大白兔奶糖",
                price = 15.00,
                category = Category.SNACK.displayName,
                barcode = "6901234569006",
                imageUrl = null,
                stock = 90,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),

            // 日用品类 (3个)
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "纸巾",
                description = "抽纸 3包装",
                price = 12.00,
                category = Category.DAILY.displayName,
                barcode = "6901234570001",
                imageUrl = null,
                stock = 100,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "湿巾",
                description = "消毒湿巾 80抽",
                price = 15.00,
                category = Category.DAILY.displayName,
                barcode = "6901234570002",
                imageUrl = null,
                stock = 80,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "口罩",
                description = "一次性口罩 50只",
                price = 25.00,
                category = Category.DAILY.displayName,
                barcode = "6901234570003",
                imageUrl = null,
                stock = 60,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),

            // 电子产品类 (2个)
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "充电宝",
                description = "10000mAh 快充",
                price = 89.00,
                category = Category.ELECTRONICS.displayName,
                barcode = "6901234571001",
                imageUrl = null,
                stock = 30,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "数据线",
                description = "Type-C 快充线",
                price = 29.00,
                category = Category.ELECTRONICS.displayName,
                barcode = "6901234571002",
                imageUrl = null,
                stock = 50,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            ),

            // 文具类 (1个)
            ProductEntity(
                id = UUID.randomUUID().toString(),
                name = "笔记本",
                description = "A5 线圈本",
                price = 12.00,
                category = Category.STATIONERY.displayName,
                barcode = "6901234572001",
                imageUrl = null,
                stock = 70,
                isActive = true,
                createdAt = currentTime,
                updatedAt = currentTime
            )
        )
    }

    /**
     * 根据分类获取商品
     *
     * @param category 商品分类
     * @return 该分类下的所有商品
     */
    fun getProductsByCategory(category: Category): List<ProductEntity> {
        return getMockProducts().filter { it.category == category.displayName }
    }

    /**
     * 根据条形码查找商品
     *
     * @param barcode 商品条形码
     * @return 匹配的商品，如果未找到则返回null
     */
    fun findProductByBarcode(barcode: String): ProductEntity? {
        return getMockProducts().find { it.barcode == barcode }
    }

    /**
     * 获取热门商品（价格在10-50元之间）
     *
     * @return 热门商品列表
     */
    fun getPopularProducts(): List<ProductEntity> {
        return getMockProducts().filter { it.price in 10.0..50.0 }
    }

    /**
     * 获取所有商品分类
     *
     * @return 分类列表
     */
    fun getAllCategories(): List<String> {
        return Category.values().map { it.displayName }
    }
}
