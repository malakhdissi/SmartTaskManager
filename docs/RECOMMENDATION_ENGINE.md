# Phase 3 — Advanced Recommendation Engine = Productivity Coach V2

**Status:** **Implemented (C0–C4), backend + tests compile-verified** (`javac` EXIT 0; awaiting local `mvn test` /
`javafx:run`). Decisions honored: **ship now / degrade honestly**, **fold into Phase 3**. Models, 8 lenses +
composite, `ProductivityCoachService`, `CoachController`, rebuilt `AiCoachView` are live; `AICoachService` retired.

The Coach is a **weighted multi-lens composite** over existing services — a reasoning layer, not a chatbot,
not a static card. Every recommendation carries a human-readable reason breakdown.

## Data-honesty contract
Reason on real signals only; each lens reports `realData`. Insights/avoid-items render only when backed by
real data; otherwise omitted (never faked).

| Source | Real today | Plan |
|---|---|---|
| Tasks (priority/deadline/duration/status/type/goalContribution) | ✅ | primary |
| Goals (active/progress) | ✅ | used (`Goal.importance` optional later) |
| Temporal period | ✅ | used |
| Energy / available time | user input now (profile fallback) | persist later (optional) |
| Deep Work history / focus score | proxy only | full after **Phase 5** |
| Habits / procrastination / distraction | mostly missing | full after **Phases 4 & 7** |

## Components

### Models (new value objects / records)
- `AvailableTimeSlot` enum — QUARTER(15) / HALF(30) / HOUR(60) / DEEP(120).
- `CoachContext` — now, `DayPeriod`, `EnergyLevel`, availableMinutes, activeTasks, overdue, focusScore (proxy),
  activeGoal, goalProgress, deepWorkMinutes (nullable), `deepWorkHistoryAvailable`, `behavioralDataAvailable`.
- `CoachRecommendation` — task, score, `List<CoachReason>`, goalContributionPct, duration, priority, deadline, expectedImpact.
- `CoachReason` — lens, weightedScore, text, realData.
- `CoachInsight` — type {FOCUS_WINDOW, EFFORT_BALANCE, GOAL_NEGLECT, MOMENTUM}, message, evidence.
- **Reuse** existing `EnergyLevel`, `DayPeriod`, `TemporalProfile`, `Task`, `Goal` (do not recreate).

### Strategy (`strategy/`)
- `CoachLens` interface — `LensResult evaluate(Task, CoachContext)` (`LensResult(score 0..1, reason, realData)`) + `weight()`.
- Lenses: `DeadlineLens`, `GoalContributionLens`, `EnergyAwareLens`, `TemporalLens`, `DeepWorkLens` (degrades),
  `ProcrastinationLens` (degrades).
- `CompositeCoachStrategy` — weighted sum + hard filters: drop tasks longer than availableMinutes; never place
  `INDIVISIBLE` into a short slot (reuses Phase 1 rule). Reuses `DefaultScoringStrategy` / `BasicTemporalRecommendationStrategy` internally.

### Service (`service/`)
- `ProductivityCoachService` — `analyzeCurrentSituation()`, `recommendNextAction()`, `recommendForAvailableTime(int)`,
  `recommendForEnergyLevel(EnergyLevel)`, `detectProcrastinationRisk()`, `detectFocusRisk()`, `generateInsights()`,
  `generateAlternatives()`, `generateAvoidList()`. Orchestrates `TaskService`, `GoalService`, `AnalyticsService`,
  `TemporalPlanningService`. **Supersedes** `AICoachService` (fold in; migrate its test).
- **No new DAO / no schema change** for V2 core.

### Controller / View
- `CoachController` — set energy mode / time slot → re-query → refresh.
- `ProductivityCoachView` replaces `AiCoachView`; route `NavigationController.showAiCoach()` unchanged.
- Sections: Current Situation · Best Next Action · Why This Action · Alternatives A/B/C · If I only have [15/30/60/120] ·
  Energy Mode [Low/Normal/High] · Avoid Now · Coach Insights. Empty state when no tasks.

## Coach questions → methods
| Question | Method |
|---|---|
| What should I do next? | `recommendNextAction()` |
| Why now? | `CoachRecommendation.reasons` (lens breakdown) |
| What contributes most to goals? | `GoalContributionLens` / top goal-driver query |
| What to avoid now? | `generateAvoidList()` |
| If I only have 15/30/60/120 min | `recommendForAvailableTime(int)` |
| If tired / low / high energy | `recommendForEnergyLevel(EnergyLevel)` |

## Sub-sprints (each: validate-before-next)
- **C0** models + `CoachLens` contract (+tests).
- **C1** lenses + `CompositeCoachStrategy` (+per-lens tests, filter tests).
- **C2** `ProductivityCoachService` (all 9 methods), retire `AICoachService` (+tests).
- **C3** `ProductivityCoachView` + `CoachController`, wire route, empty states.
- **C4** insights + avoid-list + docs + verify.
C0–C2 are backend-only (fully `javac`/unit-testable); C3 is the JavaFX screen.

## Risks
Fabrication of unavailable signals (mitigated by `realData` flags + omission); weight tuning (transparent reasons +
per-lens tests); energy/time not persisted (acceptable session input). No Maven/MySQL on the dev machine →
backend verified via `javac`, app run locally.
