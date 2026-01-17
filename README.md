<div align="center">

# 🎮 KimDog SMP

### A Comprehensive Minecraft Server Enhancement Suite

**Enhance your SMP with powerful features, anti-cheat protection, and more!**

[![Minecraft 1.21](https://img.shields.io/badge/Minecraft-1.21-green?style=for-the-badge&logo=minecraft)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-Latest-orange?style=for-the-badge&logo=files)](https://fabricmc.net/)
[![License MIT](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE.txt)
[![GitHub Release](https://img.shields.io/github/v/release/NotKimDog/KimDogSMP-Client?style=for-the-badge&logo=github)](https://github.com/NotKimDog/KimDogSMP-Client/releases)
[![GitHub Stars](https://img.shields.io/github/stars/NotKimDog/KimDogSMP-Client?style=for-the-badge&logo=github)](https://github.com/NotKimDog/KimDogSMP-Client)

---

<!-- ADD YOUR HERO IMAGE HERE -->
<!-- ![KimDog SMP Banner](https://imgur.com/YOUR_IMAGE_ID.png) -->
<!-- Replace the URL above with your image -->

</div>

## 📖 About

KimDog SMP is a comprehensive **server enhancement suite** for Minecraft 1.21 built on the Fabric mod loader. It combines powerful utilities, anti-cheat protection, automated systems, and player conveniences into one cohesive package designed specifically for SMPs and community servers.

> **Perfect for:** Vanilla+ Survival Servers • SMPs • Community Play • Custom Mechanics

---

## ✨ Features at a Glance

<div align="center">

| Feature | Description | Command |
|---------|-------------|---------|
| **⛏️ VeinMiner** | Mine entire ore veins with Shift • Quest system • Upgrades | `/veinminer` |
| **💬 Chat System** | Custom formatting • Auto-announcements • Admin controls | `/chatmessages` |
| **🚪 Double Doors** | Synchronized door pairs • Trapdoor support • Smart detection | Auto-enabled |
| **🛡️ AntiCheat** | Speed/Fly/Reach detection • Real-time logging • Configurable | `/anticheat` |
| **🎮 Commands** | Flight mode • Configuration • Statistics • Version checking | `/fly`, `/quest` |
| **🔄 Auto Updates** | GitHub integration • One-click updates • Auto-installation | `/kimdogsmp update` |
| **🔍 Zoom [WIP]** | Camera zoom capability • Configurable levels • Smooth zoom | Press Z |

</div>

---

## 🎯 Core Modules

### ⛏️ VeinMiner Pro
Mine entire ore veins instantly and track your progress!

- **Instant Vein Mining** - Hold Shift while mining to instantly collect entire ore veins
- **Quest System** - Daily mining challenges with rewards and progression
- **Level Upgrades** - Upgrade your mining capabilities and unlock new features
- **Statistics** - Track blocks mined, veins found, time played, and more
- **Configurable** - Customize ore types, limits, and behavior
- **Anti-Lag** - Smart optimization to prevent server lag

```
/veinminer stats      - View your statistics
/veinminer quests     - View available quests
/veinminer upgrades   - View and purchase upgrades
```

### 💬 Enhanced Chat System
Transform your server's communication!

- **Custom Formatting** - Server-wide message formatting and colors
- **Auto-Announcements** - Scheduled server announcements and tips
- **Admin Controls** - Manage messages from in-game
- **Broadcasting** - Announce events and milestones
- **Customizable** - Configure formats, timing, and content

```
/chatmessages reload  - Reload chat configuration
/chatmessages list    - View all messages
/chatmessages add     - Add a new message
```

### 🚪 Double Door Mechanics
Smart, synchronized door operations!

- **Auto-Pair Detection** - Automatically detects door pairs
- **Synchronized Opening** - Both doors open/close together
- **Trapdoor Support** - Works with trapdoors, fence gates, and more
- **Seamless Integration** - No configuration needed
- **Zero Performance Impact** - Optimized code

```
Configuration: config/kimdog_smp/doubledoor.json
```

### 🛡️ AntiCheat System [WIP]
Protect your server from cheaters!

- **Speed Detection** - Detects speed hacking with configurable sensitivity
- **Fly Prevention** - Monitors abnormal flight patterns
- **Reach Monitoring** - Detects extended player reach
- **Real-Time Logging** - Detailed violation logs and statistics
- **Configurable Actions** - Warnings, kicks, or bans
- **Whitelist Support** - Exempt trusted players

```
/anticheat status     - View AntiCheat status
/anticheat violations - View recent violations
/anticheat config     - Configure settings (Admin)
```

### 🎮 Command System
Full control in your hands!

```
/fly [on|off]              - Toggle flight mode (Admin)
/veinminer [subcommand]    - VeinMiner controls
/quest [view|complete]     - Manage quests
/chatmessages [subcommand] - Chat system controls (Admin)
/anticheat [subcommand]    - AntiCheat controls (Admin)
/kimdogsmp version         - Show version info
/kimdogsmp update check    - Check for updates
/kimdogsmp update download - Download and install updates
/zoom [level]              - Adjust zoom level (default: Z key)
```

### 🔄 Automatic Updates
Stay up-to-date automatically!

- **GitHub Integration** - Monitors releases in real-time
- **Auto-Detection** - Notifies when updates are available
- **One-Click Install** - Download and apply updates with one command
- **Smart Cleanup** - Automatically removes old versions
- **Progress Tracking** - Real-time download progress
- **Version Checking** - Always know what you're running

```
Automatic checks on server startup
Use: /kimdogsmp update download
```

### 🔍 Zoom Feature [WIP]
Camera zoom for better gameplay! (Work in Progress)

- **Zoom Toggle** - Press Z to zoom in/out (configurable keybind)
- **Smooth Transitions** - Fluid zoom animations
- **Adjustable Levels** - Customize zoom distances
- **Performance Friendly** - Minimal server impact

```
Press Z          - Toggle zoom
/zoom [level]    - Adjust zoom level
```

---

## 📥 Installation

### System Requirements
- ✅ **Minecraft 1.21**
- ✅ **Fabric Loader** (latest version)
- ✅ **Fabric API** (latest version)
- ✅ **Java 21+** (for Minecraft 1.21)

### Installation Steps

1. **Download**
   - Go to [Releases](https://github.com/NotKimDog/KimDogSMP-Client/releases)
   - Download `kimdog-smp-X.X.X.jar`

2. **Install**
   - Place the JAR in your `mods/` folder
   - Ensure `fabric-api-*.jar` is also in `mods/`

3. **Configure**
   - Start the server/client
   - Configuration files will be created in `config/kimdog_smp/`
   - Customize settings as needed (optional)

4. **Enjoy!**
   - All features are enabled by default
   - Use `/help` to see available commands

### Auto-Updates
The mod checks for updates on startup. Install updates with:
```
/kimdogsmp update download
```

---

## ⚙️ Configuration

All configuration files are located in `config/kimdog_smp/`:

| File | Purpose |
|------|---------|
| `veinminer.json` | VeinMiner settings, ore types, limits |
| `chatmessages.json` | Chat formatting, announcements, scheduling |
| `doubledoor.json` | Door mechanics, detection radius |
| `anticheat.json` | Sensitivity, detection thresholds, actions |
| `zoom.json` | Zoom settings and keybinds |

### Example: veinminer.json
```json
{
  "enabled": true,
  "ores": ["diamond", "emerald", "lapis", "redstone"],
  "maxVeinSize": 32,
  "requireShift": true,
  "questsEnabled": true
}
```

---

## 🎮 Tips & Tricks

### VeinMiner
- **Hold Shift** while mining ore to activate vein mining
- Check **daily quests** for bonus rewards
- **Upgrade your level** to unlock special abilities
- Monitor **statistics** to track your progress

### AntiCheat
- Enable **logging** to monitor suspicious activity
- Set **appropriate thresholds** for your server playstyle
- Whitelist **trusted admins** to prevent false positives
- Review **violation logs** regularly

### Chat System
- Schedule **announcements** during peak hours
- Use **formatting codes** for colorful messages
- Create **welcome messages** for new players
- Set **recurring reminders** for server rules

---

## 🚀 Advanced Features

### Version Management
- Automatic version bumping during development
- GitHub release integration
- One-click deployment to players

### Performance
- Optimized code for minimal server impact
- Smart caching and memory management
- Efficient update checking (daily, configurable)

### Compatibility
- Works with other Fabric mods
- Compatible with most server types
- No conflicts with vanilla features

---

## 📝 Changelog

See [CHANGELOG.md](CHANGELOG.md) for detailed version history and updates.

### Latest Version: v1.0.0
- ✨ Initial release
- 🎯 All core features enabled
- 🔧 Full configuration support

---

## 🤝 Contributing

Contributions are welcome! Feel free to:
- Report bugs via [Issues](https://github.com/NotKimDog/KimDogSMP-Client/issues)
- Submit feature requests
- Create pull requests with improvements

---

## 📄 License

This project is licensed under the **MIT License** - see [LICENSE.txt](LICENSE.txt) for details.

---

## 🔗 Links

- **GitHub Repository**: https://github.com/NotKimDog/KimDogSMP-Client
- **Releases**: https://github.com/NotKimDog/KimDogSMP-Client/releases
- **Issues**: https://github.com/NotKimDog/KimDogSMP-Client/issues

---

<div align="center">

**Made with ❤️ for the Minecraft community**

If you find this mod useful, please consider giving it a ⭐ on GitHub!

</div>
