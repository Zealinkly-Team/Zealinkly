# Agent统一入口测试脚本
# 测试Agent自动识别意图并执行相应操作
# 功能：自动识别用户输入，自动发布任务/触发报警/开始AI聊天

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

$base = "http://localhost:8080"

# 测试用的老人账号
$elderUsername = "agent_test_elder_" + (Get-Date -Format "yyyyMMddHHmmss")
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

function Login-Elder {
    param([string]$Username, [string]$Password)
    
    try {
        $result = Do-Request -Uri "$base/api/auth/login" -Method POST -Body @{
            username = $Username
            password = $Password
            userType = "ELDER"
        }
        return $result.data.token
    } catch {
        Write-Host "登录失败，尝试注册..." -ForegroundColor Yellow
        # 尝试注册
        try {
            $registerResult = Do-Request -Uri "$base/api/auth/register/elder" -Method POST -Body @{
                username = $Username
                password = $Password
                realName = "测试老人"
                phone = "13800138000"
            }
            Write-Host "注册成功，重新登录..." -ForegroundColor Green
            $loginResult = Do-Request -Uri "$base/api/auth/login" -Method POST -Body @{
                username = $Username
                password = $Password
                userType = "ELDER"
            }
            return $loginResult.data.token
        } catch {
            Write-Host "注册/登录失败: $_" -ForegroundColor Red
            return $null
        }
    }
}

Write-Host "`n==========================================" -ForegroundColor Yellow
Write-Host "Agent统一入口测试" -ForegroundColor Yellow
Write-Host "==========================================`n" -ForegroundColor Yellow

# 登录获取token
Write-Host "[Step 1] 登录..." -ForegroundColor Cyan
$token = Login-Elder -Username $elderUsername -Password $elderPassword

if (-not $token) {
    Write-Host "无法获取token，测试终止" -ForegroundColor Red
    exit 1
}

Write-Host "登录成功，Token: $($token.Substring(0, [Math]::Min(20, $token.Length)))..." -ForegroundColor Green
$headers = @{
    "Authorization" = "Bearer $token"
}

# 测试用例
$testCases = @(
    @{
        input = "帮我买点菜"
        description = "简单互助任务"
        expectedIntent = "MUTUAL_AID"
    },
    @{
        input = "我摔倒了，快来帮我"
        description = "紧急报警"
        expectedIntent = "EMERGENCY"
    },
    @{
        input = "今天天气怎么样"
        description = "AI聊天"
        expectedIntent = "AI_CHAT"
    },
    @{
        input = "帮我买点菜，还有送点药"
        description = "多个互助任务"
        expectedIntent = "MUTUAL_AID"
    },
    @{
        input = "我摔倒了，而且家里的菜也没了"
        description = "紧急+互助任务（应优先紧急）"
        expectedIntent = "EMERGENCY"
    },
    @{
        input = "你好，我今天感觉有点不舒服，头有点疼，而且家里的菜也快吃完了，能不能帮我买点菜回来？另外，我昨天摔了一跤，现在腿还有点疼，可能需要有人陪我去医院看看。"
        description = "长文本：紧急+互助任务"
        expectedIntent = "EMERGENCY"
    },
    @{
        input = "最近天气不错，我想出去走走，但是一个人有点孤单。另外，家里的水龙头坏了，一直漏水，不知道能不能找人帮我修一下？"
        description = "长文本：多个互助任务"
        expectedIntent = "MUTUAL_AID"
    },
    @{
        input = "今天天气真好，我想和你聊聊天。最近社区有什么新鲜事吗？"
        description = "长文本：纯AI聊天"
        expectedIntent = "AI_CHAT"
    }
)

Write-Host "`n[Step 2] 测试Agent处理..." -ForegroundColor Cyan
Write-Host "=" * 50 -ForegroundColor Gray

$successCount = 0
$failCount = 0

foreach ($testCase in $testCases) {
    $input = $testCase.input
    $desc = $testCase.description
    $expectedIntent = $testCase.expectedIntent
    
    Write-Host "`n  Test: $desc" -ForegroundColor White
    $preview = if ($input.Length -gt 60) { $input.Substring(0, 60) + "..." } else { $input }
    Write-Host "    Input: `"$preview`"" -ForegroundColor Gray
    Write-Host "    Length: $($input.Length) characters" -ForegroundColor Gray
    
    try {
        $result = Do-Request -Uri "$base/api/agent/process" -Method POST -Headers $headers -Body @{
            userInput = $input
        }
        
        $data = $result.data
        $intentType = $data.intentType
        $intentDesc = $data.intentDescription
        
        # 验证意图识别
        $intentCorrect = $intentType -eq $expectedIntent
        if ($intentCorrect) {
            Write-Host "    Intent: $intentType ($intentDesc) ✓" -ForegroundColor Green
            $successCount++
        } else {
            Write-Host "    Intent: $intentType ($intentDesc) - Expected: $expectedIntent" -ForegroundColor Yellow
            $failCount++
        }
        
        Write-Host "    Message: $($data.message)" -ForegroundColor Cyan
        
        # 显示提取的任务
        if ($data.tasks -and $data.tasks.Count -gt 0) {
            Write-Host "    Tasks Extracted ($($data.tasks.Count)):" -ForegroundColor Yellow
            foreach ($task in $data.tasks) {
                $priorityColor = switch ($task.priority) {
                    "HIGH" { "Red" }
                    "MEDIUM" { "Yellow" }
                    "LOW" { "Cyan" }
                    default { "White" }
                }
                Write-Host "      - [$($task.priority)] $($task.typeDescription) : $($task.description)" -ForegroundColor $priorityColor
            }
        } else {
            Write-Host "    No tasks extracted" -ForegroundColor Gray
        }
        
        # 显示创建的任务
        if ($data.createdTasks -and $data.createdTasks.Count -gt 0) {
            Write-Host "    Created Tasks ($($data.createdTasks.Count)):" -ForegroundColor Green
            foreach ($task in $data.createdTasks) {
                Write-Host "      ✓ Task ID: $($task.id), Type: $($task.taskType), Status: $($task.status)" -ForegroundColor Green
                if ($task.content) {
                    $contentPreview = if ($task.content.Length -gt 40) { $task.content.Substring(0, 40) + "..." } else { $task.content }
                    Write-Host "        Content: $contentPreview" -ForegroundColor Gray
                }
            }
        }
        
        # 显示AI回复
        if ($data.aiResponse) {
            $aiPreview = if ($data.aiResponse.Length -gt 150) { $data.aiResponse.Substring(0, 150) + "..." } else { $data.aiResponse }
            Write-Host "    AI Response:" -ForegroundColor Cyan
            Write-Host "      $aiPreview" -ForegroundColor Gray
        }
        
    } catch {
        Write-Host "    Failed: $_" -ForegroundColor Red
        $failCount++
    }
}

Write-Host "`n" + ("=" * 50) -ForegroundColor Gray
Write-Host "  Test Summary:" -ForegroundColor Yellow
Write-Host "    Success: $successCount" -ForegroundColor Green
Write-Host "    Failed: $failCount" -ForegroundColor Red
Write-Host "    Total: $($testCases.Count)" -ForegroundColor Cyan

Write-Host "`n==========================================" -ForegroundColor Yellow
Write-Host "Agent测试完成!" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Yellow
