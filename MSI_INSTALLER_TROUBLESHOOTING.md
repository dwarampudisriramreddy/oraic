# MSI Installer Troubleshooting Guide

## Issues Fixed

### 1. ✅ SQL DriverManager Error

**Problem:** `java.sql.DriverManager` error when opening the application

**Root Cause:** SQLite JDBC driver was not explicitly loaded or included in the distribution

**Solution Applied:**
- Added explicit SQLite JDBC driver dependency: `org.xerial:sqlite-jdbc:3.44.1.0`
- Added explicit driver loading in `DatabaseDriverFactory.jvm.kt`
- Ensured all modules are included in distribution

**Files Modified:**
- `composeApp/build.gradle.kts` - Added SQLite JDBC dependency
- `composeApp/src/jvmMain/kotlin/com/ram/orai/oraic/DatabaseDriverFactory.jvm.kt` - Added driver loading

### 2. ⚠️ Desktop Shortcut Not Created

**Problem:** No desktop shortcut appears after MSI installation

**Possible Causes:**
1. Windows installer settings (user chose not to create shortcut)
2. jpackage configuration issue
3. Windows permissions issue

**Solutions:**

#### Solution A: Manual Shortcut Creation

If the shortcut is not created automatically, create it manually:

1. Navigate to: `C:\Program Files\ORAIC\`
2. Find: `ORAIC.exe` (or `ORAIC.bat`)
3. Right-click → Send to → Desktop (create shortcut)
4. Or drag the executable to Desktop

#### Solution B: Rebuild with Explicit Configuration

Rebuild the MSI with the updated configuration:

```bash
# Clean previous build
.\gradlew.bat clean

# Rebuild MSI
.\gradlew.bat packageMsi
```

The MSI should now include:
- SQLite JDBC driver
- All required dependencies
- Proper module configuration

#### Solution C: Check Installation Options

During MSI installation:
1. Make sure "Create desktop shortcut" option is checked (if available)
2. Some Windows versions may not show this option explicitly
3. Check "Additional shortcuts" or "Desktop icons" section

#### Solution D: Verify Installation Location

The application is installed at:
```
C:\Program Files\ORAIC\
```

You can:
1. Navigate to this folder
2. Right-click `ORAIC.exe`
3. Create shortcut manually
4. Move shortcut to Desktop

---

## Rebuilding the MSI

After applying fixes, rebuild the MSI:

```bash
# Clean build
.\gradlew.bat clean

# Build MSI installer
.\gradlew.bat packageMsi

# Output location:
# composeApp/build/compose/binaries/main/msi/ORAIC-1.0.0.msi
```

---

## Verification Steps

After rebuilding and installing:

### 1. Check SQLite Driver

1. Install the new MSI
2. Open the application
3. If it opens without errors → ✅ SQLite driver is working
4. If you still get DriverManager error → Check error details below

### 2. Check Desktop Shortcut

1. After installation, check Desktop
2. Look for "ORAIC" shortcut
3. If not present:
   - Check Start Menu → ORAIC folder
   - Navigate to `C:\Program Files\ORAIC\`
   - Create shortcut manually

### 3. Test Application

1. Launch application (from shortcut or directly)
2. Application should open without errors
3. Database operations should work
4. Patient data should persist

---

## Common Errors and Solutions

### Error: "java.sql.DriverManager: No suitable driver found"

**Cause:** SQLite JDBC driver not in classpath

**Solution:**
- ✅ Already fixed in code
- Rebuild MSI with updated dependencies
- Ensure `org.xerial:sqlite-jdbc` is included

### Error: "ClassNotFoundException: org.sqlite.JDBC"

**Cause:** SQLite JDBC driver class not found

**Solution:**
- ✅ Already fixed in code
- The driver is now explicitly loaded
- Rebuild MSI

### Error: "Database file not found" or "Permission denied"

**Cause:** Database directory creation failed

**Solution:**
- Database is created at: `%USERPROFILE%\.oraic\oraic.db`
- Check user permissions
- Ensure user has write access to home directory

### Desktop Shortcut Missing

**Cause:** jpackage may not create desktop shortcuts by default in some Windows configurations

**Solution:**
- Create shortcut manually (see Solution A above)
- Or use Start Menu shortcut: Start → ORAIC → ORAIC

---

## Manual Shortcut Creation (Step-by-Step)

If automatic shortcut creation doesn't work:

1. **Open File Explorer**
   - Press `Win + E`

2. **Navigate to Installation Directory**
   - Go to: `C:\Program Files\ORAIC\`
   - Or search for "ORAIC" in Start Menu

3. **Find the Executable**
   - Look for: `ORAIC.exe` or `ORAIC.bat`
   - It should be in the main folder

4. **Create Shortcut**
   - Right-click on `ORAIC.exe`
   - Select "Create shortcut"
   - Windows will ask if you want it on Desktop → Click "Yes"
   - Or drag the file to Desktop while holding `Alt` key

5. **Verify Shortcut**
   - Double-click the shortcut
   - Application should launch

---

## Alternative: Start Menu Shortcut

If desktop shortcut doesn't work, use Start Menu:

1. Press `Win` key
2. Type "ORAIC"
3. Click on "ORAIC" application
4. Or navigate to: Start Menu → ORAIC → ORAIC

---

## Technical Details

### SQLite Driver Loading

The code now explicitly loads the SQLite driver:

```kotlin
try {
    Class.forName("org.sqlite.JDBC")
} catch (e: ClassNotFoundException) {
    System.err.println("Warning: SQLite JDBC driver not found.")
    e.printStackTrace()
}
```

This ensures the driver is available before creating database connections.

### Dependencies Included

The MSI now includes:
- `org.xerial:sqlite-jdbc:3.44.1.0` - SQLite JDBC driver
- All SQLDelight dependencies
- All Compose Desktop dependencies
- All runtime modules

### Module Configuration

```kotlin
modules("java.instrument", "java.sql", "jdk.unsupported")
```

This ensures Java SQL modules are available.

---

## Still Having Issues?

If problems persist after rebuilding:

1. **Check Logs**
   - Run application from command line to see errors
   - Navigate to installation directory
   - Run: `ORAIC.exe` (or `java -jar ORAIC.jar`)
   - Check console output for errors

2. **Verify Installation**
   - Check if all files are present in `C:\Program Files\ORAIC\`
   - Look for `lib` folder with JAR files
   - Verify SQLite JDBC JAR is present

3. **Test Database Connection**
   - Check if database file is created: `%USERPROFILE%\.oraic\oraic.db`
   - Verify file permissions
   - Try creating a test patient

4. **Reinstall**
   - Uninstall current version
   - Delete `C:\Program Files\ORAIC\` folder (if exists)
   - Install new MSI
   - Test again

---

## Build Commands Reference

```bash
# Clean build
.\gradlew.bat clean

# Build MSI only
.\gradlew.bat packageMsi

# Build all distributions
.\gradlew.bat package

# Check build output
# MSI location: composeApp/build/compose/binaries/main/msi/
```

---

**Last Updated:** January 2025  
**Status:** ✅ SQLite Driver Fixed | ⚠️ Desktop Shortcut (Manual Creation Available)



