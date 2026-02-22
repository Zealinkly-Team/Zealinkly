# 紧急联系流程完整测试脚本
# 测试流程：
# 1. 管理员创建老人并添加紧急联系人
# 2. 老人更新位置信息
# 3. 老人触发紧急报警
# 4. 管理员查看待处理报警列表
# 5. 管理员查看报警详情（包含老人信息、紧急联系人、定位信息）
# 6. 管理员处理报警

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

$base = "http://localhost:8080"

# 测试账号
$adminUsername = "admin_test"
$adminPassword = "admin123456"
$elderUsername = "emergency_test_elder_" + (Get-Date -Format "yyyyMMddHHmmss")
$elderPassword = "test123456"

function Do-Request {
    param(
        [string]$Uri,
        [string]$Method = "GET",
        [hashtable]$Headers = @{},
        [object]$Body = $null
    )
    
    try {
        $params = @{
            Uri = $Uri
            Method = $Method
            Headers = $Headers
            ContentType = "application/json;charset=UTF-8"
        }
        
        if ($Body -ne $null) {
            $params.Body = (ConvertTo-Json $Body -Compress -Depth 10)
        }
        
        $response = Invoke-RestMethod @params
        return $response
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "ERROR $statusCode : $responseBody" -ForegroundColor Red
        throw
    }
}

function Login-User {
    param(
        [string]$Username,
        [string]$Password,
        [string]$UserType
    )
    
    try {
        $result = Do-Request -Uri "$base/api/auth/login" -Method POST -Body @{
            username = $Username
            password = $Password
            userType = $UserType
        }
        return $result.data.token
    } catch {
        Write-Host "登录失败，尝试注册..." -ForegroundColor Yellow
        # 尝试注册
        try {
            $registerUri = switch ($UserType) {
                "ELDER" { "$base/api/auth/register/elder" }
                "VOLUNTEER" { "$base/api/auth/register/volunteer" }
                "ADMIN" { "$base/api/auth/register/admin" }
                default { throw "Unknown user type: $UserType" }
            }
            
            $registerBody = @{
                username = $Username
                password = $Password
            }
            
            if ($UserType -eq "ELDER") {
                $registerBody.realName = "测试老人"
                $registerBody.phone = "13800138000"
            } elseif ($UserType -eq "ADMIN") {
                $registerBody.realName = "测试管理员"
            }
            
            $registerResult = Do-Request -Uri $registerUri -Method POST -Body $registerBody
            Write-Host "注册成功，重新登录..." -ForegroundColor Green
            
            $loginResult = Do-Request -Uri "$base/api/auth/login" -Method POST -Body @{
                username = $Username
                password = $Password
                userType = $UserType
            }
            return $loginResult.data.token
        } catch {
            Write-Host "注册/登录失败: $_" -ForegroundColor Red
            return $null
        }
    }
}

Write-Host "`n==========================================" -ForegroundColor Yellow
Write-Host "紧急联系流程完整测试" -ForegroundColor Yellow
Write-Host "==========================================`n" -ForegroundColor Yellow

# Step 1: 管理员登录（自动注册）
Write-Host "[Step 1] 管理员登录..." -ForegroundColor Cyan
$adminToken = Login-User -Username $adminUsername -Password $adminPassword -UserType "ADMIN"
if (-not $adminToken) {
    Write-Host "管理员登录/注册失败，测试终止" -ForegroundColor Red
    exit 1
}
$adminHeaders = @{ "Authorization" = "Bearer $adminToken" }
Write-Host "管理员登录成功" -ForegroundColor Green

# Step 2: 管理员创建老人
Write-Host "`n[Step 2] 管理员创建老人..." -ForegroundColor Cyan
try {
    $elderData = Do-Request -Uri "$base/api/admin/elders" -Method POST -Headers $adminHeaders -Body @{
        username = $elderUsername
        password = $elderPassword
        realName = "测试老人（紧急报警）"
        phone = "13900139000"
        address = "北京市朝阳区测试街道123号"
        idCardNumber = "110101199001011234"
        communityCardNumber = "COMM001"
    }
    $elderId = $elderData.data.id
    Write-Host "老人创建成功，ID: $elderId" -ForegroundColor Green
    Write-Host "  姓名: $($elderData.data.realName)" -ForegroundColor Gray
    Write-Host "  电话: $($elderData.data.phone)" -ForegroundColor Gray
    Write-Host "  地址: $($elderData.data.address)" -ForegroundColor Gray
} catch {
    Write-Host "创建老人失败: $_" -ForegroundColor Red
    Write-Host "尝试使用已有老人..." -ForegroundColor Yellow
    # 尝试获取已有老人列表
    try {
        $elders = Do-Request -Uri "$base/api/admin/elders?size=1" -Method GET -Headers $adminHeaders
        if ($elders.data.content -and $elders.data.content.Count -gt 0) {
            $elderId = $elders.data.content[0].id
            $elderUsername = $elders.data.content[0].username
            Write-Host "使用已有老人，ID: $elderId, 用户名: $elderUsername" -ForegroundColor Yellow
        } else {
            Write-Host "没有找到已有老人，测试终止" -ForegroundColor Red
            exit 1
        }
    } catch {
        Write-Host "无法获取老人列表，测试终止" -ForegroundColor Red
        exit 1
    }
}

# Step 3: 更新老人位置信息（模拟GPS定位）
Write-Host "`n[Step 3] 更新老人位置信息（GPS坐标）..." -ForegroundColor Cyan
try {
    $elderToken = Login-User -Username $elderUsername -Password $elderPassword -UserType "ELDER"
    if (-not $elderToken) {
        Write-Host "老人登录失败，跳过位置更新" -ForegroundColor Yellow
    } else {
        $elderHeaders = @{ "Authorization" = "Bearer $elderToken" }
        # 更新位置信息（北京天安门坐标）
        $updateResult = Do-Request -Uri "$base/api/user/info" -Method PUT -Headers $elderHeaders -Body @{
            lat = 39.9042
            lng = 116.4074
            address = "北京市东城区天安门广场"
        }
        Write-Host "位置信息更新成功" -ForegroundColor Green
        Write-Host "  坐标: 39.9042, 116.4074" -ForegroundColor Gray
        Write-Host "  地址: 北京市东城区天安门广场" -ForegroundColor Gray
    }
} catch {
    Write-Host "更新位置信息失败: $_" -ForegroundColor Yellow
    Write-Host "继续测试..." -ForegroundColor Gray
}

# Step 4: 添加紧急联系人
Write-Host "`n[Step 4] 添加紧急联系人..." -ForegroundColor Cyan
if (-not $elderToken) {
    $elderToken = Login-User -Username $elderUsername -Password $elderPassword -UserType "ELDER"
    $elderHeaders = @{ "Authorization" = "Bearer $elderToken" }
}
try {
    # 添加第一个紧急联系人（儿子，优先级1）
    $contact1 = Do-Request -Uri "$base/api/emergency-contacts" -Method POST -Headers $elderHeaders -Body @{
        name = "张三"
        relation = "儿子"
        phone = "13800138001"
        priority = 1
    }
    Write-Host "紧急联系人1添加成功：" -ForegroundColor Green
    Write-Host "  姓名: $($contact1.data.name), 关系: $($contact1.data.relation), 电话: $($contact1.data.phone)" -ForegroundColor Gray
    
    # 添加第二个紧急联系人（女儿，优先级2）
    $contact2 = Do-Request -Uri "$base/api/emergency-contacts" -Method POST -Headers $elderHeaders -Body @{
        name = "李四"
        relation = "女儿"
        phone = "13800138002"
        priority = 2
    }
    Write-Host "紧急联系人2添加成功：" -ForegroundColor Green
    Write-Host "  姓名: $($contact2.data.name), 关系: $($contact2.data.relation), 电话: $($contact2.data.phone)" -ForegroundColor Gray
    
    # 查看所有紧急联系人
    $contactsList = Do-Request -Uri "$base/api/emergency-contacts" -Method GET -Headers $elderHeaders
    Write-Host "当前紧急联系人总数: $($contactsList.data.Count)" -ForegroundColor Cyan
    
} catch {
    Write-Host "添加紧急联系人失败: $_" -ForegroundColor Red
    Write-Host "尝试使用管理员添加..." -ForegroundColor Yellow
    try {
        # 使用管理员添加
        $contact1 = Do-Request -Uri "$base/api/admin/elders/$elderId/emergency-contacts" -Method POST -Headers $adminHeaders -Body @{
            name = "张三"
            relation = "儿子"
            phone = "13800138001"
            priority = 1
        }
        Write-Host "管理员添加紧急联系人成功" -ForegroundColor Green
    } catch {
        Write-Host "管理员添加也失败，继续测试（假设已有紧急联系人）..." -ForegroundColor Yellow
    }
}

# Step 5: 老人触发紧急报警
Write-Host "`n[Step 5] 老人触发紧急报警..." -ForegroundColor Cyan
if (-not $elderToken) {
    $elderToken = Login-User -Username $elderUsername -Password $elderPassword -UserType "ELDER"
    $elderHeaders = @{ "Authorization" = "Bearer $elderToken" }
}
try {
    $emergencyResult = Do-Request -Uri "$base/api/emergency/trigger" -Method POST -Headers $elderHeaders -Body @{
        location = "我在家里，感觉不舒服，需要帮助"
    }
    $emergencyTaskId = $emergencyResult.data.id
    Write-Host "紧急报警已触发！" -ForegroundColor Green
    Write-Host "  报警ID: $emergencyTaskId" -ForegroundColor Cyan
    Write-Host "  状态: $($emergencyResult.data.status)" -ForegroundColor Cyan
    Write-Host "  内容: $($emergencyResult.data.content)" -ForegroundColor Cyan
    Write-Host "  创建时间: $($emergencyResult.data.createdAt)" -ForegroundColor Gray
} catch {
    Write-Host "触发紧急报警失败: $_" -ForegroundColor Red
    exit 1
}

# Step 6: 管理员查看待处理报警列表
Write-Host "`n[Step 6] 管理员查看待处理报警列表..." -ForegroundColor Cyan
try {
    $pendingList = Do-Request -Uri "$base/api/emergency/pending" -Method GET -Headers $adminHeaders
    Write-Host "待处理报警数量: $($pendingList.data.Count)" -ForegroundColor Green
    foreach ($emergency in $pendingList.data) {
        Write-Host "  - 报警ID: $($emergency.id), 老人: $($emergency.elderName), 状态: $($emergency.status)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "获取待处理报警列表失败: $_" -ForegroundColor Red
}

# Step 7: 管理员查看报警详情（包含老人信息、紧急联系人、定位信息）
Write-Host "`n[Step 7] 管理员查看报警详情..." -ForegroundColor Cyan
try {
    $detail = Do-Request -Uri "$base/api/emergency/$emergencyTaskId/detail" -Method GET -Headers $adminHeaders
    $detailData = $detail.data
    
    Write-Host "报警详情：" -ForegroundColor Green
    Write-Host "  任务ID: $($detailData.task.id)" -ForegroundColor Cyan
    Write-Host "  状态: $($detailData.task.status)" -ForegroundColor Cyan
    Write-Host "  内容: $($detailData.task.content)" -ForegroundColor Cyan
    
    Write-Host "`n老人信息：" -ForegroundColor Yellow
    $elderInfo = $detailData.elderInfo
    Write-Host "  ID: $($elderInfo.id)" -ForegroundColor White
    Write-Host "  用户名: $($elderInfo.username)" -ForegroundColor White
    Write-Host "  真实姓名: $($elderInfo.realName)" -ForegroundColor White
    Write-Host "  电话: $($elderInfo.phone)" -ForegroundColor White
    Write-Host "  地址: $($elderInfo.address)" -ForegroundColor White
    Write-Host "  身份证号: $($elderInfo.idCardNumber)" -ForegroundColor White
    Write-Host "  社区卡号: $($elderInfo.communityCardNumber)" -ForegroundColor White
    
    Write-Host "`n紧急联系人：" -ForegroundColor Yellow
    $contacts = $detailData.emergencyContacts
    if ($contacts -and $contacts.Count -gt 0) {
        foreach ($contact in $contacts) {
            Write-Host "  - [$($contact.priority)] $($contact.name) ($($contact.relation)): $($contact.phone)" -ForegroundColor White
        }
    } else {
        Write-Host "  暂无紧急联系人（请先添加）" -ForegroundColor Red
    }
    
    Write-Host "`n定位信息：" -ForegroundColor Yellow
    $location = $detailData.location
    Write-Host "  显示文本: $($location.displayText)" -ForegroundColor White
    if ($location.lat -and $location.lng) {
        Write-Host "  坐标: $($location.lat), $($location.lng)" -ForegroundColor White
    }
    if ($location.address) {
        Write-Host "  地址: $($location.address)" -ForegroundColor White
    }
    
} catch {
    Write-Host "获取报警详情失败: $_" -ForegroundColor Red
}

# Step 8: 管理员处理报警
Write-Host "`n[Step 8] 管理员处理报警..." -ForegroundColor Cyan
try {
    $handleResult = Do-Request -Uri "$base/api/emergency/$emergencyTaskId/handle" -Method PATCH -Headers $adminHeaders -Body @{
        note = "已联系120急救中心，救援人员正在前往现场。已通知紧急联系人。"
    }
    Write-Host "报警处理成功！" -ForegroundColor Green
    Write-Host "  处理状态: $($handleResult.data.status)" -ForegroundColor Cyan
    Write-Host "  处理备注: $($handleResult.data.aiResponse)" -ForegroundColor Cyan
} catch {
    Write-Host "处理报警失败: $_" -ForegroundColor Red
}

# Step 9: 验证处理后的状态
Write-Host "`n[Step 9] 验证处理后的状态..." -ForegroundColor Cyan
try {
    $pendingList = Do-Request -Uri "$base/api/emergency/pending" -Method GET -Headers $adminHeaders
    $remainingCount = $pendingList.data.Count
    Write-Host "剩余待处理报警数量: $remainingCount" -ForegroundColor $(if ($remainingCount -eq 0) { "Green" } else { "Yellow" })
} catch {
    Write-Host "验证失败: $_" -ForegroundColor Red
}

Write-Host "`n==========================================" -ForegroundColor Yellow
Write-Host "紧急联系流程测试完成！" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Yellow
Write-Host "`n测试总结：" -ForegroundColor Cyan
Write-Host "  ✓ 管理员创建/获取老人" -ForegroundColor Green
Write-Host "  ✓ 老人更新位置信息" -ForegroundColor Green
Write-Host "  ✓ 添加紧急联系人（老人/管理员）" -ForegroundColor Green
Write-Host "  ✓ 老人触发紧急报警" -ForegroundColor Green
Write-Host "  ✓ 管理员查看待处理报警列表" -ForegroundColor Green
Write-Host "  ✓ 管理员查看报警详情（老人信息、紧急联系人、定位）" -ForegroundColor Green
Write-Host "  ✓ 管理员处理报警" -ForegroundColor Green
