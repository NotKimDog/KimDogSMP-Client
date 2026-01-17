# KimDog SMP - Minecraft Fabric Mod

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21-brightgreen.svg)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-Latest-orange.svg)](https://fabricmc.net/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE.txt)
[![GitHub release](https://img.shields.io/github/v/release/NotKimDog/KimDogSMP-Client)](https://github.com/NotKimDog/KimDogSMP-Client/releases)

A comprehensive server enhancement suite for Minecraft 1.21 built on Fabric, featuring VeinMiner, enhanced chat systems, double door mechanics, anti-cheat protection, and automatic update capabilities.

## ✨ Features

### ⛏️ VeinMiner
- Mine entire ore veins instantly by holding Shift
- Quest system with daily mining challenges
- Upgradeable mining levels and statistics
- Configurable ore detection and mining limits

### 💬 Enhanced Chat System
- Custom chat formatting and colors
- Automatic server announcements
- Configurable message intervals
- Admin controls for message management

### 🚪 Double Door Mechanics
- Automatically opens/closes paired doors together
- Works with trapdoors as well
- Smart detection of door pairs
- Seamless synchronized operation

### 🛡️ AntiCheat System
- Speed hack detection
- Fly hack prevention
- Reach hack monitoring
- Configurable sensitivity and actions
- Real-time violation logging

### 🎮 Custom Commands
- `/fly` - Toggle flight mode (Admin)
- `/veinminer` - VeinMiner configuration and stats
- `/quest` - View and manage mining quests
- `/chatmessages` - Message system controls (Admin)
- `/anticheat` - AntiCheat status and controls (Admin)
- `/kimdogsmp update` - Check for mod updates
- `/kimdogsmp update download` - Auto-download and install updates
- `/kimdogsmp version` - Show mod version info

### 🔄 Automatic Updates
- Checks GitHub for new releases on startup
- Notifies players when updates are available
- One-command automatic download and installation
- Smart cleanup of old mod files
- Progress tracking and status monitoring

### 🔍 Zoom Feature
- Press `Z` to zoom in (configurable keybind)
- Smooth zoom transitions
- Adjustable zoom levels

## 📥 Installation

### Requirements
- Minecraft 1.21
- Fabric Loader (latest version)
- Fabric API (latest version)

### Steps
1. Download the latest release from [Releases](https://github.com/NotKimDog/KimDogSMP-Client/releases)
2. Place `kimdog-smp-X.X.jar` in your `mods/` folder
3. Ensure Fabric API is also in your `mods/` folder
4. Start your Minecraft server or client
5. Configure settings in `config/kimdog_smp/` as needed

## ⚙️ Configuration

Configuration files are located in `config/kimdog_smp/`:

- `veinminer.json` - VeinMiner settings
- `chatmessages.json` - Chat system configuration
- `doubledoor.json` - Door mechanics settings
- `anticheat.json` - AntiCheat sensitivity and actions
- `quests/` - Quest configuration files

## 🔄 Auto-Update System

The mod includes a built-in auto-updater:

1. **Automatic Checking**: Checks for updates on server startup
2. **Player Notifications**: Notifies players when updates are available
3. **One-Command Install**: Use `/kimdogsmp update download` to automatically download and install
4. **Smart Cleanup**: Automatically removes old mod versions

### Update Commands
```
/kimdogsmp update              # Check for updates
/kimdogsmp update download     # Download and install update
/kimdogsmp update status       # Check download progress
/kimdogsmp version             # Show version info
```

## 🎮 Commands

### Player Commands
- `/veinminer help` - Show VeinMiner help
- `/veinminer stats` - View your mining statistics
- `/quest` - View active quest
- `/quest new` - Generate new quest (if allowed)
- `/kimdogsmp update` - Check for mod updates
- `/kimdogsmp version` - Show mod version

### Admin Commands
- `/fly` - Toggle flight mode
- `/fly on/off` - Enable/disable flight
- `/fly speed <0.0-2.0>` - Set flight speed
- `/chatmessages` - Manage chat message system
- `/anticheat status` - View AntiCheat status
- `/anticheat toggle` - Enable/disable AntiCheat
- `/kimdogsmp update download` - Auto-download updates

## 🚀 Development

### Building from Source

```bash
# Clone the repository
git clone https://github.com/NotKimDog/KimDogSMP-Client.git
cd KimDogSMP-Client

# Build the mod
.\gradlew.bat build

# Output will be in build/libs/
```

### Project Structure
```
src/
├── main/
│   ├── java/kimdog/kimdog_smp/
│   │   ├── veinminer/          # VeinMiner system
│   │   ├── chatmessages/       # Chat system
│   │   ├── doubledoor/         # Door mechanics
│   │   ├── anticheat/          # AntiCheat system
│   │   ├── fly/                # Flight commands
│   │   ├── commands/           # Command implementations
│   │   ├── updater/            # Auto-update system
│   │   └── utils/              # Utilities
│   └── resources/
│       ├── fabric.mod.json
│       └── assets/
└── client/
    └── java/kimdog/kimdog_smp/client/
        ├── VeinMinerClient.java
        └── ZoomClient.java
```

## 📝 Version History

See [CHANGELOG.md](CHANGELOG.md) for detailed version history.

### Latest Version: 1.0.0-DEV
- Initial release
- VeinMiner with quest system
- Enhanced chat messages
- Double door mechanics
- AntiCheat protection
- Custom commands
- Auto-update system
- Zoom feature

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.

## 🐛 Bug Reports

Found a bug? Please open an issue on [GitHub Issues](https://github.com/NotKimDog/KimDogSMP-Client/issues) with:
- Description of the bug
- Steps to reproduce
- Expected behavior
- Screenshots (if applicable)
- Minecraft version, mod version, and other relevant mods

## 💬 Support

- **GitHub Issues**: [Report bugs or request features](https://github.com/NotKimDog/KimDogSMP-Client/issues)
- **Discussions**: [Ask questions or discuss ideas](https://github.com/NotKimDog/KimDogSMP-Client/discussions)

## 🙏 Acknowledgments

- Built with [Fabric](https://fabricmc.net/)
- Inspired by various Minecraft enhancement mods
- Thanks to all contributors and testers

## 📊 Statistics

- **Lines of Code**: ~15,000+
- **Modules**: 6 (VeinMiner, Chat, Doors, AntiCheat, Commands, Updater)
- **Commands**: 20+
- **Configuration Files**: 5+

---

**Made with ❤️ by KimDog Studios**

[Download Latest Release](https://github.com/NotKimDog/KimDogSMP-Client/releases) | [Documentation](GITHUB_SETUP_GUIDE.md) | [Report Issue](https://github.com/NotKimDog/KimDogSMP-Client/issues)
