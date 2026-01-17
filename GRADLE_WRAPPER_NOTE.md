# ⚠️ Important: Gradle Wrapper Note

The Gradle wrapper zip file (`gradle-9.2.1-bin.zip`) was too large for GitHub (129 MB > 100 MB limit).

## ✅ Solution Applied

The Gradle wrapper zip is now excluded from the repository. This is actually a **common practice** and won't affect functionality!

## 🔧 For Users Cloning Your Repository

When someone clones your repository, they just need to run:

```powershell
.\gradlew.bat build
```

Gradle will **automatically download** the wrapper zip file on first run. This is the standard way Gradle projects work!

## 📝 What's in the Repository

✅ `gradlew` and `gradlew.bat` - Gradle wrapper scripts  
✅ `gradle-wrapper.jar` - Gradle wrapper jar  
✅ `gradle-wrapper.properties` - Configuration (specifies which Gradle version)  
❌ `gradle-9.2.1-bin.zip` - Excluded (auto-downloaded by Gradle)

## 🎯 This is Normal!

Most Gradle projects on GitHub exclude the wrapper zip. Check out popular Minecraft mods - they all do the same thing!

---

**Your repository is fully functional and ready to use!** ✅
