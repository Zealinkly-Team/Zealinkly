# Complete OCR Login Test Script
# Flow: Admin creates users with card info -> Users login via OCR

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

function Convert-ImageToBase64 {
    param([string]$ImagePath)
    
    if (-not (Test-Path $ImagePath)) {
        throw "Image file not found: $ImagePath"
    }
    
    $bytes = [System.IO.File]::ReadAllBytes($ImagePath)
    $base64 = [Convert]::ToBase64String($bytes)
    return $base64
}

Write-Host "`n==========================================" -ForegroundColor Yellow
Write-Host "Complete OCR Login Test" -ForegroundColor Yellow
Write-Host "Flow: Admin creates users -> OCR login" -ForegroundColor Yellow
Write-Host "==========================================`n" -ForegroundColor Yellow

# Step 0: Login as admin
Write-Host "[Step 0] Login as admin..." -ForegroundColor Cyan
try {
    $adminLogin = Do-Request -Uri "$base/api/auth/login" -Method POST -Body @{
        username = "admin1"
        password = "123456"
        userType = "ADMIN"
    }
    $adminToken = $adminLogin.data.token
    Write-Host "  - Admin logged in successfully" -ForegroundColor Green
} catch {
    Write-Host "  - Admin login failed, please register admin first" -ForegroundColor Red
    Write-Host "  - Registering admin..." -ForegroundColor Yellow
    try {
        Do-Request -Uri "$base/api/auth/register/admin" -Method POST -Body @{
            username = "admin1"
            password = "123456"
            realName = "Admin"
            roleLevel = 1
        } | Out-Null
        $adminLogin = Do-Request -Uri "$base/api/auth/login" -Method POST -Body @{
            username = "admin1"
            password = "123456"
            userType = "ADMIN"
        }
        $adminToken = $adminLogin.data.token
        Write-Host "  - Admin registered and logged in" -ForegroundColor Green
    } catch {
        Write-Host "  - Failed to register admin: $_" -ForegroundColor Red
        exit 1
    }
}

# Step 1: Get image paths (hardcoded)
Write-Host "`n[Step 1] Loading card images..." -ForegroundColor Cyan
$elderImagePath = "E:\sdu\Zealinkly\cards\1.jpg"
$volunteerImagePath = "E:\sdu\Zealinkly\cards\2.jpg"

Write-Host "  - Elder image path: $elderImagePath" -ForegroundColor Gray
Write-Host "  - Volunteer image path: $volunteerImagePath" -ForegroundColor Gray

if (-not (Test-Path $elderImagePath)) {
    Write-Host "  - Elder image not found: $elderImagePath" -ForegroundColor Red
    Write-Host "  - Please check the path and try again" -ForegroundColor Yellow
    exit 1
}

if (-not (Test-Path $volunteerImagePath)) {
    Write-Host "  - Volunteer image not found: $volunteerImagePath" -ForegroundColor Red
    Write-Host "  - Please check the path and try again" -ForegroundColor Yellow
    exit 1
}

Write-Host "  - Images found" -ForegroundColor Green

# Step 2: Convert images to base64
Write-Host "`n[Step 2] Converting images to base64..." -ForegroundColor Cyan
try {
    $elderImageBase64 = Convert-ImageToBase64 -ImagePath $elderImagePath
    $volunteerImageBase64 = Convert-ImageToBase64 -ImagePath $volunteerImagePath
    Write-Host "  - Images converted successfully" -ForegroundColor Green
} catch {
    Write-Host "  - Failed to convert images: $_" -ForegroundColor Red
    exit 1
}

# Step 3: Use OCR to recognize card numbers
Write-Host "`n[Step 3] Recognizing card numbers using OCR..." -ForegroundColor Cyan

# Recognize Elder's card
Write-Host "  - Recognizing Elder's card..." -ForegroundColor White
$elderCardNumber = $null
$elderCardType = $null

# Try ID card first
try {
    Write-Host "    - Trying ID card recognition..." -ForegroundColor Gray
    $elderOcrResult = Do-Request -Uri "$base/api/admin/ocr/recognize" -Method POST -Headers @{
        Authorization = "Bearer $adminToken"
    } -Body @{
        imageBase64 = $elderImageBase64
        cardType = "ID_CARD"
    }
    $elderCardNumber = $elderOcrResult.data.cardNumber
    $elderCardType = $elderOcrResult.data.cardType
    Write-Host "    - Recognized as ID card: $elderCardNumber" -ForegroundColor Green
} catch {
    Write-Host "    - ID card recognition failed, trying community card..." -ForegroundColor Yellow
    try {
        $elderOcrResult = Do-Request -Uri "$base/api/admin/ocr/recognize" -Method POST -Headers @{
            Authorization = "Bearer $adminToken"
        } -Body @{
            imageBase64 = $elderImageBase64
            cardType = "COMMUNITY_CARD"
        }
        $elderCardNumber = $elderOcrResult.data.cardNumber
        $elderCardType = $elderOcrResult.data.cardType
        Write-Host "    - Recognized as Community card: $elderCardNumber" -ForegroundColor Green
    } catch {
        Write-Host "    - All OCR recognition methods failed" -ForegroundColor Red
        Write-Host "    - Please enter manually:" -ForegroundColor Yellow
        $elderCardNumber = (Read-Host "      Card number").Trim()
        $elderCardType = (Read-Host "      Card type (ID_CARD or COMMUNITY_CARD)").Trim()
    }
}

# Recognize Volunteer's card
Write-Host "  - Recognizing Volunteer's card..." -ForegroundColor White
$volunteerCardNumber = $null
$volunteerCardType = $null

# Try ID card first
try {
    Write-Host "    - Trying ID card recognition..." -ForegroundColor Gray
    $volunteerOcrResult = Do-Request -Uri "$base/api/admin/ocr/recognize" -Method POST -Headers @{
        Authorization = "Bearer $adminToken"
    } -Body @{
        imageBase64 = $volunteerImageBase64
        cardType = "ID_CARD"
    }
    $volunteerCardNumber = $volunteerOcrResult.data.cardNumber
    $volunteerCardType = $volunteerOcrResult.data.cardType
    Write-Host "    - Recognized as ID card: $volunteerCardNumber" -ForegroundColor Green
} catch {
    Write-Host "    - ID card recognition failed, trying community card..." -ForegroundColor Yellow
    try {
        $volunteerOcrResult = Do-Request -Uri "$base/api/admin/ocr/recognize" -Method POST -Headers @{
            Authorization = "Bearer $adminToken"
        } -Body @{
            imageBase64 = $volunteerImageBase64
            cardType = "COMMUNITY_CARD"
        }
        $volunteerCardNumber = $volunteerOcrResult.data.cardNumber
        $volunteerCardType = $volunteerOcrResult.data.cardType
        Write-Host "    - Recognized as Community card: $volunteerCardNumber" -ForegroundColor Green
    } catch {
        Write-Host "    - All OCR recognition methods failed" -ForegroundColor Red
        Write-Host "    - Please enter manually:" -ForegroundColor Yellow
        $volunteerCardNumber = (Read-Host "      Card number").Trim()
        $volunteerCardType = (Read-Host "      Card type (ID_CARD or COMMUNITY_CARD)").Trim()
    }
}

# Step 4: Admin creates Elder with card info
Write-Host "`n[Step 4] Admin creates Elder with card info..." -ForegroundColor Cyan
$elderUsername = "elder_ocr_test_$(Get-Date -Format 'yyyyMMddHHmmss')"
$elderId = $null

try {
    $createElderBody = @{
        username = $elderUsername
        password = "123456"
        realName = "Test Elder OCR"
        phone = "13800138000"
        address = "Test Address"
    }
    
    if ($elderCardType -eq "ID_CARD") {
        $createElderBody.idCardNumber = $elderCardNumber
    } else {
        $createElderBody.communityCardNumber = $elderCardNumber
    }
    
    $elderCreate = Do-Request -Uri "$base/api/admin/elders" -Method POST -Headers @{
        Authorization = "Bearer $adminToken"
    } -Body $createElderBody
    
    $elderId = $elderCreate.data.id
    Write-Host "  - Elder created successfully: ID=$elderId, Username=$elderUsername" -ForegroundColor Green
    Write-Host "    Card Type: $elderCardType, Card Number: $elderCardNumber" -ForegroundColor Gray
} catch {
    Write-Host "  - Failed to create Elder: $_" -ForegroundColor Red
    exit 1
}

# Step 5: Admin creates Volunteer with card info
Write-Host "`n[Step 5] Admin creates Volunteer with card info..." -ForegroundColor Cyan
$volunteerUsername = "vol_ocr_test_$(Get-Date -Format 'yyyyMMddHHmmss')"
$volunteerId = $null

try {
    $createVolunteerBody = @{
        username = $volunteerUsername
        password = "123456"
        realName = "Test Volunteer OCR"
        phone = "13900139000"
    }
    
    if ($volunteerCardType -eq "ID_CARD") {
        $createVolunteerBody.idCardNumber = $volunteerCardNumber
    } else {
        $createVolunteerBody.communityCardNumber = $volunteerCardNumber
    }
    
    $volunteerCreate = Do-Request -Uri "$base/api/admin/volunteers" -Method POST -Headers @{
        Authorization = "Bearer $adminToken"
    } -Body $createVolunteerBody
    
    $volunteerId = $volunteerCreate.data.id
    Write-Host "  - Volunteer created successfully: ID=$volunteerId, Username=$volunteerUsername" -ForegroundColor Green
    Write-Host "    Card Type: $volunteerCardType, Card Number: $volunteerCardNumber" -ForegroundColor Gray
} catch {
    Write-Host "  - Failed to create Volunteer: $_" -ForegroundColor Red
    exit 1
}

# Step 6: Test OCR login for Elder
Write-Host "`n[Step 6] Testing OCR login for Elder..." -ForegroundColor Cyan
try {
    $elderOcrLogin = Do-Request -Uri "$base/api/auth/login-by-card" -Method POST -Body @{
        userType = "ELDER"
        imageBase64 = $elderImageBase64
        cardType = $elderCardType
    }
    Write-Host "  - Elder OCR login successful!" -ForegroundColor Green
    Write-Host "    Token: $($elderOcrLogin.data.token.Substring(0, [Math]::Min(50, $elderOcrLogin.data.token.Length)))..." -ForegroundColor Gray
    Write-Host "    User ID: $($elderOcrLogin.data.userId)" -ForegroundColor Gray
    Write-Host "    Username: $($elderOcrLogin.data.username)" -ForegroundColor Gray
    Write-Host "    User Type: $($elderOcrLogin.data.userType)" -ForegroundColor Gray
    
    # Verify login by getting user info
    $elderInfo = Do-Request -Uri "$base/api/user/info" -Method GET -Headers @{
        Authorization = "Bearer $($elderOcrLogin.data.token)"
    }
    Write-Host "    Verified: Real Name = $($elderInfo.data.realName)" -ForegroundColor Gray
} catch {
    Write-Host "  - Elder OCR login failed: $_" -ForegroundColor Red
}

# Step 7: Test OCR login for Volunteer
Write-Host "`n[Step 7] Testing OCR login for Volunteer..." -ForegroundColor Cyan
try {
    $volunteerOcrLogin = Do-Request -Uri "$base/api/auth/login-by-card" -Method POST -Body @{
        userType = "VOLUNTEER"
        imageBase64 = $volunteerImageBase64
        cardType = $volunteerCardType
    }
    Write-Host "  - Volunteer OCR login successful!" -ForegroundColor Green
    Write-Host "    Token: $($volunteerOcrLogin.data.token.Substring(0, [Math]::Min(50, $volunteerOcrLogin.data.token.Length)))..." -ForegroundColor Gray
    Write-Host "    User ID: $($volunteerOcrLogin.data.userId)" -ForegroundColor Gray
    Write-Host "    Username: $($volunteerOcrLogin.data.username)" -ForegroundColor Gray
    Write-Host "    User Type: $($volunteerOcrLogin.data.userType)" -ForegroundColor Gray
    
    # Verify login by getting user info
    $volunteerInfo = Do-Request -Uri "$base/api/user/info" -Method GET -Headers @{
        Authorization = "Bearer $($volunteerOcrLogin.data.token)"
    }
    Write-Host "    Verified: Real Name = $($volunteerInfo.data.realName)" -ForegroundColor Gray
} catch {
    Write-Host "  - Volunteer OCR login failed: $_" -ForegroundColor Red
}

Write-Host "`n==========================================" -ForegroundColor Yellow
Write-Host "OCR Login Test Completed!" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Yellow
Write-Host "Summary:" -ForegroundColor Cyan
Write-Host "  Elder: Username=$elderUsername, Card=$elderCardNumber" -ForegroundColor White
Write-Host "  Volunteer: Username=$volunteerUsername, Card=$volunteerCardNumber" -ForegroundColor White
