@echo off
echo.
echo ============================================================
echo   KimDog SMP - GitHub Push Helper
echo ============================================================
echo.
echo The GitHub repository page is opening in your browser...
echo.
start https://github.com/new
echo.
echo Please create the repository with these settings:
echo   - Name: KimDogSMP-Client
echo   - Description: KimDog SMP - Minecraft Fabric Mod
echo   - DO NOT initialize with README
echo   - DO NOT add .gitignore or license
echo.
echo Press any key after creating the repository...
pause > nul
echo.
echo Pushing to GitHub...
echo.
cd /d "%~dp0"
git push -u origin main
echo.
if %errorlevel% equ 0 (
    echo ============================================================
    echo   SUCCESS! Repository is live on GitHub!
    echo ============================================================
    echo.
    echo View your repository:
    echo https://github.com/NotKimDog/KimDogSMP-Client
    echo.
    echo Next steps:
    echo   1. Build mod: gradlew.bat build
    echo   2. Create release at:
    echo      https://github.com/NotKimDog/KimDogSMP-Client/releases/new
    echo.
    start https://github.com/NotKimDog/KimDogSMP-Client
) else (
    echo ============================================================
    echo   ERROR: Push failed!
    echo ============================================================
    echo.
    echo Possible issues:
    echo   - Repository not created yet
    echo   - Authentication required
    echo   - Wrong GitHub account
    echo.
    echo Try using GitHub Desktop instead:
    echo https://desktop.github.com/
)
echo.
pause
