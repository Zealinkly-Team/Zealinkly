# Test Remaining Features
$base = 'http://localhost:8080'
$ErrorActionPreference = 'Stop'

function Do-Request {
    param($Uri, $Method = 'GET', $Headers = $null, $Body = $null)
    try {
        $params = @{ Uri = $Uri; Method = $Method; ContentType = 'application/json' }
        if ($Headers) { $params['Headers'] = $Headers }
        if ($Body -ne $null) { $params['Body'] = $Body }
        Invoke-RestMethod @params
    } catch {
        $status = $_.Exception.Response.StatusCode.value__
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $reader.BaseStream.Position = 0
        $errBody = $reader.ReadToEnd()
        Write-Host "ERROR $status : $errBody" -ForegroundColor Red
        throw
    }
}

Write-Host "=== Login ===" -ForegroundColor Cyan
$elderLogin = Do-Request -Uri ($base + '/api/auth/login') -Method POST -Body (ConvertTo-Json @{ username = 'elder1'; password = '123456'; userType = 'ELDER' } -Compress)
$volLogin = Do-Request -Uri ($base + '/api/auth/login') -Method POST -Body (ConvertTo-Json @{ username = 'vol1'; password = '123456'; userType = 'VOLUNTEER' } -Compress)
$adminLogin = Do-Request -Uri ($base + '/api/auth/login') -Method POST -Body (ConvertTo-Json @{ username = 'admin1'; password = '123456'; userType = 'ADMIN' } -Compress)
$elderToken = $elderLogin.data.token
$volToken = $volLogin.data.token
$adminToken = $adminLogin.data.token
$elderId = $elderLogin.data.userId
$volId = $volLogin.data.userId
Write-Host "OK"

Write-Host "=== 1. AI Chat: ask + history ===" -ForegroundColor Cyan
$aiResp = Do-Request -Uri ($base + '/api/ai/ask') -Method POST -Headers @{ Authorization = "Bearer $elderToken" } -Body (ConvertTo-Json @{ question = 'How to use phone?' } -Compress)
Write-Host "AI answer: $($aiResp.data.Substring(0, [Math]::Min(50, $aiResp.data.Length)))..."
$history = Do-Request -Uri ($base + '/api/ai/history') -Method GET -Headers @{ Authorization = "Bearer $elderToken" }
Write-Host "History count: $($history.data.Count)"

Write-Host "=== 2. User: update info ===" -ForegroundColor Cyan
Do-Request -Uri ($base + '/api/user/info') -Method PUT -Headers @{ Authorization = "Bearer $elderToken" } -Body (ConvertTo-Json @{ realName = 'Zhang Updated'; phone = '13800138000' } -Compress) | Out-Null
Write-Host "OK"

Write-Host "=== 3. Tasks: my-as-elder, my-as-volunteer, detail ===" -ForegroundColor Cyan
$myElder = Do-Request -Uri ($base + '/api/tasks/cooperation/my-as-elder') -Method GET -Headers @{ Authorization = "Bearer $elderToken" }
$myVol = Do-Request -Uri ($base + '/api/tasks/cooperation/my-as-volunteer') -Method GET -Headers @{ Authorization = "Bearer $volToken" }
$taskId = if($myElder.data.Count -gt 0) { $myElder.data[0].id } else { if($myVol.data.Count -gt 0) { $myVol.data[0].id } else { 1 } }
$detail = Do-Request -Uri ($base + '/api/tasks/cooperation/' + $taskId) -Method GET -Headers @{ Authorization = "Bearer $elderToken" }
Write-Host "Elder tasks: $($myElder.data.Count), Volunteer tasks: $($myVol.data.Count), Detail has evidence: $($detail.data.evidenceList.Count -gt 0)"

Write-Host "=== 4. Task: submit appeal ===" -ForegroundColor Cyan
Do-Request -Uri ($base + '/api/tasks/cooperation/' + $taskId + '/appeal') -Method POST -Headers @{ Authorization = "Bearer $elderToken" } -Body (ConvertTo-Json @{ content = 'Task not completed properly' } -Compress) | Out-Null
Write-Host "OK"

Write-Host "=== 5. Admin: elder CRUD ===" -ForegroundColor Cyan
$createElder = Do-Request -Uri ($base + '/api/admin/elders') -Method POST -Headers @{ Authorization = "Bearer $adminToken" } -Body (ConvertTo-Json @{ username = 'elder2'; password = '123456'; realName = 'Elder2'; phone = '13900139000' } -Compress)
$newElderId = $createElder.data.id
Do-Request -Uri ($base + '/api/admin/elders/' + $newElderId) -Method PUT -Headers @{ Authorization = "Bearer $adminToken" } -Body (ConvertTo-Json @{ realName = 'Elder2 Updated'; points = 50 } -Compress) | Out-Null
$getElder = Do-Request -Uri ($base + '/api/admin/elders/' + $newElderId) -Method GET -Headers @{ Authorization = "Bearer $adminToken" }
Write-Host "Created elder id: $newElderId, points: $($getElder.data.points)"

Write-Host "=== 6. Admin: volunteer CRUD ===" -ForegroundColor Cyan
$createVol = Do-Request -Uri ($base + '/api/admin/volunteers') -Method POST -Headers @{ Authorization = "Bearer $adminToken" } -Body (ConvertTo-Json @{ username = 'vol2'; password = '123456'; realName = 'Vol2'; phone = '13900139001' } -Compress)
$newVolId = $createVol.data.id
Do-Request -Uri ($base + '/api/admin/volunteers/' + $newVolId) -Method PUT -Headers @{ Authorization = "Bearer $adminToken" } -Body (ConvertTo-Json @{ realName = 'Vol2 Updated'; idCardStatus = $true } -Compress) | Out-Null
Write-Host "Created volunteer id: $newVolId"

Write-Host "=== 7. Admin: disable/enable ===" -ForegroundColor Cyan
Do-Request -Uri ($base + '/api/admin/elders/' + $newElderId + '/disable') -Method PATCH -Headers @{ Authorization = "Bearer $adminToken" } | Out-Null
Do-Request -Uri ($base + '/api/admin/elders/' + $newElderId + '/enable') -Method PATCH -Headers @{ Authorization = "Bearer $adminToken" } | Out-Null
Do-Request -Uri ($base + '/api/admin/volunteers/' + $newVolId + '/disable') -Method PATCH -Headers @{ Authorization = "Bearer $adminToken" } | Out-Null
Do-Request -Uri ($base + '/api/admin/volunteers/' + $newVolId + '/enable') -Method PATCH -Headers @{ Authorization = "Bearer $adminToken" } | Out-Null
Write-Host "OK"

Write-Host "=== 8. Admin: bulk delete ===" -ForegroundColor Cyan
Do-Request -Uri ($base + '/api/admin/elders/bulk-delete') -Method POST -Headers @{ Authorization = "Bearer $adminToken" } -Body (ConvertTo-Json @($newElderId) -Compress) | Out-Null
Do-Request -Uri ($base + '/api/admin/volunteers/bulk-delete') -Method POST -Headers @{ Authorization = "Bearer $adminToken" } -Body (ConvertTo-Json @($newVolId) -Compress) | Out-Null
Write-Host "OK"

Write-Host "=== 9. Admin: task detail (with evidence + ledger) ===" -ForegroundColor Cyan
$taskDetail = Do-Request -Uri ($base + '/api/admin/tasks/' + $taskId) -Method GET -Headers @{ Authorization = "Bearer $adminToken" }
Write-Host "Task has evidence: $($taskDetail.data.evidenceList.Count), ledger entries: $($taskDetail.data.pointsLedgerList.Count)"

Write-Host "=== 10. Admin: update task ===" -ForegroundColor Cyan
Do-Request -Uri ($base + '/api/admin/tasks/' + $taskId) -Method PUT -Headers @{ Authorization = "Bearer $adminToken" } -Body (ConvertTo-Json @{ content = 'Updated content'; aiResponse = 'Admin note' } -Compress) | Out-Null
Write-Host "OK"

Write-Host "=== 11. Admin: appeals list + resolve ===" -ForegroundColor Cyan
$appeals = Do-Request -Uri ($base + '/api/admin/appeals/pending') -Method GET -Headers @{ Authorization = "Bearer $adminToken" }
if ($appeals.data -and $appeals.data.Count -gt 0) {
    $appealId = $appeals.data[0].id
    Do-Request -Uri ($base + '/api/admin/appeals/' + $appealId + '/resolve') -Method PATCH -Headers @{ Authorization = "Bearer $adminToken" } -Body (ConvertTo-Json @{ adminNote = 'Resolved' } -Compress) | Out-Null
    Write-Host "Resolved appeal id: $appealId"
} else {
    Write-Host "No pending appeals"
}

Write-Host "=== 12. Admin: broadcast to volunteers ===" -ForegroundColor Cyan
$broadcast = Do-Request -Uri ($base + '/api/admin/notifications/broadcast') -Method POST -Headers @{ Authorization = "Bearer $adminToken" } -Body (ConvertTo-Json @{ targetType = 'ALL_VOLUNTEERS'; title = 'Notice'; message = 'Test to volunteers' } -Compress)
Write-Host "Sent: $($broadcast.data.sentCount)"

Write-Host "`n=== All remaining tests passed ===" -ForegroundColor Green
