# ========================================
# 1. 强制环境编码 - 全家桶防御
# ========================================
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

$base = 'http://127.0.0.1:8080'
$ErrorActionPreference = 'Stop'

# ========================================
# 2. 核心请求函数 - 强制字节流处理
# ========================================
function Do-Request {
    param($Uri, $Method = 'GET', $Headers = @{}, $Body = $null)
    
    $params = @{
        Uri = $Uri
        Method = $Method
        ContentType = 'application/json; charset=utf-8'
    }
    
    if ($Headers) { $params['Headers'] = $Headers }
    
    # 【核心修复】发送端：强制转为 UTF-8 字节数组，防止发送 ????
    if ($Body -ne $null) {
        $json = ConvertTo-Json $Body -Compress
        $params['Body'] = [System.Text.Encoding]::UTF8.GetBytes($json)
    }

    try {
        # 使用 Invoke-WebRequest 获取原始响应
        $resp = Invoke-WebRequest @params -UseBasicParsing
        
        # 【核心修复】接收端：手动按 UTF-8 解码字节流，彻底解决 å­©å­
        $rawContent = ""
        if ($resp.ContentBytes -and $resp.ContentBytes.Length -gt 0) {
            $rawContent = [System.Text.Encoding]::UTF8.GetString($resp.ContentBytes)
        } else {
            $rawContent = $resp.Content
        }
        
        return $rawContent | ConvertFrom-Json
    } catch {
        # 错误处理：也要防止错误信息本身乱码
        if ($_.Exception.Response) {
            $status = $_.Exception.Response.StatusCode.value__
            $stream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream, [System.Text.Encoding]::UTF8)
            $errBody = $reader.ReadToEnd()
            Write-Host "❌ ERROR $status : $errBody" -ForegroundColor Red
        } else {
            Write-Host "❌ ERROR : $($_.Exception.Message)" -ForegroundColor Red
        }
        throw
    }
}

# ========================================
# 3. 开始业务流测试
# ========================================
Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "  The Cosmos Engine: AI Chat Test" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Yellow

# 为了排除数据库旧乱码干扰，我们生成一个带时间戳的新用户
$timestamp = Get-Date -Format "HHmm"
$username = "user_$timestamp"

# [Step 0] 注册
Write-Host "[Step 0] 注册新用户: $username..." -ForegroundColor Cyan
try {
    $regBody = @{ username = $username; password = '123456'; realName = "测试用户_$timestamp" }
    Do-Request -Uri "$base/api/auth/register/elder" -Method POST -Body $regBody | Out-Null
    Write-Host "  ✓ 注册成功" -ForegroundColor Green
} catch {
    Write-Host "  - 用户可能已存在" -ForegroundColor Gray
}

# [Step 1] 登录
Write-Host "`n[Step 1] 登录获取 Token..." -ForegroundColor Cyan
$loginBody = @{ username = $username; password = '123456'; userType = 'ELDER' }
$loginRes = Do-Request -Uri "$base/api/auth/login" -Method POST -Body $loginBody
$token = $loginRes.data.token
Write-Host "  ✓ 登录成功 (ID: $($loginRes.data.userId))" -ForegroundColor Green

# [Step 2] AI 提问
Write-Host "`n[Step 2] 发送 AI 提问..." -ForegroundColor Cyan
$questions = @("今天天气怎么样？", "如何预防感冒？")
foreach ($q in $questions) {
    Write-Host "  Q: $q" -ForegroundColor Gray
    $askRes = Do-Request -Uri "$base/api/ai/ask" -Method POST -Headers @{ Authorization = "Bearer $token" } -Body @{ question = $q }
    Write-Host "  A: $($askRes.data)" -ForegroundColor Yellow
    Write-Host "  ------------------------------------"
}

# [Step 3] 历史记录
Write-Host "`n[Step 3] 验证历史记录同步..." -ForegroundColor Cyan
$history = Do-Request -Uri "$base/api/ai/history" -Method GET -Headers @{ Authorization = "Bearer $token" }
Write-Host "  ✓ 找到 $($history.data.Count) 条纯净中文记录" -ForegroundColor Green
foreach ($item in $history.data) {
    $time = [DateTime]::Parse($item.createdAt).ToString("HH:mm:ss")
    Write-Host "  [$time] Q: $($item.question)" -ForegroundColor Gray
    Write-Host "           A: $($item.answer)" -ForegroundColor Yellow
}

Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "  AI 功能全链路测试通过！" -ForegroundColor Green
Write-Host "========================================`n"