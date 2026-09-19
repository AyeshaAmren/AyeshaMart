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
        foreach ($k in $form.Keys) {
            $pairs += ([System.Web.HttpUtility]::UrlEncode($k) + "=" + [System.Web.HttpUtility]::UrlEncode([string]$form[$k]))
        }
        $req.Content = New-Object System.Net.Http.StringContent(($pairs -join "&"), [System.Text.Encoding]::UTF8, "application/x-www-form-urlencoded")
    }
    $resp = $client.SendAsync($req).Result
    $body = $resp.Content.ReadAsStringAsync().Result
    $loc = ""
    if ($null -ne $resp.Headers.Location) { $loc = $resp.Headers.Location.ToString() }
    return @{ Status = [int]$resp.StatusCode; Location = $loc; Body = $body }
}

function Check($name, $cond, $extra) {
    if ($cond) {
        $script:pass++
        Write-Output ("  PASS  " + $name)
    } else {
        $script:fail++
        Write-Output ("  FAIL  " + $name + "   -> " + $extra)
    }
}

Write-Output "== Phase 4 : Product Management =="

# ---- Sessions ----
$anon = New-AmSession
$sellerA = New-AmSession
$sellerB = New-AmSession
$buyer = New-AmSession

# ---- Registration ----
foreach ($s in @(
    @{ c = $sellerA; d = @{ fullName = "Seller A"; email = "sellerA@test.com"; password = "Seller@123"; confirmPassword = "Seller@123"; phone = "9999999901"; address = "1 A Street"; role = "seller" } },
    @{ c = $sellerB; d = @{ fullName = "Seller B"; email = "sellerB@test.com"; password = "Seller@123"; confirmPassword = "Seller@123"; phone = "9999999902"; address = "2 B Street"; role = "seller" } },
    @{ c = $buyer;   d = @{ fullName = "Buyer One"; email = "buyer@test.com"; password = "Buyer@123"; confirmPassword = "Buyer@123"; phone = "9999999903"; address = "3 C Street"; role = "buyer" } }
)) {
    $r = Send-Am $s.c POST "$base/register" $s.d
    Check ("register " + $s.d.email) ($r.Status -eq 200) ("status=" + $r.Status)
}

# ---- Logins ----
$r = Send-Am $sellerA POST "$base/login" @{ email = "sellerA@test.com"; password = "Seller@123" }
Check "login Seller A -> seller dashboard" (($r.Status -eq 302) -and ($r.Location -match "seller-dashboard")) ("status=" + $r.Status + " loc=" + $r.Location)

$r = Send-Am $sellerB POST "$base/login" @{ email = "sellerB@test.com"; password = "Seller@123" }
Check "login Seller B" (($r.Status -eq 302) -and ($r.Location -match "/seller-dashboard")) ("status=" + $r.Status + " loc=" + $r.Location)

$r = Send-Am $buyer POST "$base/login" @{ email = "buyer@test.com"; password = "Buyer@123" }
Check "login Buyer -> /buyer-dashboard.jsp" (($r.Status -eq 302) -and ($r.Location -match "buyer-dashboard")) ("status=" + $r.Status + " loc=" + $r.Location)

# ---- Seller A creates a product ----
$r = Send-Am $sellerA POST "$base/seller/product" @{
    name = "Wireless Headphones"; description = "Great sound quality over bluetooth."; category = "Electronics";
    price = "1999.50"; stock = "10"; image = "https://example.com/headphones.jpg"; status = "active"
}
Check "Seller A create product -> msg=created" (($r.Status -eq 302) -and ($r.Location -match "msg=created")) ("status=" + $r.Status + " loc=" + $r.Location)

# ---- Seller A list shows it; capture id ----
$r = Send-Am $sellerA GET "$base/seller/products" $null
Check "Seller A list contains new product" (($r.Status -eq 200) -and ($r.Body -match "Wireless Headphones")) ("status=" + $r.Status)
$m = [regex]::Match($r.Body, "P\d{4}")
$productId = $m.Value
Check "product id captured" ($productId -ne "") ("pid=" + $productId)

# ---- Buyer catalog (anonymous) ----
$r = Send-Am $anon GET "$base/products" $null
Check "anon catalog lists product" (($r.Status -eq 200) -and ($r.Body -match "Wireless Headphones")) ("status=" + $r.Status)
Check "anon catalog shows seller name" ($r.Body -match "Seller A") "seller name missing"
Check "anon catalog shows price" ($r.Body -match "999\.50") "price display missing"

# ---- Product details ----
$r = Send-Am $anon GET "$base/product?id=$productId" $null
Check "anon details 200 + name" (($r.Status -eq 200) -and ($r.Body -match "Wireless Headphones")) ("status=" + $r.Status)
Check "anon details shows seller" ($r.Body -match "Seller A") "seller missing"

# ---- Seller B cannot update A's product ----
$r = Send-Am $sellerB POST "$base/seller/product" @{
    productId = $productId; name = "HACKED NAME"; description = "Hacked description text."; category = "Electronics";
    price = "1.00"; stock = "1"; image = ""; status = "active"
}
Check "Seller B update A product -> forbidden" (($r.Status -eq 302) -and ($r.Location -match "error=forbidden")) ("status=" + $r.Status + " loc=" + $r.Location)

$r = Send-Am $anon GET "$base/product?id=$productId" $null
Check "A product unchanged after B update attempt" (($r.Body -match "Wireless Headphones") -and ($r.Body -notmatch "HACKED")) "product was modified!"

# ---- Seller B cannot delete A's product ----
$r = Send-Am $sellerB POST "$base/seller/product/delete" @{ id = $productId }
Check "Seller B delete A product -> forbidden" (($r.Status -eq 302) -and ($r.Location -match "error=forbidden")) ("status=" + $r.Status + " loc=" + $r.Location)

$r = Send-Am $anon GET "$base/product?id=$productId" $null
Check "A product still exists after B delete attempt" ($r.Status -eq 200) ("status=" + $r.Status)

# ---- Seller B edit form for A's product is blocked ----
$r = Send-Am $sellerB GET "$base/seller/product?id=$productId" $null
Check "Seller B edit form for A product -> forbidden" (($r.Status -eq 302) -and ($r.Location -match "error=forbidden")) ("status=" + $r.Status + " loc=" + $r.Location)

# ---- Invalid data rejected ----
$r = Send-Am $sellerA POST "$base/seller/product" @{
    name = ""; description = "x"; category = "Electronics"; price = "abc"; stock = "-5"
}
Check "invalid create -> form with errors" (($r.Status -eq 200) -and ($r.Body -match "Product name is required") -and ($r.Body -match "valid price")) ("status=" + $r.Status)
Check "invalid create -> negative stock error" ($r.Body -match "Stock cannot be negative") "stock error missing"
Check "invalid create -> description length error" ($r.Body -match "Description must be between") "desc error missing"

# invalid image
$r = Send-Am $sellerA POST "$base/seller/product" @{
    name = "Bad Image Item"; description = "Some description here."; category = "Electronics"; price = "10"; stock = "1"; image = "not an image"
}
Check "invalid image rejected" (($r.Status -eq 200) -and ($r.Body -match "valid image URL or path")) ("status=" + $r.Status)

# invalid category
$r = Send-Am $sellerA POST "$base/seller/product" @{
    name = "Bad Cat Item"; description = "Some description here."; category = "Nonsense"; price = "10"; stock = "1"
}
Check "invalid category rejected" (($r.Status -eq 200) -and ($r.Body -match "choose a valid category")) ("status=" + $r.Status)

# ---- Seller A updates own product ----
$r = Send-Am $sellerA POST "$base/seller/product" @{
    productId = $productId; name = "Wireless Headphones Pro"; description = "Updated description text here."; category = "Electronics";
    price = "2499.00"; stock = "8"; image = ""; status = "active"
}
Check "Seller A update own product -> msg=updated" (($r.Status -eq 302) -and ($r.Location -match "msg=updated")) ("status=" + $r.Status + " loc=" + $r.Location)

$r = Send-Am $anon GET "$base/product?id=$productId" $null
Check "catalog reflects update" ($r.Body -match "Wireless Headphones Pro") "update not reflected"
Check "empty image shows placeholder" ($r.Body -match "No image available") "placeholder missing"

# ---- Inactive product hidden from storefront ----
$r = Send-Am $sellerA POST "$base/seller/product" @{
    name = "Hidden Item"; description = "Should not appear publicly."; category = "Grocery";
    price = "99.00"; stock = "5"; image = ""; status = "inactive"
}
Check "Seller A create inactive -> msg=created" (($r.Status -eq 302) -and ($r.Location -match "msg=created")) ("status=" + $r.Status + " loc=" + $r.Location)
$hid = [regex]::Match($r.Location, "P\d{4}").Value

$r = Send-Am $anon GET "$base/products" $null
Check "inactive product hidden from catalog" ($r.Body -notmatch "Hidden Item") "inactive leaked to catalog"

$r = Send-Am $sellerA GET "$base/seller/products" $null
Check "inactive product visible to owner" ($r.Body -match "Hidden Item") "owner cannot see inactive"

# ---- Catalog filters ----
$r = Send-Am $anon GET "$base/products?category=Electronics" $null
Check "category filter includes electronics" ($r.Body -match "Wireless Headphones Pro") "missing product"
Check "category filter excludes grocery" ($r.Body -notmatch "Hidden Item") "filter leak"

$r = Send-Am $anon GET "$base/products?q=headphones" $null
Check "keyword search works" ($r.Body -match "Wireless Headphones Pro") "search failed"

$r = Send-Am $anon GET "$base/products?category=Grocery" $null
Check "empty category shows empty state" (($r.Status -eq 200) -and ($r.Body -match "No products found")) "empty state missing"

# ---- Access control ----
$r = Send-Am $anon GET "$base/seller/products" $null
Check "anon seller area -> login" (($r.Status -eq 302) -and ($r.Location -match "/login\?error=loginRequired")) ("status=" + $r.Status + " loc=" + $r.Location)

$r = Send-Am $buyer GET "$base/seller/products" $null
Check "buyer seller area -> forbidden" (($r.Status -eq 302) -and ($r.Location -match "buyer-dashboard.jsp\?error=forbidden")) ("status=" + $r.Status + " loc=" + $r.Location)

$r = Send-Am $buyer GET "$base/seller/product" $null
Check "buyer add-product form -> forbidden" (($r.Status -eq 302) -and ($r.Location -match "error=forbidden")) ("status=" + $r.Status + " loc=" + $r.Location)

$r = Send-Am $buyer POST "$base/seller/product/delete" @{ id = $productId }
Check "buyer delete attempt -> forbidden" (($r.Status -eq 302) -and ($r.Location -match "error=forbidden")) ("status=" + $r.Status + " loc=" + $r.Location)

# ---- Seller A deletes own product ----
$r = Send-Am $sellerA POST "$base/seller/product/delete" @{ id = $productId }
Check "Seller A delete own product -> msg=deleted" (($r.Status -eq 302) -and ($r.Location -match "msg=deleted")) ("status=" + $r.Status + " loc=" + $r.Location)

$r = Send-Am $anon GET "$base/product?id=$productId" $null
Check "deleted product details -> notfound" (($r.Status -eq 302) -and ($r.Location -match "error=notfound")) ("status=" + $r.Status + " loc=" + $r.Location)

$r = Send-Am $anon GET "$base/products" $null
Check "deleted product gone from catalog" ($r.Body -notmatch "Wireless Headphones Pro") "still listed"

Write-Output ""
Write-Output ("RESULT: " + $script:pass + " passed, " + $script:fail + " failed")
if ($script:fail -gt 0) { exit 1 } else { exit 0 }
