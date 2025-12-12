# POS 收银系统 - 开发者指南

## 📖 目录

1. [开发环境设置](#开发环境设置)
2. [项目架构详解](#项目架构详解)
3. [核心模块开发](#核心模块开发)
4. [代码规范](#代码规范)
5. [常见开发任务](#常见开发任务)
6. [调试技巧](#调试技巧)
7. [性能优化](#性能优化)

---

## 开发环境设置

### 系统要求
```gradle
compileSdk = 35
minSdk = 26
targetSdk = 35
jvmTarget = "11"
```

### IDE 配置
```
Android Studio 2023.2+
Kotlin 1.9.0+
Gradle 8.1.0+
```

### 必要插件
```gradle
id("com.android.application")
id("org.jetbrains.kotlin.android")
id("com.google.dagger.hilt.android")
id("androidx.room")
id("kotlinx-serialization")
```

### 初始化步骤
```bash
# 1. 克隆项目
git clone <repo-url>
cd mvi-app

# 2. Gradle 同步
./gradlew sync

# 3. 编译项目
./gradlew assembleDebug

# 4. 运行测试
./gradlew test
```

---

## 项目架构详解

### 整体架构图
```
┌─────────────────────────────────────────┐
│             User Interface              │
│  (Jetpack Compose - 声明式 UI)          │
├─────────────────────────────────────────┤
│            MVI Architecture              │
│  Intent → ViewModel → State → UI        │
├─────────────────────────────────────────┤
│         Repository & UseCase             │
│  (业务逻辑层)                           │
├─────────────────────────────────────────┤
│          Data Layer                      │
│  ┌──────────────┬──────────────┐        │
│  │ Local DB     │ Remote API   │        │
│  │ (Room)       │ (Retrofit)   │        │
│  └──────────────┴──────────────┘        │
└─────────────────────────────────────────┘
```

### MVI 三层结构

#### 1. Intent（意图）
**文件**: `feature/*/mvi/*Contract.kt`

```kotlin
// 用户操作的意图表示
sealed class POSIntent : MviIntent {
    object LoadProducts : POSIntent()
    data class AddToCart(val product: ProductEntity) : POSIntent()
    object ProceedToCheckout : POSIntent()
    // ... 更多意图
}
```

#### 2. State（状态）
**文件**: `feature/*/mvi/*Contract.kt`

```kotlin
// 完整的 UI 状态快照
data class POSState(
    val products: List<ProductEntity> = emptyList(),
    val cartItems: List<CartItemEntity> = emptyList(),
    val total: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
) : MviState
```

#### 3. Effect（副作用）
**文件**: `feature/*/mvi/*Contract.kt`

```kotlin
// 一次性事件（导航、提示等）
sealed class POSEffect : MviEffect {
    data class ShowError(val message: String) : POSEffect()
    object NavigateToPayment : POSEffect()
    // ... 更多副作用
}
```

### ViewModel 处理流程
```kotlin
@HiltViewModel
class POSViewModel @Inject constructor(
    private val repository: ProductRepository
) : MviViewModel<POSIntent, POSState, POSEffect>() {

    override fun processIntent(intent: POSIntent) {
        when (intent) {
            is POSIntent.LoadProducts -> {
                // 1. 发起异步操作
                viewModelScope.launch {
                    try {
                        // 2. 调用数据层
                        repository.getAllProducts().collect { products ->
                            // 3. 更新状态
                            val newState = state.value.copy(products = products)
                            _state.value = newState
                        }
                    } catch (e: Exception) {
                        // 4. 发送副作用
                        _effects.send(POSEffect.ShowError(e.message ?: "Unknown error"))
                    }
                }
            }
            // ... 处理其他 Intent
        }
    }
}
```

### 数据流向
```
User Interaction (点击按钮)
        ↓
handleIntent(Intent)
        ↓
processIntent(Intent)
        ↓
Repository.getData()
        ↓
Database/Network
        ↓
State 更新
        ↓
UI 重组 (Recomposition)
        ↓
Effect 发送 (一次性事件)
```

---

## 核心模块开发

### 1. 添加新的 Compose 屏幕

**步骤 1**: 创建 Contract
```kotlin
// feature/newfeature/mvi/NewFeatureContract.kt
sealed class NewFeatureIntent : MviIntent {
    object Load : NewFeatureIntent()
}

data class NewFeatureState(
    val data: String = "",
    val isLoading: Boolean = false
) : MviState

sealed class NewFeatureEffect : MviEffect {
    data class ShowMessage(val message: String) : NewFeatureEffect()
}
```

**步骤 2**: 创建 ViewModel
```kotlin
// feature/newfeature/viewmodel/NewFeatureViewModel.kt
@HiltViewModel
class NewFeatureViewModel @Inject constructor(
    private val repository: MyRepository
) : MviViewModel<NewFeatureIntent, NewFeatureState, NewFeatureEffect>() {

    override fun processIntent(intent: NewFeatureIntent) {
        when (intent) {
            is NewFeatureIntent.Load -> loadData()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = state.value.copy(isLoading = true)
            try {
                val data = repository.getData()
                _state.value = state.value.copy(
                    data = data,
                    isLoading = false
                )
            } catch (e: Exception) {
                _effects.send(NewFeatureEffect.ShowMessage(e.message ?: "Error"))
            }
        }
    }
}
```

**步骤 3**: 创建 Screen
```kotlin
// feature/newfeature/screen/NewFeatureScreen.kt
@Composable
fun NewFeatureScreen(
    viewModel: NewFeatureViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is NewFeatureEffect.ShowMessage -> {
                    // 显示消息
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("New Feature") })
        }
    ) { padding ->
        // UI 实现
    }
}
```

**步骤 4**: 在 MainActivity 添加路由
```kotlin
composable("newfeature") {
    NewFeatureScreen(onNavigateBack = { navController.popBackStack() })
}
```

### 2. 添加新的数据库实体

**步骤 1**: 创建实体
```kotlin
// data/db/entity/NewEntity.kt
@Entity(tableName = "new_table")
data class NewEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
```

**步骤 2**: 创建 DAO
```kotlin
@Dao
interface NewEntityDao {
    @Insert
    suspend fun insert(entity: NewEntity)

    @Query("SELECT * FROM new_table WHERE id = :id")
    fun getById(id: String): Flow<NewEntity?>

    @Query("SELECT * FROM new_table")
    fun getAll(): Flow<List<NewEntity>>

    @Delete
    suspend fun delete(entity: NewEntity)
}
```

**步骤 3**: 在 Database 中注册
```kotlin
@Database(
    entities = [
        // ... 现有实体
        NewEntity::class  // 添加新实体
    ],
    version = 2  // 增加版本号
)
abstract class POSDatabase : RoomDatabase() {
    abstract fun newEntityDao(): NewEntityDao
}
```

### 3. 添加新的 Repository

```kotlin
// data/repository/NewRepository.kt
interface NewRepository {
    fun getAll(): Flow<List<NewEntity>>
    suspend fun add(entity: NewEntity)
}

class NewRepositoryImpl @Inject constructor(
    private val dao: NewEntityDao
) : NewRepository {
    override fun getAll(): Flow<List<NewEntity>> = dao.getAll()

    override suspend fun add(entity: NewEntity) {
        dao.insert(entity)
    }
}

// 在 DataModule 中注册
@Provides
@Singleton
fun provideNewRepository(dao: NewEntityDao): NewRepository {
    return NewRepositoryImpl(dao)
}
```

### 4. 添加新的 UI 组件

```kotlin
// ui/components/POSComponents.kt
/**
 * 新组件说明
 *
 * 特性：
 * - 功能说明
 * - 功能说明
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
    // 实现
}
```

---

## 代码规范

### Kotlin 风格指南

#### 命名规范
```kotlin
// ✅ 类和接口 - PascalCase
class ProductRepository
interface UserDao

// ✅ 函数和变量 - camelCase
fun getProductById(id: String)
val isLoading = false

// ✅ 常量 - UPPER_SNAKE_CASE
const val DEFAULT_PAGE_SIZE = 20

// ✅ 私有属性 - 前缀下划线
private val _state = MutableStateFlow(State())
```

#### 函数文档
```kotlin
/**
 * 获取指定 ID 的商品
 *
 * 会从本地数据库查询。如果本地无数据，
 * 则从网络获取并缓存到本地。
 *
 * @param id 商品 ID
 * @return 商品对象流，如果不存在则返回 null
 * @throws NotFoundException 当商品不存在时抛出
 *
 * @author POS Team
 * @since 1.0.0
 */
fun getProductById(id: String): Flow<ProductEntity?>
```

#### 类文档
```kotlin
/**
 * 商品仓库实现
 *
 * 负责商品数据的获取和缓存。采用离线优先策略，
 * 优先从本地数据库读取，无数据则从网络同步。
 *
 * @property productDao 商品 DAO
 * @property productApi 商品 API
 * @author POS Team
 * @since 1.0.0
 */
@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val productApi: POSApi
) : ProductRepository
```

### Compose 最佳实践

#### 正确的状态管理
```kotlin
// ✅ 好：使用 viewModel 管理状态
@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    // ...
}

// ❌ 坏：在 Composable 中维护状态
@Composable
fun MyScreen() {
    var state by remember { mutableStateOf(...) }  // 不推荐
}
```

#### 正确的副作用处理
```kotlin
// ✅ 好：使用 LaunchedEffect 处理一次性事件
LaunchedEffect(viewModel) {
    viewModel.effects.collect { effect ->
        when (effect) {
            is MyEffect.Navigate -> navigate()
            is MyEffect.ShowError -> showError()
        }
    }
}

// ❌ 坏：在 Composable 中直接启动协程
LaunchedEffect(Unit) {
    viewModel.someFlow.collect { ... }  // 可能重复执行
}
```

#### 修饰符顺序
```kotlin
// ✅ 推荐顺序：
Box(
    modifier = Modifier
        .fillMaxSize()              // 大小
        .padding(16.dp)             // 内边距
        .background(Color.White)    // 背景
        .clickable { }              // 交互
        .clip(RoundedCornerShape(8.dp))  // 形状
)
```

---

## 常见开发任务

### 任务 1：添加新的支付方式

**文件修改**:
1. `feature/payment/mvi/PaymentContract.kt` - 添加 PaymentMethod
2. `feature/payment/screen/PaymentScreen.kt` - 添加 UI
3. `feature/payment/viewmodel/PaymentViewModel.kt` - 添加处理逻辑

**代码示例**:
```kotlin
// 在 PaymentMethod 中添加
enum class PaymentMethod {
    CASH, CARD, MOBILE_PAY, NEW_METHOD
}

// 在 PaymentScreen 中添加 UI
PaymentMethodCard(
    method = "新支付方式",
    isSelected = selectedMethod == "new",
    onClick = { selectedMethod = "new" }
)
```

### 任务 2：实现订单历史查询

```kotlin
// 创建新的 Feature
feature/orderhistory/

// 实现 ViewModel
class OrderHistoryViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    fun loadOrders(dateRange: Pair<Long, Long>) {
        viewModelScope.launch {
            orderRepository.getOrdersByDateRange(
                dateRange.first,
                dateRange.second
            ).collect { orders ->
                // 更新状态
            }
        }
    }
}
```

### 任务 3：添加商品图片

```kotlin
// 修改 ProductCard 组件
@Composable
fun ProductCard(
    // ... 现有参数
    imageUrl: String? = null,  // 新增
    // ...
) {
    Card {
        // 显示图片
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
        }
        // ... 其他内容
    }
}
```

---

## 调试技巧

### 使用 Timber 日志

```kotlin
// 添加依赖
implementation("com.jakewharton.timber:timber:5.0.1")

// 在 Application 中初始化
class POSApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }
}

// 使用日志
Timber.d("Debug: %s", message)
Timber.i("Info: %s", message)
Timber.w("Warning: %s", message)
Timber.e(exception, "Error: %s", message)
```

### 查看数据库内容

```kotlin
// 在 ViewModel 中检查数据
viewModelScope.launch {
    productDao.getAllProducts().collect { products ->
        Timber.d("Products count: ${products.size}")
        products.forEach { product ->
            Timber.d("Product: ${product.name}, Price: ${product.price}")
        }
    }
}
```

### 使用 Layout Inspector

1. 在 Android Studio 中打开 **Layout Inspector**
2. **Tools** → **Layout Inspector**
3. 选择运行的应用
4. 点击查看 UI 树和属性

### 调试 Compose

```kotlin
// 使用 debugInspector 查看重组
@Composable
fun MyComposable() {
    CompositionLocalProvider(
        LocalInspectionMode provides true
    ) {
        // 内容
    }
}

// 查看重组次数
var recomposeCount by remember { mutableIntStateOf(0) }
LaunchedEffect(Unit) {
    recomposeCount++
    Timber.d("Recomposed: $recomposeCount times")
}
```

---

## 性能优化

### Compose 优化

#### 1. 避免不必要的重组
```kotlin
// ❌ 坏：每次都创建新对象
Text(
    text = data.name,
    style = TextStyle(color = Color.Red)  // 新对象
)

// ✅ 好：提取为常量
val redTextStyle = TextStyle(color = Color.Red)
Text(text = data.name, style = redTextStyle)
```

#### 2. 使用 key 优化列表
```kotlin
// ✅ 好：使用唯一 key
LazyColumn {
    items(items, key = { it.id }) { item ->
        ProductCard(item)
    }
}
```

#### 3. 使用 memoization
```kotlin
// ✅ 好：缓存计算结果
val rememberedValue = remember(key1, key2) {
    expensiveCalculation()
}
```

### 数据库优化

```kotlin
// ✅ 使用索引加速查询
@Entity(
    tableName = "products",
    indices = [
        Index("barcode", unique = true),  // 条形码索引
        Index("category"),                 // 分类索引
    ]
)
data class ProductEntity(...)
```

### 内存优化

```kotlin
// ✅ 及时清理资源
DisposableEffect(key1) {
    onDispose {
        // 清理资源
    }
}

// ✅ 使用 LazyColumn 而不是 Column
LazyColumn {
    items(largeList) { item ->
        ItemRow(item)
    }
}
```

---

## 测试

### 单元测试

```kotlin
// data/repository/ProductRepositoryTest.kt
@RunWith(JUnit4::class)
class ProductRepositoryTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var productDao: ProductDao
    private lateinit var repository: ProductRepository

    @Before
    fun setUp() {
        // 初始化测试数据库
    }

    @Test
    fun testGetProductById() {
        // 测试逻辑
    }
}
```

### UI 测试

```kotlin
// feature/pos/POSScreenTest.kt
@RunWith(ComposeTestRule::class)
class POSScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testProductCardClick() {
        composeTestRule.setContent {
            POSTheme {
                ProductCard(
                    name = "测试商品",
                    price = 99.0,
                    barcode = "123",
                    onAddToCart = {}
                )
            }
        }

        composeTestRule.onNodeWithText("加入购物车").performClick()
    }
}
```

---

## 常见问题

### Q: 如何添加新的权限？
**A**: 在 `AndroidManifest.xml` 中添加：
```xml
<uses-permission android:name="android.permission.CAMERA" />
```

### Q: 如何处理网络错误？
**A**: 在 ViewModel 中使用 try-catch：
```kotlin
try {
    val data = repository.fetchData()
    _state.value = state.value.copy(data = data)
} catch (e: IOException) {
    _effects.send(ShowError("网络错误"))
} catch (e: Exception) {
    _effects.send(ShowError("未知错误"))
}
```

### Q: 如何进行数据库迁移？
**A**: 在 `POSDatabase.kt` 中定义迁移：
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE products ADD COLUMN description TEXT")
    }
}
```

---

**最后更新**: 2025-12-10
**版本**: 1.0.0
