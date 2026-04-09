---
name: release
description: "Open a release PR from main to the release branch with semantic version labeling. CI handles the rest: version bumping, tagging, GitHub Release, and Play Store publishing. Use this skill whenever the user says /release, 'do a release', 'cut a release', 'publish a new version', 'ship it', or anything related to releasing or publishing the app."
user_invocable: true
---

# Release Flow

This skill opens a PR from `main` to the `release` branch. When the PR is merged, CI automatically:
- Bumps version in `composeApp/build.gradle.kts` and `iosApp/Configuration/Config.xcconfig`
- Creates a git tag and GitHub Release with categorized notes
- Builds and publishes the AAB to Google Play production
- Opens a backport PR to sync the version bump back to main

## Steps

### Step 1: Preflight checks

Verify the working tree is clean and on `main`. If not, warn the user.

### Step 2: Check for existing release PR

```bash
gh pr list --base release --state open
```

If an open PR to `release` already exists, show it and ask the user if they want to proceed or use the existing one.

### Step 3: Find latest tag and show changelog

```bash
PREV_TAG=$(git tag --sort=-v:refname | grep -E '^v[0-9]+\.[0-9]+\.[0-9]+$' | head -1)
git log "$PREV_TAG"..HEAD --oneline
```

Categorize commits:
- **Features**: add, feat, implement, introduce
- **Fixes**: fix, patch, resolve, correct
- **CI/Testing**: ci, test, screenshot, workflow, build
- **Docs**: doc, readme, comment
- **Other**: everything else

Show the categorized summary. If no commits since last tag, tell the user there's nothing to release and stop.

### Step 4: Ensure release branch exists

```bash
git ls-remote --heads origin release
```

If it doesn't exist, create it:
```bash
git push origin main:release
```

### Step 5: Open the PR

Use `gh pr create` from `main` to `release`. Include the categorized changelog in the PR body.

```bash
gh pr create --base release --head main --title "Release: <summary>" --body "<changelog>"
```

### Step 6: Apply version label

Ask the user which version bump to apply via `AskUserQuestion`:
- **Patch** (x.y.Z) — fixes, CI, docs
- **Minor** (x.Y.0) — new features
- **Major** (X.0.0) — breaking changes

Suggest the appropriate default based on the commit categories. Show the computed next version for each option.

Apply the label:
```bash
gh pr edit <PR_NUMBER> --add-label "version:<bump>"
```

### Step 7: Done

Tell the user:
- PR link
- Which version label was applied and what the next version will be
- "Merge the PR and CI will handle version bumping, tagging, GitHub Release, and Play Store publishing."
- Remind them to check the Actions tab after merging
