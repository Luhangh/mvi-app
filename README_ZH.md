# POS 收银系统 - 完整开发文档

## 📋 目录
1. [项目概述](#项目概述)
2. [快速开始](#快速开始)
3. [项目结构](#项目结构)
4. [核心功能](#核心功能)
5. [开发指南](#开发指南)
6. [API 文档](#api-文档)
7. [常见问题](#常见问题)

---

## 项目概述

### 简介
这是一个现代化的 Android POS（Point of Sale）收银平板应用，采用最新的 Android 开发技术栈。应用提供完整的收银流程，从商品浏览、购物车管理、支付处理到订单打印。

### 核心特性
- ✅ **MVI 架构** - 单向数据流，状态可预测
- ✅ **Jetpack Compose** - 现代声明式 UI
- ✅ **离线优先** - 本地数据库存储，自动同步
- ✅ **实时搜索** - 快速商品查询
- ✅ **动画效果** - 流畅的用户交互体验
- ✅ **多支付方式** - 现金、卡、移动支付
- ✅ **条码扫描** - 集成摄像头扫描
- ✅ **打印功能** - 热敏打印机支持
- ✅ **订单管理** - 历史查询和统计分析

### 技术栈
```
Kotlin 2.0.0                      # 编程语言
Jetpack Compose 2024.12.01        # UI 框架
Material Design 3                 # 设计系统
Hilt 2.51.1                       # 依赖注入
Room 2.6.1                        # 数据库
Retrofit 2.11.0                   # 网络请求
Coroutines 1.8.1                  # 异步处理
Flow & StateFlow                  # 响应式数据流
CameraX 1.3.0                     # 摄像头
ML Kit Barcode 17.2.0             # 条码识别
```

---

## 快速开始

### 前置要求
- Android Studio 2023.2 或更高版本
- Kotlin 1.9.0 或更高版本
- JDK 11 或更高版本
- 最低 API 级别 26 (Android 8.0)

### 项目设置

1. **克隆项目**
```bash
git clone <repository-url>
cd mvi-app
```

2. **同步 Gradle**
```bash
./gradlew sync
```

3. **初始化模拟数据**
   - 打开应用后，点击顶部工具栏的 **⚙️ 按钮**（开发者菜单）
   - 点击 **"初始化数据库"** 按钮
   - 自动插入 30+ 商品的模拟数据

4. **编译和运行**
```bash
./gradlew installDebug
```

### 模拟数据
应用包含预设的 30+ 商品数据，涵盖以下分类：
- **食品** (10个) - 三明治、汉堡、炸鸡等
- **饮料** (8个) - 可乐、咖啡、奶茶等
- **零食** (6个) - 薯片、巧克力、饼干等
- **日用品** (3个) - 纸巾、湿巾、口罩
- **电子产品** (2个) - 充电宝、数据线
- **文具** (1个) - 笔记本

---

## 项目结构

```
mvi-app/
├── app/                          # 主应用模块
│   ├── src/main/java/com/pos/app/
│   │   ├── MainActivity.kt        # 导航入口
│   │   └── POSApplication.kt      # Application 类
│   └── AndroidManifest.xml        # 应用配置
│
├── core/                          # 核心库模块
│   ├── mvi/
│   │   ├── MviContract.kt         # MVI 接口定义
│   │   └── MviViewModel.kt        # MVI ViewModel 基类
│   └── result/
│       └── Result.kt              # 异步结果类型
│
├── data/                          # 数据层模块
│   ├── db/                        # 本地数据库
│   │   ├── POSDatabase.kt         # Room 配置
│   │   ├── entity/                # 6 个数据实体
│   │   ├── dao/                   # 6 个数据访问对象
│   │   └── migrations/            # 数据库迁移
│   ├── mock/                      # 模拟数据
│   │   ├── MockDataProvider.kt    # 30+ 商品数据
│   │   └── DatabaseInitializer.kt # 数据库初始化
│   ├── network/                   # 网络层
│   │   ├── POSApi.kt              # Retrofit API
│   │   └── model/                 # API 数据模型
│   ├── repository/                # 仓库层
│   │   └── Repositories.kt        # 5 个 Repository
│   └── di/
│       └── DataModule.kt          # Hilt 配置
│
├── ui/                            # UI 组件库
│   ├── theme/
│   │   ├── Theme.kt               # Material 3 主题
│   │   ├── Color.kt               # 颜色定义
│   │   └── Type.kt                # 字体定义
│   └── components/
│       └── POSComponents.kt        # 可复用组件
│           ├── ProductCard        # 商品卡片
│           ├── CartItemRow        # 购物车行
│           ├── OrderSummary       # 订单摘要
│           ├── LoadingIndicator   # 加载指示
│           ├── PaymentMethodCard  # 支付方式
│           ├── NumericInputField  # 数字输入
│           └── ActionButtonGroup  # 按钮组
│
└── feature/                       # 功能模块
    ├── pos/                       # POS 主功能
    │   ├── DeveloperMenuScreen.kt # 开发者菜单
    │   ├── DeveloperMenuViewModel.kt
    │   ├── mvi/
    │   │   ├── POSContract.kt      # Intent/State/Effect
    │   │   └── POSEvent.kt
    │   ├── viewmodel/
    │   │   └── POSViewModel.kt     # 业务逻辑
    │   └── screen/
    │       └── POSScreen.kt        # UI 界面
    ├── payment/                   # 支付功能
    ├── scanner/                   # 扫码功能
    └── printer/                   # 打印功能
```

---

## 核心功能

### 1️⃣ POS 收银主页面

**文件位置**: `feature/pos/src/main/java/com/pos/feature/pos/screen/POSScreen.kt`

**功能特性**:
- 🔍 **商品搜索** - 实时按名称过滤
- 🛒 **购物车管理** - 添加、删除、修改数量
- 📊 **订单统计** - 自动计算小计、折扣、总额
- 💳 **结账流程** - 跳转支付页面
- 📱 **扫码集成** - 一键进入扫码模式
- 🔧 **开发者菜单** - 数据库初始化和重置

**使用流程**:
```
1. 打开应用 → POS 主页面
   ↓
2. 搜索或浏览商品 → 点击"加入购物车"
   ↓
3. 购物车显示已添加商品 → 调整数量
   ↓
4. 点击"结 账"按钮 → 跳转支付页面
   ↓
5. 选择支付方式 → 完成支付
   ↓
6. 生成打印 → 返回 POS 主页
```

### 2️⃣ 开发者菜单

**文件位置**: `feature/pos/src/main/java/com/pos/feature/pos/DeveloperMenuScreen.kt`

**访问方式**: 在 POS 主页面点击顶部工具栏的 **⚙️ 按钮**

**功能列表**:

| 功能 | 描述 |
|------|------|
| **初始化数据库** | 仅当数据库为空时插入 30+ 商品 |
| **重置数据库** | 清空所有数据并重新插入模拟数据 |
| **刷新统计信息** | 显示商品数量、分类、库存统计 |
| **添加热门商品** | 快速添加价格 10-50 元的商品 |
| **清空购物车** | 删除默认购物车的所有商品 |

### 3️⃣ 商品管理

**模拟数据提供者**: `data/mock/MockDataProvider.kt`

**主要方法**:

```kotlin
// 获取所有商品
MockDataProvider.getMockProducts()          // 返回 30+ 商品列表

// 按分类获取
MockDataProvider.getProductsByCategory(Category.FOOD)

// 按条形码查找
MockDataProvider.findProductByBarcode("6901234567890")

// 获取热门商品
MockDataProvider.getPopularProducts()       // 价格 10-50 元

// 所有分类
MockDataProvider.getAllCategories()
```

### 4️⃣ 数据库初始化

**初始化器**: `data/mock/DatabaseInitializer.kt`

**初始化流程**:
```kotlin
// 在应用启动时自动检查
val initializer = DatabaseInitializer(productDao)

// 检查并初始化（仅当为空）
initializer.initialize()

// 重置数据库
initializer.reset()

// 获取统计信息
val stats = initializer.getStatistics()
```

---

## 开发指南

### MVI 架构说明

MVI (Model-View-Intent) 是一种单向数据流架构：

```
用户操作
   ↓
Intent (意图)
   ↓
ViewModel.processIntent()
   ↓
State (状态)
   ↓
UI 重组
   ↓
Effect (副作用)
   ↓
导航/提示
```

### 添加新的意图（Intent）

**步骤 1**: 在 `POSContract.kt` 中添加意图
```kotlin
sealed class POSIntent : MviIntent {
    data class MyNewIntent(val param: String) : POSIntent()
}
```

**步骤 2**: 在 `POSState` 中添加对应状态
```kotlin
data class POSState(
    // ... 现有字段
    val myNewField: String = ""
)
```

**步骤 3**: 在 `POSViewModel` 中处理意图
```kotlin
private suspend fun processIntent(intent: POSIntent) {
    when (intent) {
        is POSIntent.MyNewIntent -> {
            // 处理逻辑
            val newState = state.value.copy(myNewField = intent.param)
            _state.value = newState
        }
        // ... 其他情况
    }
}
```

### 添加新的 UI 组件

**文件**: `ui/components/POSComponents.kt`

```kotlin
/**
 * 新组件说明
 *
 * @param param1 参数说明
 * @param param2 参数说明
 */
@Composable
fun MyNewComponent(
    param1: String,
    param2: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 实现逻辑
}
```

### 添加新的页面

**步骤 1**: 创建 Contract 文件
```kotlin
// feature/myfeature/mvi/MyFeatureContract.kt
sealed class MyFeatureIntent : MviIntent
data class MyFeatureState(...)
sealed class MyFeatureEffect : MviEffect
```

**步骤 2**: 创建 ViewModel
```kotlin
@HiltViewModel
class MyFeatureViewModel @Inject constructor(
    private val repository: MyRepository
) : MviViewModel<MyFeatureIntent, MyFeatureState, MyFeatureEffect>() {
    // 实现
}
```

**步骤 3**: 创建 Screen
```kotlin
@Composable
fun MyFeatureScreen(
    viewModel: MyFeatureViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    // 实现
}
```

**步骤 4**: 在 MainActivity 添加路由
```kotlin
composable("myfeature") {
    MyFeatureScreen(onNavigateBack = { navController.popBackStack() })
}
```

---

## API 文档

### 数据实体

#### ProductEntity - 商品实体
```kotlin
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,              // 商品名称
    val description: String,       // 描述
    val price: Double,             // 价格（元）
    val category: String,          // 分类
    val barcode: String,           // 条形码
    val imageUrl: String? = null,  // 图片URL
    val stock: Int = 0,            // 库存数量
    val isActive: Boolean = true,  // 是否激活
    val createdAt: Long,           // 创建时间（毫秒）
    val updatedAt: Long            // 更新时间（毫秒）
)
```

#### CartItemEntity - 购物车项实体
```kotlin
@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val id: String,
    val productId: String,         // 商品ID
    val productName: String,       // 商品名称
    val price: Double,             // 单价
    val quantity: Int,             // 数量
    val cartId: String,            // 购物车ID
    val addedAt: Long              // 添加时间
)
```

#### OrderEntity - 订单实体
```kotlin
@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val orderNumber: String,       // 订单号
    val totalAmount: Double,       // 总金额
    val discountAmount: Double,    // 折扣金额
    val paymentMethod: String,     // 支付方式（CASH|CARD|MOBILE_PAY）
    val orderStatus: String,       // 订单状态（PENDING|COMPLETED|CANCELLED）
    val itemCount: Int,            // 商品数量
    val createdAt: Long,           // 创建时间
    val updatedAt: Long,           // 更新时间
    val printedAt: Long? = null    // 打印时间
)
```

### Repository 接口

#### ProductRepository
```kotlin
interface ProductRepository {
    fun getAllProducts(): Flow<List<ProductEntity>>
    fun searchProducts(query: String): Flow<List<ProductEntity>>
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>
    fun getProductByBarcode(barcode: String): Flow<ProductEntity?>
    suspend fun addProduct(product: ProductEntity)
}
```

#### CartRepository
```kotlin
interface CartRepository {
    fun getCartItems(cartId: String): Flow<List<CartItemEntity>>
    suspend fun addToCart(cartId: String, product: ProductEntity, quantity: Int)
    suspend fun updateQuantity(cartId: String, productId: String, quantity: Int)
    suspend fun removeFromCart(cartId: String, productId: String)
    suspend fun clearCart(cartId: String)
}
```

### 常用函数

#### 货币格式化
```kotlin
// 将 Double 格式化为货币字符串
String.format("%.2f", price)
// 输出: "25.00"

// 在 Compose 中使用
Text("¥${String.format("%.2f", price)}")
```

#### 日期格式化
```kotlin
val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
val dateString = formatter.format(Date(timestamp))
```

#### Flow 收集
```kotlin
// 在 Composable 中收集 Flow
val products by viewModel.products.collectAsState(initial = emptyList())

// 在 ViewModel 中收集
viewModelScope.launch {
    repository.products.collect { products ->
        _state.value = state.value.copy(products = products)
    }
}
```

---

## 常见问题

### Q1: 如何初始化模拟数据？
**A**: 打开应用后：
1. 点击顶部右侧的 **⚙️ 按钮**（开发者菜单）
2. 点击 **"初始化数据库"** 按钮
3. 等待提示信息显示成功

### Q2: 如何添加自己的商品数据？
**A**: 修改 `MockDataProvider.getMockProducts()` 方法：
```kotlin
ProductEntity(
    id = UUID.randomUUID().toString(),
    name = "商品名称",
    price = 99.00,
    category = "分类",
    barcode = "1234567890",
    stock = 100
)
```

### Q3: 如何修改应用的配色方案？
**A**: 编辑 `ui/theme/Theme.kt`：
```kotlin
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),           // 修改主色
    secondary = Color(0xFF03DAC6),         // 修改副色
    tertiary = Color(0xFF018786)           // 修改第三色
)
```

### Q4: 如何在支付页面添加新的支付方式？
**A**:
1. 在 `PaymentMethod` 枚举中添加
2. 在 `PaymentScreen` 中添加对应的 UI
3. 在 `PaymentViewModel` 中处理逻辑

### Q5: 如何调试应用？
**A**: 使用 Logcat 和 Timber 日志：
```kotlin
// 记录日志
Timber.d("Debug: $message")
Timber.i("Info: $message")
Timber.e(exception, "Error: $message")
```

### Q6: 应用支持离线工作吗？
**A**: 是的！应用采用离线优先设计：
- 所有数据存储在本地 Room 数据库
- 应用可完全离线运行
- 连接网络时会后台同步数据

### Q7: 如何打印订单？
**A**:
1. 完成支付后自动跳转到打印页面
2. 点击 **"打印"** 按钮
3. 应用会连接热敏打印机打印订单

### Q8: 如何修改订单号生成规则？
**A**: 在 `OrderRepository` 中修改 `generateOrderNumber()` 方法：
```kotlin
private fun generateOrderNumber(): String {
    val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA)
    val timestamp = dateFormat.format(Date())
    val randomNum = (1000..9999).random()
    return "ORD-$timestamp-$randomNum"
}
```

---

## 项目维护

### 代码规范
- 使用 Kotlin 编程语言
- 遵循 Google Kotlin 代码规范
- 添加必要的 KDoc 文档注释
- 使用有意义的变量名称

### 测试
```bash
# 运行单元测试
./gradlew test

# 运行集成测试
./gradlew connectedAndroidTest
```

### 构建发布版本
```bash
# 构建 Release APK
./gradlew assembleRelease

# 构建 App Bundle（用于 Play Store）
./gradlew bundleRelease
```

---

## 许可证

MIT License - 详见 LICENSE 文件

---

## 支持和反馈

如有问题或建议，请：
1. 查看本文档的 FAQ 部分
2. 检查代码注释和 KDoc 文档
3. 查看开发者菜单中的统计信息
4. 联系开发团队

---

**最后更新**: 2025-12-10
**文档版本**: 1.0.0
**应用版本**: 1.0.0
