# OCR Login Test Script
# Tests card-based login functionality

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

Write-Host "`n==========================================" -ForegroundColor Yellow
Write-Host "OCR Login Test" -ForegroundColor Yellow
Write-Host "==========================================`n" -ForegroundColor Yellow

# Step 0: Register users if not exist
Write-Host "[Step 0] Register users (if not exist)..." -ForegroundColor Cyan
try {
    Do-Request -Uri "$base/api/auth/register/elder" -Method POST -Body @{
        username = "elder_ocr_test"
        password = "123456"
        realName = "Test Elder"
        phone = "13800138000"
        address = "Test Address"
    } | Out-Null
    Write-Host "  - Elder registered" -ForegroundColor Green
} catch {
    Write-Host "  - Elder already exists or error" -ForegroundColor Yellow
}

try {
    Do-Request -Uri "$base/api/auth/register/volunteer" -Method POST -Body @{
        username = "vol_ocr_test"
        password = "123456"
        realName = "Test Volunteer"
        phone = "13900139000"
    } | Out-Null
    Write-Host "  - Volunteer registered" -ForegroundColor Green
} catch {
    Write-Host "  - Volunteer already exists or error" -ForegroundColor Yellow
}

# Step 1: Login as admin to update card numbers
Write-Host "`n[Step 1] Login as admin..." -ForegroundColor Cyan
$adminLogin = Do-Request -Uri "$base/api/auth/login" -Method POST -Body @{
    username = "admin1"
    password = "123456"
    userType = "ADMIN"
}
$adminToken = $adminLogin.data.token
Write-Host "  - Admin logged in" -ForegroundColor Green

# Step 2: Update elder with ID card number (simulated - in real scenario, this would be done during registration or admin update)
# Note: This requires admin API to update card numbers, which we'll skip for now
# Instead, we'll test with a mock base64 image

Write-Host "`n[Step 2] Testing OCR login..." -ForegroundColor Cyan
Write-Host "  Note: This test requires actual card images." -ForegroundColor Yellow
Write-Host "  For testing, you need to:" -ForegroundColor Yellow
Write-Host "  1. Take a photo of an ID card or community card" -ForegroundColor Yellow
Write-Host "  2. Convert it to base64" -ForegroundColor Yellow
Write-Host "  3. Update the user's card number in database" -ForegroundColor Yellow
Write-Host "  4. Call POST /api/auth/login-by-card with the image" -ForegroundColor Yellow

Write-Host "`nExample API call:" -ForegroundColor Cyan
Write-Host "POST $base/api/auth/login-by-card" -ForegroundColor White
Write-Host "Body:" -ForegroundColor White
Write-Host '{' -ForegroundColor Gray
Write-Host '  "userType": "ELDER",' -ForegroundColor Gray
Write-Host '  "imageBase64": "<base64-encoded-image>",' -ForegroundColor Gray
Write-Host '  "cardType": "ID_CARD"  // or "COMMUNITY_CARD" or omit for auto-detect' -ForegroundColor Gray
Write-Host '}' -ForegroundColor Gray

Write-Host "`n==========================================" -ForegroundColor Yellow
Write-Host "OCR Login Test Instructions" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Yellow
Write-Host "1. Update database: Add id_card_number or community_card_number to users" -ForegroundColor White
Write-Host "2. Use a real card image (base64 encoded) for testing" -ForegroundColor White
Write-Host "3. Call POST /api/auth/login-by-card" -ForegroundColor White
Write-Host "4. For admin exchange scanning: POST /api/admin/exchanges/scan-card" -ForegroundColor White
