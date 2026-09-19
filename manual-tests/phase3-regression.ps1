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

Write-Output "== Phase 3 : Authentication regression =="

$anon = New-AmSession
$tmp = New-AmSession

$r = Send-Am $tmp POST "$base/login" @{ email = "sellerA@test.com"; password = "WrongPass1!" }
Check "wrong password message" (($r.Status -eq 200) -and ($r.Body -match "Incorrect password")) ("status=" + $r.Status)

$r = Send-Am $tmp POST "$base/login" @{ email = "nobody@test.com"; password = "Whatever@1" }
Check "unknown email message" (($r.Status -eq 200) -and ($r.Body -match "No account found")) ("status=" + $r.Status)

$admin = New-AmSession
$r = Send-Am $admin POST "$base/login" @{ email = "admin@ayeshamart.com"; password = "Admin@123" }
Check "admin login -> admin dashboard" (($r.Status -eq 302) -and ($r.Location -match "admin-dashboard")) ("status=" + $r.Status + " loc=" + $r.Location)

$r = Send-Am $anon GET "$base/buyer-dashboard.jsp" $null
Check "anon dashboard -> loginRequired" (($r.Status -eq 302) -and ($r.Location -match "/login\?error=loginRequired")) ("status=" + $r.Status + " loc=" + $r.Location)

$buyer = New-AmSession
$r = Send-Am $buyer POST "$base/login" @{ email = "buyer@test.com"; password = "Buyer@123" }
Check "buyer login" (($r.Status -eq 302) -and ($r.Location -match "buyer-dashboard")) ("status=" + $r.Status)

$r = Send-Am $buyer GET "$base/admin-dashboard.jsp" $null
Check "buyer -> admin dashboard forbidden" (($r.Status -eq 302) -and ($r.Location -match "buyer-dashboard.jsp\?error=forbidden")) ("status=" + $r.Status + " loc=" + $r.Location)

$seller = New-AmSession
$r = Send-Am $seller POST "$base/login" @{ email = "sellerA@test.com"; password = "Seller@123" }
Check "seller login" (($r.Status -eq 302) -and ($r.Location -match "seller-dashboard")) ("status=" + $r.Status)

$r = Send-Am $seller GET "$base/admin-dashboard.jsp" $null
Check "seller -> admin dashboard forbidden" (($r.Status -eq 302) -and ($r.Location -match "seller-dashboard.jsp\?error=forbidden")) ("status=" + $r.Status + " loc=" + $r.Location)

$r = Send-Am $seller GET "$base/seller/products" $null
Check "seller dashboard content loads" (($r.Status -eq 200) -and ($r.Body -match "My Products")) ("status=" + $r.Status)

$r = Send-Am $anon GET "$base/login" $null
Check "login page renders" (($r.Status -eq 200) -and ($r.Body -match "Sign in|Login")) ("status=" + $r.Status)

$r = Send-Am $seller GET "$base/logout" $null
Check "logout -> /login?logout=1" (($r.Status -eq 302) -and ($r.Location -match "/login\?logout=1")) ("status=" + $r.Status + " loc=" + $r.Location)

$r = Send-Am $seller GET "$base/seller/products" $null
Check "post-logout seller area -> login" (($r.Status -eq 302) -and ($r.Location -match "loginRequired")) ("status=" + $r.Status + " loc=" + $r.Location)

Write-Output ""
Write-Output ("RESULT: " + $script:pass + " passed, " + $script:fail + " failed")
if ($script:fail -gt 0) { exit 1 } else { exit 0 }
