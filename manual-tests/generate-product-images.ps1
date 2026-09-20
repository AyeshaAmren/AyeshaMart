# generate-product-images.ps1
#
# Deterministic, product-specific image generator for the Ayesha Mart catalog.
# For every product it reads (from data/products.xlsx) it writes a clean,
# professional SVG poster at <OutDir>/<productId>.svg whose design is derived
# from that product's OWN name, category and subcategory:
#   - category-coloured gradient background (each category has its own palette)
#   - a category-relevant icon (book, monitor, t-shirt, basket, droplet, pot,
#     armchair, dumbbell, watch, book+media)
#   - the product NAME rendered in large type (so the image always names the item)
#   - the subcategory and product id as tidy badges
#
# No randomness, no external downloads, no array/index-based assignment.
# Images are stable local files under src/main/webapp/images/products/ and travel
# inside the WAR, so the app stays fully offline at runtime.
#
# Usage:
#   .\manual-tests\generate-product-images.ps1 -DataDir <dir> -OutDir <webapp-images-products>

param(
    [Parameter(Mandatory = $true)][string]$DataDir,
    [Parameter(Mandatory = $true)][string]$OutDir
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

$productFile = Join-Path $DataDir "data\products.xlsx"
if (-not (Test-Path -LiteralPath $productFile)) {
    throw "products.xlsx not found under $DataDir"
}
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

function Get-SharedStrings {
    param([string]$File)
    $zip = [System.IO.Compression.ZipFile]::OpenRead($File)
    try {
        $e = $zip.GetEntry("xl/sharedStrings.xml")
        if ($null -eq $e) { return @() }
        $sr = New-Object System.IO.StreamReader($e.Open())
        $xml = [xml]$sr.ReadToEnd()
        $sr.Close()
        return @($xml.GetElementsByTagName("t")) | ForEach-Object { $_.InnerText }
    } finally {
        $zip.Dispose()
    }
}

function Get-Products {
    param([string]$File)
    $zip = [System.IO.Compression.ZipFile]::OpenRead($File)
    try {
        $e = $zip.GetEntry("xl/worksheets/sheet1.xml")
        if ($null -eq $e) { return @() }
        $sr = New-Object System.IO.StreamReader($e.Open())
        $xml = [xml]$sr.ReadToEnd()
        $sr.Close()
    } finally {
        $zip.Dispose()
    }
    $ns = New-Object System.Xml.XmlNamespaceManager($xml.NameTable)
    $ns.AddNamespace("x", "http://schemas.openxmlformats.org/spreadsheetml/2006/main")
    $products = @()
    foreach ($row in $xml.SelectNodes("//x:sheetData/x:row", $ns)) {
        if ($row.GetAttribute("r") -eq "1") { continue }
        $cells = @{}
        foreach ($cell in $row.SelectNodes("x:c", $ns)) {
            $ref = $cell.GetAttribute("r")
            if ([string]::IsNullOrEmpty($ref)) { continue }
            $col = ($ref.ToCharArray() | Where-Object { $_ -match '[A-Za-z]' }) -join ''
            $val = ""
            $type = $cell.GetAttribute("t")
            if ($type -eq "s") {
                $idx = [int]$cell.SelectSingleNode("x:v", $ns).InnerText
                $val = $script:shared[$idx]
            } else {
                $v = $cell.SelectSingleNode("x:v", $ns)
                if ($null -ne $v) { $val = $v.InnerText }
            }
            $cells[$col] = $val
        }
        $products += [pscustomobject]@{
            id         = $cells['A']
            name       = $cells['C']
            category   = $cells['E']
            subCategory = $cells['N']
        }
    }
    return $products
}

function ConvertTo-XmlText {
    param([string]$Text)
    return $Text -replace '&', '&amp;' -replace '<', '&lt;' -replace '>', '&gt;'
            -replace '"', '&quot;' -replace "'", '&apos;'
}

function Get-WrappedLines {
    param([string]$Text, [int]$MaxChars)
    $words = $Text -split '\s+'
    $lines = New-Object System.Collections.Generic.List[string]
    $line = ""
    foreach ($word in $words) {
        if ($line.Length -eq 0) { $line = $word }
        elseif ($line.Length + 1 + $word.Length -le $MaxChars) { $line += " " + $word }
        else { $lines.Add($line); $line = $word; if ($lines.Count -ge 3) { break } }
    }
    if ($lines.Count -lt 3 -and $line.Length -gt 0) { $lines.Add($line) }
    return $lines
}

function Get-CategoryStyle {
    param([string]$Category)
    $key = ($Category -replace '\s+', ' ').ToLower().Trim()
    $colors = @{
        'books'             = @('#8B5CF6', '#5B21B6', 'M4 9 C11 5 18 5 24 9 L24 39 C18 35 11 35 4 39 Z M24 9 C31 5 38 5 45 9 L45 39 C38 35 31 35 24 39 Z')
        'electronics'       = @('#0EA5E9', '#075985', 'M4 5 H44 V31 H4 Z M24 31 V39 M14 43 H34')
        'fashion'           = @('#F472B6', '#BE185D', 'M14 9 L24 4 L34 9 L41 15 L34 21 L34 44 L14 44 L14 21 L7 15 Z')
        'grocery'           = @('#34D399', '#047857', 'M24 4 A10 10 0 0 0 14 14 M8 20 L40 20 L36 40 A4 4 0 0 1 32 44 L16 44 A4 4 0 0 1 12 40 Z')
        'beauty'            = @('#FBBF24', '#B45309', 'M24 3 C11 18 7 26 7 32 A17 17 0 0 0 41 32 C41 26 37 18 24 3 Z')
        'home & kitchen'    = @('#FB923C', '#C2410C', 'M6 18 H42 V28 A12 12 0 0 1 30 40 H18 A12 12 0 0 1 6 28 Z M12 18 V12 A6 6 0 0 1 18 6 H30 A6 6 0 0 1 36 12 V18')
        'home & living'     = @('#F9A8D4', '#9D174D', 'M6 16 H42 V18 H6 Z M10 20 H38 V44 H30 V30 H18 V44 H10 Z')
        'sports'            = @('#4ADE80', '#15803D', 'M10 14 V34 M38 14 V34 M6 18 V30 M42 18 V30 M6 18 H10 M34 18 H42 M6 30 H10 M34 30 H42 M14 24 H34')
        'accessories'       = @('#A78BFA', '#6D28D9', 'M8 15 H40 V29 H8 Z M16 5 H32 V15 H16 Z M16 29 H32 V43 H16 Z M24 22 A3 3 0 1 0 24 22.01')
        'books & media'     = @('#60A5FA', '#2563EB', 'M4 8 C11 4 18 4 24 8 L24 32 C18 28 11 28 4 32 Z M30 12 L42 18 L30 24 Z')
    }
    if ($colors.ContainsKey($key)) { return $colors[$key] }
    return @('#E8E4F7', '#6B2FA4', 'M4 9 C11 5 18 5 24 9 L24 39 C18 35 11 35 4 39 Z M24 9 C31 5 38 5 45 9 L45 39 C38 35 31 35 24 39 Z')
}

function Export-ProductSvg {
    param([string]$id, [string]$name, [string]$category, [string]$subCategory)
    $style = Get-CategoryStyle -Category $category
    $c1 = $style[0]; $c2 = $style[1]; $icon = $style[2]

    $title = if ([string]::IsNullOrWhiteSpace($name)) { 'Ayesha Mart' } else { $name.Trim() }
    $label = if ([string]::IsNullOrWhiteSpace($subCategory)) { $category } else { $subCategory.Trim() }
    if ($label.Length -gt 30) { $label = $label.Substring(0, 30) }

    $lines = @(Get-WrappedLines -Text $title -MaxChars 24)
    $y = 212 - (($lines.Count - 1) * 27)

    $nameText = ""
    for ($i = 0; $i -lt $lines.Count; $i++) {
        $nameText += "<text x=`"300`" y=`"$($y + $i * 56)`" font-family=`"'Segoe UI', Arial, sans-serif`" font-size=`"44`" font-weight=`"700`" fill=`"#FFFFFF`" text-anchor=`"middle`">$(ConvertTo-XmlText $lines[$i])</text>"
    }

    $pillWidth = 52 + ($label.Length * 9.5)
    $pillX = [int]((600 - $pillWidth) / 2)

    $svg = @"
<svg xmlns="http://www.w3.org/2000/svg" width="600" height="400" viewBox="0 0 600 400" role="img" aria-label="$(ConvertTo-XmlText $title)">
  <defs>
    <linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0" stop-color="$c1"/>
      <stop offset="1" stop-color="$c2"/>
    </linearGradient>
  </defs>
  <rect width="600" height="400" fill="url(#g)"/>
  <circle cx="540" cy="40" r="170" fill="#FFFFFF" opacity="0.10"/>
  <circle cx="40" cy="380" r="140" fill="#FFFFFF" opacity="0.10"/>
  <g transform="translate(105,60) scale(6.2)" fill="none" stroke="#FFFFFF" stroke-width="1.15" stroke-linecap="round" stroke-linejoin="round" opacity="0.10">$icon</g>
  <rect x="40" y="130" width="520" height="150" rx="20" fill="#000000" opacity="0.10"/>
  <text x="32" y="44" font-family="'Segoe UI', Arial, sans-serif" font-size="19" font-weight="600" fill="#FFFFFF" opacity="0.9">AYESHA MART</text>
  <g transform="translate(504,50) scale(0.66)" fill="none" stroke="#FFFFFF" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round" opacity="0.9">
    <circle cx="24" cy="24" r="24" fill="#FFFFFF" opacity="0.16" stroke="none"/>
    $icon
  </g>
  $nameText
  <rect x="$pillX" y="336" width="$pillWidth" height="32" rx="16" fill="#FFFFFF" fill-opacity="0.18"/>
  <text x="300" y="357" font-family="'Segoe UI', Arial, sans-serif" font-size="16" font-weight="500" fill="#FFFFFF" text-anchor="middle">$(ConvertTo-XmlText $label)</text>
  <text x="568" y="386" font-family="'Segoe UI', Arial, sans-serif" font-size="13" fill="#FFFFFF" opacity="0.7" text-anchor="end">$id</text>
</svg>
"@
    return $svg
}

$script:shared = Get-SharedStrings -File $productFile
$products = Get-Products -File $productFile
Write-Output "Found $($products.Count) products in $productFile"

$written = 0
$errors = @()
foreach ($p in $products) {
    if ([string]::IsNullOrWhiteSpace($p.id)) { continue }
    $svg = Export-ProductSvg -id $p.id -name $p.name -category $p.category -subCategory $p.subCategory
    try {
        $tmp = Join-Path $env:TEMP ("am-svg-" + [guid]::NewGuid().ToString("N") + ".xml")
        $doc = [xml]$svg
        $doc.Save($tmp)
        Remove-Item $tmp -Force
    } catch {
        $errors += "$($p.id) :: invalid svg :: $($_.Exception.Message)"
        continue
    }
    $out = Join-Path $OutDir ($p.id + ".svg")
    Set-Content -LiteralPath $out -Value $svg -Encoding UTF8
    $written++
}

Write-Output "Generated $written product images into $OutDir"
if ($errors.Count -gt 0) { Write-Output "Errors:"; $errors | ForEach-Object { Write-Output "  $_" } }
Write-Output "Generated ids must equal product ids: $($products.Count -eq $written)"