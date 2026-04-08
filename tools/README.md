# 🔧 USB License Tools

This folder contains helper tools for managing USB dongle licenses.

## 📁 Contents

### 1. `generate_usb_hash.ps1` (Windows PowerShell)

PowerShell script to generate SHA-256 hashes for USB dongles.

**Usage:**
```powershell
# Use default SanDisk USB
.\tools\generate_usb_hash.ps1

# Use custom USB details
.\tools\generate_usb_hash.ps1 -Serial "ABC123" -Vendor "KINGSTON" -Product "DATATRAVELER"
```

**Requirements:**
- Windows PowerShell 5.0+
- No additional dependencies

---

### 2. `generate_usb_hash.py` (Python)

Python script to generate SHA-256 hashes for USB dongles.

**Usage:**
```bash
# Use default SanDisk USB
python tools/generate_usb_hash.py

# Use custom USB details
python tools/generate_usb_hash.py ABC123 KINGSTON DATATRAVELER
```

**Requirements:**
- Python 3.6+
- No additional dependencies (uses standard library)

---

## 🎯 Quick Examples

### Generate Hash for Your Default USB
```powershell
# PowerShell
.\tools\generate_usb_hash.ps1

# Python
python tools/generate_usb_hash.py
```

**Output:**
```
🔐 USB Dongle Hash Generator
================================

📱 USB Details:
   Serial:  4C530000200311207422
   Vendor:  SANDISK
   Product: CRUZER_BLADE

🔑 Raw Fingerprint:
   4C530000200311207422|SANDISK|CRUZER_BLADE

🔐 SHA-256 Hash:
   a1b96c678471f7d8d73573fb9f691e255e9e5b3296628dc52a085c4127624f3b

📋 Copy this into LicenseManager.kt:
   private const val MASTER_USB_HASH = "a1b96c678471f7d8d73573fb9f691e255e9e5b3296628dc52a085c4127624f3b"
```

### Generate Hash for Different USB
```powershell
# PowerShell
.\tools\generate_usb_hash.ps1 -Serial "XYZ789456" -Vendor "KINGSTON" -Product "DATATRAVELER"

# Python
python tools/generate_usb_hash.py XYZ789456 KINGSTON DATATRAVELER
```

---

## 🔍 How to Get USB Details

### Windows (PowerShell)
```powershell
# Method 1: Get-Disk (requires admin)
Get-Disk | Where-Object {$_.BusType -eq 'USB'} | Select-Object SerialNumber, Model, FriendlyName

# Method 2: WMIC
wmic diskdrive get serialnumber,model,pnpdeviceid
```

### Linux
```bash
# Method 1: lsusb
lsusb -v | grep -E "iSerial|idVendor|idProduct"

# Method 2: /dev/disk/by-id
ls -la /dev/disk/by-id/ | grep usb
```

### macOS
```bash
system_profiler SPUSBDataType
```

### Android (with USB OTG)
Run the app with `DEBUG_MODE = true` and check logcat:
```bash
adb logcat | grep "USB Device"
```

---

## 📝 Notes

- **Serial Format:** Remove spaces and special characters, use uppercase
- **Vendor Format:** Usually uppercase, replace spaces with underscores
- **Product Format:** Usually uppercase, replace spaces with underscores

**Examples:**
- ✅ Good: `4C530000200311207422|SANDISK|CRUZER_BLADE`
- ❌ Bad: `4c53 0000 2003|SanDisk|Cruzer Blade`

---

## 🔒 Security

⚠️ **Keep generated hashes secure!**
- Don't commit hash files to public repositories
- Store in password manager or secure location
- Only share with authorized team members

---

## 🆘 Troubleshooting

**Problem:** PowerShell execution policy error  
**Solution:**
```powershell
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
```

**Problem:** Python not found  
**Solution:** Install Python 3.6+ from https://www.python.org/

**Problem:** Generated hash doesn't match detected USB  
**Solution:** 
1. Run app with `DEBUG_MODE = true`
2. Check logs for "Raw Fingerprint"
3. Use exact format shown in logs

---

**Last Updated:** January 16, 2026



