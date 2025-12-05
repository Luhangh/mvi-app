# 项目架构与技术方案详解.md

## 📋 项目概况

这是一个基于 **Kotlin + Jetpack** 的现代化 POS 收银平板应用，采用业界最佳实践和最新的 Android 开发技术栈。

### 核心亮点

1. **MVI 架构模式**
   - 单向数据流：Intent → State → UI
   - 清晰的职责划分，易于测试
   - 状态管理集中，便于调试

2. **Compose UI 框架**
   - 声明式 UI，代码简洁直观
   - 自动的 Recomposition 优化
   - 现代化的平板优化界面

3. **完整的模块化设计**
   - 业务功能模块独立（scanner、payment、printer、pos）
   - 核心功能模块可复用（core、data、ui）
   - 支持独立编译和测试

4. **响应式编程**
   - StateFlow 实现状态管理
   - Flow 实现事件流
   - 协程处理异步操作

5. **企业级依赖注入**
   - Hilt 框架实现编译时 DI
   - 完整的 Singleton 和 Scoped 生命周期管理
   - 依赖自动注入，零配置

## 🏗️ 分层架构详解

### 第一层：UI 层（表现层）

**职责**：
- 展示数据和状态
- 捕获用户交互
- 发送 Intent 给 ViewModel

**核心文件**：
- `POSScreen.kt` - 收银主界面（双列布局：左侧商品，右侧购物车）
- `PaymentScreen.kt` - 支付界面（支付方式选择、金额显示）
- `PrinterScreen.kt` - 打印界面（打印机选择、进度显示）
- `ScannerScreen.kt` - 扫码界面（摄像头预览、扫描框）

**特点**：
- 完全使用 Compose 声明式 UI
- 通过 `collectAsState()` 订阅状态
- 通过 `collectAsState()` 监听副作用

### 第二层：ViewModel 层（演示层）

**职责**：
- 处理用户 Intent
- 管理业务状态
- 发送单次效果（Effect）

**核心类**：
```kotlin
POSViewModel - 商品和购物车管理
PaymentViewModel - 支付流程管理
PrinterViewModel - 打印任务管理
ScannerViewModel - 扫码逻辑
```

**实现方式**：
- 继承 `MviViewModel<Intent, State, Effect>` 基类
- 使用 `handleIntent()` 处理用户操作
- 通过 `setState()` 和 `updateState()` 更新状态

### 第三层：Repository 层（数据协调层）

**职责**：
- 统一数据访问接口
- 本地和远程数据的协调
- 实现缓存和同步策略

**核心类**：
```kotlin
ProductRepository - 商品数据获取和同步
CartRepository - 购物车数据管理
OrderRepository - 订单持久化
PaymentRepository - 支付交易管理
PrintRepository - 打印任务管理
```

**特点**：
- 优先从本地数据库获取数据
- 自动后台同步远程数据
- 通过 Flow 提供响应式数据流

### 第四层：Data 层（数据持久化层）

#### 本地数据库（Room）
```kotlin
POSDatabase
├── ProductDao - 商品数据访问
├── CartItemDao - 购物车数据访问
├── OrderDao - 订单数据访问
├── OrderItemDao - 订单明细访问
├── TransactionDao - 交易数据访问
└── PrintJobDao - 打印任务访问
```

#### 远程 API（Retrofit）
```kotlin
POSApi
├── getProducts() - 获取商品列表
├── getProductByBarcode() - 按条形码获取
├── processPayment() - 发起支付
├── submitPrintJob() - 提交打印任务
└── syncProducts() - 同步产品数据
```

## 🔄 数据流示例

### 场景：用户扫码添加商品

```
1. 用户点击"扫码"按钮
   ↓
2. ScannerScreen 调用 viewModel.handleIntent(ScannerIntent.StartCamera)
   ↓
3. ScannerViewModel.processIntent() 处理
   ↓
4. 相机启动，用户对准条形码
   ↓
5. ML Kit 识别条形码，调用 handleIntent(BarcodeDetected)
   ↓
6. ScannerViewModel 调用 ProductRepository.getProductByBarcode()
   ↓
7. Repository 优先查本地数据库
   ├─ 如果有：返回 Result.Success
   └─ 如果没有：调用 API 获取，存入数据库
   ↓
8. POSViewModel 收到产品信息，调用 handleIntent(AddToCart)
   ↓
9. CartRepository.addToCart() 存入购物车
   ↓
10. 状态更新：cartItems.add(product)
    ↓
11. UI 重组，购物车列表显示新商品
```

## 💾 数据库设计

### Products 表
```sql
CREATE TABLE products (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    price REAL NOT NULL,
    category TEXT,
    barcode TEXT UNIQUE,
    imageUrl TEXT,
    stock INTEGER,
    isActive BOOLEAN,
    createdAt LONG,
    updatedAt LONG
);
```

### Orders 表
```sql
CREATE TABLE orders (
    id TEXT PRIMARY KEY,
    orderNumber TEXT UNIQUE,
    totalAmount REAL,
    discountAmount REAL,
    paymentMethod TEXT,
    orderStatus TEXT,
    itemCount INTEGER,
    createdAt LONG,
    updatedAt LONG,
    printedAt LONG
);
```

### OrderItems 表
```sql
CREATE TABLE order_items (
    id TEXT PRIMARY KEY,
    orderId TEXT,
    productId TEXT,
    productName TEXT,
    price REAL,
    quantity INTEGER,
    subtotal REAL,
    addedAt LONG
);
```

## 🔌 网络请求流程

### Retrofit 配置特点

1. **JSON 序列化**
   ```kotlin
   Json {
       ignoreUnknownKeys = true  // 容错处理
       coerceInputValues = true  // 类型强制转换
   }
   ```

2. **OkHttp 拦截器**
   ```kotlin
   HttpLoggingInterceptor  // 请求/响应日志
   connectTimeout = 30秒
   readTimeout = 30秒
   writeTimeout = 30秒
   ```

3. **错误处理**
   ```kotlin
   Result.Success - 成功
   Result.Error - 失败（Exception 包装）
   Result.Loading - 加载中
   ```

## 🎯 状态管理精解

### MVI State 三大原则

1. **不可变性**（Immutable）
   ```kotlin
   data class POSState(
       val products: List<ProductEntity> = emptyList(),
       val cartItems: List<CartItemEntity> = emptyList(),
       // ... 其他只读属性
   ) : MviState
   ```

2. **完整性**（Complete）
   - 单个 State 对象包含整个界面的完整状态
   - 不依赖外部变量或上下文

3. **可重现性**（Reproducible）
   - 给定相同的 Intent 序列和初始状态，总能产生相同的 UI

### Intent 分类

```kotlin
sealed class POSIntent : MviIntent {
    object LoadProducts : POSIntent()
    data class ScanBarcode(val barcode: String) : POSIntent()
    data class AddToCart(val product: ProductEntity) : POSIntent()
    object ClearCart : POSIntent()
    object ProceedToCheckout : POSIntent()
}
```

### Effect 用途

- 一次性事件，不会重复触发
- 用于：导航、弹窗、提示、副作用
- 通过 Channel 实现，自动消费后消失

## 🛡️ 依赖注入注解详解

### @Inject
```kotlin
// 构造函数注入（推荐）
@HiltViewModel
class POSViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository
) : MviViewModel<...> { }
```

### @Provides
```kotlin
// 工厂方法提供依赖
@Provides
@Singleton
fun providePOSDatabase(context: Context): POSDatabase {
    return Room.databaseBuilder(...)
}
```

### @Binds
```kotlin
// 接口实现绑定
@Binds
abstract fun bindRepository(
    impl: ProductRepositoryImpl
): ProductRepository
```

### @Qualifier
```kotlin
// 同一类型多个实现的区分
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LocalDb

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RemoteApi
```

## 🚀 性能优化技巧

### 1. Compose 重组优化
```kotlin
// 使用 key 稳定列表项身份
LazyColumn {
    items(cartItems, key = { it.id }) { item ->
        CartItemRow(item)
    }
}

// 使用 remember 缓存计算结果
val total = remember(cartItems) {
    cartItems.sumOf { it.price * it.quantity }
}
```

### 2. Flow 背压处理
```kotlin
// 使用 buffer 处理背压
repository.getProducts()
    .buffer(capacity = 10)
    .collect { ... }
```

### 3. 数据库查询优化
```kotlin
// 创建索引
@Entity(indices = [Index("barcode", unique = true)])
data class ProductEntity(...)

// 使用关系加载
@Relation(parentColumn = "orderId", entityColumn = "id")
val items: List<OrderItemEntity>
```

### 4. 网络请求缓存
```kotlin
// 实现缓存策略
val cachedProducts = mutableMapOf<String, CachedProduct>()
val ttl = 15 * 60 * 1000  // 15分钟

fun getProduct(id: String): Flow<Result<Product>> = flow {
    // 检查缓存
    cachedProducts[id]?.let {
        if (System.currentTimeMillis() - it.timestamp < ttl) {
            emit(Result.Success(it.product))
            return@flow
        }
    }
    // 从网络获取
    val product = api.getProduct(id)
    cachedProducts[id] = CachedProduct(product, System.currentTimeMillis())
    emit(Result.Success(product))
}
```

## 🔐 安全最佳实践

### 1. API 密钥管理
```kotlin
// 使用 BuildConfig 隐藏敏感信息
const val API_BASE_URL = BuildConfig.API_URL
const val API_KEY = BuildConfig.API_KEY
```

### 2. 敏感数据加密
```kotlin
// 使用 EncryptedSharedPreferences
val encryptedSharedPrefs = EncryptedSharedPreferences.create(
    context,
    "secret_shared_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

### 3. 权限最小化原则
```kotlin
// 仅请求必要权限
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
```

## 📊 测试策略

### 单元测试（Unit Test）
```kotlin
// ViewModel 测试
@get:Rule
val instantExecutorRule = InstantTaskExecutorRule()

@Test
fun `test add to cart updates state`() = runTest {
    val viewModel = POSViewModel(mockRepository)
    viewModel.handleIntent(POSIntent.AddToCart(product))

    advanceUntilIdle()
    assert(viewModel.state.value.cartItems.size == 1)
}
```

### 集成测试（Integration Test）
```kotlin
// Repository 测试
@get:Rule
val instantExecutorRule = InstantTaskExecutorRule()

@Test
fun `test product sync from network to database`() = runTest {
    repository.syncProducts()

    val local = productDao.getAllProducts().first()
    assertThat(local).isNotEmpty()
}
```

### UI 测试（Compose Test）
```kotlin
// Compose 组件测试
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun `test payment button disabled when method not selected`() {
    composeTestRule.setContent {
        PaymentScreen(state = PaymentState())
    }

    composeTestRule
        .onNodeWithText("确认支付")
        .assertIsNotEnabled()
}
```

## 🎨 UI 设计指南

### Material Design 3 集成
```kotlin
// 主题定义
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),          // 主色
    secondary = Color(0xFF03DAC6),        // 次色
    tertiary = Color(0xFF03DAC6),         // 第三色
    background = Color(0xFFFFFFFF),       // 背景色
    surface = Color(0xFFF5F5F5),          // 表面色
    onBackground = Color(0xFF000000),     // 背景上的文本
    onSurface = Color(0xFF000000)         // 表面上的文本
)
```

### 平板屏幕适配
```kotlin
// 响应式布局
Row(modifier = Modifier.fillMaxSize()) {
    // 左侧：商品列表（权重 1.5）
    ProductListSection(modifier = Modifier.weight(1.5f))

    // 右侧：购物车（权重 1）
    CartSection(modifier = Modifier.weight(1f))
}
```

## 📈 监控和调试

### Timber 日志
```kotlin
Timber.d("加载商品列表")          // Debug 日志
Timber.w("找不到该条形码")         // 警告日志
Timber.e(exception, "支付失败")   // 错误日志
```

### 状态监控
```kotlin
LaunchedEffect(state) {
    Timber.d("State updated: $state")
}
```

### 性能监控
```kotlin
// 使用 Android Profiler
// 监控：CPU、内存、网络、电量
```

## 🔮 未来扩展方向

1. **多语言支持**
   - 集成 i18n 框架
   - 支持中文、英文等多语言

2. **离线模式**
   - 本地数据库作为主要数据源
   - 后台自动同步

3. **高级支付**
   - 集成支付宝、微信支付
   - 银行卡刷卡接口

4. **数据分析**
   - 销售数据统计
   - 交易趋势分析
   - 商品热度排行

5. **会员系统**
   - 会员卡管理
   - 积分系统
   - 优惠券管理

6. **云同步**
   - 多门店数据同步
   - 库存统一管理
   - 报表中心

---

**最后更新**: 2025年12月
**版本**: 1.0.0
