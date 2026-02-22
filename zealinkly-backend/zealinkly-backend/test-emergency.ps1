# 强制开启 UTF-8 环境
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

$base = 'http://127.0.0.1:8080'
$ErrorActionPreference = 'Stop'

function Do-Request {
    param($Uri, $Method = 'GET', $Headers = $null, $Body = $null)
    try {
        $params = @{ Uri = $Uri; Method = $Method; ContentType = 'application/json' }
        if ($Headers) { $params['Headers'] = $Headers }
        if ($Body -ne $null) { $params['Body'] = $Body }
        Invoke-RestMethod @params
    } catch {
        if ($_.Exception.Response) {
            $status = $_.Exception.Response.StatusCode.value__
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $errBody = $reader.ReadToEnd()
            Write-Host "ERROR $status : $errBody" -ForegroundColor Red
        } else {
            Write-Host "ERROR : $($_.Exception.Message)" -ForegroundColor Red
        }
        throw
    }
}

Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "  测试紧急报警功能" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Yellow

# 步骤 0: 注册用户（如不存在）
Write-Host "[步骤 0] 注册用户（如不存在）..." -ForegroundColor Cyan
$roles = @{ 'elder' = 'elder1'; 'admin' = 'admin1' }
foreach ($role in $roles.Keys) {
    try {
        $u = $roles[$role]
        Do-Request -Uri "$base/api/auth/register/$role" -Method POST -Body (ConvertTo-Json @{ username = $u; password = '123456'; realName = "User_$u" } -Compress) | Out-Null
        Write-Host "  ✓ 注册 $role" -ForegroundColor Green
    } catch {
        Write-Host "  - $role 已存在或注册跳过" -ForegroundColor Gray
    }
}

# 步骤 1: 登录获取 Token
Write-Host "`n[步骤 1] 登录获取 Token..." -ForegroundColor Cyan
$elderLogin = Do-Request -Uri "$base/api/auth/login" -Method POST -Body (ConvertTo-Json @{ username = 'elder1'; password = '123456'; userType = 'ELDER' } -Compress)
$adminLogin = Do-Request -Uri "$base/api/auth/login" -Method POST -Body (ConvertTo-Json @{ username = 'admin1'; password = '123456'; userType = 'ADMIN' } -Compress)

$elderToken = $elderLogin.data.token
$adminToken = $adminLogin.data.token
$emergencyId = ""

Write-Host "  ✓ 登录成功`n" -ForegroundColor Green

# 步骤 2: 老人触发紧急报警
Write-Host "[步骤 2] 老人触发紧急报警..." -ForegroundColor Cyan
$triggerBody = @{ location = "Room 101, Building 5, No.123 Main Street" }
$triggerResult = Do-Request -Uri "$base/api/emergency/trigger" -Method POST -Headers @{ Authorization = "Bearer $elderToken" } -Body (ConvertTo-Json $triggerBody -Compress)
$emergencyId = $triggerResult.data.id
Write-Host "  ✓ 报警已发出, ID: $emergencyId" -ForegroundColor Green
Write-Host "  - 位置: $($triggerResult.data.content)" -ForegroundColor Gray
Write-Host "  - 状态: $($triggerResult.data.status)`n" -ForegroundColor Gray

# 步骤 3: 管理员查看待处理报警列表
Write-Host "[步骤 3] 管理员查看待处理报警列表..." -ForegroundColor Cyan
$pendingList = Do-Request -Uri "$base/api/emergency/pending" -Method GET -Headers @{ Authorization = "Bearer $adminToken" }
Write-Host "  ✓ 找到 $($pendingList.data.Count) 个待处理报警" -ForegroundColor Green
foreach ($emergency in $pendingList.data) {
    Write-Host "  - 报警ID: $($emergency.id), 老人ID: $($emergency.elderId), 位置: $($emergency.content)" -ForegroundColor Gray
}
Write-Host ""

# 步骤 4: 管理员处理报警
Write-Host "[步骤 4] 管理员处理报警..." -ForegroundColor Cyan
$handleBody = @{ note = "Emergency handled, medical team dispatched" }
$handleResult = Do-Request -Uri "$base/api/emergency/$emergencyId/handle" -Method PATCH -Headers @{ Authorization = "Bearer $adminToken" } -Body (ConvertTo-Json $handleBody -Compress)
Write-Host "  ✓ 处理成功" -ForegroundColor Green
Write-Host "  - 处理人ID: $($handleResult.data.adminId)" -ForegroundColor Gray
Write-Host "  - 处理备注: $($handleBody.note)" -ForegroundColor Gray
Write-Host "  - 状态: $($handleResult.data.status)`n" -ForegroundColor Gray

# 步骤 5: 再次查看待处理报警列表（应该为空或减少）
Write-Host "[步骤 5] 再次查看待处理报警列表..." -ForegroundColor Cyan
$pendingList2 = Do-Request -Uri "$base/api/emergency/pending" -Method GET -Headers @{ Authorization = "Bearer $adminToken" }
Write-Host "  ✓ 剩余 $($pendingList2.data.Count) 个待处理报警`n" -ForegroundColor Green

Write-Host "========================================" -ForegroundColor Yellow
Write-Host "  紧急报警功能测试完成！" -ForegroundColor Green
Write-Host "========================================"
