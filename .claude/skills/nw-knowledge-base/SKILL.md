---
name: nw-knowledge-base
description: Fetch canonical Novawerk organisational facts (identity, brand voice, three principles, portfolio projects, operations, community) from the novawerk/nw-knowledge-base repo. Use whenever the user asks about Novawerk's tagline, mission, vision, brand mark, principles, the four-step process, MAU, open-by-default, ways to join, community stats/channels/rhythm, or the current portfolio projects — and you do not already have the knowledge-base files in this repo.
---

# Novawerk knowledge base

Canonical facts about the **Novawerk** organisation live in a separate
repo: [`novawerk/nw-knowledge-base`](https://github.com/novawerk/nw-knowledge-base).
This skill makes those facts available to you in any other Novawerk repo.

## When to use this skill

Trigger this skill when the user asks anything that the knowledge base is
designed to answer, such as:

- *What is Novawerk's tagline / mission / vision / brand voice?*
- *What are the three principles?*
- *What is the Nova Star? What are the brand colours?*
- *How do projects move from idea to shipped? What's a MAU?*
- *What does "open by default" mean?*
- *Which projects are in the current portfolio?*
- *What are the ways to join (Initiator / Builder / Supporter)?*
- *Community stats, channels, rhythm.*

Do **not** trigger this skill for:

- Live blog post or portfolio detail bodies (those live in Payload, not the KB).
- Per-team-member information (out of KB scope).
- Anything specific to the website's visual design or current copy — read
  `nw-web-portal` directly for that.

## How to use it

### 1. Sync the local cache

The knowledge base is cached at `~/.cache/nw-knowledge-base`. Run this before
reading — it clones on first use and fast-forwards on subsequent uses:

```bash
KB_DIR="${HOME}/.cache/nw-knowledge-base"
if [ -d "${KB_DIR}/.git" ]; then
  git -C "${KB_DIR}" fetch --quiet origin main \
    && git -C "${KB_DIR}" reset --quiet --hard origin/main
else
  mkdir -p "$(dirname "${KB_DIR}")"
  git clone --quiet --depth 1 https://github.com/novawerk/nw-knowledge-base.git "${KB_DIR}"
fi
```

If the clone fails (offline, auth required), tell the user and stop —
do not invent facts.

### 2. Read the index first

Always start with `${KB_DIR}/README.md`. It lists every fact file under
five categories: `identity/`, `principles/`, `projects-overview.md`,
`operations/`, `community/`. Pick the file(s) that match the question.

### 3. Read only the files you need

Each fact file is short and self-contained, with YAML frontmatter
(`title`, `category`, `status`, `source`, `updated`) followed by the
canonical text. Read the specific file rather than grepping the whole KB.

### 4. Cite provenance

When you quote or paraphrase a fact, cite the KB file (e.g.
`nw-knowledge-base/identity/brand-voice.md`). If the fact is marked
`status: evolving` (community stats, project list), note the `updated:`
date so the user knows how fresh it is.

## What the KB does and does not guarantee

- **Stable** facts (identity, principles, operations methodology) should
  rarely change. Treat them as authoritative.
- **Evolving** facts (community stats, portfolio list) refresh against
  `nw-web-portal` on a manual cadence. The KB's `updated:` field is the
  snapshot date — don't quote numbers older than a quarter without
  flagging the staleness.
- The KB is **never** the source for live blog content, admin-authored
  portfolio bodies, or anything dynamically rendered from Payload.

## Installing this skill in a consumer repo

Copy this directory into the consumer repo's `.claude/skills/`:

```bash
mkdir -p .claude/skills
curl -sSL https://raw.githubusercontent.com/novawerk/nw-knowledge-base/main/skills/nw-knowledge-base/SKILL.md \
  -o .claude/skills/nw-knowledge-base/SKILL.md
```

(Or `git clone` the repo and copy `skills/nw-knowledge-base/` across.)
