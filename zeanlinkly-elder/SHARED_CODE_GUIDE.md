# 三端共用代码清单

## 📋 概述

本文档详细列出"智链邻里"项目中三个端（老人端、志愿者端、管理员端）可以共用的代码，以及如何使用它们。

---

## 🔧 完全共用代码

### 1. API 响应格式 (`core/api/Response.kt`)

```kotlin
// 统一的 API 响应格式
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)

// 分页响应格式
data class PaginatedResponse<T>(
    val content: List<T>,
    val totalElements: Int,
    val totalPages: Int,
    val currentPage: Int
)
```

**说明**：所有三个端的 API 返回值都遵循这个格式。

**使用场景**：
- 志愿者端：处理 API 响应
- 管理员端：处理 API 响应

---

### 2. 通用数据模型 (`core/api/Models.kt`)

#### 认证相关
```kotlin
data class LoginRequest(username, password)
data class LoginResponse(token, userType, userId)
data class RegisterRequest(username, password, realName, phone)
data class RegisterResponse(id, username, userType)
data class CardLoginRequest(cardImageBase64)
```

#### 用户信息
```kotlin
data class UserInfo(
    id, username, realName, phone, address, points, userType
)

data class UpdateUserRequest(realName, phone, address)
```

#### 紧急联系人
```kotlin
data class EmergencyContact(id, name, relation, phone, priority)
data class CreateEmergencyContactRequest(name, relation, phone, priority)
```

**说明**：这些是三个端都需要使用的数据模型。

**使用场景**：
- 老人端：用户认证、信息管理、紧急联系人
- 志愿者端：用户认证、信息管理、紧急联系人
- 管理员端：用户认证、信息管理、紧急联系人

---

### 3. API 客户端工厂 (`core/api/ApiClient.kt`)

```kotlin
// Token 存储
class TokenStore(context: Context) {
    fun saveToken(token, userId, userType)
    fun clearToken()
}

// 认证拦截器
class AuthInterceptor(tokenStore: TokenStore)

// API 客户端工厂
class ApiClientFactory(context: Context) {
    fun createOkHttpClient(): OkHttpClient
    fun createRetrofit(baseUrl: String): Retrofit
    fun createAuthApi(retrofit: Retrofit): AuthApi
    fun createUserApi(retrofit: Retrofit): UserApi
    fun createEmergencyContactApi(retrofit: Retrofit): EmergencyContactApi
}
```

**特点**：
- 统一的 Token 存储机制
- 自动的认证 Header 注入
- 支持 DataStore 永久化存储

**使用场景**：
- 老人端：初始化 API 客户端
- 志愿者端：初始化 API 客户端
- 管理员端：初始化 API 客户端

---

### 4. 认证 API 接口 (`core/api/ApiService.kt`)

```kotlin
interface AuthApi {
    @POST("/api/auth/register/elder")
    suspend fun registerElder(@Body request: RegisterRequest)
    
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest)
    
    @POST("/api/auth/login-by-card")
    suspend fun loginByCard(@Body request: CardLoginRequest)
}
```

**说明**：认证 API 接口定义是通用的。

**使用场景**：三端都使用相同的认证接口

---

### 5. 用户 API 接口 (`core/api/ApiService.kt`)

```kotlin
interface UserApi {
    @GET("/api/user/info")
    suspend fun getUserInfo(): ApiResponse<UserInfo>
    
    @PUT("/api/user/info")
    suspend fun updateUserInfo(@Body request: UpdateUserRequest)
}
```

**说明**：用户信息管理 API 接口定义是通用的。

**使用场景**：三端都使用相同的用户信息接口

---

### 6. 紧急联系人 API 接口 (`core/api/ApiService.kt`)

```kotlin
interface EmergencyContactApi {
    @GET("/api/emergency-contacts")
    suspend fun getEmergencyContacts(): ApiResponse<List<EmergencyContact>>
    
    @POST("/api/emergency-contacts")
    suspend fun addEmergencyContact(@Body request: CreateEmergencyContactRequest)
    
    @PUT("/api/emergency-contacts/{id}")
    suspend fun updateEmergencyContact(@Path("id") id: Long, @Body request: CreateEmergencyContactRequest)
    
    @DELETE("/api/emergency-contacts/{id}")
    suspend fun deleteEmergencyContact(@Path("id") id: Long)
}
```

**说明**：紧急联系人管理 API 接口定义是通用的。

**使用场景**：三端都使用相同的紧急联系人管理接口

---

### 7. 通用仓储基类 (`core/repository/CommonRepositories.kt`)

#### AuthRepository
```kotlin
class AuthRepository(authApi: AuthApi, tokenStore: TokenStore) {
    suspend fun login(username, password): Result<LoginResponse>
    suspend fun register(username, password, realName, phone): Result<RegisterResponse>
    suspend fun loginByCard(cardImageBase64): Result<LoginResponse>
    suspend fun logout()
}
```

#### UserRepository
```kotlin
class UserRepository(userApi: UserApi) {
    suspend fun getUserInfo(): Result<UserInfo>
    suspend fun updateUserInfo(realName, phone, address): Result<UserInfo>
}
```

#### EmergencyContactRepository
```kotlin
class EmergencyContactRepository(api: EmergencyContactApi) {
    suspend fun getEmergencyContacts(): Result<List<EmergencyContact>>
    suspend fun addEmergencyContact(...)
    suspend fun updateEmergencyContact(...)
    suspend fun deleteEmergencyContact(...)
}
```

**特点**：
- 统一的错误处理
- 统一的 Result 包装
- 统一的数据转换逻辑

**使用场景**：
- 老人端：直接使用
- 志愿者端：直接使用
- 管理员端：直接使用

---

### 8. 通用 ViewModel 基类 (`core/viewmodel/CommonViewModels.kt`)

#### AuthViewModel
```kotlin
open class AuthViewModel(authRepository: AuthRepository) : ViewModel() {
    val loginState: StateFlow<LoginState>
    val registerState: StateFlow<RegisterState>
    
    fun login(username, password)
    fun register(username, password, realName, phone)
    fun loginByCard(cardImageBase64)
    fun logout()
}
```

#### UserViewModel
```kotlin
open class UserViewModel(userRepository: UserRepository) : ViewModel() {
    val userInfo: StateFlow<UserInfo?>
    val loading: StateFlow<Boolean>
    val error: StateFlow<String?>
    
    fun getUserInfo()
    fun updateUserInfo(realName, phone, address)
    fun clearError()
}
```

#### EmergencyContactViewModel
```kotlin
open class EmergencyContactViewModel(repository: EmergencyContactRepository) : ViewModel() {
    val contacts: StateFlow<List<EmergencyContact>>
    val loading: StateFlow<Boolean>
    val error: StateFlow<String?>
    
    fun getEmergencyContacts()
    fun addEmergencyContact(...)
    fun updateEmergencyContact(...)
    fun deleteEmergencyContact(...)
    fun clearError()
}
```

**特点**：
- 状态管理
- 生命周期感知
- 错误处理

**使用场景**：
- 老人端：直接使用或继承
- 志愿者端：直接使用或继承
- 管理员端：直接使用或继承

---

### 9. 通用 UI 组件 (`ui/component/CommonComponents.kt`)

| 组件 | 功能 | 使用场景 |
|------|------|---------|
| `LoadingIndicator()` | 显示加载中状态 | 所有需要显示加载状态的页面 |
| `ErrorMessage()` | 显示错误信息 | 所有需要显示错误的页面 |
| `StateContainer()` | 封装加载/错误/内容状态 | 简化状态管理 |
| `CardContainer()` | 卡片样式容器 | 内容卡片 |
| `PrimaryButton()` | 主按钮 | 主要操作 |
| `SecondaryButton()` | 次按钮 | 次要操作 |
| `CommonTextField()` | 输入框 | 用户输入 |
| `CommonTopBar()` | 顶部栏 | 页面顶部 |
| `EmptyState()` | 空状态提示 | 没有数据时显示 |
| `ConfirmDialog()` | 确认对话框 | 确认操作 |

**代码示例**：
```kotlin
// 使用通用输入框
CommonTextField(
    value = username,
    onValueChange = { username = it },
    label = "用户名"
)

// 使用通用按钮
PrimaryButton(
    text = "登录",
    onClick = { /* 登录逻辑 */ }
)

// 使用通用错误显示
error?.let {
    ErrorMessage(it) { clearError() }
}
```

**使用场景**：
- 老人端：所有屏幕
- 志愿者端：所有屏幕
- 管理员端：所有屏幕

---

### 10. 工具类 (`core/utils/Utils.kt`)

#### FileUtils
```kotlin
object FileUtils {
    fun fileToBase64(file: File): String
    fun base64ToFile(base64String: String, outputFile: File): Boolean
    fun getMimeType(filename: String): String
}
```

#### DateUtils
```kotlin
object DateUtils {
    fun formatDateTime(timestamp: Long): String
    fun formatDate(timestamp: Long): String
    fun getRelativeTimeDescription(timestamp: Long): String
}
```

#### AudioUtils
```kotlin
object AudioUtils {
    fun pcmToWav(pcmData: ByteArray, sampleRate: Int, channels: Int, sampleBits: Int): ByteArray
}
```

#### ValidationUtils
```kotlin
object ValidationUtils {
    fun isValidPhone(phone: String): Boolean
    fun isValidEmail(email: String): Boolean
    fun isValidUsername(username: String): Boolean
    fun isStrongPassword(password: String): Boolean
}
```

**使用场景**：
- 老人端：文件上传、数据验证、日期格式化、音频转换
- 志愿者端：文件上传、数据验证、日期格式化
- 管理员端：文件上传、数据验证、日期格式化

---

## 🎯 端特定代码

### 老人端特定 (`ElderModels.kt`)

```kotlin
data class Task(...)
data class PublishTaskRequest(...)
data class ConfirmTaskRequest(...)

data class EmergencyAlert(...)
data class TriggerEmergencyRequest(...)

data class AgentProcessRequest(...)
data class AgentProcessResponse(...)

data class Notification(...)
data class PointsInfo(...)
data class ChatHistory(...)
```

### 志愿者端特定

需要定义志愿者专用的数据模型和 API 接口，例如：
- 接单任务相关模型
- 完成任务相关模型
- 志愿者评分相关模型

### 管理员端特定

需要定义管理员专用的数据模型和 API 接口，例如：
- 用户管理模型
- 任务审核模型
- 统计数据模型
- 系统配置模型

---

## 📝 复用指南

### 对于志愿者端开发者

1. **复制这些文件**：
   ```
   ✅ core/api/Response.kt
   ✅ core/api/Models.kt
   ✅ core/api/ApiClient.kt
   ✅ core/api/ApiService.kt (保留前两个 interface)
   ✅ core/repository/CommonRepositories.kt
   ✅ core/viewmodel/CommonViewModels.kt
   ✅ core/utils/Utils.kt
   ✅ ui/component/CommonComponents.kt
   ```

2. **创建自己的实现**：
   ```
   ❌ core/api/ElderModels.kt (创建 VolunteerModels.kt)
   ❌ core/api/ElderApi (创建 VolunteerApi)
   ❌ core/repository/ElderRepository (创建 VolunteerRepository)
   ❌ core/viewmodel/ElderViewModels (创建 VolunteerViewModels)
   ❌ ui/screen/HomeScreen.kt (创建自己的屏幕)
   ```

3. **使用共用 API**：
   ```kotlin
   val authApi = retrofit.create(AuthApi::class.java)
   val userApi = retrofit.create(UserApi::class.java)
   val emergencyContactApi = retrofit.create(EmergencyContactApi::class.java)
   ```

### 对于管理员端开发者

步骤相同，只需将"志愿者"替换为"管理员"。

---

## 🚀 最佳实践

1. **创建 shared 模块** (可选但推荐)：
   ```
   创建一个 Gradle 模块来管理共用代码
   所有三个端都依赖这个模块
   这样便于同步更新
   ```

2. **使用 Git Submodule**：
   ```bash
   git submodule add <shared-repo-url> shared
   ```

3. **定期同步更新**：
   - 在共用代码有更新时，及时通知其他端
   - 制定版本控制策略

4. **编写单元测试**：
   - 为共用代码编写完整的单元测试
   - 确保各端的一致性

5. **文档维护**：
   - 保持 API 文档的一致性
   - 及时更新本文档

---

## 📊 共用代码统计

| 类别 | 文件数 | 代码行数 | 共用程度 |
|------|--------|---------|---------|
| API 接口 | 1 | ~200 | ✅✅✅ |
| 数据模型 | 2 | ~150 | ✅✅✅ |
| 仓储层 | 1 | ~400 | ✅✅✅ |
| ViewModel | 1 | ~300 | ✅✅✅ |
| UI 组件 | 1 | ~300 | ✅✅✅ |
| 工具类 | 1 | ~200 | ✅✅✅ |
| 总计 | 7 | ~1550 | 约 40-50% 代码可复用 |

---

## 🔗 相关文档

- API 文档：`../zealinkly-backend/API文档-老人端.md`
- 项目结构：`README.md`
- 老人端代码：当前项目

## 联系方式

如有问题或建议，请联系项目组。

---

**最后更新**：2026-02-09
**版本**：1.0

