# Phase 6 test suite: checkout, orders, demo payment, seller fulfilment,
# admin order/payment management, delivery tracking and reviews.
# Run against a FRESH data dir (account emails and product/order ids are deterministic).
# Run order: phase6 -> phase5 -> phase4 -> phase3
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

Write-Output "== Phase 6 : Checkout, Orders, Payments, Delivery & Reviews =="

$anon = New-AmSession
$buyer = New-AmSession
$buyer2 = New-AmSession
$seller = New-AmSession
$sellerX = New-AmSession
$admin = New-AmSession

# ---- 1. seeded catalogue ----
$r = Send-Am $anon GET "$base/home" $null
Check "home page 200 with live featured products" (($r.Status -eq 200) -and ($r.Body -match "Featured Products")) ("status=" + $r.Status)
$r = Send-Am $anon GET "$base/products" $null
$allIds = [regex]::Matches($r.Body, "P\d{4}") | ForEach-Object { $_.Value } | Select-Object -Unique
Check "catalogue has 30+ products" ($allIds.Count -ge 30) ("count=" + $allIds.Count)

$r = Send-Am $anon GET "$base/products?q=basmati" $null
$A = [regex]::Match($r.Body, "P\d{4}").Value
$r = Send-Am $anon GET "$base/products?q=yoga" $null
$B = [regex]::Match($r.Body, "P\d{4}").Value
Check "captured product ids A=$A B=$B" (($A -match "P\d{4}") -and ($B -match "P\d{4}") -and ($A -ne $B)) ("A=$A B=$B")

# ---- 2. register accounts ----
Send-Am $buyer  POST "$base/register" @{ fullName = "Phase6 Buyer"; email = "p6buyer@test.com"; password = "Buyer@123"; confirmPassword = "Buyer@123"; phone = "9000000061"; address = "Six St"; role = "buyer" } | Out-Null
Send-Am $buyer2 POST "$base/register" @{ fullName = "Phase6 Buyer Two"; email = "p6buyer2@test.com"; password = "Buyer@123"; confirmPassword = "Buyer@123"; phone = "9000000062"; address = "SixTwo St"; role = "buyer" } | Out-Null
Send-Am $sellerX POST "$base/register" @{ fullName = "Other Seller"; email = "p6sellerX@test.com"; password = "Seller@123"; confirmPassword = "Seller@123"; phone = "9000000063"; address = "Shop Six"; role = "seller" } | Out-Null

$r = Send-Am $buyer POST "$base/login" @{ email = "p6buyer@test.com"; password = "Buyer@123" }
Check "buyer login -> buyer dashboard" (($r.Status -eq 302) -and ($r.Location -match "buyer-dashboard")) ("status=" + $r.Status + " loc=" + $r.Location)
$r = Send-Am $seller POST "$base/login" @{ email = "seller@ayeshamart.com"; password = "Seller@123" }
Check "demo seller login" (($r.Status -eq 302) -and ($r.Location -match "seller-dashboard")) ("status=" + $r.Status + " loc=" + $r.Location)
$r = Send-Am $admin POST "$base/login" @{ email = "admin@ayeshamart.com"; password = "Admin@123" }
Check "admin login -> admin dashboard" (($r.Status -eq 302) -and ($r.Location -match "admin-dashboard")) ("status=" + $r.Status + " loc=" + $r.Location)

# ---- 3. cart + checkout page ----
$r = Send-Am $buyer POST "$base/cart" @{ action = "add"; productId = $A; quantity = "1" }
$r = Send-Am $buyer POST "$base/cart" @{ action = "add"; productId = $B; quantity = "1" }
$r = Send-Am $buyer GET "$base/cart" $null
Check "cart lists both products + checkout button" (($r.Body -match "Basmati") -and ($r.Body -match "Yoga") -and ($r.Body -match "Proceed to Checkout")) "cart/checkout missing"

$r = Send-Am $buyer GET "$base/checkout" $null
Check "checkout page 200 with address + payment form" (($r.Status -eq 200) -and ($r.Body -match "Delivery Address") -and ($r.Body -match "Cash on Delivery") -and ($r.Body -match "Place Order")) ("status=" + $r.Status)

# ---- 4. COD checkout creates order O0001 ----
$r = Send-Am $buyer POST "$base/checkout" @{ fullName = "Phase6 Buyer"; phone = "9000000061"; address = "123 Test Avenue, Test City"; pincode = "400001"; paymentMethod = "COD"; upiId = ""; cardNumber = ""; cardName = "" }
Check "COD order placed -> /order/confirm?orderId=O0001" (($r.Status -eq 302) -and ($r.Location -match "/order/confirm\?orderId=O0001")) ("status=" + $r.Status + " loc=" + $r.Location)
$r = Send-Am $buyer GET "$base/order/confirm?orderId=O0001" $null
Check "confirmation page shows PLACED + COD pending + expected delivery" (($r.Status -eq 200) -and ($r.Body -match "placed successfully") -and ($r.Body -match "Placed") -and ($r.Body -match "Awaiting payment") -and ($r.Body -match "Expected delivery")) ("status=" + $r.Status)

# stock reduced: A 100->99, B 55->54
$r = Send-Am $anon GET "$base/product?id=$A" $null
Check "stock A decremented to 99" ($r.Body -match "99 unit") "stock A wrong"
$r = Send-Am $anon GET "$base/product?id=$B" $null
Check "stock B decremented to 54" ($r.Body -match "54 unit") "stock B wrong"

# ---- 5. demo payment failures ----
$r = Send-Am $buyer POST "$base/cart" @{ action = "add"; productId = $A; quantity = "1" }
$r = Send-Am $buyer POST "$base/checkout" @{ fullName = "Phase6 Buyer"; phone = "9000000061"; address = "123 Test Avenue, Test City"; pincode = "400001"; paymentMethod = "UPI"; upiId = "phase6fail@upi"; cardNumber = ""; cardName = "" }
Check "UPI fail -> checkout?error=paymentfailed" (($r.Status -eq 302) -and ($r.Location -match "error=paymentfailed")) ("loc=" + $r.Location)
$r = Send-Am $buyer GET "$base/cart" $null
Check "cart kept after failed UPI" ($r.Body -match "Basmati") "cart lost after failed payment"

Send-Am $buyer POST "$base/cart" @{ action = "add"; productId = $B; quantity = "1" } | Out-Null
$r = Send-Am $buyer POST "$base/checkout" @{ fullName = "Phase6 Buyer"; phone = "9000000061"; address = "123 Test Avenue, Test City"; pincode = "400001"; paymentMethod = "CARD"; upiId = ""; cardNumber = "4111111111111110"; cardName = "Phase6 Buyer" }
Check "CARD fail (ends 0) -> error=paymentfailed" (($r.Status -eq 302) -and ($r.Location -match "error=paymentfailed")) ("loc=" + $r.Location)
$r = Send-Am $buyer GET "$base/cart" $null
Check "cart kept after failed CARD" (($r.Body -match "Basmati") -and ($r.Body -match "Yoga")) "cart lost after failed card"

# ---- 6. server-side validation ----
$r = Send-Am $buyer POST "$base/checkout" @{ fullName = ""; phone = "1"; address = "short"; pincode = "12"; paymentMethod = "COD"; upiId = ""; cardNumber = ""; cardName = "" }
Check "invalid address re-renders checkout with errors" (($r.Status -eq 200) -and ($r.Body -match "Full name is required") -and ($r.Body -match "Please enter a valid phone number") -and ($r.Body -match "Enter a valid 6-digit PIN code")) ("status=" + $r.Status)

# ---- 7. UPI success -> O0002 then buyer cancel ----
Send-Am $buyer POST "$base/cart" @{ action = "clear" } | Out-Null
Send-Am $buyer POST "$base/cart" @{ action = "add"; productId = $A; quantity = "1" } | Out-Null
$r = Send-Am $buyer POST "$base/checkout" @{ fullName = "Phase6 Buyer"; phone = "9000000061"; address = "123 Test Avenue, Test City"; pincode = "400001"; paymentMethod = "UPI"; upiId = "phase6ok@upi"; cardNumber = ""; cardName = "" }
Check "UPI success -> order O0002" (($r.Status -eq 302) -and ($r.Location -match "orderId=O0002")) ("loc=" + $r.Location)
$r = Send-Am $buyer GET "$base/order?id=O0002" $null
Check "paid UPI order shows Paid" ($r.Body -match "Paid") "payment status wrong"
$r = Send-Am $buyer POST "$base/order/cancel" @{ orderId = "O0002" }
Check "buyer cancels PLACED order" (($r.Status -eq 302) -and ($r.Location -match "msg=cancelled")) ("loc=" + $r.Location)
$r = Send-Am $buyer GET "$base/order?id=O0002" $null
Check "cancelled order shows CANCELLED + stock note" (($r.Body -match "Cancelled") -and ($r.Body -match "Order cancelled")) "cancel state wrong"
$r = Send-Am $anon GET "$base/product?id=$A" $null
Check "stock A restored to 99 after cancel" ($r.Body -match "99 unit") "stock A not restored"

# ---- 8. CARD success -> O0003 ----
Send-Am $buyer POST "$base/cart" @{ action = "add"; productId = $A; quantity = "1" } | Out-Null
$r = Send-Am $buyer POST "$base/checkout" @{ fullName = "Phase6 Buyer"; phone = "9000000061"; address = "123 Test Avenue, Test City"; pincode = "400001"; paymentMethod = "CARD"; upiId = ""; cardNumber = "4111111111111111"; cardName = "Phase6 Buyer" }
Check "CARD success -> order O0003" (($r.Status -eq 302) -and ($r.Location -match "orderId=O0003")) ("loc=" + $r.Location)
$r = Send-Am $buyer GET "$base/order?id=O0003" $null
Check "card order paid" ($r.Body -match "Paid") "payment status wrong"

# ---- 9. order ownership/security ----
$r = Send-Am $buyer2 POST "$base/login" @{ email = "p6buyer2@test.com"; password = "Buyer@123" }
Check "buyer2 login" (($r.Status -eq 302) -and ($r.Location -match "buyer-dashboard")) ("loc=" + $r.Location)
$r = Send-Am $buyer2 GET "$base/order?id=O0001" $null
Check "buyer2 cannot view buyer1 order" (($r.Status -eq 302) -and ($r.Location -match "orders\?error=notfound")) ("loc=" + $r.Location)
$r = Send-Am $anon GET "$base/checkout" $null
Check "anon checkout -> loginRequired" (($r.Status -eq 302) -and ($r.Location -match "/login\?error=loginRequired")) ("loc=" + $r.Location)
$r = Send-Am $seller GET "$base/checkout" $null
Check "seller checkout -> forbidden" (($r.Status -eq 302) -and ($r.Location -match "error=forbidden")) ("loc=" + $r.Location)
$r = Send-Am $seller GET "$base/admin/orders" $null
Check "seller admin orders -> forbidden" (($r.Status -eq 302) -and ($r.Location -match "seller-dashboard.jsp\?error=forbidden")) ("loc=" + $r.Location)

# ---- 10. seller fulfilments ----
$r = Send-Am $seller GET "$base/seller/orders" $null
Check "seller orders page lists O0001" (($r.Status -eq 200) -and ($r.Body -match "O0001")) ("status=" + $r.Status)
$itemIds = [regex]::Matches($r.Body, "OI\d{4}") | ForEach-Object { $_.Value } | Select-Object -Unique
Check "seller page lists O0001 line item ids" (($itemIds -contains "OI0001") -and ($itemIds -contains "OI0002")) ("ids=" + ($itemIds -join ","))
$IA = "OI0001"; $IB = "OI0002"

# advance IA (Basmati) to DELIVERED: 5 steps
foreach ($n in 1..5) {
    $r = Send-Am $seller POST "$base/seller/order/advance" @{ itemId = $IA }
    Check ("advance line $IA step $n") (($r.Status -eq 302) -and ($r.Location -match "msg=advanced")) ("loc=" + $r.Location)
}
# advance IB (Yoga mat) to SHIPPED: 3 steps
foreach ($n in 1..3) {
    Send-Am $seller POST "$base/seller/order/advance" @{ itemId = $IB } | Out-Null
}
$r = Send-Am $seller GET "$base/seller/orders" $null
Check "seller sees delivered A line" (($r.Body -match "Delivered")) "delivered status missing"
$r = Send-Am $buyer GET "$base/orders" $null
Check "buyer order O0001 recomputed to Shipped" (($r.Status -eq 200) -and ($r.Body -match "Shipped")) ("status=" + $r.Status)
Check "buyer order list has 3 orders" (($r.Body -match "O0001") -and ($r.Body -match "O0002") -and ($r.Body -match "O0003")) "orders missing"

# non-owner seller cannot advance another seller's line
$r = Send-Am $sellerX POST "$base/login" @{ email = "p6sellerX@test.com"; password = "Seller@123" }
$r = Send-Am $sellerX POST "$base/seller/order/advance" @{ itemId = $IA }
Check "non-owner seller blocked (forbidden)" (($r.Status -eq 302) -and ($r.Location -match "error=forbidden")) ("loc=" + $r.Location)

# ---- 11. reviews ----
$r = Send-Am $buyer GET "$base/product?id=$A" $null
Check "delivered product shows review form" (($r.Body -match "Write a Review") -and ($r.Body -match "Submit Review")) "review form missing"
$r = Send-Am $buyer POST "$base/review" @{ productId = $A; rating = "5"; title = "Great rice"; comment = "Aromatic and delivered on time, excellent quality." }
Check "review submitted -> product?msg=reviewed" (($r.Status -eq 302) -and ($r.Location -match "msg=reviewed")) ("loc=" + $r.Location)
$r = Send-Am $anon GET "$base/product?id=$A" $null
Check "product rating updated to 5 (1)" ($r.Body -match "5.0 out of 5") "rating not updated"
$r = Send-Am $buyer GET "$base/product?id=$A" $null
Check "already-reviewed shows thank-you note" ($r.Body -match "already reviewed") "duplicate gate missing"
$r = Send-Am $buyer POST "$base/review" @{ productId = $A; rating = "3"; title = "Again"; comment = "Trying to review twice." }
Check "duplicate review blocked -> error=duplicate" (($r.Status -eq 302) -and ($r.Location -match "error=duplicate")) ("loc=" + $r.Location)

# B is only SHIPPED, not delivered -> cannot review
$r = Send-Am $buyer POST "$base/review" @{ productId = $B; rating = "4"; title = "Soon"; comment = "Waiting for delivery." }
Check "not-delivered product review blocked -> error=notDelivered" (($r.Status -eq 302) -and ($r.Location -match "error=notDelivered")) ("loc=" + $r.Location)

# ---- 12. admin payments & status ----
$r = Send-Am $admin GET "$base/admin/orders" $null
Check "admin orders page lists all orders" (($r.Status -eq 200) -and ($r.Body -match "O0001") -and ($r.Body -match "O0002")) ("status=" + $r.Status)
$r = Send-Am $admin POST "$base/admin/order/payment" @{ orderId = "O0001" }
Check "admin marks O0001 paid" (($r.Status -eq 302) -and ($r.Location -match "msg=paid")) ("loc=" + $r.Location)
$r = Send-Am $admin POST "$base/admin/order/status" @{ orderId = "O0003"; status = "OUT_FOR_DELIVERY" }
Check "admin advances O0003 to OUT_FOR_DELIVERY" (($r.Status -eq 302) -and ($r.Location -match "msg=updated")) ("loc=" + $r.Location)
$r = Send-Am $admin GET "$base/admin/orders" $null
Check "admin page shows new states" (($r.Body -match "Out For Delivery") -and ($r.Body -match "Paid")) "admin states missing"
$r = Send-Am $admin GET "$base/admin" $null
Check "admin dashboard 200 with stats" (($r.Status -eq 200) -and ($r.Body -match "Total orders") -and ($r.Body -match "Manage Orders")) ("status=" + $r.Status)

# ---- 13. final persistence checks ----
$r = Send-Am $anon GET "$base/product?id=$A" $null
Check "final stock A = 98" ($r.Body -match "98 unit") "stock A final wrong"
$r = Send-Am $anon GET "$base/product?id=$B" $null
Check "final stock B = 54" ($r.Body -match "54 unit") "stock B final wrong"
$r = Send-Am $anon GET "$base/products?q=basmati" $null
Check "seeded product reusable for another buyer" ($r.Body -match "P\d{4}") "seeded product gone"

Write-Output ""
Write-Output ("RESULT: " + $script:pass + " passed, " + $script:fail + " failed")
if ($script:fail -gt 0) { exit 1 } else { exit 0 }