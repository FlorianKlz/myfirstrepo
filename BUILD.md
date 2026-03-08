# Build Instructions

## Prerequisites

Before building the WhatsApp Sticker Importer app, ensure you have:

1. **Java Development Kit (JDK) 8 or higher**
   ```bash
   java -version
   ```

2. **Android Studio** (Arctic Fox or newer)
   - Download from: https://developer.android.com/studio

3. **Android SDK**
   - API Level 21 (Android 5.0) minimum
   - API Level 34 (Android 14) recommended for compilation

## Build Options

### Option 1: Build with Android Studio (Recommended)

1. **Open the Project**
   ```bash
   git clone https://github.com/FlorianKlz/myfirstrepo.git
   cd myfirstrepo
   ```

2. Launch Android Studio and select "Open an existing project"
3. Navigate to the cloned repository folder and click "OK"
4. Wait for Gradle sync to complete

5. **Build the APK**
   - Go to: Build → Build Bundle(s) / APK(s) → Build APK(s)
   - Or press: `Ctrl+Shift+A` (Windows/Linux) or `Cmd+Shift+A` (Mac)
   - Type "Build APK" and select it

6. **Find the APK**
   - Location: `app/build/outputs/apk/debug/app-debug.apk`
   - A notification will appear with "locate" link

### Option 2: Build with Gradle Command Line

1. **Clone and Navigate**
   ```bash
   git clone https://github.com/FlorianKlz/myfirstrepo.git
   cd myfirstrepo
   ```

2. **Build Debug APK**
   ```bash
   # On Linux/Mac
   ./gradlew assembleDebug

   # On Windows
   gradlew.bat assembleDebug
   ```

3. **Build Release APK**
   ```bash
   # On Linux/Mac
   ./gradlew assembleRelease

   # On Windows
   gradlew.bat assembleRelease
   ```

4. **Find the APK**
   - Debug: `app/build/outputs/apk/debug/app-debug.apk`
   - Release: `app/build/outputs/apk/release/app-release-unsigned.apk`

### Option 3: Install Directly to Device

1. **Connect your Android device**
   - Enable USB debugging on your device
   - Connect via USB cable

2. **Install Debug Build**
   ```bash
   ./gradlew installDebug
   ```

3. **Or use ADB directly**
   ```bash
   ./gradlew assembleDebug
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## Signing the Release APK

For distribution, you need to sign the release APK:

1. **Create a Keystore** (first time only)
   ```bash
   keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
   ```

2. **Create `keystore.properties`** in project root
   ```properties
   storePassword=YOUR_STORE_PASSWORD
   keyPassword=YOUR_KEY_PASSWORD
   keyAlias=my-key-alias
   storeFile=my-release-key.jks
   ```

3. **Update `app/build.gradle`**
   Add before `android` block:
   ```gradle
   def keystorePropertiesFile = rootProject.file("keystore.properties")
   def keystoreProperties = new Properties()
   keystoreProperties.load(new FileInputStream(keystorePropertiesFile))
   ```

   Add in `android` block:
   ```gradle
   signingConfigs {
       release {
           keyAlias keystoreProperties['keyAlias']
           keyPassword keystoreProperties['keyPassword']
           storeFile file(keystoreProperties['storeFile'])
           storePassword keystoreProperties['storePassword']
       }
   }

   buildTypes {
       release {
           signingConfig signingConfigs.release
           minifyEnabled false
           proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
       }
   }
   ```

4. **Build Signed Release**
   ```bash
   ./gradlew assembleRelease
   ```

## Troubleshooting

### "SDK location not found"
**Solution:** Create `local.properties` in project root:
```properties
sdk.dir=/path/to/your/Android/Sdk
```

On Linux/Mac: typically `~/Android/Sdk`
On Windows: typically `C:\Users\YourName\AppData\Local\Android\Sdk`

### "Gradle sync failed"
**Solution:**
1. Check internet connection
2. Invalidate caches: File → Invalidate Caches / Restart
3. Delete `.gradle` folder and re-sync

### "Build failed: Java version"
**Solution:**
1. Install JDK 11 or higher
2. Set JAVA_HOME environment variable
3. In Android Studio: File → Project Structure → SDK Location → JDK location

### "adb: device unauthorized"
**Solution:**
1. Check device screen for "Allow USB debugging" prompt
2. Accept the prompt
3. If not appearing, revoke USB debugging authorizations in Developer Options and reconnect

### Permission Issues on Linux/Mac
```bash
chmod +x gradlew
./gradlew assembleDebug
```

## Build Variants

The app has two build variants:

1. **Debug**
   - Debuggable
   - Not optimized
   - Larger APK size
   - For testing

2. **Release**
   - Optimized
   - Requires signing
   - Smaller APK size
   - For distribution

Switch variants in Android Studio: Build → Select Build Variant

## Gradle Tasks

View all available tasks:
```bash
./gradlew tasks
```

Common tasks:
- `clean` - Delete build directory
- `assembleDebug` - Build debug APK
- `assembleRelease` - Build release APK
- `installDebug` - Build and install debug APK
- `lint` - Run lint checks
- `test` - Run unit tests

## Build Configuration

- **Min SDK:** 21 (Android 5.0)
- **Target SDK:** 34 (Android 14)
- **Compile SDK:** 34
- **Build Tools:** Latest
- **Java Version:** 8
- **Gradle Version:** 8.1.0
- **Android Gradle Plugin:** 8.1.0

## Next Steps After Building

1. **Test on device/emulator**
2. **Check app functionality**
3. **Test with different Android versions**
4. **Verify WhatsApp integration**
5. **Test with various WebP files**

## Distribution

For Google Play Store:
1. Build signed release APK or AAB (Android App Bundle)
2. Test thoroughly
3. Create Play Store listing
4. Upload APK/AAB
5. Submit for review

For direct distribution:
1. Build signed release APK
2. Distribute via website, email, etc.
3. Users must enable "Install from unknown sources"

---

Need help? Create an issue on GitHub!
