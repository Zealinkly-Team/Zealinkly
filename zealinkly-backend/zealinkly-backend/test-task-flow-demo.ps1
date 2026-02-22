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
Write-Host "  模拟完整任务发布流程" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Yellow

# 步骤 0: 注册
Write-Host "[步骤 0] 注册用户（如不存在）..." -ForegroundColor Cyan
$roles = @{ 'elder' = 'elder1'; 'volunteer' = 'vol1'; 'admin' = 'admin1' }
foreach ($role in $roles.Keys) {
    try {
        $u = $roles[$role]
        Do-Request -Uri "$base/api/auth/register/$role" -Method POST -Body (ConvertTo-Json @{ username = $u; password = '123456'; realName = "User_$u" } -Compress) | Out-Null
        Write-Host "  ✓ 注册 $role" -ForegroundColor Green
    } catch {
        Write-Host "  - $role 已存在或注册跳过" -ForegroundColor Gray
    }
}

# 步骤 0.5: 登录
Write-Host "`n[步骤 0.5] 登录获取 Token..." -ForegroundColor Cyan
$loginUri = "$base/api/auth/login"
$elderLogin = Do-Request -Uri $loginUri -Method POST -Body (ConvertTo-Json @{ username = 'elder1'; password = '123456'; userType = 'ELDER' } -Compress)
$volLogin = Do-Request -Uri $loginUri -Method POST -Body (ConvertTo-Json @{ username = 'vol1'; password = '123456'; userType = 'VOLUNTEER' } -Compress)
$adminLogin = Do-Request -Uri $loginUri -Method POST -Body (ConvertTo-Json @{ username = 'admin1'; password = '123456'; userType = 'ADMIN' } -Compress)

$elderToken = $elderLogin.data.token
$volToken = $volLogin.data.token
$adminToken = $adminLogin.data.token
$elderId = $elderLogin.data.userId
$taskId = ""

Write-Host "  ✓ 登录成功`n" -ForegroundColor Green

# 步骤 0.6: 管理员给老人发放积分
Write-Host "[步骤 0.6] 管理员给老人发放积分..." -ForegroundColor Cyan
$grantBody = @{ amount = 100 }
Do-Request -Uri "$base/api/admin/elders/$elderId/grant-points" -Method POST -Headers @{ Authorization = "Bearer $adminToken" } -Body (ConvertTo-Json $grantBody -Compress) | Out-Null
Write-Host "  ✓ 发放成功（100积分）`n" -ForegroundColor Green

# 步骤 1: 老人发布任务
Write-Host "[步骤 1] 老人发布任务..." -ForegroundColor Cyan
$pubBody = @{ title = 'Help groceries'; description = 'Need help'; pointsReward = 15 }
$publish = Do-Request -Uri "$base/api/tasks/cooperation/publish" -Method POST -Headers @{ Authorization = "Bearer $elderToken" } -Body (ConvertTo-Json $pubBody -Compress)
$taskId = $publish.data.id
Write-Host "  ✓ 发布成功, ID: $taskId`n" -ForegroundColor Green

# 步骤 3: 志愿者接单
Write-Host "[步骤 3] 志愿者接单..." -ForegroundColor Cyan
Do-Request -Uri "$base/api/tasks/cooperation/$taskId/accept" -Method POST -Headers @{ Authorization = "Bearer $volToken" } | Out-Null
Write-Host "  ✓ 接单成功`n" -ForegroundColor Green

# 步骤 6: 提交完成
Write-Host "[步骤 6] 提交完成..." -ForegroundColor Cyan
$subBody = @{ note = 'Done'; evidences = @( @{ evidenceType = 'IMAGE'; fileUrl = 'http://test.jpg' } ) }
Do-Request -Uri "$base/api/tasks/cooperation/$taskId/submit" -Method POST -Headers @{ Authorization = "Bearer $volToken" } -Body (ConvertTo-Json $subBody -Compress) | Out-Null
Write-Host "  ✓ 提交成功`n" -ForegroundColor Green

# 步骤 7: 老人确认
Write-Host "[步骤 7] 老人确认..." -ForegroundColor Cyan
Do-Request -Uri "$base/api/tasks/cooperation/$taskId/confirm" -Method POST -Headers @{ Authorization = "Bearer $elderToken" } | Out-Null
Write-Host "  ✓ 交接完成`n" -ForegroundColor Green

# 步骤 10: 管理员审计 (兼容 PS 5.1 语法)
Write-Host "[步骤 10] 管理员审计..." -ForegroundColor Cyan
$adminDetail = Do-Request -Uri "$base/api/admin/tasks/$taskId" -Method GET -Headers @{ Authorization = "Bearer $adminToken" }
foreach ($ledger in $adminDetail.data.pointsLedgerList) {
    $sign = ""
    if ($ledger.amount -gt 0) { $sign = "+" }
    Write-Host "  - User $($ledger.userId): $sign$($ledger.amount) ($($ledger.reason))" -ForegroundColor Gray
}

Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "  Zealinkly 业务流测试完成！" -ForegroundColor Green
Write-Host "========================================"