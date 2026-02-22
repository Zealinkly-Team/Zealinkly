# Agent语音处理完整流程测试脚本
# 测试：语音识别 -> 意图识别 -> 自动执行（发布任务/触发报警/AI聊天）

$baseUrl = "http://localhost:8080"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "  Agent语音处理完整流程测试" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""

# 步骤1: 注册并登录老人用户
Write-Host "[步骤1] 注册并登录老人用户..." -ForegroundColor Yellow

$registerBody = @{
    username = "test_elder_voice"
    password = "test123456"
    realName = "测试老人语音"
    phone = "13800138001"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/register/elder" `
        -Method POST `
        -ContentType "application/json" `
        -Body $registerBody `
        -ErrorAction Stop
    
    Write-Host "注册成功" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode -eq 409 -or $_.Exception.Response.StatusCode -eq 400) {
        Write-Host "用户已存在，继续登录..." -ForegroundColor Yellow
    } else {
        Write-Host "注册失败: $_" -ForegroundColor Red
        $errorDetails = $_.ErrorDetails.Message
        if ($errorDetails) {
            Write-Host "错误详情: $errorDetails" -ForegroundColor Red
        }
    }
}

# 登录
$loginBody = @{
    username = "test_elder_voice"
    password = "test123456"
    userType = "ELDER"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody `
        -ErrorAction Stop
    
    $token = $loginResponse.data.token
    Write-Host "登录成功，Token: $($token.Substring(0, 20))..." -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "登录失败: $_" -ForegroundColor Red
    exit 1
}

# 步骤2: 测试语音处理完整流程
Write-Host "[步骤2] 测试Agent语音处理完整流程..." -ForegroundColor Yellow
Write-Host ""

$audioFilePath = "E:\sdu\Zealinkly\cards\3.wav"

if (-not (Test-Path $audioFilePath)) {
    Write-Host "未找到音频文件: $audioFilePath" -ForegroundColor Red
    Write-Host "请设置正确的音频文件路径" -ForegroundColor Yellow
    exit 1
}

Write-Host "找到音频文件: $audioFilePath" -ForegroundColor Green

# 读取音频文件并转换为base64
$audioBytes = [System.IO.File]::ReadAllBytes($audioFilePath)
$audioBase64 = [Convert]::ToBase64String($audioBytes)

Write-Host "音频文件大小: $($audioBytes.Length) 字节" -ForegroundColor Cyan
Write-Host ""

# 调用Agent语音处理API
$voiceRequest = @{
    audioBase64 = $audioBase64
    format = "wav"
    rate = 16000
} | ConvertTo-Json

try {
    Write-Host "正在调用Agent语音处理API..." -ForegroundColor Yellow
    Write-Host "流程: 语音识别 -> 意图识别 -> 自动执行" -ForegroundColor Cyan
    Write-Host ""
    
    $agentResponse = Invoke-RestMethod -Uri "$baseUrl/api/agent/process-voice" `
        -Method POST `
        -ContentType "application/json" `
        -Headers @{ "Authorization" = "Bearer $token" } `
        -Body $voiceRequest `
        -ErrorAction Stop
    
    Write-Host "===========================================" -ForegroundColor Green
    Write-Host "  Agent语音处理成功！" -ForegroundColor Green
    Write-Host "===========================================" -ForegroundColor Green
    Write-Host ""
    
    $data = $agentResponse.data
    
    Write-Host "识别到的文字: $($data.userInput)" -ForegroundColor Cyan
    Write-Host "意图类型: $($data.intentType) - $($data.intentDescription)" -ForegroundColor Yellow
    Write-Host ""
    
    if ($data.tasks -and $data.tasks.Count -gt 0) {
        Write-Host "提取的任务 ($($data.tasks.Count)个):" -ForegroundColor Cyan
        foreach ($task in $data.tasks) {
            Write-Host "  - [$($task.priority)] $($task.typeDescription): $($task.description)" -ForegroundColor White
        }
        Write-Host ""
    }
    
    if ($data.createdTasks -and $data.createdTasks.Count -gt 0) {
        Write-Host "创建的任务 ($($data.createdTasks.Count)个):" -ForegroundColor Green
        foreach ($task in $data.createdTasks) {
            Write-Host "  - 任务ID: $($task.id), 类型: $($task.taskType), 状态: $($task.status)" -ForegroundColor White
            Write-Host "    内容: $($task.content.Substring(0, [Math]::Min(50, $task.content.Length)))..." -ForegroundColor Gray
        }
        Write-Host ""
    }
    
    if ($data.aiResponse) {
        Write-Host "AI回复:" -ForegroundColor Cyan
        Write-Host $data.aiResponse -ForegroundColor White
        Write-Host ""
    }
    
    Write-Host "处理结果: $($data.message)" -ForegroundColor Green
    Write-Host ""
    
} catch {
    Write-Host ""
    Write-Host "✗ Agent语音处理失败" -ForegroundColor Red
    $errorDetails = $_.ErrorDetails.Message
    if ($errorDetails) {
        Write-Host "错误详情: $errorDetails" -ForegroundColor Red
    } else {
        Write-Host "错误: $_" -ForegroundColor Red
    }
    Write-Host ""
}

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "  测试完成" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
