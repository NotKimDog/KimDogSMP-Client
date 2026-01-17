# 🤖 Automated Release System - Complete Guide

## 🎯 What I've Set Up For You

Your KimDog SMP mod now has a **fully automated release system**!

---

## ✨ Features

### 1. **Auto Version Bump**
- One command to increment version
- Updates all necessary files automatically
- Creates git tags

### 2. **Auto Build & Release**
- Builds mod on GitHub servers
- Creates GitHub release automatically
- Uploads JAR file
- Your auto-updater detects it immediately!

### 3. **Continuous Integration**
- Auto-builds on every push to main
- Catches errors early
- Artifacts available for testing

---

## 🚀 How to Release a New Version

### Method 1: Auto Version Bump (EASIEST)

Just run this command:

```powershell
.\bump-version.ps1
```

**Or specify bump type:**

```powershell
.\bump-version.ps1 -BumpType patch   # 1.0.0 → 1.0.1 (bug fixes)
.\bump-version.ps1 -BumpType minor   # 1.0.0 → 1.1.0 (new features)
.\bump-version.ps1 -BumpType major   # 1.0.0 → 2.0.0 (breaking changes)
```

**What it does:**
1. ✅ Increments version number
2. ✅ Updates `gradle.properties`
3. ✅ Updates `Kimdog_smp.java`
4. ✅ Updates `CHANGELOG.md`
5. ✅ Commits changes
6. ✅ Creates git tag
7. ✅ Pushes to GitHub
8. ✅ **Triggers automatic build and release!**

**Wait 2-3 minutes and your release is live!** 🎉

---

### Method 2: Manual (Traditional Way)

If you prefer manual control:

```powershell
# 1. Update version in gradle.properties
# mod_version=1.0.1

# 2. Update VERSION in Kimdog_smp.java
# private static final String VERSION = "1.0.1";

# 3. Update CHANGELOG.md with your changes

# 4. Commit changes
git add gradle.properties src/main/java/kimdog/kimdog_smp/Kimdog_smp.java CHANGELOG.md
git commit -m "Release version 1.0.1"

# 5. Create and push tag
git tag -a v1.0.1 -m "Release version 1.0.1"
git push origin main
git push origin v1.0.1
```

**The tag push triggers the automatic release!**

---

## 📋 How the Automation Works

### When You Push a Tag (v1.0.0, v1.1.0, etc.)

**GitHub Actions Workflow (`release.yml`) runs:**

```
1. Checkout code
2. Set up Java 21
3. Build mod with Gradle
4. Rename JAR to kimdog-smp-X.X.X.jar
5. Create GitHub Release
6. Upload JAR file
7. Done! 🎉
```

**Result:**
- Release appears at: https://github.com/NotKimDog/KimDogSMP-Client/releases
- JAR file is attached
- Your auto-updater detects it instantly!

### When You Push to Main Branch

**GitHub Actions Workflow (`build.yml`) runs:**

```
1. Checkout code
2. Set up Java 21
3. Build mod with Gradle
4. Upload artifacts (available for 7 days)
```

**Result:**
- Ensures your code always compiles
- Dev builds available for testing
- Catches errors before release

---

## 🎮 Example Workflow

### Scenario: You fixed a bug

```powershell
# 1. Make your code changes
# ... edit files ...

# 2. Commit your changes
git add .
git commit -m "Fixed XYZ bug"
git push origin main

# 3. Wait for build to pass (check GitHub Actions)

# 4. When ready to release, bump version:
.\bump-version.ps1 -BumpType patch

# 5. Done! GitHub Actions handles the rest
```

**In 2-3 minutes:**
- New release is live on GitHub
- Players get notified in-game
- They can use `/kimdogsmp update download` to auto-install

---

## 📁 Files Created

### `.github/workflows/release.yml`
**Triggers:** When you push a version tag (v1.0.0, v1.1.0, etc.)
**Does:**
- Builds mod
- Creates GitHub release
- Uploads JAR file
- Adds release notes

### `.github/workflows/build.yml`
**Triggers:** On every push to main branch
**Does:**
- Builds mod
- Runs tests
- Uploads dev artifacts

### `bump-version.ps1`
**Purpose:** Automate version bumping
**Usage:** `.\bump-version.ps1 -BumpType patch|minor|major`
**Does:**
- Updates all version references
- Creates git tag
- Pushes to GitHub

---

## 🎯 Version Numbering Guide

### Semantic Versioning: MAJOR.MINOR.PATCH

**PATCH (1.0.0 → 1.0.1)**
- Bug fixes
- Small tweaks
- No new features
- Command: `.\bump-version.ps1 -BumpType patch`

**MINOR (1.0.0 → 1.1.0)**
- New features
- Improvements
- Backward compatible
- Command: `.\bump-version.ps1 -BumpType minor`

**MAJOR (1.0.0 → 2.0.0)**
- Breaking changes
- Major overhaul
- Not backward compatible
- Command: `.\bump-version.ps1 -BumpType major`

---

## 🔍 Checking Build Status

### GitHub Actions Page
https://github.com/NotKimDog/KimDogSMP-Client/actions

**You'll see:**
- ✅ Build passed
- ❌ Build failed (with error details)
- ⏳ Build in progress

### Release Page
https://github.com/NotKimDog/KimDogSMP-Client/releases

**You'll see:**
- All your releases
- Download counts
- Release notes
- JAR files

---

## 🐛 Troubleshooting

### Build Fails on GitHub

**Check:**
1. Go to Actions tab
2. Click the failed run
3. Read error messages
4. Fix issues locally
5. Push fix and tag again

### Tag Already Exists

```powershell
# Delete local tag
git tag -d v1.0.0

# Delete remote tag
git push origin :refs/tags/v1.0.0

# Create new tag
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0
```

### Want to Redo a Release

```powershell
# Delete release on GitHub (via web interface)
# Delete tag (see above)
# Re-run bump-version.ps1 or create tag manually
```

---

## 📝 Customizing Release Notes

### Edit `.github/workflows/release.yml`

Find the `body:` section and customize:

```yaml
body: |
  ## 🎮 KimDog SMP v${{ steps.get_version.outputs.VERSION }}
  
  ### 🆕 What's New
  - Your custom changes here
  
  ### 🐛 Bug Fixes
  - List of fixes
  
  ### 📥 Installation
  Download and install instructions
```

---

## 🎨 Adding Discord Notifications

### Step 1: Get Discord Webhook

1. Go to your Discord server
2. Server Settings → Integrations → Webhooks
3. Create webhook, copy URL

### Step 2: Add to GitHub Secrets

1. Go to: https://github.com/NotKimDog/KimDogSMP-Client/settings/secrets/actions
2. Click "New repository secret"
3. Name: `DISCORD_WEBHOOK`
4. Value: Your webhook URL

### Step 3: Enable in release.yml

Change this line:
```yaml
- name: Notify Discord (Optional)
  if: false  # Change to: if: true
```

Add this code:
```yaml
  run: |
    curl -H "Content-Type: application/json" \
         -d "{\"content\": \"🎉 New release: KimDog SMP v${{ steps.get_version.outputs.VERSION }} is now available!\"}" \
         ${{ secrets.DISCORD_WEBHOOK }}
```

---

## 📊 Release Checklist

Before running `bump-version.ps1`:

- [ ] All code changes committed and pushed
- [ ] Tests passing locally
- [ ] CHANGELOG.md updated with changes
- [ ] Version bump type decided (patch/minor/major)
- [ ] Ready to make release public

After running script:

- [ ] Check GitHub Actions status
- [ ] Verify release appears on GitHub
- [ ] Test auto-updater in-game
- [ ] Announce to players

---

## 🚀 Quick Reference Commands

```powershell
# Release a patch version (bug fixes)
.\bump-version.ps1

# Release a minor version (new features)
.\bump-version.ps1 -BumpType minor

# Release a major version (breaking changes)
.\bump-version.ps1 -BumpType major

# Check build status
# Go to: https://github.com/NotKimDog/KimDogSMP-Client/actions

# View releases
# Go to: https://github.com/NotKimDog/KimDogSMP-Client/releases

# Manual build locally
.\gradlew.bat build

# View current version
cat gradle.properties | Select-String "mod_version"
```

---

## 🎉 Summary

**You can now release a new version with ONE command:**

```powershell
.\bump-version.ps1
```

**And GitHub automatically:**
1. ✅ Builds your mod
2. ✅ Creates release
3. ✅ Uploads JAR
4. ✅ Your auto-updater works!

**No more manual building, renaming, uploading!** 🎊

---

## 📚 Additional Resources

- **GitHub Actions Docs:** https://docs.github.com/en/actions
- **Semantic Versioning:** https://semver.org/
- **GitHub Releases:** https://docs.github.com/en/repositories/releasing-projects-on-github

---

## ✅ First Release Steps

To test the system and create your first release:

```powershell
# 1. Commit and push the workflow files
git add .github/workflows/* bump-version.ps1 gradle.properties
git commit -m "Add automated release system"
git push origin main

# 2. Create your first release
.\bump-version.ps1

# 3. Watch the magic happen at:
# https://github.com/NotKimDog/KimDogSMP-Client/actions
```

**That's it! Your automated release system is ready!** 🚀
