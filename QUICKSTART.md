# 快速开始指南 (Quick Start Guide)

## 🎯 5分钟快速上手

### 1️⃣ 项目导入

```bash
# 克隆项目
git clone <your-repository>
cd mvi-app

# 使用 Android Studio 打开
open -a "Android Studio" .

# 或者通过命令行：
android-studio .
```

### 2️⃣ 依赖同步

```bash
# 使用 Gradle Wrapper 同步依赖
./gradlew clean build

# 或在 Android Studio 中：
File → Sync Now
```

### 3️⃣ 配置虚拟设备

```bash
# 列出所有虚拟设备
emulator -list-avds

# 启动平板虚拟设备
emulator -avd Pixel_Tablet -scale 1.0

# 如果没有，创建新的：
avdmanager create avd -n "pos-tablet" \
  -k "system-images;android-35;google_apis;arm64-v8a" \
  -d "Pixel Tablet"
```

### 4️⃣ 安装运行

```bash
# 直接运行（使用 Android Studio 的 Run）
./gradlew installDebug

# 或通过 adb
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.pos.app/.MainActivity
```

## 📁 文件快速导航

| 文件 | 位置 | 作用 |
|------|------|------|
| 主页面 | `feature/pos/screen/POSScreen.kt` | 收银主界面 |
| POS 逻辑 | `feature/pos/viewmodel/POSViewModel.kt` | 业务逻辑 |
| 支付屏幕 | `feature/payment/screen/PaymentScreen.kt` | 支付界面 |
| 打印屏幕 | `feature/printer/screen/PrinterScreen.kt` | 打印界面 |
| 扫码屏幕 | `feature/scanner/screen/ScannerScreen.kt` | 扫码界面 |
| 数据库 | `data/db/POSDatabase.kt` | Room 数据库 |
| API 接口 | `data/network/POSApi.kt` | 网络请求 |
| 主题配置 | `ui/theme/Theme.kt` | 样式主题 |

## 🔧 常见开发任务

### 添加新屏幕

1. 在 `feature/xxx/mvi/` 下创建 `Contract.kt`：
```kotlin
sealed class NewScreenIntent : MviIntent
data class NewScreenState(...) : MviState
sealed class NewScreenEffect : MviEffect
```

2. 在 `feature/xxx/viewmodel/` 下创建 `ViewModel.kt`：
```kotlin
@HiltViewModel
class NewScreenViewModel @Inject constructor(
    private val repository: SomeRepository
) : MviViewModel<NewScreenIntent, NewScreenState, NewScreenEffect>(
    initialState = NewScreenState()
) {
    override suspend fun processIntent(intent: NewScreenIntent) {
        // 处理逻辑
    }
}
```

3. 在 `feature/xxx/screen/` 下创建 `Screen.kt`：
```kotlin
@Composable
fun NewScreen(viewModel: NewScreenViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    // UI 实现
}
```

4. 在 `MainActivity.kt` 中添加导航：
```kotlin
composable("newscreen") {
    NewScreen()
}
```

### 添加新数据库表

1. 在 `data/db/entity/Entities.kt` 中定义实体：
```kotlin
@Entity(tableName = "new_table")
data class NewEntity(
    @PrimaryKey val id: String,
    val name: String,
    // ...
)
```

2. 在 `data/db/dao/Daos.kt` 中创建 DAO：
```kotlin
@Dao
interface NewEntityDao {
    @Insert suspend fun insert(entity: NewEntity)
    @Query("SELECT * FROM new_table") fun getAll(): Flow<List<NewEntity>>
    // ...
}
```

3. 在 `POSDatabase.kt` 中注册：
```kotlin
@Database(
    entities = [..., NewEntity::class],
    version = 1
)
abstract class POSDatabase : RoomDatabase() {
    abstract fun newEntityDao(): NewEntityDao
}
```

4. 更新 `DataModule.kt` 的 `version`

### 添加新 API 端点

1. 在 `POSApi.kt` 中添加方法：
```kotlin
@GET("/api/new-endpoint")
suspend fun getNewData(): ApiResponse<NewData>
```

2. 创建对应的数据模型：
```kotlin
@Serializable
data class NewData(val id: String, val value: String)
```

3. 在 `Repository` 中集成：
```kotlin
fun getNewData(): Flow<Result<NewData>> = flow {
    emit(Result.Loading)
    try {
        val response = api.getNewData()
        emit(Result.Success(response.data))
    } catch (e: Exception) {
        emit(Result.Error(e))
    }
}
```

## 🐛 调试技巧

### 查看日志
```bash
# 实时日志
adb logcat | grep "pos"

# 保存日志到文件
adb logcat > logcat.txt

# 只看 Error 和 Warning
adb logcat *:W
```

### 数据库检查
```bash
# 进入 SQLite 命令行
adb shell
cd /data/data/com.pos.app/databases
sqlite3 pos_database

# 查看表
.tables

# 查询数据
SELECT * FROM products LIMIT 10;
```

### 性能分析
```bash
# 使用 Android Profiler（Android Studio 内置）
Run → Profile 'app'

# 监控：
# - CPU 使用率
# - 内存占用
# - 网络流量
# - 电池消耗
```

### 断点调试
```kotlin
// 在代码中设置断点
viewModel.handleIntent(POSIntent.LoadProducts)  // ← 点击左侧行号设置断点

// 使用调试变量监视
// Variables 窗口中查看本地变量值
// Watches 窗口添加自定义表达式
```

## 🚀 构建和发布

### Debug 版本
```bash
./gradlew assembleDebug
# 输出: app/build/outputs/apk/debug/app-debug.apk
```

### Release 版本
```bash
# 需要签名密钥
./gradlew assembleRelease
# 输出: app/build/outputs/apk/release/app-release.apk
```

### 生成签名密钥
```bash
keytool -genkey -v -keystore release.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias pos-key
```

### 在 `local.properties` 中配置签名
```properties
sdk.dir=/path/to/android/sdk

# 签名配置
RELEASE_STORE_FILE=/path/to/release.keystore
RELEASE_STORE_PASSWORD=password
RELEASE_KEY_ALIAS=pos-key
RELEASE_KEY_PASSWORD=password
```

## 📦 模块编译

```bash
# 只编译 core 模块
./gradlew :core:build

# 只编译 feature:pos
./gradlew :feature:pos:build

# 构建特定模块的 APK
./gradlew :feature:pos:assembleDebug
```

## 🧪 运行测试

```bash
# 运行所有单元测试
./gradlew test

# 运行特定测试
./gradlew test --tests com.pos.feature.pos.viewmodel.*

# 生成测试覆盖率报告
./gradlew testDebugUnitTest jacocoTestDebugUnitTestReport
# 报告位置: app/build/reports/jacoco/index.html

# 运行集成测试（需要连接设备或模拟器）
./gradlew connectedAndroidTest
```

## 🔗 API 服务配置

### 修改 API 基础 URL

编辑 `data/src/main/java/com/pos/data/di/DataModule.kt`：

```kotlin
@Provides
fun providePOSApi(retrofit: Retrofit): POSApi {
    return retrofit.create(POSApi::class.java)
}

// 修改这里的 URL
@Provides
fun provideRetrofit(...): Retrofit {
    return Retrofit.Builder()
        .baseUrl("http://your-api-server.com/")  // ← 修改这里
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()
}
```

### API 本地模拟

创建 `MockPOSApi.kt` 用于本地开发：

```kotlin
class MockPOSApi : POSApi {
    override suspend fun getProducts(category: String?): ApiResponse<List<ProductResponse>> {
        return ApiResponse(
            code = 200,
            message = "success",
            data = listOf(
                ProductResponse(
                    id = "1",
                    name = "可乐",
                    description = "冰镇可乐",
                    price = 5.0,
                    category = "饮料",
                    barcode = "123456789",
                    stock = 100
                )
            )
        )
    }
    // ...
}
```

在开发中使用本地 Mock：

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DebugDataModule {
    @Provides
    @Singleton
    fun providePOSApi(): POSApi = MockPOSApi()
}
```

## 📚 学习路线

### 初级（Week 1-2）
- [ ] 理解 Kotlin 基础语法
- [ ] 学习 MVI 架构原理
- [ ] 理解 Compose 声明式 UI
- [ ] 熟悉项目结构

### 中级（Week 3-4）
- [ ] 修改现有界面样式
- [ ] 添加新的功能模块
- [ ] 修改数据库表结构
- [ ] 集成新的 API 端点

### 高级（Week 5+）
- [ ] 性能优化和调试
- [ ] 单元测试编写
- [ ] CI/CD 流程搭建
- [ ] 发布生产版本

## 🆘 常见问题

### Q: 编译错误 "kapt"

A: 确保在 `build.gradle.kts` 中添加：
```kotlin
plugins {
    // ...
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}
```

### Q: 模拟器无法访问本地 API

A: 使用特殊 IP 地址访问主机：
```kotlin
// 在模拟器中，使用 10.0.2.2 访问主机
.baseUrl("http://10.0.2.2:8080/")
```

### Q: Gradle 同步卡慢

A:
```bash
# 使用本地 Gradle 缓存
./gradlew --build-cache build

# 或者刷新缓存
./gradlew clean build --refresh-dependencies
```

### Q: 数据库版本不匹配

A: 更新数据库版本并添加迁移：
```kotlin
@Database(version = 2)  // 从 1 升级到 2
abstract class POSDatabase : RoomDatabase()

// Room 会自动检测版本，需要提供迁移路径
```

## 📞 获取帮助

- 📖 查看 `ARCHITECTURE.md` 了解深入的架构设计
- 🔍 搜索现有代码中的类似实现
- 💬 在团队讨论区提问
- 🐛 提交 Issue 反馈问题

---

**祝编码愉快！** 🚀
