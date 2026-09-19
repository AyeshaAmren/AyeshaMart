Add-Type -AssemblyName System.Web
Add-Type -AssemblyName System.Net.Http

$base = "http://localhost:8080/ayesha-mart"
$script:pass = 0
$script:fail = 0

function New-AmSession {
    $handler = New-Object System.Net.Http.HttpClientHandler
    $handler.AllowAutoRedirect = $false
    $handler.UseCookies = $true
    $handler.CookieContainer = New-Object System.Net.CookieContainer
    return (New-Object System.Net.Http.HttpClient($handler))
}
function Send-Am($client, $method, $url, $form) {
    $req = New-Object System.Net.Http.HttpRequestMessage ((New-Object System.Net.Http.HttpMethod($method)), $url)
    if ($null -ne $form) {
        $pairs = @()
        foreach ($k in $form.Keys) { $pairs += ([System.Web.HttpUtility]::UrlEncode($k) + "=" + [System.Web.HttpUtility]::UrlEncode([string]$form[$k])) }
        $req.Content = New-Object System.Net.Http.StringContent(($pairs -join "&"), [System.Text.Encoding]::UTF8, "application/x-www-form-urlencoded")
    }
    $resp = $client.SendAsync($req).Result
    $body = $resp.Content.ReadAsStringAsync().Result
    $loc = ""
    if ($null -ne $resp.Headers.Location) { $loc = $resp.Headers.Location.ToString() }
    return @{ Status = [int]$resp.StatusCode; Location = $loc; Body = $body }
}
function Check($name, $cond, $extra) {
    if ($cond) { $script:pass++; Write-Output ("  PASS  " + $name) }
    else { $script:fail++; Write-Output ("  FAIL  " + $name + "   -> " + $extra) }
}
function CartTotal($session, $expected, $label) {
    $r = Send-Am $session GET "$base/cart" $null
    Check $label (($r.Status -eq 200) -and ($r.Body -match [regex]::Escape($expected))) ("total " + $expected + " not found")
}

Write-Output "== Phase 5 : Buyer Shopping & Cart =="

$anon = New-AmSession
$seller = New-AmSession
$buyer1 = New-AmSession
$buyer2 = New-AmSession

# ---- register + login ----
Send-Am $seller POST "$base/register" @{ fullName = "Seller S"; email = "seller@test.com"; password = "Seller@123"; confirmPassword = "Seller@123"; phone = "9000000001"; address = "Shop St"; role = "seller" } | Out-Null
Send-Am $buyer1 POST "$base/register" @{ fullName = "Buyer One"; email = "buyer1@test.com"; password = "Buyer@123"; confirmPassword = "Buyer@123"; phone = "9000000002"; address = "One St"; role = "buyer" } | Out-Null
Send-Am $buyer2 POST "$base/register" @{ fullName = "Buyer Two"; email = "buyer2@test.com"; password = "Buyer@123"; confirmPassword = "Buyer@123"; phone = "9000000003"; address = "Two St"; role = "buyer" } | Out-Null

$r = Send-Am $seller POST "$base/login" @{ email = "seller@test.com"; password = "Seller@123" }
Check "seller login" (($r.Status -eq 302) -and ($r.Location -match "seller-dashboard")) ("status=" + $r.Status)
$r = Send-Am $buyer1 POST "$base/login" @{ email = "buyer1@test.com"; password = "Buyer@123" }
Check "buyer1 login" (($r.Status -eq 302) -and ($r.Location -match "buyer-dashboard")) ("status=" + $r.Status)
$r = Send-Am $buyer2 POST "$base/login" @{ email = "buyer2@test.com"; password = "Buyer@123" }
Check "buyer2 login" (($r.Status -eq 302) -and ($r.Location -match "buyer-dashboard")) ("status=" + $r.Status)

# ---- seller creates products ----
$defs = @(
    @{ name = "Alpha Phone";  category = "Electronics";   price = "100"; stock = "10"; status = "active" },
    @{ name = "Beta Cable";   category = "Electronics";   price = "50";  stock = "2";  status = "active" },
    @{ name = "Gamma Lamp";   category = "Home & Living"; price = "30";  stock = "0";  status = "active" },
    @{ name = "Delta Chair";  category = "Home & Living"; price = "80";  stock = "5";  status = "inactive" }
)
foreach ($d in $defs) {
    $r = Send-Am $seller POST "$base/seller/product" @{
        name = $d.name; description = "A quality product for testing purposes."; category = $d.category;
        price = $d.price; stock = $d.stock; image = ""; status = $d.status
    }
    Check ("create " + $d.name) (($r.Status -eq 302) -and ($r.Location -match "msg=created")) ("status=" + $r.Status)
}

$r = Send-Am $seller GET "$base/seller/products" $null
$ids = [regex]::Matches($r.Body, "P\d{4}") | ForEach-Object { $_.Value } | Select-Object -Unique
Check "captured 4 product ids" ($ids.Count -eq 4) ("count=" + $ids.Count + " ids=" + ($ids -join ","))
$X = $ids[0]; $Y = $ids[1]; $Z = $ids[2]; $W = $ids[3]

# ---- empty cart ----
$r = Send-Am $buyer1 GET "$base/cart" $null
Check "buyer1 empty cart" (($r.Status -eq 200) -and ($r.Body -match "cart is empty")) ("status=" + $r.Status)

# ---- 1. add products ----
$r = Send-Am $buyer1 POST "$base/cart" @{ action = "add"; productId = $X; quantity = "2" }
Check "add Alpha x2" (($r.Status -eq 302) -and ($r.Location -match "msg=added")) ("status=" + $r.Status + " loc=" + $r.Location)
$r = Send-Am $buyer1 POST "$base/cart" @{ action = "add"; productId = $X; quantity = "3" }
Check "add Alpha x3 (merge)" (($r.Status -eq 302) -and ($r.Location -match "msg=added")) ("status=" + $r.Status)
$r = Send-Am $buyer1 POST "$base/cart" @{ action = "add"; productId = $Y; quantity = "1" }
Check "add Beta x1" (($r.Status -eq 302) -and ($r.Location -match "msg=added")) ("status=" + $r.Status)
CartTotal $buyer1 "550.00" "cart total 550.00 after adds"

$r = Send-Am $buyer1 GET "$base/cart" $null
Check "cart lists both products" (($r.Body -match "Alpha Phone") -and ($r.Body -match "Beta Cable")) "products missing"
Check "cart badge count present" ($r.Body -match ">6<" -or $r.Body -match "Cart") "badge"

# ---- 2. change quantity ----
$r = Send-Am $buyer1 POST "$base/cart" @{ action = "update"; productId = $X; quantity = "7" }
Check "update Alpha to 7" (($r.Status -eq 302) -and ($r.Location -match "msg=updated")) ("status=" + $r.Status + " loc=" + $r.Location)
CartTotal $buyer1 "750.00" "cart total 750.00 after update"

# ---- 7. quantity greater than stock ----
$r = Send-Am $buyer1 POST "$base/cart" @{ action = "update"; productId = $X; quantity = "11" }
Check "update Alpha to 11 (>stock) rejected" (($r.Status -eq 302) -and ($r.Location -match "error=stock")) ("loc=" + $r.Location)
CartTotal $buyer1 "750.00" "cart unchanged after over-stock update"

$r = Send-Am $buyer1 POST "$base/cart" @{ action = "update"; productId = $Y; quantity = "3" }
Check "update Beta to 3 (>stock 2) rejected" (($r.Status -eq 302) -and ($r.Location -match "error=stock")) ("loc=" + $r.Location)

# ---- quantity must be > 0 ----
$r = Send-Am $buyer1 POST "$base/cart" @{ action = "add"; productId = $X; quantity = "0" }
Check "add qty 0 rejected" (($r.Status -eq 302) -and ($r.Location -match "error=quantity")) ("loc=" + $r.Location)
$r = Send-Am $buyer1 POST "$base/cart" @{ action = "update"; productId = $X; quantity = "-1" }
Check "update qty -1 rejected" (($r.Status -eq 302) -and ($r.Location -match "error=quantity")) ("loc=" + $r.Location)
$r = Send-Am $buyer1 POST "$base/cart" @{ action = "add"; productId = $X; quantity = "abc" }
Check "add qty abc rejected" (($r.Status -eq 302) -and ($r.Location -match "error=quantity")) ("loc=" + $r.Location)

# ---- 6. out of stock ----
$r = Send-Am $buyer1 POST "$base/cart" @{ action = "add"; productId = $Z; quantity = "1" }
Check "add out-of-stock rejected" (($r.Status -eq 302) -and ($r.Location -match "error=outofstock")) ("loc=" + $r.Location)
$r = Send-Am $buyer1 GET "$base/cart" $null
Check "out-of-stock product not in cart" ($r.Body -notmatch "Gamma Lamp") "out-of-stock leaked into cart"

# ---- inactive product ----
$r = Send-Am $buyer1 POST "$base/cart" @{ action = "add"; productId = $W; quantity = "1" }
Check "add inactive product rejected" (($r.Status -eq 302) -and ($r.Location -match "error=unavailable")) ("loc=" + $r.Location)

# ---- add beyond remaining stock ----
$r = Send-Am $buyer1 POST "$base/cart" @{ action = "update"; productId = $X; quantity = "10" }
Check "update Alpha to 10 (max)" (($r.Status -eq 302) -and ($r.Location -match "msg=updated")) ("loc=" + $r.Location)
$r = Send-Am $buyer1 POST "$base/cart" @{ action = "add"; productId = $X; quantity = "1" }
Check "add Alpha beyond stock rejected" (($r.Status -eq 302) -and ($r.Location -match "error=stock")) ("loc=" + $r.Location)

# ---- 3. remove product ----
$r = Send-Am $buyer1 POST "$base/cart" @{ action = "remove"; productId = $Y }
Check "remove Beta" (($r.Status -eq 302) -and ($r.Location -match "msg=removed")) ("loc=" + $r.Location)
CartTotal $buyer1 "1,000.00" "cart total 1,000.00 after remove"

# ---- 8. cart ownership / security ----
$r = Send-Am $buyer2 GET "$base/cart" $null
Check "buyer2 cart is empty/independent" (($r.Status -eq 200) -and ($r.Body -match "cart is empty")) ("status=" + $r.Status)
$r = Send-Am $buyer2 POST "$base/cart" @{ action = "update"; productId = $X; quantity = "5" }
Check "buyer2 cannot update buyer1 item" (($r.Status -eq 302) -and ($r.Location -match "error=notincart")) ("loc=" + $r.Location)
$r = Send-Am $buyer2 GET "$base/cart" $null
Check "buyer1 cart untouched by buyer2" ($r.Body -match "cart is empty") "buyer2 cart not empty"
CartTotal $buyer1 "1,000.00" "buyer1 cart intact after buyer2 attempt"

$r = Send-Am $anon GET "$base/cart" $null
Check "anon cart -> login" (($r.Status -eq 302) -and ($r.Location -match "/login\?error=loginRequired")) ("loc=" + $r.Location)
$r = Send-Am $seller GET "$base/cart" $null
Check "seller cart -> forbidden" (($r.Status -eq 302) -and ($r.Location -match "error=forbidden")) ("loc=" + $r.Location)

# ---- 5. search / filter / sort ----
$r = Send-Am $anon GET "$base/products?q=alpha" $null
Check "search by name" (($r.Status -eq 200) -and ($r.Body -match "Alpha Phone")) ("status=" + $r.Status)
Check "search excludes others" ($r.Body -notmatch "Beta Cable") "search leak"

$r = Send-Am $anon GET "$base/products?category=Electronics" $null
Check "filter by category" (($r.Body -match "Alpha Phone") -and ($r.Body -match "Beta Cable")) "electronics missing"
Check "filter excludes other category" ($r.Body -notmatch "Gamma Lamp") "category leak"

$r = Send-Am $anon GET "$base/products?sort=priceAsc" $null
$posBeta = $r.Body.IndexOf("Beta Cable"); $posAlpha = $r.Body.IndexOf("Alpha Phone")
Check "sort price ascending" (($posBeta -ge 0) -and ($posAlpha -ge 0) -and ($posBeta -lt $posAlpha)) ("beta=" + $posBeta + " alpha=" + $posAlpha)

# inactive + out-of-stock not in public catalogue
$r = Send-Am $anon GET "$base/products" $null
Check "inactive product hidden" ($r.Body -notmatch "Delta Chair") "inactive leaked"
Check "out-of-stock shown but not addable" (($r.Body -match "Gamma Lamp") -and ($r.Body -match "Out of stock")) "out-of-stock handling wrong"

# ---- product details ----
$r = Send-Am $anon GET "$base/product?id=$X" $null
Check "details 200 + add to cart" (($r.Status -eq 200) -and ($r.Body -match "Add to Cart")) ("status=" + $r.Status)
Check "details shows rating area" ($r.Body -match "No ratings yet") "rating area missing"
Check "details shows seller" ($r.Body -match "Seller S") "seller missing"
$r = Send-Am $anon GET "$base/product?id=$Z" $null
Check "out-of-stock details blocks add" (($r.Body -match "Out of stock") -and ($r.Body -notmatch "Add to Cart")) "out-of-stock button wrong"

# ---- 4. clear cart ----
$r = Send-Am $buyer1 POST "$base/cart" @{ action = "clear" }
Check "clear cart" (($r.Status -eq 302) -and ($r.Location -match "msg=cleared")) ("loc=" + $r.Location)
$r = Send-Am $buyer1 GET "$base/cart" $null
Check "cart empty after clear" ($r.Body -match "cart is empty") "cart not cleared"

Write-Output ""
Write-Output ("RESULT: " + $script:pass + " passed, " + $script:fail + " failed")
if ($script:fail -gt 0) { exit 1 } else { exit 0 }
