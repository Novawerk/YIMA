#!/bin/sh
set -e

# Xcode Cloud post-clone script
# Installs JDK and builds the KMP Compose framework before Xcode compilation

echo ">>> Installing JDK 17 via Homebrew..."
brew install --cask temurin@17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
echo "JAVA_HOME=$JAVA_HOME"

echo ">>> Building Compose KMP framework..."
cd "$CI_WORKSPACE"
./gradlew :composeApp:embedAndSignAppleFrameworkForXcode \
    -PXCODE_CONFIGURATION="$CONFIGURATION" \
    -PXCODE_PLATFORM_NAME="$PLATFORM_NAME" \
    -PXCODE_ARCHS="$ARCHS" \
    -PXCODE_SDK="$SDK_NAME"

echo ">>> KMP framework built successfully"
