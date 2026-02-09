# 智链邻里 - 管理员端 API 接口文档

## 目录
- [基础信息](#基础信息)
- [认证授权](#认证授权)
- [老人管理](#老人管理)
- [志愿者管理](#志愿者管理)
- [任务管理](#任务管理)
- [紧急报警管理](#紧急报警管理)
- [商品管理](#商品管理)
- [兑换管理](#兑换管理)
- [申诉管理](#申诉管理)
- [通知管理](#通知管理)
- [紧急联系人管理](#紧急联系人管理)
- [积分管理](#积分管理)
- [OCR工具](#ocr工具)

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
  "code": 200,           // 状态码：200成功，其他为错误码
  "message": "操作成功",  // 提示信息
  "data": {}             // 响应数据（成功时）
}
```

### 认证方式
所有管理员API都需要在请求头中携带JWT Token：

```
Authorization: Bearer <token>
```

### 分页参数
支持分页的接口使用Spring Data的分页参数：
- `page`: 页码（从0开始，默认0）
- `size`: 每页数量（默认20）
- `sort`: 排序字段（可选，如 `sort=createdAt,desc`）

### 时间格式
所有时间字段使用ISO 8601格式，时区为北京时间（Asia/Shanghai）：
```
2026-02-09T20:00:00+08:00
```

---

## 认证授权

### 1. 管理员注册
**POST** `/api/auth/register/admin`

**请求体：**
```json
{
  "username": "admin001",
  "password": "admin123456",
  "realName": "管理员001"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "admin001",
    "realName": "管理员001"
  }
}
```

### 2. 管理员登录
**POST** `/api/auth/login`

**请求体：**
```json
{
  "username": "admin001",
  "password": "admin123456",
  "userType": "ADMIN"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "userType": "ADMIN",
    "userId": 1,
    "username": "admin001"
  }
}
```

---

## 老人管理

### 1. 获取老人列表（分页）
**GET** `/api/admin/elders`

**查询参数：**
- `enabled`: 是否启用（可选，true/false）
- `page`: 页码（默认0）
- `size`: 每页数量（默认20）

**请求示例：**
```
GET /api/admin/elders?enabled=true&page=0&size=20
```

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": 1,
        "username": "elder001",
        "realName": "张大爷",
        "phone": "13800138000",
        "address": "北京市朝阳区xxx",
        "points": 100,
        "lat": 39.9042,
        "lng": 116.4074,
        "enabled": true,
        "createdAt": "2026-02-09T10:00:00+08:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 5,
    "size": 20,
    "number": 0
  }
}
```

### 2. 获取老人详情
**GET** `/api/admin/elders/{id}`

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "elder001",
    "realName": "张大爷",
    "phone": "13800138000",
    "address": "北京市朝阳区xxx",
    "points": 100,
    "lat": 39.9042,
    "lng": 116.4074,
    "enabled": true,
    "createdAt": "2026-02-09T10:00:00+08:00"
  }
}
```

### 3. 创建老人
**POST** `/api/admin/elders`

**请求体：**
```json
{
  "username": "elder002",
  "password": "elder123456",
  "realName": "李大爷",
  "phone": "13800138001",
  "address": "北京市海淀区xxx",
  "idCardNumber": "110101199001011234",
  "communityCardNumber": "CARD001",
  "lat": 39.9042,
  "lng": 116.4074
}
```

**响应：**
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 2,
    "username": "elder002",
    "realName": "李大爷",
    "phone": "13800138001",
    "address": "北京市海淀区xxx",
    "points": 0,
    "enabled": true,
    "createdAt": "2026-02-09T20:00:00+08:00"
  }
}
```

### 4. 更新老人信息
**PUT** `/api/admin/elders/{id}`

**请求体：**
```json
{
  "realName": "李大爷（更新）",
  "phone": "13800138002",
  "address": "北京市海淀区yyy",
  "points": 200,
  "enabled": true
}
```

**响应：**
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 2,
    "realName": "李大爷（更新）",
    "phone": "13800138002",
    "points": 200
  }
}
```

### 5. 删除老人
**DELETE** `/api/admin/elders/{id}`

**响应：**
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 6. 批量删除老人
**POST** `/api/admin/elders/bulk-delete`

**请求体：**
```json
[1, 2, 3]
```

**响应：**
```json
{
  "code": 200,
  "message": "批量删除成功",
  "data": null
}
```

### 7. 批量导入老人（Excel）
**POST** `/api/admin/elders/bulk-import`

**请求：**
- Content-Type: `multipart/form-data`
- 参数：`file` (Excel文件)

**Excel格式要求：**
- 列：用户名、密码、真实姓名、手机号、地址、身份证号、社区卡号、纬度、经度
- 第一行为表头

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "successCount": 95,
    "failCount": 5,
    "errors": [
      "第3行：用户名已存在",
      "第10行：手机号格式错误"
    ]
  }
}
```

### 8. 禁用老人
**PATCH** `/api/admin/elders/{id}/disable`

**响应：**
```json
{
  "code": 200,
  "message": "已禁用",
  "data": {
    "id": 1,
    "enabled": false
  }
}
```

### 9. 解封老人
**PATCH** `/api/admin/elders/{id}/enable`

**响应：**
```json
{
  "code": 200,
  "message": "已解封",
  "data": {
    "id": 1,
    "enabled": true
  }
}
```

### 10. 给老人发放积分
**POST** `/api/admin/elders/{id}/grant-points`

**请求体：**
```json
{
  "amount": 100
}
```

**响应：**
```json
{
  "code": 200,
  "message": "发放成功",
  "data": {
    "id": 1,
    "points": 200
  }
}
```

---

## 志愿者管理

### 1. 获取志愿者列表（分页）
**GET** `/api/admin/volunteers`

**查询参数：**
- `enabled`: 是否启用（可选）
- `page`: 页码（默认0）
- `size`: 每页数量（默认20）

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": 1,
        "username": "volunteer001",
        "realName": "小王",
        "phone": "13900139000",
        "points": 500,
        "idCardStatus": true,
        "enabled": true,
        "createdAt": "2026-02-09T10:00:00+08:00"
      }
    ],
    "totalElements": 50,
    "totalPages": 3
  }
}
```

### 2. 获取志愿者详情
**GET** `/api/admin/volunteers/{id}`

### 3. 创建志愿者
**POST** `/api/admin/volunteers`

**请求体：**
```json
{
  "username": "volunteer002",
  "password": "volunteer123456",
  "realName": "小李",
  "phone": "13900139001",
  "idCardNumber": "110101199501011234",
  "communityCardNumber": "VCARD001"
}
```

### 4. 更新志愿者信息
**PUT** `/api/admin/volunteers/{id}`

**请求体：**
```json
{
  "realName": "小李（更新）",
  "phone": "13900139002",
  "idCardStatus": true
}
```

### 5. 删除志愿者
**DELETE** `/api/admin/volunteers/{id}`

### 6. 批量删除志愿者
**POST** `/api/admin/volunteers/bulk-delete`

**请求体：**
```json
[1, 2, 3]
```

### 7. 批量导入志愿者（Excel）
**POST** `/api/admin/volunteers/bulk-import`

**请求：**
- Content-Type: `multipart/form-data`
- 参数：`file` (Excel文件)

### 8. 禁用志愿者
**PATCH** `/api/admin/volunteers/{id}/disable`

### 9. 解封志愿者
**PATCH** `/api/admin/volunteers/{id}/enable`

### 10. 给志愿者发放积分
**POST** `/api/admin/volunteers/{id}/grant-points`

**请求体：**
```json
{
  "amount": 100
}
```

---

## 任务管理

### 1. 获取任务列表（分页）
**GET** `/api/admin/tasks`

**查询参数：**
- `volunteerId`: 志愿者ID（可选，用于筛选该志愿者的任务）
- `page`: 页码（默认0）
- `size`: 每页数量（默认20）

**请求示例：**
```
# 获取所有任务
GET /api/admin/tasks?page=0&size=20

# 获取指定志愿者的任务
GET /api/admin/tasks?volunteerId=1&page=0&size=20
```

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": 1,
        "taskType": "COOPERATION",
        "status": "PENDING",
        "elderId": 1,
        "elderName": "张大爷",
        "elderPhone": "13800138000",
        "volunteerId": null,
        "volunteerName": null,
        "content": "需要帮忙买菜",
        "pointsReward": 10,
        "createdAt": "2026-02-09T10:00:00+08:00",
        "updatedAt": "2026-02-09T10:00:00+08:00"
      }
    ],
    "totalElements": 200
  }
}
```

**任务类型（taskType）：**
- `COOPERATION`: 互助任务
- `EMERGENCY`: 紧急报警
- `AI_CHAT`: AI聊天
- `POLICY`: 政策咨询

**任务状态（status）：**
- `PENDING`: 待接单
- `CLAIMED`: 已接单
- `IN_PROGRESS`: 进行中
- `SUBMITTED`: 已提交（等待确认）
- `COMPLETED`: 已完成
- `CANCELLED`: 已取消

### 2. 获取任务详情（含凭证和积分流水）
**GET** `/api/admin/tasks/{id}`

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "task": {
      "id": 1,
      "taskType": "COOPERATION",
      "status": "COMPLETED",
      "elderId": 1,
      "elderName": "张大爷",
      "volunteerId": 1,
      "volunteerName": "小王",
      "content": "需要帮忙买菜",
      "pointsReward": 10,
      "createdAt": "2026-02-09T10:00:00+08:00"
    },
    "evidenceList": [
      {
        "id": 1,
        "evidenceType": "IMAGE",
        "fileUrl": "http://localhost:8080/api/files/image/2026/02/09/xxx.jpg",
        "createdAt": "2026-02-09T11:00:00+08:00"
      }
    ],
    "pointsLedgerList": [
      {
        "id": 1,
        "amount": -10,
        "balanceAfter": 90,
        "reason": "TASK_COST",
        "createdAt": "2026-02-09T10:00:00+08:00"
      },
      {
        "id": 2,
        "amount": 10,
        "balanceAfter": 500,
        "reason": "TASK_REWARD",
        "createdAt": "2026-02-09T12:00:00+08:00"
      }
    ]
  }
}
```

### 3. 更新任务
**PUT** `/api/admin/tasks/{id}`

**请求体：**
```json
{
  "content": "更新后的任务内容",
  "pointsReward": 20,
  "status": "COMPLETED",
  "aiResponse": "管理员备注：任务已完成"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "content": "更新后的任务内容",
    "pointsReward": 20,
    "status": "COMPLETED"
  }
}
```

---

## 紧急报警管理

### 1. 获取待处理报警列表
**GET** `/api/emergency/pending`

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 10,
      "taskType": "EMERGENCY",
      "status": "PENDING",
      "elderId": 1,
      "elderName": "张大爷",
      "elderPhone": "13800138000",
      "content": "紧急报警：老人摔倒",
      "createdAt": "2026-02-09T15:00:00+08:00"
    }
  ]
}
```

### 2. 获取紧急报警详情（含老人信息、紧急联系人、定位）
**GET** `/api/emergency/{id}/detail`

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "task": {
      "id": 10,
      "taskType": "EMERGENCY",
      "status": "PENDING",
      "elderId": 1,
      "elderName": "张大爷",
      "elderPhone": "13800138000",
      "content": "紧急报警：老人摔倒",
      "createdAt": "2026-02-09T15:00:00+08:00"
    },
    "elderInfo": {
      "id": 1,
      "username": "elder001",
      "realName": "张大爷",
      "phone": "13800138000",
      "address": "北京市朝阳区xxx",
      "idCardNumber": "110101199001011234",
      "communityCardNumber": "CARD001"
    },
    "emergencyContacts": [
      {
        "id": 1,
        "name": "张小明",
        "relation": "儿子",
        "phone": "13900139000",
        "priority": 1
      },
      {
        "id": 2,
        "name": "张小红",
        "relation": "女儿",
        "phone": "13900139001",
        "priority": 2
      }
    ],
    "location": {
      "lat": 39.9042,
      "lng": 116.4074,
      "address": "北京市朝阳区xxx街道",
      "displayText": "北京市朝阳区xxx街道（39.9042, 116.4074）"
    }
  }
}
```

### 3. 处理紧急报警
**PATCH** `/api/emergency/{id}/handle`

**请求体：**
```json
{
  "note": "已联系120，正在前往现场"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "处理成功",
  "data": {
    "id": 10,
    "status": "COMPLETED",
    "adminId": 1,
    "adminName": "管理员001"
  }
}
```

---

## 商品管理

### 1. 获取商品列表（分页）
**GET** `/api/admin/products`

**查询参数：**
- `page`: 页码（默认0）
- `size`: 每页数量（默认20）

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "大米10kg",
        "description": "优质大米",
        "pointsPrice": 100,
        "stock": 50,
        "imageUrl": "http://example.com/image.jpg",
        "enabled": true,
        "createdAt": "2026-02-09T10:00:00+08:00",
        "updatedAt": "2026-02-09T10:00:00+08:00"
      }
    ],
    "totalElements": 20
  }
}
```

### 2. 获取已启用商品列表
**GET** `/api/admin/products/enabled`

**查询参数：**
- `page`: 页码（默认0）
- `size`: 每页数量（默认20）

### 3. 获取商品详情
**GET** `/api/admin/products/{id}`

### 4. 创建商品
**POST** `/api/admin/products`

**请求体：**
```json
{
  "name": "大米10kg",
  "description": "优质大米，营养丰富",
  "pointsPrice": 100,
  "stock": 50,
  "imageUrl": "http://example.com/image.jpg"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 1,
    "name": "大米10kg",
    "pointsPrice": 100,
    "stock": 50,
    "enabled": true
  }
}
```

### 5. 更新商品
**PUT** `/api/admin/products/{id}`

**请求体：**
```json
{
  "name": "大米10kg（更新）",
  "pointsPrice": 120,
  "stock": 30,
  "enabled": true
}
```

### 6. 删除商品
**DELETE** `/api/admin/products/{id}`

---

## 兑换管理

### 1. 兑换商品（管理员操作）
**POST** `/api/admin/exchanges/exchange`

**请求体：**
```json
{
  "volunteerId": 1,
  "productId": 1,
  "quantity": 2
}
```

**响应：**
```json
{
  "code": 200,
  "message": "兑换成功",
  "data": {
    "id": 1,
    "volunteerId": 1,
    "volunteerName": "小王",
    "productId": 1,
    "productName": "大米10kg",
    "quantity": 2,
    "pointsCost": 200,
    "adminId": 1,
    "adminName": "管理员001",
    "createdAt": "2026-02-09T20:00:00+08:00"
  }
}
```

### 2. 获取兑换记录列表（分页）
**GET** `/api/admin/exchanges`

**查询参数：**
- `page`: 页码（默认0）
- `size`: 每页数量（默认20）

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": 1,
        "volunteerId": 1,
        "volunteerName": "小王",
        "productId": 1,
        "productName": "大米10kg",
        "quantity": 2,
        "pointsCost": 200,
        "adminId": 1,
        "adminName": "管理员001",
        "createdAt": "2026-02-09T20:00:00+08:00"
      }
    ],
    "totalElements": 50
  }
}
```

### 3. 获取兑换记录详情
**GET** `/api/admin/exchanges/{id}`

### 4. 扫描志愿者卡片（OCR识别，用于兑换时自动填充）
**POST** `/api/admin/exchanges/scan-card`

**请求体：**
```json
{
  "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
  "cardType": "ID_CARD"  // 可选：ID_CARD, COMMUNITY_CARD，不传则自动识别
}
```

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "volunteer001",
    "realName": "小王",
    "phone": "13900139000",
    "points": 500,
    "idCardStatus": true,
    "enabled": true
  }
}
```

---

## 申诉管理

### 1. 获取申诉列表（分页）
**GET** `/api/admin/appeals`

**查询参数：**
- `status`: 状态（可选，PENDING/RESOLVED）
- `page`: 页码（默认0）
- `size`: 每页数量（默认20）

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": 1,
        "taskId": 5,
        "complainantType": "ELDER",
        "complainantId": 1,
        "content": "志愿者没有完成任务",
        "status": "PENDING",
        "adminNote": null,
        "resolvedAt": null,
        "createdAt": "2026-02-09T14:00:00+08:00"
      }
    ],
    "totalElements": 10
  }
}
```

### 2. 获取待处理申诉列表
**GET** `/api/admin/appeals/pending`

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "taskId": 5,
      "content": "志愿者没有完成任务",
      "status": "PENDING",
      "createdAt": "2026-02-09T14:00:00+08:00"
    }
  ]
}
```

### 3. 获取申诉详情
**GET** `/api/admin/appeals/{id}`

### 4. 处理申诉
**PATCH** `/api/admin/appeals/{id}/resolve`

**请求体：**
```json
{
  "adminNote": "已核实，已联系志愿者重新完成任务"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "已处理",
  "data": {
    "id": 1,
    "status": "RESOLVED",
    "adminNote": "已核实，已联系志愿者重新完成任务",
    "resolvedAt": "2026-02-09T16:00:00+08:00"
  }
}
```

---

## 通知管理

### 1. 广播通知
**POST** `/api/admin/notifications/broadcast`

**请求体：**
```json
{
  "targetType": "ALL_ELDERS",  // ALL_ELDERS 或 ALL_VOLUNTEERS
  "title": "系统通知",
  "message": "系统将于今晚22:00进行维护，预计30分钟"
}
```

**响应：**
```json
{
  "code": 200,
  "message": "已发送",
  "data": {
    "sentCount": 150
  }
}
```

---

## 紧急联系人管理

### 1. 为老人添加紧急联系人
**POST** `/api/admin/elders/{elderId}/emergency-contacts`

**请求体：**
```json
{
  "name": "张小明",
  "relation": "儿子",
  "phone": "13900139000",
  "priority": 1
}
```

**响应：**
```json
{
  "code": 200,
  "message": "添加成功",
  "data": {
    "id": 1,
    "name": "张小明",
    "relation": "儿子",
    "phone": "13900139000",
    "priority": 1
  }
}
```

### 2. 获取老人的紧急联系人列表
**GET** `/api/admin/elders/{elderId}/emergency-contacts`

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
      "phone": "13900139000",
      "priority": 1
    },
    {
      "id": 2,
      "name": "张小红",
      "relation": "女儿",
      "phone": "13900139001",
      "priority": 2
    }
  ]
}
```

### 3. 更新紧急联系人
**PUT** `/api/admin/elders/{elderId}/emergency-contacts/{contactId}`

**请求体：**
```json
{
  "name": "张小明（更新）",
  "phone": "13900139002",
  "priority": 1
}
```

### 4. 删除紧急联系人
**DELETE** `/api/admin/elders/{elderId}/emergency-contacts/{contactId}`

---

## 积分管理

### 1. 查看用户的积分总数
**GET** `/api/admin/points/users/{userType}/{userId}/total`

**路径参数：**
- `userType`: 用户类型（ELDER 或 VOLUNTEER）
- `userId`: 用户ID

**请求示例：**
```
GET /api/admin/points/users/VOLUNTEER/1/total
```

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

### 2. 查看用户的积分流水
**GET** `/api/admin/points/users/{userType}/{userId}/history`

**路径参数：**
- `userType`: 用户类型（ELDER 或 VOLUNTEER）
- `userId`: 用户ID

**请求示例：**
```
GET /api/admin/points/users/VOLUNTEER/1/history
```

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

## OCR工具

### 1. 识别卡片号码（仅识别，不查找用户）
**POST** `/api/admin/ocr/recognize`

**请求体：**
```json
{
  "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
  "cardType": "ID_CARD"  // 可选：ID_CARD, COMMUNITY_CARD，不传则自动识别
}
```

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "cardNumber": "110101199001011234",
    "cardType": "ID_CARD"
  }
}
```

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证（Token无效或过期） |
| 403 | 无权限（非管理员） |
| 404 | 资源不存在 |
| 409 | 资源冲突（如用户名已存在） |
| 500 | 服务器内部错误 |

---

## 注意事项

1. **认证要求**：所有管理员API都需要在请求头中携带有效的JWT Token
2. **权限验证**：所有API都会验证用户类型是否为`ADMIN`
3. **时间格式**：所有时间字段使用北京时间（Asia/Shanghai），格式为ISO 8601
4. **分页**：列表接口支持分页，默认每页20条
5. **批量操作**：批量导入支持Excel格式，批量删除需要传递ID数组
6. **文件上传**：批量导入使用`multipart/form-data`格式
7. **Base64编码**：OCR相关接口需要将图片转换为Base64编码（可包含data URI前缀）

---

## 测试建议

1. **使用Postman或类似工具**：导入API集合，设置环境变量（baseUrl、token）
2. **先登录获取Token**：使用管理员账号登录，获取Token后设置到请求头
3. **按模块测试**：建议按模块顺序测试（认证 → 老人管理 → 志愿者管理 → 任务管理等）
4. **注意数据依赖**：某些操作需要先创建相关数据（如兑换需要先有商品和志愿者）

---

**文档版本**: v1.0  
**最后更新**: 2026-02-09  
**维护者**: 后端开发团队
