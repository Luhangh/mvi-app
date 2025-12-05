# POS 收银平板应用 - 现代化架构实现

一个基于 Kotlin 和 Jetpack 技术栈的现代化收银系统，采用 **MVI 架构 + Compose UI + 模块化设计**。

## 📋 项目特点

### 技术栈
- **UI 框架**: Jetpack Compose（现代化 Declarative UI）
- **架构模式**: MVI（Model-View-Intent）+ MVVM
- **依赖注入**: Hilt（编译时依赖注入）
- **异步处理**: Kotlin 协程 + Flow（响应式编程）
- **数据持久化**: Room ORM（本地 SQLite 数据库）
- **网络请求**: Retrofit 2 + OkHttp（带拦截器和日志）
- **序列化**: Kotlinx Serialization（JSON 解析）
- **日志**: Timber（智能日志库）
- **相机**: CameraX + ML Kit（条形码识别）

### 架构优势
✅ **完全解耦**: 各层职责明确，易于测试和维护
✅ **响应式 UI**: 基于 StateFlow 和 Flow 的实时状态更新
✅ **模块化设计**: 功能模块独立，支持独立测试和编译
✅ **单向数据流**: Intent → State → UI，易追踪数据变化
✅ **性能优化**: 协程避免线程阻塞，Flow 背压支持
✅ **类型安全**: Kotlin 的完整类型检查和密封类

## 📂 项目结构

```
mvi-app/
├── app/                       # 主应用模块
│   ├── MainActivity.kt         # 导航和 UI 入口
│   ├── POSApplication.kt       # 应用初始化
│   └── AndroidManifest.xml     # 应用配置
│
├── core/                       # 核心模块
│   ├── mvi/
│   │   ├── MviContract.kt      # MVI 基础接口（Intent/State/Effect）
│   │   └── MviViewModel.kt     # MVI ViewModel 基类
│   └── result/
│       └── Result.kt           # 异步操作结果包装类
│
├── data/                       # 数据层
│   ├── db/
│   │   ├── POSDatabase.kt      # Room 数据库配置
│   │   ├── entity/
│   │   │   └── Entities.kt     # 数据库实体（Product/Order/Cart等）
│   │   └── dao/
│   │       └── Daos.kt         # 数据访问对象
│   ├── network/
│   │   ├── POSApi.kt           # API 接口定义
│   │   └── model/
│   │       └── ApiModels.kt    # API 数据模型
│   ├── repository/
│   │   └── Repositories.kt     # 数据仓库（统一数据访问接口）
│   └── di/
│       └── DataModule.kt       # Hilt 依赖注入配置
│
├── ui/                         # UI 组件库
│   ├── theme/
│   │   └── Theme.kt            # Compose 主题定义
│   └── components/
│       └── POSComponents.kt     # 可复用的 Compose 组件
│
├── feature/
│   ├── pos/                    # POS 收银主页面模块
│   │   ├── mvi/
│   │   │   └── POSContract.kt  # POS 的 Intent/State/Effect
│   │   ├── viewmodel/
│   │   │   └── POSViewModel.kt # POS 业务逻辑
│   │   └── screen/
│   │       └── POSScreen.kt    # POS UI 实现
│   │
│   ├── scanner/                # 扫码功能模块
│   │   ├── mvi/
│   │   │   └── ScannerContract.kt
│   │   ├── viewmodel/
│   │   │   └── ScannerViewModel.kt
│   │   └── screen/
│   │       └── ScannerScreen.kt
│   │
│   ├── payment/                # 支付功能模块
│   │   ├── mvi/
│   │   │   └── PaymentContract.kt
│   │   ├── viewmodel/
│   │   │   └── PaymentViewModel.kt
│   │   └── screen/
│   │       └── PaymentScreen.kt
│   │
│   └── printer/                # 打印功能模块
│       ├── mvi/
│       │   └── PrinterContract.kt
│       ├── viewmodel/
│       │   └── PrinterViewModel.kt
│       └── screen/
│           └── PrinterScreen.kt
│
├── gradle/
│   └── libs.versions.toml      # 依赖版本统一管理
├── settings.gradle.kts         # 项目模块配置
└── build.gradle.kts            # 根项目配置
```

## 🏗️ 核心架构详解

### MVI 架构流程

```
用户交互 (UI Event)
        ↓
    Intent (用户意图)
        ↓
  ViewModel.processIntent()
        ↓
 State 更新 (StateFlow)
        ↓
    UI 重组 (Recomposition)
        ↓
    Effect 发送 (单次事件)
        ↓
  导航/弹窗等副作用
```

### 数据流向

```
UI 层 (Compose)
    ↑↓
ViewModel (MVI 协调)
    ↑↓
Repository (数据聚合)
    ↑
Data 层 (Room + Network)
```

## 🔧 核心组件说明

### 1. MviViewModel 基类
```kotlin
// 在 core/mvi/MviViewModel.kt
abstract class MviViewModel<I, S, E>(initialState: S) : ViewModel() {
    // state: 状态流
    val state: StateFlow<S>
    // effects: 单次事件流
    val effects: Flow<E>
    // 处理意图
    fun handleIntent(intent: I)
    // 更新状态
    protected fun setState(newState: S)
    // 发送事件
    protected suspend fun sendEffect(effect: E)
}
```

### 2. Repository 模式
```kotlin
// 统一的数据访问接口
@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao,
    private val api: POSApi
) {
    // 本地优先的数据获取策略
    // 自动同步远程数据到本地
}
```

### 3. Hilt 依赖注入
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Singleton
    @Provides
    fun provideDatabase(...): POSDatabase { ... }

    @Singleton
    @Provides
    fun providePOSApi(...): POSApi { ... }
}
```

## 📱 功能模块详解

### POS 收银模块
- **加载商品列表**：从网络或本地数据库
- **搜索商品**：实时过滤
- **管理购物车**：加入、删除、修改数量
- **应用折扣**：支持折扣百分比
- **订单结算**：生成订单记录

### 扫码模块
- **条形码扫描**：使用 ML Kit 识别
- **权限管理**：动态请求相机权限
- **闪光灯控制**：支持开关
- **扫描反馈**：振动和声音提示

### 支付模块
- **支付方式选择**：现金、银行卡、移动支付
- **实时计算**：自动计算找零
- **支付状态追踪**：PENDING → PROCESSING → SUCCESS/FAILED
- **交易记录**：保存到本地数据库

### 打印模块
- **打印机管理**：发现和选择可用打印机
- **多份打印**：支持设置打印份数
- **打印队列**：管理待打印任务
- **打印状态监控**：实时反馈打印进度

## 🚀 快速开始

### 1. 克隆项目
```bash
git clone <repository-url>
cd mvi-app
```

### 2. 同步 Gradle
```bash
./gradlew clean build
```

### 3. 运行应用
```bash
./gradlew installDebug
adb shell am start -n com.pos.app/.MainActivity
```

### 4. 构建发布版
```bash
./gradlew assembleRelease
```

## 🔌 API 配置

修改 `data/src/main/java/com/pos/data/di/DataModule.kt` 中的 API 基础 URL：

```kotlin
@Provides
fun provideRetrofit(...): Retrofit {
    return Retrofit.Builder()
        .baseUrl("http://your-api-server.com/")  // 替换为实际 API 地址
        .build()
}
```

## 📊 数据库 Schema

### 主要表结构
- `products`：商品信息（ID、名称、价格、条形码等）
- `cart_items`：购物车项目（产品ID、数量、价格）
- `orders`：订单记录（订单号、总额、支付方式、状态）
- `order_items`：订单明细（产品、数量、小计）
- `transactions`：交易记录（订单ID、金额、支付方式、状态）
- `print_jobs`：打印任务队列（订单ID、内容、状态）

## 🧪 测试

```bash
# 单元测试
./gradlew testDebug

# 集成测试
./gradlew connectedAndroidTest

# 特定模块测试
./gradlew :feature:pos:testDebug
```

## 📈 性能优化建议

1. **列表优化**：使用 `LazyColumn` 和 `LazyRow` 替代 `Column`
2. **图片加载**：集成 Coil 或 Glide 进行图片缓存
3. **数据库查询**：使用 Room 的索引优化查询性能
4. **网络请求**：实现缓存策略和请求去重
5. **内存管理**：使用 Profiler 检查内存泄漏

## 🔐 安全建议

1. **API 通信**：始终使用 HTTPS
2. **敏感数据**：使用 EncryptedSharedPreferences 加密存储
3. **权限管理**：动态请求最小必要权限
4. **日志脱敏**：生产版本关闭详细日志

## 📝 开发规范

### 命名规范
- Kotlin 文件：PascalCase（如 POSViewModel.kt）
- 函数和变量：camelCase
- 常量：UPPER_SNAKE_CASE
- 包名：全小写（如 com.pos.feature.pos）

### Code Style
- 最大行长：120 字符
- 缩进：4 个空格
- 使用 Kotlin 的幂等性特性

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 📞 支持

- 问题反馈：提交 Issue
- 功能建议：讨论 Discussion
- 技术咨询：联系维护者

## 🎓 学习资源

- [Jetpack Compose 官方文档](https://developer.android.com/jetpack/compose)
- [Room 数据库指南](https://developer.android.com/training/data-storage/room)
- [Hilt 依赖注入](https://developer.android.com/training/dependency-injection/hilt-android)
- [Kotlin 协程](https://kotlinlang.org/docs/coroutines-overview.html)
- [MVI 架构模式](https://github.com/cyclone-project/cyclone)

---

**最后更新**: 2025年12月
**维护者**: POS 开发团队
