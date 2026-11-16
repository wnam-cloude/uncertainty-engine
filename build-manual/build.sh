#!/bin/bash
set -e

# Paths
SDK="/usr/lib/android-sdk"
BUILD_TOOLS_29="$SDK/build-tools/29.0.3"
BUILD_TOOLS_34="$SDK/build-tools/34.0.0"
PLATFORM="$SDK/platforms/android-35"
ANDROID_JAR="$PLATFORM/android.jar"
APP_DIR="/root/uncertainty-engine/android/app"

# Clean
rm -rf out && mkdir -p out/classes out/res out/dex out/apk

echo "=== Compiling Java ==="
$BUILD_TOOLS_34/d8 --version

# Find all Java sources
find "$APP_DIR/src/main/java" -name "*.java" > sources.txt

# Also need capacitor-android sources
CAPACITOR_DIR="/root/uncertainty-engine/node_modules/@capacitor/android/capacitor"
find "$CAPACITOR_DIR/src/main/java" -name "*.java" >> sources.txt

# Find capacitor-cordova-android-plugins sources
CORDOVA_DIR="/root/uncertainty-engine/android/capacitor-cordova-android-plugins"
find "$CORDOVA_DIR/src/main/java" -name "*.java" >> sources.txt

echo "Java files found:"
wc -l sources.txt

# Compile with javac
javac -d out/classes \
  -source 17 -target 17 \
  -bootclasspath "$ANDROID_JAR" \
  -cp "$(find ~/.gradle/caches -name "*.jar" | tr '\n' ':')" \
  @sources.txt 2>&1 | head -50

echo "=== Converting to DEX ==="
$BUILD_TOOLS_34/d8 --output out/dex out/classes

echo "=== Compiling Resources ==="
$BUILD_TOOLS_29/aapt2 compile \
  -o out/res \
  --dir "$APP_DIR/src/main/res" 2>&1

echo "=== Linking Resources ==="
$BUILD_TOOLS_29/aapt2 link \
  -o out/apk/resources.apk \
  -I "$ANDROID_JAR" \
  --manifest "$APP_DIR/src/main/AndroidManifest.xml" \
  -R out/res/*.flat \
  --auto-add-overlay \
  --java out/classes 2>&1

echo "=== Creating APK ==="
cd out/apk
# Add resources
cp resources.apk app.apk

# Add DEX files
unzip -o ../dex/*.dex -d .
jar uf app.apk classes.dex

# Add assets
mkdir -p assets/public
cp -r "$APP_DIR/src/main/assets/public/"* assets/public/
jar uf app.apk assets/public/index.html

# Sign
$BUILD_TOOLS_29/apksigner sign --ks /root/debug.keystore --ks-pass pass:android --key-pass pass:android --out app-signed.apk app.apk

echo "=== Done: out/apk/app-signed.apk ==="
ls -la app-signed.apk
