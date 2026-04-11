#!/bin/sh
set -e

# Xcode Cloud post-clone script.
#
# We ONLY install the JDK here. The actual KMP framework build happens
# inside Xcode's "Compile Kotlin Framework" Run Script build phase —
# that phase has the full Xcode build environment ($ARCHS, $CONFIGURATION,
# $PLATFORM_NAME, $SDK_NAME) which Compose Multiplatform's
# syncComposeResourcesForIos task requires. If we tried to run
# embedAndSignAppleFrameworkForXcode here, we'd fail with:
#     Could not infer iOS target architectures
# because those env vars aren't set until xcodebuild starts.
#
# Note: We use `brew install openjdk@17` (not `--cask temurin@17`) because
# the cask path writes to /usr/local/Caskroom which needs sudo, and Xcode
# Cloud runs scripts without a TTY or passwordless sudo. The regular
# `openjdk@17` formula installs into Homebrew's own writable prefix.

echo ">>> Installing OpenJDK 17 via Homebrew..."
brew install openjdk@17

JAVA_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home"
echo "JAVA_HOME resolved to: $JAVA_HOME"
"$JAVA_HOME/bin/java" -version

echo ">>> JDK ready. The Xcode 'Compile Kotlin Framework' Run Script phase"
echo ">>> will handle the KMP build using this JDK."
