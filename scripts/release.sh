#!/usr/bin/env bash
#
# Release script for YIMA (Lunar Cycle)
#
# Usage:
#   ./scripts/release.sh patch     # 1.0.1 -> 1.0.2
#   ./scripts/release.sh minor     # 1.0.1 -> 1.1.0
#   ./scripts/release.sh major     # 1.0.1 -> 2.0.0
#   ./scripts/release.sh 2.1.0     # explicit version
#
# What it does:
#   1. Bumps versionName in gradle/libs.versions.toml
#   2. Syncs version to iOS Config.xcconfig
#   3. Commits the version bump
#   4. Creates a git tag (v2.1.0)
#   5. Pushes commit + tag to trigger the Google Play release workflow
#
# Add -prod suffix to tag for production track:
#   ./scripts/release.sh patch --prod
#

set -euo pipefail

TOML="gradle/libs.versions.toml"
XCCONFIG="iosApp/Configuration/Config.xcconfig"

# Parse arguments
BUMP_TYPE="${1:?Usage: $0 <patch|minor|major|x.y.z> [--prod]}"
PROD_FLAG=""
if [[ "${2:-}" == "--prod" ]]; then
  PROD_FLAG="-prod"
fi

# Read current version
CURRENT_NAME=$(grep '^app-versionName' "$TOML" | sed 's/.*= *"\(.*\)"/\1/')
CURRENT_CODE=$(grep '^app-versionCode' "$TOML" | sed 's/.*= *"\(.*\)"/\1/')

IFS='.' read -r major minor patch <<< "$CURRENT_NAME"

# Calculate new version name
case "$BUMP_TYPE" in
  patch) NEW_NAME="$major.$minor.$((patch + 1))" ;;
  minor) NEW_NAME="$major.$((minor + 1)).0" ;;
  major) NEW_NAME="$((major + 1)).0.0" ;;
  *.*.*)  NEW_NAME="$BUMP_TYPE" ;;
  *) echo "Error: Invalid bump type '$BUMP_TYPE'. Use patch, minor, major, or x.y.z"; exit 1 ;;
esac

# Bump version code
NEW_CODE=$((CURRENT_CODE + 1))

echo "Version bump: $CURRENT_NAME ($CURRENT_CODE) -> $NEW_NAME ($NEW_CODE)"
echo ""

# Ensure clean working tree
if [[ -n $(git status --porcelain) ]]; then
  echo "Error: Working tree is not clean. Commit or stash changes first."
  exit 1
fi

# Ensure we're on main
BRANCH=$(git branch --show-current)
if [[ "$BRANCH" != "main" ]]; then
  echo "Warning: You are on '$BRANCH', not 'main'."
  read -rp "Continue anyway? [y/N] " confirm
  [[ "$confirm" =~ ^[yY]$ ]] || exit 1
fi

# Update version catalog
sed -i '' "s/^app-versionName = \"$CURRENT_NAME\"/app-versionName = \"$NEW_NAME\"/" "$TOML"
sed -i '' "s/^app-versionCode = \"$CURRENT_CODE\"/app-versionCode = \"$NEW_CODE\"/" "$TOML"

# Sync to iOS
sed -i '' "s/CURRENT_PROJECT_VERSION=.*/CURRENT_PROJECT_VERSION=$NEW_CODE/" "$XCCONFIG"
sed -i '' "s/MARKETING_VERSION=.*/MARKETING_VERSION=$NEW_NAME/" "$XCCONFIG"

# Commit and tag
TAG="v${NEW_NAME}${PROD_FLAG}"
git add "$TOML" "$XCCONFIG"
git commit -m "Release $NEW_NAME ($NEW_CODE)"
git tag -a "$TAG" -m "Release $NEW_NAME"

echo ""
echo "Created commit and tag: $TAG"
echo ""
echo "To publish the release, push with:"
echo "  git push origin main $TAG"
echo ""
if [[ -n "$PROD_FLAG" ]]; then
  echo "This will deploy to the PRODUCTION track on Google Play."
else
  echo "This will deploy to the INTERNAL TESTING track on Google Play."
  echo "To deploy to production, use: $0 $BUMP_TYPE --prod"
fi
