# Temporal Intelligence (Phase 1)

Match the right task to the right **energy** and **time** — and never split work that must stay whole.

## Concepts
- **`TaskTemporalType`** — `FIXED_TIME` (meeting/exam), `FLEXIBLE` (email/reading), `DEEP_WORK` (needs a protected window), `INDIVISIBLE` (one uninterrupted sitting, e.g. doctoral research). Tasks without an explicit value derive one from their `TaskType` (backward compatible).
- **`EnergyLevel`** — LOW / MEDIUM / HIGH (with an orderable weight).
- **`Chronotype`** — EARLY_BIRD / INTERMEDIATE / NIGHT_OWL.
- **`DayPeriod`** — MORNING (<12:00) / AFTERNOON (<18:00) / EVENING.
- **`TimeBlock`** — a daily window with an energy level and available minutes.
- **`TemporalProfile`** — one per user: morning/afternoon/evening energy, chronotype, best deep-work period, fatigue period. `defaultFor(userId)` provides a sensible starting profile.

## Recommendation rules (`BasicTemporalRecommendationStrategy`)
Score = priority + deadline urgency + goal contribution + **energy match** + **temporal fit**:
- **FIXED_TIME** due today → strongly boosted (can't move).
- **INDIVISIBLE** → only recommended when the current block has enough uninterrupted minutes; otherwise heavily penalized ("wait for a longer block"). **Never split into a short gap.**
- **DEEP_WORK / INDIVISIBLE** → need HIGH energy; boosted in the profile's best deep-work period, suppressed when energy is LOW.
- **FLEXIBLE** → good use of low-energy time.

Each recommendation carries a plain-language **reason** and a confidence (0–1).

## Layers (MVC / DAO / Service / Strategy)
- **Model:** `TaskTemporalType`, `EnergyLevel`, `Chronotype`, `DayPeriod`, `TimeBlock`, `TemporalProfile`; `Task.temporalType`.
- **DAO:** `TimeBlockDAO`, `TemporalProfileDAO` (JDBC + in-memory, user-scoped via `Session`).
- **Service:** `TemporalPlanningService` (profile + blocks + deep-work windows + suggested defaults), `TemporalRecommendationService` (best task now, tasks by temporal type).
- **Strategy:** `TemporalRecommendationStrategy` ← `BasicTemporalRecommendationStrategy`.
- **Controller:** `TemporalController` (save profile, add block).
- **UI:** `TemporalIntelligenceView` — best task now, editable energy profile, time blocks + detected deep-work windows, tasks grouped by temporal type. Defensive: a service/DB error degrades to a calm message, never a crash.

## Schema
- `tasks.temporal_type VARCHAR(20)` (added via `CREATE` for fresh installs + a best-effort `ALTER` migration for existing DBs).
- `time_blocks(id, user_id, start_time, end_time, energy_level, available_minutes, label)`.
- `temporal_profiles(user_id PK, morning/afternoon/evening_energy, chronotype, best_deep_work_period, fatigue_period)`.

## Tests
- `BasicTemporalRecommendationStrategyTest` — empty→empty; indivisible never picked for a short gap; fixed-time-due-today preferred; deep work preferred in a high-energy window.
- `TemporalPlanningTest` — default profile; suggested blocks; deep-work window detection; stored blocks override suggestions.

## Known limitations
- Energy profile is **user-declared**, not yet learned from focus-session history (Phase 5 + analytics will feed it later).
- "Suggested" blocks are derived from the profile until the user defines real blocks; block CRUD UI currently supports add (delete/edit via service is ready for a later UI pass).
