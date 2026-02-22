# 语音识别测试脚本
# 注意：需要准备一个音频文件（wav格式，16kHz采样率）并转换为base64

$baseUrl = "http://localhost:8080"
# 强制设置输出编码为UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "  语音识别 (ASR) 测试" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""

# 步骤1: 注册并登录老人用户
Write-Host "[步骤1] 注册并登录老人用户..." -ForegroundColor Yellow

$registerBody = @{
    username = "test_elder_asr"
    password = "test123456"
    realName = "测试老人ASR"
    phone = "13800138000"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/register/elder" `
        -Method POST `
        -ContentType "application/json" `
        -Body $registerBody `
        -ErrorAction Stop
    Write-Host "注册成功" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode -eq 409) {
        Write-Host "用户已存在，继续登录..." -ForegroundColor Yellow
    } else {
        $errorDetails = $_.ErrorDetails.Message
        Write-Host "注册信息: $errorDetails" -ForegroundColor Yellow
    }
}

# 登录
$loginBody = @{
    username = "test_elder_asr"
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
    Write-Host "登录成功，Token获取成功" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "登录失败: $_" -ForegroundColor Red
    exit 1
}

# 步骤2: 测试语音识别
Write-Host "[步骤2] 测试语音识别..." -ForegroundColor Yellow
$audioFilePath = "E:\sdu\Zealinkly\cards\3.wav"

if (Test-Path $audioFilePath) {
    Write-Host "找到音频文件: $audioFilePath" -ForegroundColor Green
    
    $audioBytes = [System.IO.File]::ReadAllBytes($audioFilePath)
    $audioBase64 = [Convert]::ToBase64String($audioBytes)
    
    $asrBody = @{
        audioBase64 = $audioBase64
        format = "wav"
        rate = 16000
    } | ConvertTo-Json
    
    try {
        Write-Host "正在调用语音识别API..." -ForegroundColor Yellow
        $asrResponse = Invoke-RestMethod -Uri "$baseUrl/api/asr/recognize" `
            -Method POST `
            -ContentType "application/json" `
            -Headers @{ "Authorization" = "Bearer $token" } `
            -Body $asrBody `
            -ErrorAction Stop
        
        Write-Host "识别成功！" -ForegroundColor Green
        Write-Host "识别结果: $($asrResponse.data.text)" -ForegroundColor Cyan
        
        $recognizedText = $asrResponse.data.text
        if ($recognizedText -match '^[嗯啊呃]+[。，！？]*$') {
            Write-Host "警告：识别结果可能不准确，请检查录音质量或编码格式。" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "语音识别接口调用失败" -ForegroundColor Red
        Write-Host "错误详情: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "未找到音频文件: $audioFilePath" -ForegroundColor Red
}

Write-Host ""
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "  测试完成" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan