# 智链邻里 - 老人端 API 接口文档

## 目录
- [基础信息](#基础信息)
- [认证授权](#认证授权)
- [首页核心功能](#首页核心功能)
  - [语音输入（推荐）](#语音输入推荐)
  - [文字输入](#文字输入)
  - [手动报警](#手动报警)
- [Agent统一入口](#agent统一入口)
- [语音识别](#语音识别)
- [意图识别](#意图识别)
- [任务管理](#任务管理)
- [紧急报警](#紧急报警)
- [紧急联系人管理](#紧急联系人管理)
- [积分管理](#积分管理)
- [个人信息](#个人信息)
- [通知管理](#通知管理)
- [AI聊天](#ai聊天)
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
- `403`: 权限不足（非老人用户）
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
**POST** `/api/auth/register/elder`

**请求体：**
```json
{
  "username": "zhangsan",
  "password": "123456",
  "realName": "张三",
  "phone": "13800138000"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "zhangsan",
    "userType": "ELDER"
  }
}
```

### 2. 登录
**POST** `/api/auth/login`

**请求体：**
```json
{
  "username": "zhangsan",
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
    "userType": "ELDER",
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
    "userType": "ELDER",
    "userId": 1
  }
}
```

---

## 首页核心功能

首页是老人端的主要入口，提供三种交互方式：语音输入（推荐）、文字输入、手动报警。

### 语音输入（推荐）

**流程：**
1. 用户点击语音按钮，开始录音
2. 录音结束后，调用语音转文字API
3. 将识别出的文字传递给Agent处理API
4. Agent自动识别意图并执行相应操作

**第一步：语音转文字**
**POST** `/api/asr/recognize`

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
    "text": "帮我买点菜"
  }
}
```

**第二步：Agent处理（识别意图并执行）**
**POST** `/api/agent/process`

**请求体：**
```json
{
  "userInput": "帮我买点菜"
}
```

**功能说明：**
- 自动识别用户输入意图（互助任务/紧急报警/AI聊天）
- 如果是互助任务：自动发布任务
- 如果是紧急报警：自动触发紧急报警
- 如果是AI聊天：自动开始AI对话

**响应：**
```json
{
  "code": 200,
  "message": "任务已发布",
  "data": {
    "intentType": "MUTUAL_AID",
    "intentDescription": "互助任务",
    "userInput": "帮我买点菜",
    "tasks": [
      {
        "type": "MUTUAL_AID",
        "typeDescription": "互助任务",
        "description": "买点菜",
        "priority": "MEDIUM"
      }
    ],
    "message": "任务已发布",
    "createdTasks": [
      {
        "id": 1,
        "taskType": "COOPERATION",
        "status": "PENDING",
        "content": "买点菜",
        "pointsReward": 10,
        "createdAt": "2026-02-09T12:00:00+08:00"
      }
    ],
    "aiResponse": null
  }
}
```

**示例场景：**

1. **互助任务：**
   - 语音："帮我买点菜"
   - 识别文字："帮我买点菜"
   - 结果：自动发布互助任务

2. **紧急报警：**
   - 语音："我摔倒了，快来帮我"
   - 识别文字："我摔倒了，快来帮我"
   - 结果：自动触发紧急报警

3. **AI聊天：**
   - 语音："今天天气怎么样"
   - 识别文字："今天天气怎么样"
   - 结果：自动开始AI对话，返回AI回复

### 文字输入

**流程：**
1. 用户输入文字
2. 直接调用Agent处理API
3. Agent自动识别意图并执行相应操作

**API：** `POST /api/agent/process`

**请求体：**
```json
{
  "userInput": "帮我买点菜"
}
```

**响应：** 同语音输入的第二步

### 手动报警

**说明：**
- 首页提供一键报警按钮
- 点击后直接触发紧急报警，无需语音或文字输入

**API：** `POST /api/emergency/trigger`

**请求体：**
```json
{
  "location": "北京市朝阳区xxx街道xxx号"
}
```

**说明：**
- `location`为可选参数，如果不提供则使用老人档案中的地址
- 报警会自动通知管理员和紧急联系人

**响应：**
```json
{
  "code": 200,
  "message": "报警已发出，救援正在路上",
  "data": {
    "id": 1,
    "taskType": "EMERGENCY",
    "status": "PENDING",
    "content": "紧急报警",
    "createdAt": "2026-02-09T14:00:00+08:00"
  }
}
```

---

## Agent统一入口

Agent可以自动识别用户意图并执行相应操作，支持文本和语音两种输入方式。

### 1. Agent文本处理
**POST** `/api/agent/process`

**功能说明：**
- 自动识别用户输入意图（互助任务/紧急报警/AI聊天）
- 如果是互助任务：自动发布任务
- 如果是紧急报警：自动触发紧急报警
- 如果是AI聊天：自动开始AI对话

**请求体：**
```json
{
  "userInput": "帮我买点菜"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "任务已发布",
  "data": {
    "intentType": "MUTUAL_AID",
    "intentDescription": "互助任务",
    "userInput": "帮我买点菜",
    "tasks": [
      {
        "type": "MUTUAL_AID",
        "typeDescription": "互助任务",
        "description": "买点菜",
        "priority": "MEDIUM"
      }
    ],
    "message": "任务已发布",
    "createdTasks": [
      {
        "id": 1,
        "taskType": "COOPERATION",
        "status": "PENDING",
        "content": "买点菜",
        "pointsReward": 10,
        "createdAt": "2026-02-09T12:00:00+08:00"
      }
    ],
    "aiResponse": null
  }
}
```

### 2. Agent语音处理（一步到位）
**POST** `/api/agent/process-voice`

**功能说明：**
- 将语音转文字和Agent处理合并为一步
- 先进行语音识别，将音频转换为文字
- 然后自动识别意图并执行相应操作

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
  "message": "语音识别并处理成功",
  "data": {
    "intentType": "MUTUAL_AID",
    "intentDescription": "互助任务",
    "userInput": "帮我买点菜",
    "tasks": [...],
    "message": "任务已发布",
    "createdTasks": [...],
    "aiResponse": null
  }
}
```

---

## 语音识别

### 语音转文字
**POST** `/api/asr/recognize`

**功能说明：**
- 将语音转换为文字
- 可以与Agent处理API配合使用，实现两步流程

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
    "text": "帮我买点菜"
  }
}
```

---

## 意图识别

### 识别用户意图
**POST** `/api/intent/recognize`

**功能说明：**
- 仅用于识别用户意图，不会执行任何操作
- 返回识别出的意图类型和提取的任务列表

**请求体：**
```json
{
  "userInput": "帮我买点菜"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "意图识别成功",
  "data": {
    "intentType": "MUTUAL_AID",
    "intentDescription": "互助任务",
    "userInput": "帮我买点菜",
    "tasks": [
      {
        "type": "MUTUAL_AID",
        "typeDescription": "互助任务",
        "description": "买点菜",
        "priority": "MEDIUM"
      }
    ]
  }
}
```

---

## 任务管理

### 1. 发布互助任务
**POST** `/api/tasks/cooperation/publish`

**请求体：**
```json
{
  "title": "买点菜",
  "description": "需要买一些蔬菜和水果",
  "pointsReward": 10
}
```

**响应：**
```json
{
  "code": 200,
  "message": "发布成功",
  "data": {
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
  }
}
```

### 2. 查看我发布的任务列表
**GET** `/api/tasks/cooperation/my-as-elder`

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "taskType": "COOPERATION",
      "status": "CLAIMED",
      "volunteer": {
        "id": 2,
        "realName": "李四"
      },
      "content": "买点菜",
      "pointsReward": 10,
      "createdAt": "2026-02-09T12:00:00+08:00"
    }
  ]
}
```

### 3. 查看任务详情
**GET** `/api/tasks/cooperation/{taskId}`

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
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
        "fileUrl": "http://example.com/image.jpg",
        "createdAt": "2026-02-09T13:00:00+08:00"
      }
    ],
    "createdAt": "2026-02-09T12:00:00+08:00"
  }
}
```

### 4. 确认交接（任务完成）
**POST** `/api/tasks/cooperation/{taskId}/confirm`

**说明：**
- 志愿者提交完成并上传凭证后，老人确认交接
- 确认后任务完成，积分自动结算

**响应：**
```json
{
  "code": 200,
  "message": "交接已确认，任务完成",
  "data": {
    "id": 1,
    "status": "COMPLETED",
    ...
  }
}
```

### 5. 提交申诉
**POST** `/api/tasks/cooperation/{taskId}/appeal`

**请求体：**
```json
{
  "content": "志愿者没有完成我的要求"
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

## 紧急报警

### 一键报警
**POST** `/api/emergency/trigger`

**功能说明：**
- 触发紧急报警
- 报警会自动通知管理员和紧急联系人

**请求体：**
```json
{
  "location": "北京市朝阳区xxx街道xxx号"
}
```

**说明：**
- `location`为可选参数，如果不提供则使用老人档案中的地址

**响应：**
```json
{
  "code": 200,
  "message": "报警已发出，救援正在路上",
  "data": {
    "id": 1,
    "taskType": "EMERGENCY",
    "status": "PENDING",
    "content": "紧急报警",
    "createdAt": "2026-02-09T14:00:00+08:00"
  }
}
```

---

## 紧急联系人管理

### 1. 添加紧急联系人
**POST** `/api/emergency-contacts`

**请求体：**
```json
{
  "name": "张小明",
  "relation": "儿子",
  "phone": "13800138001",
  "priority": 1
}
```

**参数说明：**
- `priority`: 优先级（1-最高，数字越大优先级越低）

**响应：**
```json
{
  "code": 200,
  "message": "添加成功",
  "data": {
    "id": 1,
    "name": "张小明",
    "relation": "儿子",
    "phone": "13800138001",
    "priority": 1
  }
}
```

### 2. 获取紧急联系人列表
**GET** `/api/emergency-contacts`

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "张小明",
      "relation": "儿子",
      "phone": "13800138001",
      "priority": 1
    },
    {
      "id": 2,
      "name": "李小红",
      "relation": "女儿",
      "phone": "13800138002",
      "priority": 2
    }
  ]
}
```

### 3. 更新紧急联系人
**PUT** `/api/emergency-contacts/{id}`

**请求体：**
```json
{
  "name": "张小明",
  "relation": "儿子",
  "phone": "13800138001",
  "priority": 1
}
```

### 4. 删除紧急联系人
**DELETE** `/api/emergency-contacts/{id}`

**响应：**
```json
{
  "code": 200,
  "message": "删除成功",
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
      "amount": -200,
      "balanceAfter": 310,
      "reason": "GIFT_EXCHANGE",
      "reasonDescription": "礼品兑换",
      "taskId": null,
      "exchangeId": 1,
      "createdAt": "2026-02-09T15:00:00+08:00"
    }
  ]
}
```

**积分变动原因（reason）：**
- `TASK_REWARD`: 任务奖励
- `TASK_COST`: 任务消耗
- `GIFT_EXCHANGE`: 礼品兑换
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
    "username": "zhangsan",
    "realName": "张三",
    "phone": "13800138000",
    "address": "北京市朝阳区xxx街道xxx号",
    "points": 500,
    "userType": "ELDER"
  }
}
```

### 2. 更新个人信息
**PUT** `/api/user/info`

**请求体：**
```json
{
  "realName": "张三",
  "phone": "13800138000",
  "address": "北京市朝阳区xxx街道xxx号"
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
    "username": "zhangsan",
    "realName": "张三",
    "phone": "13800138000",
    "address": "北京市朝阳区xxx街道xxx号",
    "points": 500,
    "userType": "ELDER"
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
      "title": "任务已被接单",
      "message": "您的任务"买点菜"已被志愿者李四接单",
      "isRead": false,
      "createdAt": "2026-02-09T12:30:00+08:00"
    },
    {
      "id": 2,
      "title": "任务已完成",
      "message": "您的任务"买点菜"已完成，积分已到账",
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

## AI聊天

### 1. 提问
**POST** `/api/ai/ask`

**功能说明：**
- 直接进行AI对话
- 也可以通过Agent统一入口自动识别并处理

**请求体：**
```json
{
  "question": "今天天气怎么样？"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": "今天天气晴朗，温度适宜，适合外出活动。"
}
```

### 2. 查看聊天历史
**GET** `/api/ai/history`

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "question": "今天天气怎么样？",
      "answer": "今天天气晴朗，温度适宜，适合外出活动。",
      "createdAt": "2026-02-09T12:00:00+08:00"
    }
  ]
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

2. **首页功能（主要入口）**
   - **语音输入（推荐）**：
     - 方式一（两步流程）：先调用 `POST /api/asr/recognize` 进行语音转文字，再将文字传递给 `POST /api/agent/process` 处理
     - 方式二（一步到位）：直接调用 `POST /api/agent/process-voice` 处理语音
   - **文字输入**：
     - 直接调用 `POST /api/agent/process` 处理文字输入
   - **手动报警**：
     - 直接调用 `POST /api/emergency/trigger` 触发紧急报警

3. **其他页面功能**
   - 查看我的任务：`GET /api/tasks/cooperation/my-as-elder`
   - 查看积分：`GET /api/points/total` 和 `GET /api/points/history`
   - 查看通知：`GET /api/notifications`
   - 管理紧急联系人：`GET /api/emergency-contacts` 等

### 注意事项

1. **首页是主要入口**，提供语音输入、文字输入、手动报警三种方式
2. **语音输入推荐使用两步流程**（先语音转文字，再Agent处理），更灵活
3. 所有需要认证的接口都需要在请求头中携带JWT Token
4. Agent统一入口会自动识别意图并执行相应操作，是最便捷的方式

---

## 错误码说明

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 400 | 请求参数错误 | 检查请求参数格式和必填项 |
| 401 | 未授权 | 检查Token是否过期，重新登录 |
| 403 | 权限不足 | 确认用户类型为ELDER |
| 404 | 资源不存在 | 检查资源ID是否正确 |
| 500 | 服务器内部错误 | 联系管理员 |

---

## 更新日志

- **2026-02-09**: 初始版本
  - 添加认证授权、首页核心功能（语音输入、文字输入、手动报警）
  - 添加Agent统一入口、语音识别、意图识别
  - 添加任务管理、紧急报警、紧急联系人管理
  - 添加积分管理、个人信息、通知管理
  - 添加AI聊天、文件管理
  - 所有API同等地位，统一按功能模块组织
