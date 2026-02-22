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
            $stream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream, [System.Text.Encoding]::UTF8)
            $errBody = $reader.ReadToEnd()
            Write-Host "ERROR $status : $errBody" -ForegroundColor Red
        } else {
            Write-Host "ERROR : $($_.Exception.Message)" -ForegroundColor Red
        }
        throw
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "  Test Product Exchange Function" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host ""

# Step 0: Register users
Write-Host "[Step 0] Register users..." -ForegroundColor Cyan
try {
    $regAdmin = @{ username = 'admin1'; password = '123456'; realName = 'Admin_Test' }
    Do-Request -Uri "$base/api/auth/register/admin" -Method POST -Body (ConvertTo-Json $regAdmin -Compress) | Out-Null
    Write-Host "  OK Registered admin" -ForegroundColor Green
} catch {
    Write-Host "  - Admin already exists" -ForegroundColor Gray
}

try {
    $regVol = @{ username = 'vol1'; password = '123456'; realName = 'Volunteer_Test' }
    Do-Request -Uri "$base/api/auth/register/volunteer" -Method POST -Body (ConvertTo-Json $regVol -Compress) | Out-Null
    Write-Host "  OK Registered volunteer" -ForegroundColor Green
} catch {
    Write-Host "  - Volunteer already exists" -ForegroundColor Gray
}

# Step 1: Login
Write-Host ""
Write-Host "[Step 1] Login..." -ForegroundColor Cyan
$adminLogin = Do-Request -Uri "$base/api/auth/login" -Method POST -Body (ConvertTo-Json @{ username = 'admin1'; password = '123456'; userType = 'ADMIN' } -Compress)
$volLogin = Do-Request -Uri "$base/api/auth/login" -Method POST -Body (ConvertTo-Json @{ username = 'vol1'; password = '123456'; userType = 'VOLUNTEER' } -Compress)

$adminToken = $adminLogin.data.token
$volToken = $volLogin.data.token
$volId = $volLogin.data.userId

Write-Host "  OK Login successful" -ForegroundColor Green
Write-Host "  Volunteer ID: $volId" -ForegroundColor Gray
Write-Host ""

# Step 2: Grant points to volunteer
Write-Host "[Step 2] Grant points to volunteer..." -ForegroundColor Cyan
$grantBody = @{ amount = 500 }
Do-Request -Uri "$base/api/admin/volunteers/$volId/grant-points" -Method POST -Headers @{ Authorization = "Bearer $adminToken" } -Body (ConvertTo-Json $grantBody -Compress) | Out-Null
Write-Host "  OK Granted 500 points to volunteer" -ForegroundColor Green

# Check volunteer points
$volInfo = Do-Request -Uri "$base/api/user/info" -Method GET -Headers @{ Authorization = "Bearer $volToken" }
Write-Host "  Volunteer current points: $($volInfo.data.points)" -ForegroundColor Gray
Write-Host ""

# Step 3: Create products
Write-Host "[Step 3] Create products..." -ForegroundColor Cyan
$product1 = @{
    name = "Thermos Cup"
    description = "Stainless steel thermos cup, 500ml"
    pointsPrice = 50
    stock = 10
    imageUrl = "http://example.com/cup.jpg"
}
$product1Res = Do-Request -Uri "$base/api/admin/products" -Method POST -Headers @{ Authorization = "Bearer $adminToken" } -Body (ConvertTo-Json $product1 -Compress)
$product1Id = $product1Res.data.id
Write-Host "  OK Created product 1: $($product1Res.data.name) (ID: $product1Id, Price: $($product1Res.data.pointsPrice) points)" -ForegroundColor Green

$product2 = @{
    name = "Umbrella"
    description = "Folding umbrella, UV protection"
    pointsPrice = 30
    stock = 20
}
$product2Res = Do-Request -Uri "$base/api/admin/products" -Method POST -Headers @{ Authorization = "Bearer $adminToken" } -Body (ConvertTo-Json $product2 -Compress)
$product2Id = $product2Res.data.id
Write-Host "  OK Created product 2: $($product2Res.data.name) (ID: $product2Id, Price: $($product2Res.data.pointsPrice) points)" -ForegroundColor Green
Write-Host ""

# Step 4: List products
Write-Host "[Step 4] List products..." -ForegroundColor Cyan
$productsList = Do-Request -Uri "$base/api/admin/products?page=0&size=10" -Method GET -Headers @{ Authorization = "Bearer $adminToken" }
Write-Host "  OK Found $($productsList.data.content.Count) products" -ForegroundColor Green
foreach ($p in $productsList.data.content) {
    Write-Host "  - $($p.name): $($p.pointsPrice) points, Stock: $($p.stock)" -ForegroundColor Gray
}
Write-Host ""

# Step 5: Exchange product 1
Write-Host "[Step 5] Exchange product 1 (quantity: 2)..." -ForegroundColor Cyan
$exchange1 = @{
    volunteerId = $volId
    productId = $product1Id
    quantity = 2
}
$exchange1Res = Do-Request -Uri "$base/api/admin/exchanges/exchange" -Method POST -Headers @{ Authorization = "Bearer $adminToken" } -Body (ConvertTo-Json $exchange1 -Compress)
Write-Host "  OK Exchange successful!" -ForegroundColor Green
Write-Host "  Product: $($exchange1Res.data.productName)" -ForegroundColor Gray
Write-Host "  Quantity: $($exchange1Res.data.quantity)" -ForegroundColor Gray
Write-Host "  Points cost: $($exchange1Res.data.pointsCost)" -ForegroundColor Gray
Write-Host "  Volunteer: $($exchange1Res.data.volunteerName)" -ForegroundColor Gray

# Check volunteer points after exchange
$volInfo2 = Do-Request -Uri "$base/api/user/info" -Method GET -Headers @{ Authorization = "Bearer $volToken" }
Write-Host "  Volunteer points after exchange: $($volInfo2.data.points)" -ForegroundColor Gray
Write-Host ""

# Step 6: Exchange product 2
Write-Host "[Step 6] Exchange product 2 (quantity: 1)..." -ForegroundColor Cyan
$exchange2 = @{
    volunteerId = $volId
    productId = $product2Id
    quantity = 1
}
$exchange2Res = Do-Request -Uri "$base/api/admin/exchanges/exchange" -Method POST -Headers @{ Authorization = "Bearer $adminToken" } -Body (ConvertTo-Json $exchange2 -Compress)
Write-Host "  OK Exchange successful!" -ForegroundColor Green
Write-Host "  Product: $($exchange2Res.data.productName)" -ForegroundColor Gray
Write-Host "  Quantity: $($exchange2Res.data.quantity)" -ForegroundColor Gray
Write-Host "  Points cost: $($exchange2Res.data.pointsCost)" -ForegroundColor Gray

# Check volunteer points after second exchange
$volInfo3 = Do-Request -Uri "$base/api/user/info" -Method GET -Headers @{ Authorization = "Bearer $volToken" }
Write-Host "  Volunteer points after second exchange: $($volInfo3.data.points)" -ForegroundColor Gray
Write-Host ""

# Step 7: List exchange records
Write-Host "[Step 7] List exchange records..." -ForegroundColor Cyan
$exchangesList = Do-Request -Uri "$base/api/admin/exchanges?page=0&size=10" -Method GET -Headers @{ Authorization = "Bearer $adminToken" }
Write-Host "  OK Found $($exchangesList.data.content.Count) exchange records" -ForegroundColor Green
foreach ($e in $exchangesList.data.content) {
    $timeStr = Get-Date $e.createdAt -Format 'yyyy-MM-dd HH:mm:ss'
    Write-Host "  - [$timeStr] $($e.volunteerName) exchanged $($e.quantity)x $($e.productName) for $($e.pointsCost) points" -ForegroundColor Gray
}
Write-Host ""

# Step 8: Check product stock
Write-Host "[Step 8] Check product stock..." -ForegroundColor Cyan
$product1Detail = Do-Request -Uri "$base/api/admin/products/$product1Id" -Method GET -Headers @{ Authorization = "Bearer $adminToken" }
Write-Host "  Product 1 ($($product1Detail.data.name)) stock: $($product1Detail.data.stock)" -ForegroundColor Gray

$product2Detail = Do-Request -Uri "$base/api/admin/products/$product2Id" -Method GET -Headers @{ Authorization = "Bearer $adminToken" }
Write-Host "  Product 2 ($($product2Detail.data.name)) stock: $($product2Detail.data.stock)" -ForegroundColor Gray
Write-Host ""

Write-Host "========================================" -ForegroundColor Yellow
Write-Host "  Test Completed!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Yellow
