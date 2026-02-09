# 智链邻里 - 志愿者端 API 接口文档

## 目录
- [基础信息](#基础信息)
- [认证授权](#认证授权)
- [任务管理](#任务管理)
- [积分管理](#积分管理)
- [个人信息](#个人信息)
- [通知管理](#通知管理)
- [语音识别](#语音识别)
- [文件管理](#文件管理)

---

## 基础信息

### 基础URL
```
开发环境: http://localhost:8080
生产环境: 待配置
```

### 统一响应格式
所有API响应都遵循以下格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

**状态码说明：**
- `200`: 操作成功
- `400`: 请求参数错误
- `401`: 未授权（未登录或token过期）
- `403`: 权限不足（非志愿者用户）
- `404`: 资源不存在
- `500`: 服务器内部错误

### 认证方式
所有需要认证的接口都需要在请求头中携带JWT Token：

```
Authorization: Bearer {token}
```

Token通过登录接口获取。

---

## 认证授权

### 1. 注册
**POST** `/api/auth/register/volunteer`

**请求体：**
```json
{
  "username": "lisi",
  "password": "123456",
  "realName": "李四",
  "phone": "13800138001"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "lisi",
    "userType": "VOLUNTEER"
  }
}
```

### 2. 登录
**POST** `/api/auth/login`

**请求体：**
```json
{
  "username": "lisi",
  "password": "123456"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userType": "VOLUNTEER",
    "userId": 1
  }
}
```

### 3. 卡片登录（OCR识别身份证或社区卡）
**POST** `/api/auth/login-by-card`

**请求体：**
```json
{
  "cardImageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRg..."
}
```

**说明：**
- 支持身份证和社区卡识别
- 图片需要Base64编码，包含data URI前缀（如：`data:image/jpeg;base64,`）

**响应：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userType": "VOLUNTEER",
    "userId": 1
  }
}
```

---

## 任务管理

### 1. 查看可接任务列表（任务大厅）
**GET** `/api/tasks/cooperation/available`

**功能说明：**
- 查看所有待接取的互助任务
- 显示任务的详细信息，包括任务内容、积分奖励等

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "taskType": "COOPERATION",
      "status": "PENDING",
      "elder": {
        "id": 1,
        "realName": "张三"
      },
      "content": "买点菜\n需要买一些蔬菜和水果",
      "pointsReward": 10,
      "createdAt": "2026-02-09T12:00:00+08:00"
    },
    {
      "id": 2,
      "taskType": "COOPERATION",
      "status": "PENDING",
      "elder": {
        "id": 2,
        "realName": "王五"
      },
      "content": "送东西到xxx地址",
      "pointsReward": 20,
      "createdAt": "2026-02-09T13:00:00+08:00"
    }
  ]
}
```

### 2. 接单
**POST** `/api/tasks/cooperation/{taskId}/accept`

**功能说明：**
- 志愿者接取任务
- 接单后任务状态变为`CLAIMED`（已接单）

**响应：**
```json
{
  "code": 200,
  "message": "接单成功",
  "data": {
    "id": 1,
    "taskType": "COOPERATION",
    "status": "CLAIMED",
    "elder": {
      "id": 1,
      "realName": "张三"
    },
    "volunteer": {
      "id": 2,
      "realName": "李四"
    },
    "content": "买点菜",
    "pointsReward": 10,
    "createdAt": "2026-02-09T12:00:00+08:00"
  }
}
```

### 3. 开始服务（可选）
**POST** `/api/tasks/cooperation/{taskId}/start`

**功能说明：**
- 志愿者开始执行任务
- 任务状态变为`IN_PROGRESS`（进行中）
- 此步骤为可选，可以直接接单后提交完成

**响应：**
```json
{
  "code": 200,
  "message": "已开始服务",
  "data": {
    "id": 1,
    "status": "IN_PROGRESS",
    ...
  }
}
```

### 4. 提交完成（上传凭证）
**POST** `/api/tasks/cooperation/{taskId}/submit`

**功能说明：**
- 志愿者完成任务后提交完成
- 需要上传凭证（图片等）
- 提交后任务状态变为`SUBMITTED`（已提交），等待老人确认

**请求体：**
```json
{
  "note": "已完成购买，蔬菜和水果都已送到",
  "evidences": [
    "http://localhost:8080/api/files/image/2026/02/09/xxx.jpg",
    "http://localhost:8080/api/files/image/2026/02/09/yyy.jpg"
  ]
}
```

**参数说明：**
- `note`: 完成说明（可选）
- `evidences`: 凭证文件URL列表（可选），需要先通过文件上传API上传文件

**响应：**
```json
{
  "code": 200,
  "message": "已提交完成，等待老人确认交接",
  "data": {
    "id": 1,
    "taskType": "COOPERATION",
    "status": "SUBMITTED",
    "elder": {...},
    "volunteer": {...},
    "content": "买点菜",
    "pointsReward": 10,
    "evidences": [
      {
        "id": 1,
        "evidenceType": "IMAGE",
        "fileUrl": "http://localhost:8080/api/files/image/2026/02/09/xxx.jpg",
        "createdAt": "2026-02-09T13:00:00+08:00"
      }
    ],
    "createdAt": "2026-02-09T12:00:00+08:00"
  }
}
```

### 5. 查看我接下的任务列表
**GET** `/api/tasks/cooperation/my-as-volunteer`

**功能说明：**
- 查看志愿者自己接下的所有任务
- 包括不同状态的任务（已接单、进行中、已提交、已完成等）

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "taskType": "COOPERATION",
      "status": "SUBMITTED",
      "elder": {
        "id": 1,
        "realName": "张三"
      },
      "content": "买点菜",
      "pointsReward": 10,
      "createdAt": "2026-02-09T12:00:00+08:00"
    },
    {
      "id": 2,
      "taskType": "COOPERATION",
      "status": "COMPLETED",
      "elder": {
        "id": 2,
        "realName": "王五"
      },
      "content": "送东西到xxx地址",
      "pointsReward": 20,
      "createdAt": "2026-02-09T13:00:00+08:00"
    }
  ]
}
```

### 6. 查看任务详情
**GET** `/api/tasks/cooperation/{taskId}`

**功能说明：**
- 查看任务的详细信息
- 包括任务内容、老人信息、凭证、任务状态等
- 只有接单的志愿者或发布任务的老人可以查看

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "taskType": "COOPERATION",
    "status": "SUBMITTED",
    "elder": {
      "id": 1,
      "realName": "张三",
      "phone": "13800138000",
      "address": "北京市朝阳区xxx街道xxx号"
    },
    "volunteer": {
      "id": 2,
      "realName": "李四"
    },
    "content": "买点菜\n需要买一些蔬菜和水果",
    "pointsReward": 10,
    "evidences": [
      {
        "id": 1,
        "evidenceType": "IMAGE",
        "fileUrl": "http://localhost:8080/api/files/image/2026/02/09/xxx.jpg",
        "createdAt": "2026-02-09T13:00:00+08:00"
      }
    ],
    "createdAt": "2026-02-09T12:00:00+08:00"
  }
}
```

### 7. 提交申诉
**POST** `/api/tasks/cooperation/{taskId}/appeal`

**功能说明：**
- 针对任务提交申诉
- 申诉会提交给管理员处理

**请求体：**
```json
{
  "content": "老人要求不合理，无法完成"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "申诉已提交，请等待管理员处理",
  "data": null
}
```

---

## 积分管理

### 1. 查看积分总数
**GET** `/api/points/total`

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 500
  }
}
```

### 2. 查看积分流水
**GET** `/api/points/history`

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "amount": 10,
      "balanceAfter": 510,
      "reason": "TASK_REWARD",
      "reasonDescription": "任务奖励",
      "taskId": 5,
      "exchangeId": null,
      "createdAt": "2026-02-09T12:00:00+08:00"
    },
    {
      "id": 2,
      "amount": 20,
      "balanceAfter": 530,
      "reason": "TASK_REWARD",
      "reasonDescription": "任务奖励",
      "taskId": 6,
      "exchangeId": null,
      "createdAt": "2026-02-09T15:00:00+08:00"
    }
  ]
}
```

**积分变动原因（reason）：**
- `TASK_REWARD`: 任务奖励（完成任务后获得）
- `TASK_COST`: 任务消耗（接取任务时扣除，如果任务未完成会退回）
- `GIFT_EXCHANGE`: 礼品兑换（在管理员办公室兑换商品时扣除）
- `ADJUSTMENT`: 管理员调整
- `MONTHLY_GRANT`: 月度发放
- `ADMIN_GRANT`: 管理员发放

---

## 个人信息

### 1. 获取个人信息
**GET** `/api/user/info`

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "lisi",
    "realName": "李四",
    "phone": "13800138001",
    "points": 500,
    "userType": "VOLUNTEER"
  }
}
```

### 2. 更新个人信息
**PUT** `/api/user/info`

**请求体：**
```json
{
  "realName": "李四",
  "phone": "13800138001"
}
```

**说明：**
- 所有字段都是可选的，只传需要更新的字段

**响应：**
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "username": "lisi",
    "realName": "李四",
    "phone": "13800138001",
    "points": 500,
    "userType": "VOLUNTEER"
  }
}
```

---

## 通知管理

### 1. 获取通知列表
**GET** `/api/notifications`

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "title": "新任务发布",
      "message": "老人张三发布了新任务"买点菜"",
      "isRead": false,
      "createdAt": "2026-02-09T12:00:00+08:00"
    },
    {
      "id": 2,
      "title": "任务已完成",
      "message": "您接取的任务"买点菜"已完成，积分已到账",
      "isRead": true,
      "createdAt": "2026-02-09T13:00:00+08:00"
    }
  ]
}
```

### 2. 获取未读通知数量
**GET** `/api/notifications/unread-count`

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "unreadCount": 1
  }
}
```

### 3. 标记单条通知为已读
**PATCH** `/api/notifications/{id}/read`

**响应：**
```json
{
  "code": 200,
  "message": "已标记为已读",
  "data": null
}
```

### 4. 标记所有通知为已读
**PATCH** `/api/notifications/read-all`

**响应：**
```json
{
  "code": 200,
  "message": "已全部标记为已读",
  "data": null
}
```

---

## 语音识别

### 语音转文字
**POST** `/api/asr/recognize`

**功能说明：**
- 将语音转换为文字
- 可用于语音输入等场景

**请求体：**
```json
{
  "audioBase64": "UklGRiQAAABXQVZFZm10...",
  "format": "wav",
  "rate": 16000
}
```

**参数说明：**
- `audioBase64`: 音频文件的Base64编码（不包含data URI前缀）
- `format`: 音频格式，默认`wav`（支持：wav, pcm）
- `rate`: 采样率，默认`16000`（支持：16000, 8000）

**音频格式要求：**
- 格式：PCM WAV
- 采样率：16kHz 或 8kHz
- 声道：单声道（Mono）
- 位深：16位

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "text": "我想接取这个任务"
  }
}
```

---

## 文件管理

### 1. 上传文件（MultipartFile）
**POST** `/api/files/upload`

**说明：**
- 用于上传任务凭证、头像等文件
- 支持图片、音频、文档等格式

**请求：**
```
Content-Type: multipart/form-data

file: [文件]
relatedType: TASK (可选)
relatedId: 1 (可选)
```

**响应：**
```json
{
  "code": 200,
  "message": "文件上传成功",
  "data": {
    "id": 1,
    "fileUrl": "http://localhost:8080/api/files/image/2026/02/09/xxx.jpg",
    "originalFilename": "photo.jpg",
    "fileSize": 102400,
    "contentType": "image/jpeg"
  }
}
```

### 2. 上传文件（Base64）
**POST** `/api/files/upload-base64`

**请求体：**
```json
{
  "base64Data": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
  "filename": "photo.jpg",
  "contentType": "image/jpeg",
  "relatedType": "TASK",
  "relatedId": 1
}
```

### 3. 获取文件
**GET** `/api/files/{type}/{year}/{month}/{day}/{filename}`

**说明：**
- 公开访问，无需认证
- 用于显示图片、播放音频等

**示例：**
```
GET /api/files/image/2026/02/09/1707465600000_abc123.jpg
```

### 4. 获取我的文件列表
**GET** `/api/files/my`

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "fileUrl": "http://localhost:8080/api/files/image/2026/02/09/xxx.jpg",
      "originalFilename": "photo.jpg",
      "fileSize": 102400,
      "contentType": "image/jpeg",
      "fileType": "IMAGE",
      "createdAt": "2026-02-09T12:00:00+08:00"
    }
  ]
}
```

### 5. 删除文件
**DELETE** `/api/files/{fileId}`

**说明：**
- 只能删除自己上传的文件

**响应：**
```json
{
  "code": 200,
  "message": "文件删除成功",
  "data": null
}
```

---

## 使用建议

### 推荐流程

1. **用户登录**
   - 使用卡片登录（OCR）或账号密码登录

2. **查看和接取任务**
   - 查看可接任务列表：`GET /api/tasks/cooperation/available`
   - 接取任务：`POST /api/tasks/cooperation/{taskId}/accept`
   - （可选）开始服务：`POST /api/tasks/cooperation/{taskId}/start`

3. **完成任务**
   - 上传凭证文件：`POST /api/files/upload`
   - 提交完成：`POST /api/tasks/cooperation/{taskId}/submit`（包含凭证URL）
   - 等待老人确认交接

4. **查看我的任务**
   - 查看我接下的任务列表：`GET /api/tasks/cooperation/my-as-volunteer`
   - 查看任务详情：`GET /api/tasks/cooperation/{taskId}`

5. **查看积分和通知**
   - 查看积分：`GET /api/points/total` 和 `GET /api/points/history`
   - 查看通知：`GET /api/notifications`

### 注意事项

1. **任务流程**：接单 → （可选）开始服务 → 提交完成（上传凭证） → 等待老人确认 → 任务完成，积分到账
2. **凭证上传**：提交完成前需要先上传凭证文件，然后在提交完成时传入文件URL
3. **积分获取**：任务完成后，积分会自动到账，可以在积分流水中查看
4. 所有需要认证的接口都需要在请求头中携带JWT Token

---

## 错误码说明

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 400 | 请求参数错误 | 检查请求参数格式和必填项 |
| 401 | 未授权 | 检查Token是否过期，重新登录 |
| 403 | 权限不足 | 确认用户类型为VOLUNTEER |
| 404 | 资源不存在 | 检查资源ID是否正确 |
| 500 | 服务器内部错误 | 联系管理员 |

---

## 更新日志

- **2026-02-09**: 初始版本
  - 添加认证授权（注册、登录、卡片登录）
  - 添加任务管理（查看任务、接单、开始服务、提交完成、查看我的任务、申诉）
  - 添加积分管理（查看积分总数和流水）
  - 添加个人信息、通知管理
  - 添加语音识别、文件管理
