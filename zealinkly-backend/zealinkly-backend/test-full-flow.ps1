# Zealinkly Full Test Flow (ASCII only)
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

Write-Host "=== 1. Register (skip if exists) ===" -ForegroundColor Cyan
foreach ($role in @('elder','volunteer','admin')) {
    $u = switch($role){ 'elder'{'elder1'}; 'volunteer'{'vol1'}; 'admin'{'admin1'} }
    try {
        $body = if($role -eq 'admin') { @{ username = $u; password = '123456'; realName = 'Admin' } } else { @{ username = $u; password = '123456'; realName = 'User' } }
        Do-Request -Uri ($base + '/api/auth/register/' + $role) -Method POST -Body (ConvertTo-Json $body -Compress) | Out-Null
    } catch { if ($_.Exception.Response.StatusCode.value__ -ne 400) { throw } }
}
Write-Host "OK"

Write-Host "=== 2. Login ===" -ForegroundColor Cyan
$elderLogin = Do-Request -Uri ($base + '/api/auth/login') -Method POST -Body (ConvertTo-Json @{ username = 'elder1'; password = '123456'; userType = 'ELDER' } -Compress)
$volLogin = Do-Request -Uri ($base + '/api/auth/login') -Method POST -Body (ConvertTo-Json @{ username = 'vol1'; password = '123456'; userType = 'VOLUNTEER' } -Compress)
$adminLogin = Do-Request -Uri ($base + '/api/auth/login') -Method POST -Body (ConvertTo-Json @{ username = 'admin1'; password = '123456'; userType = 'ADMIN' } -Compress)
$elderToken = $elderLogin.data.token
$volToken = $volLogin.data.token
$adminToken = $adminLogin.data.token
$elderId = $elderLogin.data.userId
Write-Host "OK"

Write-Host "=== 3. Admin grant points to elder ===" -ForegroundColor Cyan
Do-Request -Uri ($base + '/api/admin/elders/' + $elderId + '/grant-points') -Method POST -Headers @{ Authorization = "Bearer $adminToken" } -Body (ConvertTo-Json @{ amount = 100 } -Compress) | Out-Null
$info = Do-Request -Uri ($base + '/api/user/info') -Method GET -Headers @{ Authorization = "Bearer $elderToken" }
Write-Host "Elder points: $($info.data.points)"

Write-Host "=== 4. Elder publish task (10 points reward) ===" -ForegroundColor Cyan
$publish = Do-Request -Uri ($base + '/api/tasks/cooperation/publish') -Method POST -Headers @{ Authorization = "Bearer $elderToken" } -Body (ConvertTo-Json @{ title = 'help buy groceries'; description = 'tomorrow morning'; pointsReward = 10 } -Compress)
$taskId = $publish.data.id
Write-Host "Task id: $taskId"

Write-Host "=== 5. Volunteer: available -> accept -> start -> submit -> elder confirm ===" -ForegroundColor Cyan
Do-Request -Uri ($base + '/api/tasks/cooperation/available') -Method GET -Headers @{ Authorization = "Bearer $volToken" } | Out-Null
Do-Request -Uri ($base + '/api/tasks/cooperation/' + $taskId + '/accept') -Method POST -Headers @{ Authorization = "Bearer $volToken" } | Out-Null
Do-Request -Uri ($base + '/api/tasks/cooperation/' + $taskId + '/start') -Method POST -Headers @{ Authorization = "Bearer $volToken" } | Out-Null
$evidences = @( @{ evidenceType = 'IMAGE'; fileUrl = 'https://example.com/photo.jpg' } )
Do-Request -Uri ($base + '/api/tasks/cooperation/' + $taskId + '/submit') -Method POST -Headers @{ Authorization = "Bearer $volToken" } -Body (ConvertTo-Json @{ note = 'done'; evidences = $evidences } -Compress) | Out-Null
Do-Request -Uri ($base + '/api/tasks/cooperation/' + $taskId + '/confirm') -Method POST -Headers @{ Authorization = "Bearer $elderToken" } | Out-Null
Write-Host "OK"

Write-Host "=== 6. Points after task ===" -ForegroundColor Cyan
$elderInfo = Do-Request -Uri ($base + '/api/user/info') -Method GET -Headers @{ Authorization = "Bearer $elderToken" }
$volInfo = Do-Request -Uri ($base + '/api/user/info') -Method GET -Headers @{ Authorization = "Bearer $volToken" }
Write-Host "Elder points: $($elderInfo.data.points) (expect 90)"
Write-Host "Volunteer points: $($volInfo.data.points) (expect 10)"

Write-Host "=== 7. Elder trigger emergency ===" -ForegroundColor Cyan
Do-Request -Uri ($base + '/api/emergency/trigger') -Method POST -Headers @{ Authorization = "Bearer $elderToken" } -Body (ConvertTo-Json @{ location = 'Building 3' } -Compress) | Out-Null
Write-Host "OK"

Write-Host "=== 8. Admin: pending emergencies -> handle ===" -ForegroundColor Cyan
$pending = Do-Request -Uri ($base + '/api/emergency/pending') -Method GET -Headers @{ Authorization = "Bearer $adminToken" }
if ($pending.data -and $pending.data.Count -gt 0) {
    $alarmId = $pending.data[0].id
    Do-Request -Uri ($base + '/api/emergency/' + $alarmId + '/handle') -Method PATCH -Headers @{ Authorization = "Bearer $adminToken" } -Body (ConvertTo-Json @{ note = 'Contacted family' } -Compress) | Out-Null
}
Write-Host "OK"

Write-Host "=== 9. Admin broadcast to elders ===" -ForegroundColor Cyan
$broadcast = Do-Request -Uri ($base + '/api/admin/notifications/broadcast') -Method POST -Headers @{ Authorization = "Bearer $adminToken" } -Body (ConvertTo-Json @{ targetType = 'ALL_ELDERS'; title = 'Notice'; message = 'Test broadcast' } -Compress)
Write-Host "Sent: $($broadcast.data.sentCount)"

Write-Host "=== 10. Admin: list elders, volunteers, tasks ===" -ForegroundColor Cyan
Do-Request -Uri ($base + '/api/admin/elders') -Method GET -Headers @{ Authorization = "Bearer $adminToken" } | Out-Null
Do-Request -Uri ($base + '/api/admin/volunteers') -Method GET -Headers @{ Authorization = "Bearer $adminToken" } | Out-Null
Do-Request -Uri ($base + '/api/admin/tasks') -Method GET -Headers @{ Authorization = "Bearer $adminToken" } | Out-Null
Write-Host "OK"

Write-Host "`n=== All steps passed ===" -ForegroundColor Green
