Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing

$repoRoot = "C:\Users\KimDog\Documents\KimDog-Studios\KimDogSMP-Client\KimDog SMP"
Set-Location $repoRoot

function Get-CurrentVersion {
    $line = (Get-Content "$repoRoot\gradle.properties") | Where-Object { $_ -match '^mod_version\s*=\s*(.+)' }
    if ($line) { return ($Matches[1].Trim()) }
    return "unknown"
}

function Update-VersionFiles {
    param($newVersion, $releaseNote)
    # gradle.properties
    (Get-Content "$repoRoot\gradle.properties") -replace '^mod_version\s*=\s*.+', "mod_version = $newVersion" | Set-Content "$repoRoot\gradle.properties"

    # Kimdog_smp.java
    $javaFile = "$repoRoot\src\main\java\kimdog\kimdog_smp\Kimdog_smp.java"
    if (Test-Path $javaFile) {
        (Get-Content $javaFile -Raw) -replace 'private static final String VERSION = "[^"]+";', "private static final String VERSION = \"$newVersion\";" | Set-Content $javaFile
    }

    # CHANGELOG.md
    $changelog = "$repoRoot\CHANGELOG.md"
    if (Test-Path $changelog) {
        $date = Get-Date -Format "yyyy-MM-dd"
        $entry = "`n`n## [$newVersion] - $date`n`n### Added`n- $releaseNote`n`n### Changed`n- Changes to existing functionality`n`n### Fixed`n- Bug fixes`n`n### Removed`n- Removed features`n`n---`n"
        $content = Get-Content $changelog -Raw
        $content = $content -replace '(## \[Unreleased\].*?\n)', "`$1$entry"
        Set-Content $changelog $content
    }
}

function Run-GitOps {
    param($version)
    git add gradle.properties src/main/java/kimdog/kimdog_smp/Kimdog_smp.java CHANGELOG.md
    git commit -m "Bump version to $version" | Out-Null
    git tag -a "v$version" -m "Release version $version"
    git push origin main
    git push origin "v$version"
}

# Build GUI
$form = New-Object System.Windows.Forms.Form
$form.Text = "KimDog SMP - Version Bumper"
$form.Size = New-Object System.Drawing.Size(460,360)
$form.StartPosition = "CenterScreen"

$lblCurrent = New-Object System.Windows.Forms.Label
$lblCurrent.Text = "Current version: $(Get-CurrentVersion)"
$lblCurrent.Location = '10,10'
$lblCurrent.Size = '420,20'
$form.Controls.Add($lblCurrent)

$lblManual = New-Object System.Windows.Forms.Label
$lblManual.Text = "New version (leave blank to auto-bump):"
$lblManual.Location = '10,40'
$lblManual.Size = '260,20'
$form.Controls.Add($lblManual)

$txtManual = New-Object System.Windows.Forms.TextBox
$txtManual.Location = '10,60'
$txtManual.Size = '200,20'
$form.Controls.Add($txtManual)

$lblBump = New-Object System.Windows.Forms.Label
$lblBump.Text = "Auto-bump type:"
$lblBump.Location = '230,40'
$lblBump.Size = '100,20'
$form.Controls.Add($lblBump)

$cbBump = New-Object System.Windows.Forms.ComboBox
$cbBump.Items.AddRange(@('patch','minor','major'))
$cbBump.SelectedIndex = 0
$cbBump.DropDownStyle = 'DropDownList'
$cbBump.Location = '230,60'
$cbBump.Size = '100,20'
$form.Controls.Add($cbBump)

$lblNote = New-Object System.Windows.Forms.Label
$lblNote.Text = "Release highlight:"
$lblNote.Location = '10,90'
$lblNote.Size = '200,20'
$form.Controls.Add($lblNote)

$txtNote = New-Object System.Windows.Forms.TextBox
$txtNote.Location = '10,110'
$txtNote.Size = '420,20'
$txtNote.Text = "New features or fixes"
$form.Controls.Add($txtNote)

$chkGit = New-Object System.Windows.Forms.CheckBox
$chkGit.Text = "Run git commit/tag/push"
$chkGit.Checked = $true
$chkGit.Location = '10,140'
$chkGit.Size = '200,20'
$form.Controls.Add($chkGit)

$btnRun = New-Object System.Windows.Forms.Button
$btnRun.Text = "Bump Version"
$btnRun.Location = '10,180'
$btnRun.Size = '120,30'
$form.Controls.Add($btnRun)

$txtLog = New-Object System.Windows.Forms.TextBox
$txtLog.Multiline = $true
$txtLog.ScrollBars = 'Vertical'
$txtLog.Location = '10,220'
$txtLog.Size = '420,90'
$form.Controls.Add($txtLog)

function Append-Log($msg) {
    $txtLog.AppendText("$msg`r`n")
}

$btnRun.Add_Click({
    $current = Get-CurrentVersion
    $manual = $txtManual.Text.Trim()
    $bumpType = $cbBump.SelectedItem
    $note = if ([string]::IsNullOrWhiteSpace($txtNote.Text)) { "New features or fixes" } else { $txtNote.Text.Trim() }

    if ([string]::IsNullOrWhiteSpace($manual)) {
        $parts = $current -replace '-.*$', '' -split '\.'
        $maj=[int]$parts[0]; $min=[int]$parts[1]; $pat=[int]$parts[2]
        switch ($bumpType) {
            'major' { $maj++; $min=0; $pat=0 }
            'minor' { $min++; $pat=0 }
            default { $pat++ }
        }
        $newVersion = "$maj.$min.$pat"
    } else {
        $newVersion = $manual
    }

    Append-Log "New version: $newVersion"
    Update-VersionFiles -newVersion $newVersion -releaseNote $note
    Append-Log "Files updated."

    if ($chkGit.Checked) {
        Append-Log "Running git commit/tag/push..."
        Run-GitOps -version $newVersion
        Append-Log "Git operations completed."
    } else {
        Append-Log "Skipped git operations. Remember to commit/tag/push manually."
    }

    $lblCurrent.Text = "Current version: $(Get-CurrentVersion)"
    Append-Log "Done."
})

[void]$form.ShowDialog()
