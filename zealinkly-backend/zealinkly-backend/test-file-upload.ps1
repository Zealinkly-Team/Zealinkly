# 文件上传测试脚本

$baseUrl = "http://localhost:8080"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "  文件上传测试" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""

# 步骤1: 注册并登录老人用户
Write-Host "[步骤1] 注册并登录老人用户..." -ForegroundColor Yellow

$registerBody = @{
    username = "test_elder_file"
    password = "test123456"
    realName = "测试老人文件"
    phone = "13800138002"
} | ConvertTo-Json

try {
    $null = Invoke-RestMethod -Uri "$baseUrl/api/auth/register/elder" `
        -Method POST `
        -ContentType "application/json" `
        -Body $registerBody `
        -ErrorAction Stop
    Write-Host "注册成功" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode -eq 409 -or $_.Exception.Response.StatusCode -eq 400) {
        Write-Host "用户已存在，继续登录..." -ForegroundColor Yellow
    }
}

# 登录
$loginBody = @{
    username = "test_elder_file"
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
    Write-Host "登录成功" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "登录失败: $_" -ForegroundColor Red
    exit 1
}

# 步骤2: 测试图片上传（Base64）
Write-Host "[步骤2] 测试图片上传（Base64）..." -ForegroundColor Yellow

$imagePath = "E:\sdu\Zealinkly\cards\test_image.jpg"  # 请修改为实际的图片路径

if (Test-Path $imagePath) {
    Write-Host "找到图片文件: $imagePath" -ForegroundColor Green
    
    # 读取图片并转换为base64
    $imageBytes = [System.IO.File]::ReadAllBytes($imagePath)
    $imageBase64 = [Convert]::ToBase64String($imageBytes)
    
    $uploadBody = @{
        base64Data = $imageBase64
        filename = "test_image.jpg"
        contentType = "image/jpeg"
        relatedType = "TASK"
        relatedId = 1
    } | ConvertTo-Json
    
    try {
        $uploadResponse = Invoke-RestMethod -Uri "$baseUrl/api/files/upload-base64" `
            -Method POST `
            -ContentType "application/json" `
            -Headers @{ "Authorization" = "Bearer $token" } `
            -Body $uploadBody `
            -ErrorAction Stop
        
        Write-Host "✓ 图片上传成功！" -ForegroundColor Green
        Write-Host "文件ID: $($uploadResponse.data.id)" -ForegroundColor Cyan
        Write-Host "文件URL: $($uploadResponse.data.fileUrl)" -ForegroundColor Cyan
        Write-Host "文件大小: $($uploadResponse.data.fileSize) 字节" -ForegroundColor Cyan
        Write-Host ""
    } catch {
        Write-Host "✗ 图片上传失败" -ForegroundColor Red
        $errorDetails = $_.ErrorDetails.Message
        if ($errorDetails) {
            Write-Host "错误详情: $errorDetails" -ForegroundColor Red
        }
        Write-Host ""
    }
} else {
    Write-Host "未找到图片文件，跳过图片上传测试" -ForegroundColor Yellow
    Write-Host ""
}

# 步骤3: 测试音频上传（Base64）
Write-Host "[步骤3] 测试音频上传（Base64）..." -ForegroundColor Yellow

$audioPath = "E:\sdu\Zealinkly\cards\3.wav"

if (Test-Path $audioPath) {
    Write-Host "找到音频文件: $audioPath" -ForegroundColor Green
    
    # 读取音频并转换为base64
    $audioBytes = [System.IO.File]::ReadAllBytes($audioPath)
    $audioBase64 = [Convert]::ToBase64String($audioBytes)
    
    $uploadBody = @{
        base64Data = $audioBase64
        filename = "test_audio.wav"
        contentType = "audio/wav"
        relatedType = "TASK"
        relatedId = 1
    } | ConvertTo-Json
    
    try {
        $uploadResponse = Invoke-RestMethod -Uri "$baseUrl/api/files/upload-base64" `
            -Method POST `
            -ContentType "application/json" `
            -Headers @{ "Authorization" = "Bearer $token" } `
            -Body $uploadBody `
            -ErrorAction Stop
        
        Write-Host "✓ 音频上传成功！" -ForegroundColor Green
        Write-Host "文件ID: $($uploadResponse.data.id)" -ForegroundColor Cyan
        Write-Host "文件URL: $($uploadResponse.data.fileUrl)" -ForegroundColor Cyan
        Write-Host "文件大小: $($uploadResponse.data.fileSize) 字节" -ForegroundColor Cyan
        Write-Host ""
    } catch {
        Write-Host "✗ 音频上传失败" -ForegroundColor Red
        $errorDetails = $_.ErrorDetails.Message
        if ($errorDetails) {
            Write-Host "错误详情: $errorDetails" -ForegroundColor Red
        }
        Write-Host ""
    }
} else {
    Write-Host "未找到音频文件，跳过音频上传测试" -ForegroundColor Yellow
    Write-Host ""
}

# 步骤4: 获取我的文件列表
Write-Host "[步骤4] 获取我的文件列表..." -ForegroundColor Yellow

try {
    $filesResponse = Invoke-RestMethod -Uri "$baseUrl/api/files/my" `
        -Method GET `
        -Headers @{ "Authorization" = "Bearer $token" } `
        -ErrorAction Stop
    
    Write-Host "✓ 获取文件列表成功！" -ForegroundColor Green
    Write-Host "文件数量: $($filesResponse.data.Count)" -ForegroundColor Cyan
    
    foreach ($file in $filesResponse.data) {
        Write-Host "  - [$($file.fileType)] $($file.originalFilename) ($($file.fileSize) 字节)" -ForegroundColor White
        Write-Host "    URL: $($file.fileUrl)" -ForegroundColor Gray
    }
    Write-Host ""
} catch {
    Write-Host "✗ 获取文件列表失败" -ForegroundColor Red
    Write-Host ""
}

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "  测试完成" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
