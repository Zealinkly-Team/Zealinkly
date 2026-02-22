# Intent Recognition Test Script
# Tests LangChain4j intent recognition functionality
# 注意：所有文本（无论长短）都会先尝试提取任务清单

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

$base = "http://localhost:8080"

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

function Display-Result {
    param(
        [string]$Input,
        [string]$RecognizedIntent,
        [string]$Description,
        [array]$Tasks,
        [string]$Expected = $null
    )
    
    # 显示基础意图
    if ($Expected) {
        if ($RecognizedIntent -eq $Expected) {
            Write-Host "    Base Intent: $RecognizedIntent ($Description) ✓" -ForegroundColor Green
        } else {
            Write-Host "    Base Intent: $RecognizedIntent ($Description) - Expected: $Expected" -ForegroundColor Yellow
        }
    } else {
        Write-Host "    Base Intent: $RecognizedIntent ($Description)" -ForegroundColor Cyan
    }
    
    # 显示任务清单
    if ($Tasks -and $Tasks.Count -gt 0) {
        Write-Host "    Tasks Extracted ($($Tasks.Count)):" -ForegroundColor Green
        $taskIndex = 1
        foreach ($task in $Tasks) {
            $taskType = $task.typeDescription
            $taskDesc = $task.description
            $priority = $task.priority
            $priorityColor = switch ($priority) {
                "HIGH" { "Red" }
                "MEDIUM" { "Yellow" }
                "LOW" { "Cyan" }
                default { "White" }
            }
            Write-Host "      $taskIndex. [$priority] $taskType : $taskDesc" -ForegroundColor $priorityColor
            $taskIndex++
        }
    } else {
        Write-Host "    No tasks extracted (fallback to simple intent recognition)" -ForegroundColor Gray
    }
}

Write-Host "`n==========================================" -ForegroundColor Yellow
Write-Host "Intent Recognition & Task Extraction Test" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Yellow
Write-Host "Note: All texts (short or long) will attempt task extraction first" -ForegroundColor Gray
Write-Host ""

# Test cases - 短文本（也会进行任务提取）
$shortTextCases = @(
    @{input = "帮我买点菜"; expected = "MUTUAL_AID"; description = "简单互助任务"},
    @{input = "我需要有人帮我送东西"; expected = "MUTUAL_AID"; description = "互助任务请求"},
    @{input = "我摔倒了，快来帮我"; expected = "EMERGENCY"; description = "紧急情况"},
    @{input = "我迷路了，不知道在哪里"; expected = "EMERGENCY"; description = "紧急情况"},
    @{input = "今天天气怎么样"; expected = "AI_CHAT"; description = "纯聊天"},
    @{input = "你好，我想聊聊天"; expected = "AI_CHAT"; description = "纯聊天"},
    @{input = "我身体不舒服，需要帮助"; expected = "EMERGENCY"; description = "紧急情况"},
    @{input = "能陪我聊会天吗"; expected = "AI_CHAT"; description = "纯聊天"},
    @{input = "帮我买点菜，还有送点药"; description = "多个互助任务"},
    @{input = "我摔倒了，而且家里的菜也没了"; description = "紧急+互助任务"}
)

# Test cases - 长文本（任务提取）
$longTextCases = @(
    @{
        input = "你好，我今天感觉有点不舒服，头有点疼，而且家里的菜也快吃完了，能不能帮我买点菜回来？另外，我昨天摔了一跤，现在腿还有点疼，可能需要有人陪我去医院看看。"
        description = "包含紧急报警和互助任务的长文本"
    },
    @{
        input = "最近天气不错，我想出去走走，但是一个人有点孤单。另外，家里的水龙头坏了，一直漏水，不知道能不能找人帮我修一下？还有，我想了解一下最近的社区活动。"
        description = "包含多个互助任务的长文本"
    },
    @{
        input = "今天天气真好，我想和你聊聊天。最近社区有什么新鲜事吗？我听说附近新开了一家超市，不知道怎么样？"
        description = "纯聊天长文本（无任务）"
    },
    @{
        input = "我迷路了，现在不知道在哪里，而且手机快没电了，非常着急。另外，我本来要去超市买点东西的，但是现在找不到路了。"
        description = "包含紧急情况和互助任务的长文本"
    },
    @{
        input = "我想了解一下社区的活动安排，另外家里的灯坏了需要修，还有我想找人陪我聊聊天。"
        description = "多个互助任务+聊天"
    }
)

Write-Host "[Step 1] Testing Short Text (Task Extraction)" -ForegroundColor Cyan
Write-Host "=" * 50 -ForegroundColor Gray

foreach ($testCase in $shortTextCases) {
    $input = $testCase.input
    $expected = $testCase.expected
    $desc = $testCase.description
    
    Write-Host "`n  Test: $desc" -ForegroundColor White
    Write-Host "    Input: `"$input`"" -ForegroundColor Gray
    Write-Host "    Length: $($input.Length) characters" -ForegroundColor Gray
    
    try {
        $result = Do-Request -Uri "$base/api/intent/recognize" -Method POST -Body @{
            userInput = $input
        }
        
        $recognizedIntent = $result.data.intentType
        $description = $result.data.intentDescription
        $tasks = $result.data.tasks
        
        Display-Result -Input $input -RecognizedIntent $recognizedIntent -Description $description -Tasks $tasks -Expected $expected
        
    } catch {
        Write-Host "    Failed: $_" -ForegroundColor Red
    }
}

Write-Host "`n[Step 2] Testing Long Text (Task Extraction)" -ForegroundColor Cyan
Write-Host "=" * 50 -ForegroundColor Gray

foreach ($testCase in $longTextCases) {
    $input = $testCase.input
    $desc = $testCase.description
    
    Write-Host "`n  Test: $desc" -ForegroundColor White
    Write-Host "    Input length: $($input.Length) characters" -ForegroundColor Gray
    $preview = if ($input.Length -gt 60) { $input.Substring(0, 60) + "..." } else { $input }
    Write-Host "    Preview: `"$preview`"" -ForegroundColor Gray
    
    try {
        $result = Do-Request -Uri "$base/api/intent/recognize" -Method POST -Body @{
            userInput = $input
        }
        
        $recognizedIntent = $result.data.intentType
        $description = $result.data.intentDescription
        $tasks = $result.data.tasks
        
        Display-Result -Input $input -RecognizedIntent $recognizedIntent -Description $description -Tasks $tasks
        
    } catch {
        Write-Host "    Failed: $_" -ForegroundColor Red
    }
}

Write-Host "`n==========================================" -ForegroundColor Yellow
Write-Host "Intent Recognition Test Completed!" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Yellow
