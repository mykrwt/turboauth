# TurboAuth Plugin JAR

This folder contains the compiled JAR file for the **TurboAuth** Minecraft plugin.

## 📦 File Information
- **File Name**: TurboAuth-v02.jar
- **Size**: ~37KB
- **Version**: 0.2.0
- **Compatible With**: Paper 1.20+ (Minecraft servers)

## 🚀 How to Install

1. **Download** the `TurboAuth-v02.jar` file
2. **Stop** your Minecraft Paper server if it's running
3. **Copy** the JAR file to your server's `plugins/` folder
4. **Start** your server
5. The plugin will automatically generate a `config.yml` file in `plugins/TurboAuth/`

## ⚙️ Initial Setup

After first installation:

1. **Set Authentication Spawn** (where unregistered players spawn):
   ```
   /turboauth setauthspawn
   ```

2. **Set Fallback Spawn** (backup spawn location):
   ```
   /turboauth setfallbackspawn
   ```

3. **Configure settings** in `plugins/TurboAuth/config.yml` to customize:
   - Password requirements
   - Session timeout
   - Brute force protection
   - Messages and colors
   - Sound effects

4. **Reload configuration** (if you made changes):
   ```
   /turboauth reload
   ```

## 🎮 Player Commands

- `/register <password> <password>` - Register a new account
- `/login <password>` - Login to existing account

## 🛠️ Admin Commands

- `/turboauth reload` - Reload configuration
- `/turboauth setauthspawn` - Set authentication spawn point
- `/turboauth setfallbackspawn` - Set fallback spawn point
- `/turboauth info` - Show plugin information

**Required Permission**: `turboauth.admin`

## 📋 Features

✅ Lightweight authentication system for cracked/offline servers  
✅ Password-based registration and login  
✅ Automatic location saving and restoration  
✅ Session support with configurable timeouts  
✅ Brute force protection with IP banning  
✅ Beautiful visual effects (titles, sounds, animations)  
✅ Highly configurable via config.yml  
✅ File-based YAML storage (no database required)  

## 📝 Notes

- This plugin is designed for **offline/cracked** Minecraft servers
- Player data is stored in `plugins/TurboAuth/players/`
- Make sure to set spawn points before players join!
- Default password requirements: 6-20 characters

## 🔧 Troubleshooting

If the plugin doesn't load:
1. Check you're running Paper 1.20 or newer
2. Check server logs for errors
3. Ensure Java 17 or newer is installed
4. Verify the JAR file isn't corrupted (should be ~37KB)

---

**Happy authenticating! 🔐**
