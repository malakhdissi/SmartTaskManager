# Phase 1.2 — Platform UX Fix & Real-Data Consistency

Goal: make the platform feel like a real, premium SaaS product and **never present fake data as real**, without starting new feature phases.

## Problems found → root causes → fixes

| # | Problem | Root cause | Fix |
|---|---------|-----------|-----|
| 1 | Sidebar not scrollable | Sidebar placed directly in `BorderPane.left`; tall list clipped | `MainLayout` wraps the sidebar in a `ScrollPane` |
| 2–3 | Pages not scrollable / content clipped | Content scroll policies not explicit | `MainLayout` content `ScrollPane` with `fitToWidth`, vbar `AS_NEEDED`, hbar `NEVER` |
| 4 | Add-Task description unreadable | `TextArea`'s inner `.content` region was unstyled → default light bg on dark theme | CSS: `.text-area .content` + `.text-area .text` use surface bg / primary text |
| 5 | Deep Work not flexible | Timer auto-started a fixed 90-min block; no resume/restart | Full rewrite: 25/50/90/custom presets, start/pause/resume/restart/end state machine, interruption count, end summary |
| 6 | AI Coach random/static | View used a templated/mock reply | New `AICoachService` (rule-based, grounded in real tasks/goals; reasoning per answer; honest no-data prompt) |
| 7 | Smart Schedule showed non-existent tasks | View rendered `MockDataGenerator` schedule | New `SmartScheduleService.generate()` from real active tasks; empty state when none |
| 8–9 | Insights showed fake KPIs | View read mock KPIs/charts | New `AnalyticsService` (real counts); `InsightsView` real KPIs + empty state; weekly-trend honestly marked "needs history" |
| 10 | Leaderboard static/example | Mock multi-user rows shown as real | Redesigned: real current-user "Your standing" card + clearly-labeled **Preview** league (EA-Sports-style) |
| 12 | Mock screens looked real | No labeling | New `PreviewBadge`; added to Persona / Habits / Distractions / Timeline / Leaderboard league |

## Files modified / added
- **New services:** `AnalyticsService`, `AICoachService`, `SmartScheduleService` (+ `ServiceLocator` wiring).
- **New components:** `PreviewBadge`.
- **Rewritten views:** `DeepWorkSessionView`, `AiCoachView`, `SmartScheduleGeneratorView`, `InsightsView`, `LeaderboardView`, `MainLayout`.
- **Edited views:** `AddTaskView` (inline validation), Persona/Habits/Distractions/Timeline (preview badge).
- **CSS:** textarea contrast, platform-wide scrollbars, sidebar scroll, card hover, preview badge, deep-work presets/summary, leaderboard.

## UX improvements
- Sidebar and every in-app page scroll independently; nothing clipped on a laptop or maximized window.
- Readable dark-mode form fields; Add-Task inline validation (title required, duration positive).
- Deep Work is a real focus tool (choose duration, pause/resume/restart, end → summary).
- Premium touches: card hover, slim scrollbars, gradient leaderboard cards, division/level/movement.

## Data-honesty improvements
- **Real data:** Dashboard, Insights, AI Coach, Smart Schedule, Temporal, Leaderboard "Your standing".
- **Honest empty states:** Insights, Smart Schedule, Coach (all with no-data messaging).
- **Clearly labeled previews:** Persona, Habits, Distraction, Timeline, Leaderboard league (still sample-backed).
- No random fake tasks, no random AI answers, no fabricated stats shown as real.

## Tests added
`AICoachServiceTest` (no-data prompt; grounded advice), `SmartScheduleServiceTest` (empty → empty; real ordering; completed excluded), `AnalyticsServiceTest` (empty hasData=false; real counts/overdue/rate/deep-work).

## Remaining limitations
- Persona/Habits/Distraction/Timeline still use sample data (labeled Preview) — they become real in their dedicated phases (2, 4, 7).
- Weekly trends need completion-timestamp history (not tracked yet) → shown as a labeled note, not a fake chart.
- Multi-user leaderboard is single-user only → comparative table is a Preview until multiplayer exists.
- Deep Work sessions are not yet persisted (Phase 5 adds `DeepWorkSession` history); focus/deep-work KPI uses completed deep-work task minutes as an honest proxy.
- Verified via `javac` (backend + tests, EXIT 0); JavaFX views run locally via `mvn javafx:run`.
