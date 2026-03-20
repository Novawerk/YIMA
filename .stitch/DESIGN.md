# Design System: 姨妈来了 (Yimalaile)
**Project ID:** 13438847393817305953

---

## 1. Visual Theme & Atmosphere

**Vibe:** Warm, therapeutic, hand-crafted comfort. The app feels like a gentle hug — softly organic shapes, warm rose and cream tones, and hand-drawn border aesthetics that feel personal rather than clinical. The aesthetic philosophy is "非盈利温暖" (non-profit warmth): welcoming, intimate, and trustworthy rather than corporate or sterile.

**Atmosphere keywords:** cozy, warm, organic, softly rounded, earthy rose, hand-drawn, gentle, feminine

---

## 2. Color Palette & Roles

| Descriptive Name        | Hex       | Role                                              |
|:------------------------|:----------|:--------------------------------------------------|
| Deep Rose               | `#a66d6d` | Primary action color, icon tints, heading accents |
| Dark Coffee             | `#5d4037` | Body text, icon color on settings/list screens    |
| Warm Peach              | `#f7cdc3` | Accent fills, soft button backgrounds             |
| Blush Pink              | `#f4dcd6` | Card backgrounds, section fills                   |
| Soft Cream              | `#fff9f8` | Page background (light mode)                      |
| Warm Cream Beige        | `#fdf8f5` | Alternate page background (settings/detail pages) |
| Dark Chocolate          | `#2d2420` | Page background (dark mode)                       |
| Midnight Espresso       | `#221610` | Alternate dark mode background                    |

---

## 3. Typography Rules

- **Primary Font:** Public Sans (Google Fonts) — clean, modern sans-serif for body and UI
- **Decorative Font:** Ma Shan Zheng / Zhi Mang Xing (Google Fonts) — used for hand-written aesthetic on settings / about screens
- **Icon Font:** Material Symbols Outlined (variable weight 100–700, fill 0–1)
- **Hero Title:** Extra-large, tight tracking (e.g., `text-[88px] font-bold leading-none tracking-tighter`)
- **Section Headers:** `text-xl font-bold` or `text-3xl font-bold`
- **Body / Labels:** `text-base` or `text-lg font-medium`, muted with opacity variants (`/60`, `/80`)
- **Status Pills:** `text-sm font-semibold` inside pill-shaped badges

---

## 4. Component Stylings

### Buttons
- **Primary CTA:** Full-width, pill-shaped (`rounded-full`), Deep Rose (`#B37D71`), white text, `font-bold text-xl`, `h-16`, flex row with Material Symbol icon + label. Active state: `scale-95` transform.
- **Ghost/Dashed:** Dashed oval border (`border-2 dashed`, `border-radius: 50% / 50%`), Deep Coffee text, hover soft fill
- **Nav Buttons:** Icon-only, `size-10 rounded-full bg-accent/30 text-primary`

### Cards / Containers
- **Organic Cards:** Custom blob border-radius shapes (asymmetric, e.g., `border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%`), soft fill (`bg-primary/5`), thin border (`border border-primary/10`)
- **Blob Shape 1:** `border-radius: 60% 40% 70% 30% / 40% 50% 60% 70%` — used for language/list items
- **Blob Shape 2:** `border-radius: 40% 60% 30% 70% / 60% 30% 70% 40%` — used for active nav icon backgrounds
- **Standard rounded containers:** `rounded-2xl` or `rounded-xl` for calendar/stats cards

### Hand-drawn Border
- CSS: `border-radius: 255px 15px 225px 15px/15px 225px 15px 255px; border: 2px solid #5d4037;`
- Used for decorative framing, section dividers

### Status Pill / Badge
- `bg-accent/20 px-6 py-2 rounded-full border border-accent/30 text-primary/70 text-sm font-semibold`
- Used to show predicted period end date, cycle day count, etc.

### Bottom Navigation Bar
- **Not used** — all screens use full-height content without a persistent bottom nav bar.
- Navigation between screens is handled by back arrows (`arrow_back` in rounded-full peach bg) and top-bar icon buttons.

---

## 5. Layout Principles

- **Max width container:** `max-w-md mx-auto` — mobile-first, centered column layout
- **Page padding:** `px-6` horizontal, `pt-12 pb-6` top/bottom for headers
- **Generous whitespace:** Sections separated by `gap-8`, liberal use of `py-4` / `mb-8`
- **Illustration zone:** Centered circular image area with `blur-3xl` ambient glow background
- **Bottom sheet / dialogs:** Slide up overlay style, rounded top corners `rounded-t-3xl`
- **Stat cards:** 2-column grid with equal-width cards at `rounded-2xl`

---

## 6. Complete Screens Inventory

| Folder | Title | Description |
|:-------|:------|:------------|
| `_1/` | 首页 (状态：该来了) | Home — period overdue, "姨妈还没来吗？", shows days delayed |
| `_2/` | 首页 (状态：快了) | Home — period due in ~3 days, "姨妈快来了吗？" |
| `_3/` | 首页 (状态：还早) | Home — period still ~20 days away, "离姨妈还远吗？" |
| `_4/` | 记录对话框 (补记日期) | Bottom sheet — log period start, date picker, flow intensity |
| `_5/` | 首页 (经期中 - 抱抱) | Home — active period state, "照顾好自己哦", shows days remaining |
| `_6/` | 首页 (状态：快了) — 副本 | Duplicate variant of "快了" state |
| `cycle_statistics_and_insights/` | 统计 / 洞察 | Cycle length chart, average duration, trend visualization |
| `disclaimer_and_privacy_notice/` | 免责声明 / 隐私说明 | First-launch disclaimer + privacy notice |
| `first_launch_empty_state_welcome/` | 首次使用 / 欢迎空状态 | Onboarding empty state — no period data recorded yet |

> **Note:** Legacy named assets (`home-active-hugging-warm.html`, `history-list.html`, etc.) and their PNGs have been removed from the repo. All current designs live in the numbered/named folders above.
