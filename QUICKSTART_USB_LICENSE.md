# ⚡ Quick Start - USB Dongle License

**5-Minute Setup Guide**

---

## ✅ What's Already Done

Your app now has USB dongle license protection configured for:

**Your SanDisk USB:**
- Serial: `4C530000200311207422`
- Vendor: `SANDISK`
- Product: `CRUZER_BLADE`
- Hash: `a1b96c678471f7d8d73573fb9f691e255e9e5b3296628dc52a085c4127624f3b`

✅ All code implemented  
✅ Android permissions configured  
✅ Desktop detection implemented  
✅ License UI screens created  

---

## 🚀 Test Right Now

### Android (with USB OTG)

1. **Connect USB** to Android device using OTG adapter
2. **Build and run:**
   ```bash
   ./gradlew installDebug
   ```
3. **Check logs:**
   ```bash
   adb logcat | grep -E "License|USB"
   ```
4. **Expected:** App launches normally ✅

5. **Remove USB** while app running
6. **Expected:** App locks immediately 🔒

### Desktop (Windows/Linux/macOS)

1. **Connect USB** to computer
2. **Build and run:**
   ```bash
   ./gradlew run
   ```
3. **Check console** for:
   ```
   ✅ Licensed USB detected!
   ```
4. **Expected:** App launches normally ✅

5. **Remove USB** while app running
6. **Expected:** App locks within 3 seconds 🔒

---

## 🔍 Debug Mode (Currently ENABLED)

Debug mode is **ON** by default to help you test.

**You'll see detailed logs like:**
```
🖥️  Operating System: windows 11
🔍 Scanning Windows USB devices...
🔑 Raw Fingerprint: 4C530000200311207422|SANDISK|CRUZER_BLADE
🔐 SHA-256 Hash: a1b96c678471f7d8d73573fb9f691e255e9e5b3296628dc52a085c4127624f3b
✅ Licensed USB detected!
```

### ⚠️ Before Release: Disable Debug Mode

Edit: `composeApp/src/commonMain/kotlin/com/ram/orai/oraic/license/LicenseManager.kt`

```kotlin
const val DEBUG_MODE = false  // Change from true to false
```

---

## 🧪 Testing Scenarios

### Scenario 1: Correct USB Connected
- ✅ App launches
- ✅ Main screen appears
- ✅ Logs show: `✅ Licensed USB detected!`

### Scenario 2: No USB Connected
- ❌ App shows lock screen
- 🔒 "Licensed USB Not Found" message
- 🔘 "Retry" and "Exit" buttons

### Scenario 3: Wrong USB Connected
- ❌ App shows lock screen
- 🔒 "Licensed USB Not Found" message
- 📝 Logs show: `⚠️ USB connected but not licensed`

### Scenario 4: USB Removed While Running
- ⚠️ App locks (instant on Android, ~3s on Desktop)
- 🔒 Lock screen appears
- 📝 Logs show: `⚠️ USB removed - locking application`

### Scenario 5: USB Reconnected
- 🔘 Click "Retry" button
- ✅ App unlocks
- 📱 Main screen appears

---

## 📦 Build for Distribution

### Android APK

```bash
# Debug build (for testing)
./gradlew assembleDebug
# Output: composeApp/build/outputs/apk/debug/composeApp-debug.apk

# Release build (for distribution)
./gradlew assembleRelease
# Output: composeApp/build/outputs/apk/release/composeApp-release.apk
```

### Desktop MSI (Windows)

```bash
# Create distributable
./gradlew createDistributable

# Package MSI installer
./gradlew packageMsi
# Output: composeApp/build/compose/binaries/main/msi/
```

### Desktop JAR

```bash
./gradlew jvmJar
# Output: composeApp/build/libs/composeApp-jvm.jar
```

---

## 🔧 Adding More USBs

Want to authorize additional USB dongles?

1. **Connect new USB**
2. **Run app** with `DEBUG_MODE = true`
3. **Copy hash** from logs
4. **Edit** `LicenseManager.kt`:

```kotlin
private val AUTHORIZED_HASHES = setOf(
    "a1b96c678471f7d8d73573fb9f691e255e9e5b3296628dc52a085c4127624f3b", // USB 1
    "your_new_hash_here",  // USB 2
    "another_hash_here"    // USB 3
)

fun isLicensed(): Boolean {
    val currentFingerprint = getUsbFingerprint() ?: return false
    return currentFingerprint in AUTHORIZED_HASHES
}
```

Or use the hash generator tool:
```powershell
.\tools\generate_usb_hash.ps1 -Serial "XYZ" -Vendor "VENDOR" -Product "PRODUCT"
```

---

## 📱 Platform Requirements

### Android
- ✅ Android 6.0+ (API 23+)
- ✅ USB OTG support
- ✅ OTG adapter (if device doesn't have full USB port)

### Windows
- ✅ Windows 10/11
- ⚠️ May need Administrator rights (first run)

### Linux
- ✅ Ubuntu/Debian/Fedora/etc.
- ⚠️ May need user in `disk` group

### macOS
- ✅ macOS 10.14+
- ⚠️ May need Terminal permissions

---

## 🆘 Quick Troubleshooting

| Problem | Solution |
|---------|----------|
| App always locked | 1. Check USB is connected<br>2. Enable `DEBUG_MODE`<br>3. Check logs for fingerprint |
| USB not detected (Android) | 1. Check OTG adapter<br>2. Try different USB port<br>3. Check USB has serial number |
| USB not detected (Windows) | 1. Run as Administrator<br>2. Check USB driver installed<br>3. Try different USB port |
| Wrong hash in logs | Use exact fingerprint from logs to regenerate hash |

---

## 📚 Full Documentation

- **Complete Guide:** `USB_DONGLE_LICENSE_SETUP.md`
- **Hash Generator Tools:** `tools/README.md`
- **Code Reference:** See `/license/` folders in `commonMain`, `androidMain`, `jvmMain`

---

## 🎯 Next Steps

1. ✅ **Test with your USB** (connected and disconnected)
2. ✅ **Check debug logs** to verify detection
3. ✅ **Test on all target platforms**
4. ⚠️ **Set `DEBUG_MODE = false`** before release
5. 📦 **Build release versions**
6. 🚀 **Distribute with USB dongle**

---

**Implementation Date:** January 16, 2026  
**Status:** ✅ Ready to Test  
**License Type:** Hardware USB Dongle  

🎉 **Your app is now protected with USB dongle licensing!**



