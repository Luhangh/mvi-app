# 🔧 Gradle 版本问题修复指南

## 问题描述
```
* What went wrong:
An exception occurred applying plugin request [id: 'com.android.application', version: '8.5.0']
> Minimum supported Gradle version is 8.7. Current version is 7.5.1.
```

**原因**: Android Gradle Plugin 8.5.0 需要 Gradle 8.7+ 的支持

---

## ✅ 解决方案

### 方法 1: 使用 Gradle Wrapper（推荐）

我已经为你创建了 Gradle Wrapper 配置文件：

#### 已创建的文件：
- ✅ `gradle/wrapper/gradle-wrapper.properties` - 已更新到 Gradle 8.7
- ✅ `gradlew` - Unix/Linux 脚本
- ✅ `gradlew.bat` - Windows 脚本

#### Windows 用户操作步骤：

1. **打开命令提示符 (CMD)**
   ```bash
   cd E:\android_workpace\mvi-app
   ```

2. **首次运行 Gradle Wrapper**
   ```bash
   gradlew.bat clean build
   ```

   这会自动下载 Gradle 8.7（首次下载较慢，耐心等待）

3. **等待下载完成**
   ```
   Downloading https://services.gradle.org/distributions/gradle-8.7-bin.zip
   ```

#### macOS/Linux 用户操作步骤：

```bash
cd ~/android_workspace/mvi-app
chmod +x gradlew
./gradlew clean build
```

---

### 方法 2: 手动下载 Gradle Wrapper JAR

如果自动下载失败，可以手动下载：

1. **创建目录结构**
   ```bash
   mkdir -p gradle\wrapper
   ```

2. **下载 gradle-wrapper.jar**
   - 从官方源下载: https://services.gradle.org/distributions/gradle-8.7-bin.zip
   - 解压后提取 `gradle/wrapper/gradle-wrapper.jar`
   - 放入项目的 `gradle/wrapper/` 目录

3. **验证文件结构**
   ```
   mvi-app/
   └── gradle/
       └── wrapper/
           ├── gradle-wrapper.jar         ← 需要此文件
           ├── gradle-wrapper.properties   ✅ 已创建
   ```

---

### 方法 3: 在 Android Studio 中配置

#### 步骤 1: 打开项目设置
1. Android Studio → File → Settings
2. 搜索 "Gradle"
3. 找到 Build, Execution, Deployment → Gradle

#### 步骤 2: 配置 Gradle JDK
```
Gradle JDK: 确保设置为 JDK 11 或更高版本
```

#### 步骤 3: 启用 Gradle Wrapper
```
☑️ Use gradle wrapper (recommended)
```

#### 步骤 4: 同步项目
```
File → Sync Now
```

---

## 📋 完整的修复命令

### Windows 用户（推荐）

```batch
REM 进入项目目录
cd E:\android_workpace\mvi-app

REM 清理并构建
gradlew.bat clean

REM 同步依赖
gradlew.bat build

REM 或者一条命令
gradlew.bat clean build --refresh-dependencies
```

### macOS/Linux 用户

```bash
# 进入项目目录
cd ~/android_workspace/mvi-app

# 清理并构建
./gradlew clean

# 同步依赖
./gradlew build

# 或者一条命令
./gradlew clean build --refresh-dependencies
```

---

## 🔍 验证是否成功

修复成功后，你会看到：

```
...
> Configure project :app
> Configure project :feature:pos
> Configure project :feature:payment
...
BUILD SUCCESSFUL in XXs
```

---

## 📝 检查清单

- [ ] `gradle/wrapper/gradle-wrapper.properties` 已更新到 Gradle 8.7
- [ ] `gradlew` 和 `gradlew.bat` 脚本已创建
- [ ] Java 环境已正确安装（Java 11+）
- [ ] JAVA_HOME 环境变量已设置
- [ ] 运行了 `gradlew clean build` 命令

---

## 🆘 如果仍然报错

### 错误 1: "gradle-wrapper.jar not found"

**解决**: 运行以下命令自动下载：
```bash
gradlew.bat wrapper --gradle-version 8.7
```

### 错误 2: "Java not found"

**解决**: 设置 JAVA_HOME
```bash
# Windows
set JAVA_HOME=C:\Program Files\Java\jdk-11.0.15

# Linux/macOS
export JAVA_HOME=/usr/libexec/java_home -v 11
```

### 错误 3: "Permission denied (gradlew)"

**解决**:
```bash
chmod +x gradlew
./gradlew clean build
```

### 错误 4: 网络连接超时

**解决**: 配置代理或换源
```bash
# 编辑 ~/.gradle/gradle.properties
systemProp.https.proxyHost=your-proxy-host
systemProp.https.proxyPort=your-proxy-port
```

---

## ✨ 验证环境

运行此命令检查 Gradle 版本：

```bash
# Windows
gradlew.bat --version

# Linux/macOS
./gradlew --version
```

输出应该是：
```
Gradle 8.7

Build time:   2025-01-XX XX:XX:XX UTC
Revision:     XXXXXXXXXXXXXXX

Kotlin:       1.9.25
Groovy:       3.0.17
Ant:          Apache Ant(TM) version 1.10.13
JVM:          11.0.15 (vendor, version)
OS:           Windows 11 (arch, version)
```

---

## 🎯 快速修复（一键解决）

### Windows 用户：
```batch
@echo off
cd /d E:\android_workpace\mvi-app
echo "正在下载 Gradle 8.7..."
call gradlew.bat clean build --refresh-dependencies
echo "修复完成！"
pause
```

保存为 `fix.bat`，双击运行。

### Linux/macOS 用户：
```bash
#!/bin/bash
cd ~/android_workspace/mvi-app
echo "正在下载 Gradle 8.7..."
./gradlew clean build --refresh-dependencies
echo "修复完成！"
```

保存为 `fix.sh`，运行 `bash fix.sh`。

---

**如果以上方法都不行，请检查：**

1. ✅ Java 已安装 (Java 11+)
2. ✅ 网络连接正常
3. ✅ Android SDK 已安装
4. ✅ 项目路径没有中文字符

---

**最常见的解决办法：直接运行**
```bash
gradlew.bat clean build --refresh-dependencies
```

**耐心等待 3-5 分钟，让 Gradle 自动下载依赖即可！** ✨
