# Data Integrity Audit

Goal: Smart Task Manager shows **real persisted data only**. Where no real data
exists, it shows a professional empty state or clearly disables the feature —
never fabricated data presented as real.

## Mock-data inventory (found) and resolution

| Source | File · class · method · variable | Why it was fake | Replacement |
|---|---|---|---|
| Demo dataset factory | `util.MockDataGenerator` (all `sample*()`) | Hardcoded demo objects | **Deleted** |
| Placeholder service | `service.InsightsService` (all getters) | Returned `MockDataGenerator.*` collections | **Deleted** |
| Fake chart component | `view.components.ChartPlaceholder.draw(...)` (`Random r`) | Drew a random curve resembling analytics | **Deleted** |
| Fake timeline | `ProductivityTimelineView.build()` → `insightsService().getTimeline()` | Invented events + fake chart | `AnalyticsService.recentTimeline()` from **real completed tasks**; empty state if none |
| Fake habits | `HabitTrackingView.build()` → `getHabits()` | Fake streaks/consistency | **Empty/disabled state** (real in Phase 4) |
| Fake distractions | `DistractionManagementView.build()` → `getDistractions()` | Fake usage minutes | **Empty/disabled state** (real in Phase 7) |
| Fake leaderboard | `LeaderboardView` → `getLeaderboard()`, `previewRow(...)` | Fake competitors in a single-user app | League **disabled** with a clear message; only the real "Your standing" remains |
| Fake dashboard trend | `DashboardView.build()` right column `ChartPlaceholder("Productivity Trend"…)` | Random fake curve | Honest "trend appears once you have history" card |
| Fake active persona | `PersonaAdaptationView.personaCard("Anti-Procrastination", …, true)` (`active` flag) | Hardcoded "Active" persona | Neutralized to "Available" (real selection in Phase 2) |
| Dead mock getters | `InsightsService.getInsightKpis/getFocusSessions/getCoachThread/getSchedule/getPriorityDistribution/getStatusDistribution` | Unused after Phase 1.2 | Removed with the class |

## Real data sources now in use (DAO → Service → UI)
- **Dashboard KPIs / Insights / Leaderboard standing / Timeline** → `AnalyticsService` over `TaskService` → `JdbcTaskDao` → MySQL `tasks` (user-scoped via `Session`).
- **AI Coach** → `AICoachService` over real tasks/goals + recommendation.
- **Smart Schedule** → `SmartScheduleService` over real active tasks.
- **Temporal** → `TemporalRecommendationService` / `TemporalPlanningService` over `time_blocks` / `temporal_profiles`.
- **Goals** → `GoalService` → `JdbcGoalDao`. **Auth/User** → `AuthService`/`UserService` → `JdbcUserDao`.

## Per-screen state after audit
| Screen | State |
|---|---|
| Welcome / Login / Signup / Forgot / Settings | Real (auth) / static UI |
| Dashboard | **Real** KPIs + honest empty/trend states |
| Tasks (list/add/edit/details) | **Real** (persisted) |
| Insights | **Real** analytics + empty state |
| AI Coach | **Real** task analysis + no-data prompt |
| Smart Schedule | **Real** tasks + empty state |
| Temporal Intelligence | **Real** |
| Leaderboard | **Real** "Your standing"; multiplayer **disabled** |
| Productivity Timeline | **Real** completed tasks + empty state |
| Habits | **Disabled** empty state (Phase 4) |
| Distraction Management | **Disabled** empty state (Phase 7) |
| Persona | Mode picker, no fake active; **Preview** (Phase 2) |

## Files modified
- **Deleted:** `util/MockDataGenerator.java`, `service/InsightsService.java`, `view/components/ChartPlaceholder.java`.
- **Service:** `AnalyticsService` (+`recentTimeline()`), `ServiceLocator` (removed `InsightsService` wiring + accessor).
- **Views:** `ProductivityTimelineView`, `HabitTrackingView`, `DistractionManagementView`, `LeaderboardView`, `DashboardView`, `PersonaAdaptationView`.

## Tests added
`AnalyticsServiceTest` — `recentTimeline()` empty when nothing completed; lists real completed tasks with **no fabricated timestamp** (`getWhen() == null`). Existing analytics/coach/schedule/temporal/auth/dashboard tests remain green.

## Remaining limitations (honest)
- Habits (Phase 4), Distractions (Phase 7), multiplayer Leaderboard, and Persona persistence (Phase 2) are intentionally empty/disabled until their phases — no sample data is shown.
- Completion timestamps and focus-session history aren't persisted yet (Phase 5), so the timeline shows "—" for time and weekly trends remain a labeled note.
- Unused domain models (`HabitEntry`, `DistractionEntry`, `FocusSession`, `ChatMessage`, `LeaderboardEntry`) are retained for their upcoming phases — they are not rendered anywhere, so no fake data is shown.
- Verified via `javac` (backend + tests, EXIT 0); JavaFX runs locally via `mvn javafx:run`.
