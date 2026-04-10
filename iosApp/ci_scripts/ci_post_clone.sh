#!/bin/sh
set -e

# Xcode Cloud post-clone script
# Installs JDK 17 and builds the KMP Compose framework before Xcode compilation.
#
# NOTE: We use `brew install openjdk@17` (not `--cask temurin@17`) because the
# cask path writes to /usr/local/Caskroom, which requires `sudo` — and Xcode
# Cloud runs scripts without a TTY and without passwordless sudo, so any sudo
# invocation fails with:
#     sudo: a terminal is required to read the password
# The regular `openjdk@17` formula installs into Homebrew's own prefix
# (writable by the CI user) with no sudo required.

echo ">>> Installing OpenJDK 17 via Homebrew..."
brew install openjdk@17

# Derive JAVA_HOME from brew --prefix instead of /usr/libexec/java_home,
# which would only work after a `sudo ln` into /Library/Java/JavaVirtualMachines.
JAVA_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home"
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"
echo "JAVA_HOME=$JAVA_HOME"
java -version

echo ">>> Building Compose KMP framework..."
cd "$CI_WORKSPACE"
./gradlew :composeApp:embedAndSignAppleFrameworkForXcode \
    -PXCODE_CONFIGURATION="$CONFIGURATION" \
    -PXCODE_PLATFORM_NAME="$PLATFORM_NAME" \
    -PXCODE_ARCHS="$ARCHS" \
    -PXCODE_SDK="$SDK_NAME"

echo ">>> KMP framework built successfully"
