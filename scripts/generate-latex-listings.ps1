$outputFile = "zalaczniki_kod.tex"
Remove-Item -Path $outputFile -ErrorAction SilentlyContinue

$basePath = (Get-Location).Path

# CONFIG: choose your source folder
# $rootFolder = ".\..\backend\src\main"
$rootFolder = ".\..\backend\src\test"
# $rootFolder = ".\..\frontend\src"


# ============================================================
# 1. Remove diacritics (Polish chars) via Unicode normalization
# ============================================================
function Remove-Diacritics {
    param([string]$text)
    if ($null -eq $text) { return "" }

    $normalized = $text.Normalize([System.Text.NormalizationForm]::FormKD)
    $result = ""
    foreach ($c in $normalized.ToCharArray()) {
        if ([System.Globalization.CharUnicodeInfo]::GetUnicodeCategory($c) -ne [System.Globalization.UnicodeCategory]::NonSpacingMark) {
            $result += $c
        }
    }
    return $result
}

# ============================================================
# 2. Escape special LaTeX characters
# ============================================================
function Escape-Latex {
    param([string]$text)
    if ($null -eq $text) { return "" }

    return $text `
        -replace '\\', '\\textbackslash{}' `
        -replace '_', '\\_' `
        -replace '%', '\\%' `
        -replace '#', '\\#' `
        -replace '&', '\\&' `
        -replace '\$', '\\$' `
        -replace '{', '\\{' `
        -replace '}', '\\}'
}

# ============================================================
# 3. Collect files and generate LaTeX entries
# ============================================================
Get-ChildItem -Path $rootFolder -Recurse -File |
Where-Object {
    $_.Extension -in ".java", ".ts", ".ps1" -and
    $_.FullName -notmatch "node_modules|dist|target|\.git"
} |
ForEach-Object {

    # Relative path (remove Polish chars)
    $relPath = $_.FullName.Substring($basePath.Length).TrimStart("\") -replace "\\", "/"
    $relPathClean = Remove-Diacritics $relPath
    $fileNameClean = Remove-Diacritics $_.Name

    # Escape for LaTeX
    $relPathEscaped = Escape-Latex $relPathClean
    $fileNameEscaped = Escape-Latex $fileNameClean

    # Write LaTeX header
    "\clearpage" | Out-File -Append $outputFile -Encoding utf8
    "\phantomsection" | Out-File -Append $outputFile -Encoding utf8
    "\addcontentsline{toc}{subsection}{Listing pliku: $fileNameEscaped}" | Out-File -Append $outputFile -Encoding utf8
    "\begin{lstlisting}[title={$relPathEscaped}]" | Out-File -Append $outputFile -Encoding utf8

    # File content (source code - lstlisting handles UTF-8)
    Get-Content $_.FullName -Encoding UTF8 | Out-File -Append $outputFile -Encoding utf8

    "\end{lstlisting}" | Out-File -Append $outputFile -Encoding utf8
}

Write-Host "Done! Generated file: $outputFile"