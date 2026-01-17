#!/usr/bin/env powershell
# Auto-increment version and create release

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet('major', 'minor', 'patch')]
    [string]$BumpType = 'patch'
)

Write-Host ""
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "       KimDog SMP - Auto Version Bump & Release Script         " -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

# Navigate to project directory
$projectPath = "C:\Users\KimDog\Documents\KimDog-Studios\KimDogSMP-Client\KimDog SMP"
Set-Location $projectPath

# Get current version from gradle.properties
$gradleProps = Get-Content "gradle.properties"
$versionLine = $gradleProps | Select-String -Pattern "^mod_version\s*=\s*(.+)"

if ($versionLine) {
    $currentVersion = $versionLine.Matches.Groups[1].Value.Trim()
    Write-Host "Current Version: $currentVersion" -ForegroundColor Yellow
}
else {
    Write-Host "ERROR: Could not find version in gradle.properties!" -ForegroundColor Red
    exit 1
}

# Parse version
$versionParts = $currentVersion -replace '-.*$', '' -split '\.'
$major = [int]$versionParts[0]
$minor = [int]$versionParts[1]
$patch = [int]$versionParts[2]

# Increment version based on bump type
switch ($BumpType) {
    'major' {
        $major++
        $minor = 0
        $patch = 0
    }
    'minor' {
        $minor++
        $patch = 0
    }
    'patch' {
        $patch++
    }
}

$newVersion = "$major.$minor.$patch"
Write-Host "[+] New Version: $newVersion" -ForegroundColor Green
Write-Host ""

# Ask for confirmation
Write-Host "Changes:" -ForegroundColor Yellow
Write-Host "  Version: $currentVersion -> $newVersion" -ForegroundColor White
Write-Host "  This will:" -ForegroundColor White
Write-Host "    1. Update gradle.properties" -ForegroundColor Gray
Write-Host "    2. Update Kimdog_smp.java VERSION constant" -ForegroundColor Gray
Write-Host "    3. Commit changes" -ForegroundColor Gray
Write-Host "    4. Create git tag v$newVersion" -ForegroundColor Gray
Write-Host "    5. Push to GitHub (triggers auto-release)" -ForegroundColor Gray
Write-Host ""

$confirm = Read-Host "Continue? (y/n)"
if ($confirm -ne "y") {
    Write-Host "[X] Cancelled." -ForegroundColor Red
    exit 0
}

Write-Host ""
Write-Host "[*] Updating files..." -ForegroundColor Cyan

# Update gradle.properties
$gradleProps = $gradleProps -replace "^mod_version\s*=\s*.+", "mod_version = $newVersion"
$gradleProps | Out-File "gradle.properties" -Encoding UTF8
Write-Host "  [OK] Updated gradle.properties" -ForegroundColor Green

# Update Kimdog_smp.java
$javaFile = "src\main\java\kimdog\kimdog_smp\Kimdog_smp.java"
if (Test-Path $javaFile) {
    $javaContent = Get-Content $javaFile -Raw
    $javaContent = $javaContent -replace 'private static final String VERSION = "[^"]+";', "private static final String VERSION = `"$newVersion`";"
    $javaContent | Out-File $javaFile -Encoding UTF8 -NoNewline
    Write-Host "  [OK] Updated Kimdog_smp.java" -ForegroundColor Green
}

# Update CHANGELOG.md
$changelogFile = "CHANGELOG.md"
if (Test-Path $changelogFile) {
    $changelog = Get-Content $changelogFile -Raw
    $date = Get-Date -Format "yyyy-MM-dd"

    $newEntry = "`n`n## [$newVersion] - $date`n`n"
    $newEntry += "### Added`n"
    $newEntry += "- New features or functionality`n`n"
    $newEntry += "### Changed`n"
    $newEntry += "- Changes to existing functionality`n`n"
    $newEntry += "### Fixed`n"
    $newEntry += "- Bug fixes`n`n"
    $newEntry += "### Removed`n"
    $newEntry += "- Removed features`n`n"
    $newEntry += "---`n"

    # Insert after the first heading
    $changelog = $changelog -replace '(## \[Unreleased\].*?\n)', "`$1$newEntry"
    $changelog | Out-File $changelogFile -Encoding UTF8 -NoNewline
    Write-Host "  [OK] Updated CHANGELOG.md" -ForegroundColor Green
}

Write-Host ""
Write-Host "[*] Committing changes..." -ForegroundColor Cyan

# Git operations
git add gradle.properties $javaFile CHANGELOG.md
git commit -m "Bump version to $newVersion"

if ($LASTEXITCODE -ne 0) {
    Write-Host "[X] Git commit failed!" -ForegroundColor Red
    exit 1
}

Write-Host "  [OK] Changes committed" -ForegroundColor Green

Write-Host ""
Write-Host "[*] Creating git tag v$newVersion..." -ForegroundColor Cyan

git tag -a "v$newVersion" -m "Release version $newVersion"

if ($LASTEXITCODE -ne 0) {
    Write-Host "[X] Git tag creation failed!" -ForegroundColor Red
    exit 1
}

Write-Host "  [OK] Tag created" -ForegroundColor Green

Write-Host ""
Write-Host "[*] Pushing to GitHub..." -ForegroundColor Cyan

# Push commit
git push origin main

if ($LASTEXITCODE -ne 0) {
    Write-Host "[!] Push to main failed, but continuing..." -ForegroundColor Yellow
}

# Push tag (this triggers the GitHub Action)
git push origin "v$newVersion"

if ($LASTEXITCODE -ne 0) {
    Write-Host "[X] Tag push failed!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "================================================================" -ForegroundColor Green
Write-Host "               VERSION BUMP SUCCESSFUL!                        " -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Green
Write-Host ""
Write-Host "[+] Version bumped: $currentVersion -> $newVersion" -ForegroundColor Cyan
Write-Host "[+] Git tag created: v$newVersion" -ForegroundColor Cyan
Write-Host "[+] Pushed to GitHub" -ForegroundColor Cyan
Write-Host ""
Write-Host "GitHub Actions will now:" -ForegroundColor Yellow
Write-Host "   1. Build your mod automatically" -ForegroundColor White
Write-Host "   2. Create a GitHub release" -ForegroundColor White
Write-Host "   3. Upload kimdog-smp-$newVersion.jar" -ForegroundColor White
Write-Host "   4. Your auto-updater will detect it!" -ForegroundColor White
Write-Host ""
Write-Host "Check progress at:" -ForegroundColor Yellow
Write-Host "   https://github.com/NotKimDog/KimDogSMP-Client/actions" -ForegroundColor Cyan
Write-Host ""
Write-Host "[+] Release will be live in ~2-3 minutes!" -ForegroundColor Green
Write-Host ""

# Open GitHub Actions in browser
$openBrowser = Read-Host "Open GitHub Actions in browser? (y/n)"
if ($openBrowser -eq "y") {
    Start-Process "https://github.com/NotKimDog/KimDogSMP-Client/actions"
}

Write-Host ""
Write-Host "[+] Done! Your auto-updater will detect this release automatically." -ForegroundColor Green
Write-Host ""
