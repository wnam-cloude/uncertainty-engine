#!/bin/bash
set -e

SDK="/usr/lib/android-sdk"
BUILD_TOOLS_29="$SDK/build-tools/29.0.3"
BUILD_TOOLS_34="$SDK/build-tools/34.0.0"
PLATFORM="$SDK/platforms/android-36"
ANDROID_JAR="$PLATFORM/android.jar"
APP_DIR="/root/uncertainty-engine/android/app"

rm -rf /root/uncertainty-engine/build-manual/out
mkdir -p /root/uncertainty-engine/build-manual/out/classes /root/uncertainty-engine/build-manual/out/dex /root/uncertainty-engine/build-manual/out/apk

# Copy source
cp /root/uncertainty-engine/src/com/example/uncertaintyengine/MainActivity.java \
   "$APP_DIR/src/main/java/com/example/uncertaintyengine/MainActivity.java"

echo "=== Compiling Java ==="
find "$APP_DIR/src/main/java" -name "*.java" > /root/uncertainty-engine/build-manual/sources.txt
javac -d /root/uncertainty-engine/build-manual/out/classes \
  -source 1.8 -target 1.8 \
  -bootclasspath "$ANDROID_JAR" \
  @/root/uncertainty-engine/build-manual/sources.txt

echo "=== Converting to DEX ==="
cd /root/uncertainty-engine/build-manual/out/classes
find . -name "*.class" > ../dex/sources.txt
$BUILD_TOOLS_34/d8 --lib "$ANDROID_JAR" --output ../dex @../dex/sources.txt

echo "=== Creating APK (zip structure only, no aapt) ==="
cd /root/uncertainty-engine/build-manual/out/apk

# Create minimal manifest as text (will be binary by zip, apksigner handles it)
cat > AndroidManifest.xml << 'MANIFEST'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.uncertaintyengine">
    <application android:label="UncertaintyEngine">
        <activity android:name=".MainActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
    </application>
    <uses-permission android:name="android.permission.INTERNET"/>
</manifest>
MANIFEST

# Copy DEX and assets
cp ../dex/classes.dex .
mkdir -p assets/public
cp -r "$APP_DIR/src/main/assets/public/"* assets/public/ 2>/dev/null || true

# Build APK as zip
zip -r app-unsigned.apk AndroidManifest.xml classes.dex assets/ -x "*.DS_Store"

echo "=== Signing APK ==="
if [ ! -f /root/debug.keystore ]; then
  keytool -genkeypair -v -keystore /root/debug.keystore -alias androiddebugkey \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -storepass android -keypass android \
    -dname "CN=Android Debug,O=Android,C=US"
fi

$BUILD_TOOLS_29/apksigner sign \
  --min-sdk-version 24 \
  --ks /root/debug.keystore \
  --ks-pass pass:android \
  --key-pass pass:android \
  --out app-signed.apk \
  app-unsigned.apk

echo "=== Done ==="
ls -la /root/uncertainty-engine/build-manual/out/apk/app-signed.apk
