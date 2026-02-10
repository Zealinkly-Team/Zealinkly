# 智链邻里 - 老人端 Android 应用

## 项目概述

这是"智链邻里"社区服务平台的老人端 Android 应用，基于 Jetpack Compose 构建。老人通过这个应用可以：
- 发布互助任务
- 使用语音/文字输入快速表达需求
- 紧急报警
- 管理积分
- 与 AI 聊天

## 项目结构

```
zeanlinkly-elder/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/example/elderui/
│   │   │   │   ├── core/
│   │   │   │   │   ├── api/              # API 接口和数据模型（三端共用）
│   │   │   │   │   │   ├── ApiClient.kt       # API 客户端工厂
│   │   │   │   │   │   ├── ApiService.kt      # API 接口定义
│   │   │   │   │   │   ├── ElderModels.kt     # 老人端特定模型
│   │   │   │   │   │   ├── Models.kt          # 共用数据模型
│   │   │   │   │   │   └── Response.kt        # 统一响应格式
│   │   │   │   │   ├── repository/      # 仓储层（三端共用基类）
│   │   │   │   │   │   ├── CommonRepositories.kt
│   │   │   │   │   │   └── ElderRepository.kt
│   │   │   │   │   ├── viewmodel/       # ViewModel 层
│   │   │   │   │   │   ├── CommonViewModels.kt
│   │   │   │   │   │   └── ElderViewModels.kt
│   │   │   │   │   └── utils/           # 工具类（三端共用）
│   │   │   │   │       └── Utils.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── component/       # UI 组件（三端共用）
│   │   │   │   │   │   └── CommonComponents.kt
│   │   │   │   │   ├── navigation/      # 导航
│   │   │   │   │   │   └── Navigation.kt
│   │   │   │   │   ├── screen/          # 屏幕页面
│   │   │   │   │   │   ├── AuthScreen.kt
│   │   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   │   ├── TasksScreen.kt
│   │   │   │   │   │   ├── PointsScreen.kt
│   │   │   │   │   │   ├── ProfileScreen.kt
│   │   │   │   │   │   └── ChatScreen.kt
│   │   │   │   │   └── theme/           # 主题配置
│   │   │   │   ├── MainActivity.kt
│   │   │   │   └── ElderApplication.kt
│   │   │   └── res/                     # 资源文件
│   │   │       ├── drawable/
│   │   │       ├── values/
│   │   │       └── xml/
│   │   └── test/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   ├── libs.versions.toml              # 依赖版本定义
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## 核心技术栈

- **UI 框架**: Jetpack Compose
- **网络**: Retrofit2 + OkHttp
- **JSON 序列化**: Moshi
- **本地存储**: DataStore
- **异步处理**: Kotlin Coroutines
- **导航**: Jetpack Navigation Compose
- **图片加载**: Coil

## 三端共用代码说明

### 1. 数据模型层 (`core/api`)

✅ **完全共用**：
- `Response.kt` - 统一 API 响应格式
- `Models.kt` - 通用数据模型（认证、用户、紧急联系人）

⚠️ **半共用**：
- `ElderModels.kt` - 老人端专用模型（包含互助任务、AI 聊天等）
- `ApiService.kt` - API 接口定义（可针对不同端有不同实现）

### 2. 仓储层 (`core/repository`)

✅ **完全共用基类**：
- `CommonRepositories.kt` - 通用仓储基类（认证、用户、紧急联系人）
- 实现了错误处理和数据转换的通用逻辑

⚠️ **特定端实现**：
- `ElderRepository.kt` - 老人端特定业务逻辑

**使用方式**：志愿者端和管理员端可以继承 `CommonRepositories.kt` 中的仓储类，复用认证和用户管理逻辑。

### 3. ViewModel 层 (`core/viewmodel`)

✅ **完全共用基类**：
- `CommonViewModels.kt` 中的：
  - `AuthViewModel` - 认证逻辑
  - `UserViewModel` - 用户信息管理
  - `EmergencyContactViewModel` - 紧急联系人管理

⚠️ **特定端实现**：
- `ElderViewModels.kt` - 老人端专用 ViewModel（任务、紧急报警、Agent、AI 聊天、积分等）

**使用方式**：志愿者端和管理员端可以直接使用或继承 `AuthViewModel`、`UserViewModel` 等基类。

### 4. UI 组件 (`ui/component`)

✅ **完全共用**：
- `CommonComponents.kt` - 通用 UI 组件，包括：
  - `LoadingIndicator()` - 加载指示器
  - `ErrorMessage()` - 错误消息显示
  - `StateContainer()` - 状态容器
  - `CommonTextField()` - 输入框
  - `PrimaryButton()` / `SecondaryButton()` - 按钮
  - `CommonTopBar()` - 顶部栏
  - `EmptyState()` - 空状态
  - `ConfirmDialog()` - 确认对话框

**使用方式**：其他端可以直接导入并使用这些组件，确保 UI 统一性。

### 5. 工具类 (`core/utils`)

✅ **完全共用**：
- `FileUtils` - 文件处理工具
- `DateUtils` - 日期工具
- `AudioUtils` - 音频工具（PCM to WAV 转换）
- `ValidationUtils` - 数据验证工具

**使用方式**：直接在其他端复用这些工具类。

### 6. API 客户端 (`core/api/ApiClient.kt`)

✅ **完全共用**：
- `TokenStore` - Token 存储和管理
- `AuthInterceptor` - 认证拦截器
- `ApiClientFactory` - API 客户端工厂

**特点**：
- 统一的 Token 存储机制
- 自动的认证 Header 注入
- 支持 DataStore 永久化存储

## 功能模块

### 首页（HomeScreen）
- **语音输入**：支持语音识别后自动识别意图
- **文字输入**：直接输入需求，通过 Agent 处理
- **紧急报警**：一键触发紧急报警

### 任务管理（TasksScreen）
- 发布互助任务
- 查看我发布的任务列表
- 查看任务详情

### 积分管理（PointsScreen）
- 查看总积分
- 查看积分流水记录

### 个人信息（ProfileScreen）
- 查看个人信息
- 编辑个人信息
- 管理紧急联系人快捷操作
- 登出

### AI 聊天（ChatScreen）
- 与 AI 进行对话
- 查看聊天历史

## 依赖库

```toml
# 网络相关
retrofit = "2.9.0"
okhttp = "4.11.0"
moshi = "1.15.1"

# Compose
composeBom = "2024.09.00"

# 导航
navigationCompose = "2.7.7"

# 图片加载
coilCompose = "2.5.0"

# 异步处理
coroutines = "1.7.3"

# 本地存储
datastore = "1.0.0"

# 权限管理
accompanist = "0.33.2"
```

## 如何使用共用代码

### 对于志愿者端和管理员端开发者

1. **复制共用文件**：
   ```
   将以下文件复制到您的项目中：
   - core/api/Response.kt
   - core/api/Models.kt
   - core/api/ApiClient.kt
   - core/viewmodel/CommonViewModels.kt
   - core/repository/CommonRepositories.kt
   - core/utils/Utils.kt
   - ui/component/CommonComponents.kt
   ```

2. **创建自己的实现**：
   ```
   创建与 ElderRepository.kt 类似的仓储层实现
   创建与 ElderViewModels.kt 类似的 ViewModel 实现
   创建特定于该端的屏幕
   ```

3. **共用 API 接口**：
   ```kotlin
   // 继承共用的 API 定义
   val authApi = retrofit.create(AuthApi::class.java)
   val userApi = retrofit.create(UserApi::class.java)
   val emergencyContactApi = retrofit.create(EmergencyContactApi::class.java)
   ```

## API 文档

详见后端的 API 文档：`../zealinkly-backend/API文档-老人端.md`

主要 API 端点：
- `POST /api/auth/register/elder` - 注册
- `POST /api/auth/login` - 登录
- `GET /api/user/info` - 获取用户信息
- `POST /api/tasks/cooperation/publish` - 发布任务
- `GET /api/tasks/cooperation/my-as-elder` - 获取我的任务
- `POST /api/agent/process` - Agent 处理文本
- `POST /api/asr/recognize` - 语音识别

## 开发建议

1. **为三端共用代码编写单元测试**
2. **使用 Git submodule 管理共用代码**
3. **保持 API 接口定义的一致性**
4. **统一的错误处理机制**
5. **定期同步各端的共用代码更新**

## 构建和运行

### 前置条件
- Android Studio 2023.1 或更高版本
- Kotlin 2.0.21
- Android SDK 30+

### 编译
```bash
./gradlew build
```

### 运行
```bash
./gradlew installDebug
```

## 许可证

待定

## 联系方式

项目组：智链邻里开发团队

