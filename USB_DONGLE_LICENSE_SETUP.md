# 🔐 USB Dongle License Protection - Implementation Guide

**Implementation Date:** January 16, 2026  
**Project:** ORAIC (Oral Radiographic Artificial Intelligence Consultant)  
**License Type:** Hardware USB Dongle

---

## 📋 Overview

Your application now includes **commercial-grade USB dongle license protection** that:

✅ **Works offline** - No internet or cloud services required  
✅ **Multi-platform** - Supports Android, Windows, Linux, and macOS  
✅ **Real-time monitoring** - Detects USB removal instantly (Android) or within 3 seconds (Desktop)  
✅ **Secure** - Only SHA-256 hash stored, never plaintext USB details  
✅ **Zero recurring costs** - No subscription or licensing server needed  

---

## 🔑 Your Licensed USB Details

**Device Information:**
- **Serial Number:** `4C530000200311207422`
- **Vendor:** `SANDISK`
- **Product:** `CRUZER_BLADE`

**Raw Fingerprint:**
```
4C530000200311207422|SANDISK|CRUZER_BLADE
```

**SHA-256 Hash (configured in app):**
```
a1b96c678471f7d8d73573fb9f691e255e9e5b3296628dc52a085c4127624f3b
```

> ⚠️ **IMPORTANT:** Keep this information secure! This is your master license key.

---

## 📁 Files Created

### Common (Platform-Agnostic)
```
composeApp/src/commonMain/kotlin/com/ram/orai/oraic/license/
├── LicenseManager.kt      - Core license logic
└── Sha256Util.kt          - SHA-256 hashing interface
```

### Android Implementation
```
composeApp/src/androidMain/kotlin/com/ram/orai/oraic/license/
├── LicenseManager.android.kt  - Android USB detection
└── Sha256Util.android.kt      - Android SHA-256 implementation
```

### Desktop (JVM) Implementation
```
composeApp/src/jvmMain/kotlin/com/ram/orai/oraic/license/
├── LicenseManager.jvm.kt      - Windows/Linux/macOS USB detection
└── Sha256Util.jvm.kt          - JVM SHA-256 implementation
```

### Modified Files
- `composeApp/src/androidMain/kotlin/com/ram/orai/oraic/MainActivity.kt` - Added license check UI
- `composeApp/src/androidMain/AndroidManifest.xml` - Added USB permissions
- `composeApp/src/jvmMain/kotlin/com/ram/orai/oraic/main.kt` - Added license check UI

---

## 🚀 How It Works

### 1. **Startup**
When the app launches, it:
1. Initializes the license manager
2. Scans for connected USB devices
3. Generates SHA-256 hash of each USB device's fingerprint
4. Compares with the stored `MASTER_USB_HASH`
5. Shows main app if match found, otherwise shows lock screen

### 2. **Runtime Monitoring**

**Android:**
- Uses `BroadcastReceiver` to listen for USB attach/detach events
- **Instant detection** when USB is removed
- App locks immediately without USB

**Desktop (Windows/Linux/macOS):**
- Polls USB status every 3 seconds
- **~3 second delay** when USB is removed
- App locks when USB not found

### 3. **USB Fingerprint Format**

```
SERIAL|VENDOR|PRODUCT
```

**Examples:**
- `4C530000200311207422|SANDISK|CRUZER_BLADE`
- `ABC123XYZ456|KINGSTON|DATATRAVELER`

The app creates a SHA-256 hash of this fingerprint and compares it to `MASTER_USB_HASH`.

---

## 🧪 Testing Checklist

### ✅ Android Testing

1. **With USB connected (OTG adapter):**
   - [ ] App launches normally
   - [ ] Main app screen appears
   - [ ] Check logcat for: `✅ Licensed USB detected!`

2. **Without USB on startup:**
   - [ ] Shows "Checking License" screen briefly
   - [ ] Shows "Licensed USB Not Found" screen
   - [ ] "Retry" and "Exit" buttons appear

3. **USB removal while running:**
   - [ ] App locks immediately
   - [ ] Shows lock screen
   - [ ] Check logcat for: `⚠️ USB device detached`

4. **USB reconnection:**
   - [ ] Click "Retry" button
   - [ ] App unlocks if USB is correct
   - [ ] Main app appears

### ✅ Desktop (Windows) Testing

1. **With USB connected:**
   - [ ] App launches normally
   - [ ] Main app screen appears
   - [ ] Check console for: `✅ Licensed USB detected!`

2. **Without USB on startup:**
   - [ ] Shows "Checking License" screen briefly
   - [ ] Shows "Licensed USB Not Found" screen
   - [ ] "Retry" and "Exit" buttons appear

3. **USB removal while running:**
   - [ ] App locks within ~3 seconds
   - [ ] Shows lock screen
   - [ ] Check console for: `⚠️ USB removed`

4. **USB reconnection:**
   - [ ] Wait ~3 seconds OR click "Retry"
   - [ ] App unlocks automatically or on retry
   - [ ] Main app appears

### ✅ Wrong USB Testing

1. **Connect different USB device:**
   - [ ] App stays locked
   - [ ] Shows "USB connected but not licensed" in logs
   - [ ] Lock screen persists

---

## 🔍 Debug Mode

**Current Setting:** `DEBUG_MODE = true` in `LicenseManager.kt`

When enabled, you'll see detailed logs:

```
🖥️  Operating System: windows 11
🔍 Scanning Windows USB devices...
📝 WMIC Output: (USB device info)
🔑 Raw Fingerprint: 4C530000200311207422|SANDISK|CRUZER_BLADE
🔐 SHA-256 Hash: a1b96c678471f7d8d73573fb9f691e255e9e5b3296628dc52a085c4127624f3b
🔍 License Check Debug:
   Current Fingerprint: a1b96c678471f7d8d73573fb9f691e255e9e5b3296628dc52a085c4127624f3b
   Master Hash: a1b96c678471f7d8d73573fb9f691e255e9e5b3296628dc52a085c4127624f3b
   Match: true
✅ Licensed USB detected!
```

### ⚠️ IMPORTANT: Disable Debug Mode for Production

In `composeApp/src/commonMain/kotlin/com/ram/orai/oraic/license/LicenseManager.kt`:

```kotlin
const val DEBUG_MODE = false  // Change to false before release
```

---

## 🔒 Production Release Checklist

Before distributing your app:

- [ ] Set `DEBUG_MODE = false` in `LicenseManager.kt`
- [ ] Test with USB connected (should work)
- [ ] Test without USB (should block)
- [ ] Build release APK/MSI
- [ ] Test release build (not debug)
- [ ] Enable ProGuard/R8 obfuscation (Android)
- [ ] Code signing for release builds
- [ ] Document USB serial number for backup

### Android ProGuard/R8 Configuration

Add to `composeApp/build.gradle.kts`:

```kotlin
android {
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

Create `composeApp/proguard-rules.pro`:

```proguard
# Keep license manager
-keep class com.ram.orai.oraic.license.** { *; }

# Obfuscate everything else
-repackageclasses ''
-allowaccessmodification
```

---

## 🔧 Troubleshooting

### Android

**Problem:** "UsbManager not available"  
**Solution:** Device doesn't support USB OTG. Use OTG adapter or test on different device.

**Problem:** USB device not detected  
**Solution:** 
- Check USB OTG adapter is connected properly
- Ensure USB has permissions (check Settings > Apps > Permissions)
- Try different USB port

**Problem:** "Serial number is null"  
**Solution:** Some USB devices don't report serial numbers. Try different USB brand.

### Desktop (Windows)

**Problem:** "Access denied" when running WMIC  
**Solution:** Run app as Administrator

**Problem:** USB not detected  
**Solution:** 
- Check USB is properly connected
- Try different USB port
- Restart application

**Problem:** PowerShell script errors  
**Solution:** Check PowerShell execution policy:
```powershell
Get-ExecutionPolicy
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Desktop (Linux)

**Problem:** "/dev/disk/by-id/ not accessible"  
**Solution:** Add user to disk group:
```bash
sudo usermod -a -G disk $USER
```

**Problem:** lsusb command not found  
**Solution:** Install usbutils:
```bash
sudo apt install usbutils  # Debian/Ubuntu
sudo yum install usbutils  # RHEL/CentOS
```

### Desktop (macOS)

**Problem:** system_profiler permission denied  
**Solution:** Grant Terminal/IDE security permissions in System Preferences > Security & Privacy

---

## 🔄 Adding Additional Licensed USBs

If you need to authorize multiple USB dongles:

### Option 1: Multiple Hash Check (Recommended)

Edit `LicenseManager.kt`:

```kotlin
private val AUTHORIZED_HASHES = setOf(
    "a1b96c678471f7d8d73573fb9f691e255e9e5b3296628dc52a085c4127624f3b", // USB 1
    "xyz123abc456...",  // USB 2
    "789def012ghi..."   // USB 3
)

fun isLicensed(): Boolean {
    val currentFingerprint = getUsbFingerprint() ?: return false
    return currentFingerprint in AUTHORIZED_HASHES
}
```

### Option 2: Generate Hash for New USB

1. Connect new USB device
2. Run app with `DEBUG_MODE = true`
3. Check logs for "Raw Fingerprint" and "SHA-256 Hash"
4. Add hash to `MASTER_USB_HASH` or `AUTHORIZED_HASHES`

---

## 📊 Platform Support Matrix

| Platform | Detection Method | Real-time Monitoring | Admin Required |
|----------|-----------------|---------------------|----------------|
| **Android** | UsbManager API | ✅ Instant (BroadcastReceiver) | ❌ No |
| **Windows** | WMIC / PowerShell | ⏱️ 3-second polling | ⚠️ Sometimes |
| **Linux** | /dev/disk/by-id | ⏱️ 3-second polling | ⚠️ For some USBs |
| **macOS** | system_profiler | ⏱️ 3-second polling | ❌ No |

---

## 🎯 Security Best Practices

1. **Never commit real hashes to public repositories**
   - Use environment variables for different builds
   - Store hashes in secure configuration files

2. **Use code obfuscation** (ProGuard/R8 for Android)
   - Makes reverse engineering harder
   - Protects license checking logic

3. **Keep backup USBs**
   - USB devices can fail
   - Have 2-3 backup dongles with same serial

4. **Document USB details securely**
   - Keep serial numbers in secure password manager
   - Don't email or share publicly

5. **Regular testing**
   - Test license check monthly
   - Verify USB still works
   - Check for OS updates that might break detection

---

## 📞 Technical Support

### Build Commands

**Android APK:**
```bash
./gradlew assembleDebug          # Debug build
./gradlew assembleRelease        # Release build
```

**Desktop JAR:**
```bash
./gradlew jvmJar
```

**Desktop MSI (Windows):**
```bash
./gradlew createDistributable
./gradlew packageMsi
```

### Logs Location

**Android:**
```bash
adb logcat | grep -E "License|USB"
```

**Desktop:**
- Check console output in IDE
- Or redirect stdout to file:
  ```bash
  java -jar app.jar > license.log 2>&1
  ```

---

## 🎉 Summary

Your ORAIC application is now protected with:

✅ **USB Dongle-based licensing**  
✅ **Multi-platform support** (Android, Windows, Linux, macOS)  
✅ **Real-time USB monitoring**  
✅ **Secure SHA-256 hashing**  
✅ **Professional lock screen UI**  
✅ **Zero cloud dependencies**  
✅ **No recurring costs**  

**Your licensed USB:**
- Serial: `4C530000200311207422`
- Vendor: `SANDISK`
- Product: `CRUZER_BLADE`

**Next Steps:**
1. Test thoroughly on all target platforms
2. Set `DEBUG_MODE = false` for production
3. Build and distribute with USB dongle
4. Keep backup USB dongles safe

---

**Last Updated:** January 16, 2026  
**Implementation Status:** ✅ Complete and Ready for Testing



