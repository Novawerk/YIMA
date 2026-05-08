---
name: release
description: "Cut a release from main. Triggers the Release workflow which bumps version, tags, creates a GitHub Release, builds the signed AAB, and uploads to the Play Store internal track. Use this skill whenever the user says /release, 'do a release', 'cut a release', 'publish a new version', 'ship it', or anything related to releasing or publishing the app."
user_invocable: true
---

# Release Flow

One workflow on `main` does everything. No release branch, no PR, no backport.

## Steps

### 1. Preflight

Confirm the user is on `main` with a clean working tree, and that `main` is pushed to `origin`. If not, warn before continuing.

### 2. Show what will ship

```bash
PREV_TAG=$(git tag --sort=-v:refname | grep -E '^v[0-9]+\.[0-9]+\.[0-9]+$' | head -1)
git log "$PREV_TAG"..HEAD --oneline --no-merges
```

If there are no new commits, tell the user there's nothing to release and stop.

### 3. Pick the bump

Use `AskUserQuestion` to pick **patch / minor / major**. Suggest a default based on the commits (fixes/CI → patch, new features → minor). Show the computed next version for each option.

### 4. Trigger the workflow

```bash
gh workflow run release.yml --ref main -f bump=<patch|minor|major>
```

### 5. Watch it

```bash
sleep 3
RUN_ID=$(gh run list --workflow=release.yml --limit 1 --json databaseId -q '.[0].databaseId')
gh run watch "$RUN_ID" --exit-status
```

The workflow will:
1. Bump `versionName` and `versionCode` in `gradle/libs.versions.toml` and `iosApp/Configuration/Config.xcconfig`
2. Commit `Release <version>` to main and push
3. Create tag `v<version>` and push
4. Create GitHub Release with auto-generated notes
5. Build signed AAB
6. Upload to Play Store internal track (status: draft)

### 6. Done

Tell the user:
- Tag URL
- Promote from internal to production in the Play Console when ready.
