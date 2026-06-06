# Roadmap

## Roadmap Philosophy

The project should evolve by complete vertical slices:

```text
Model -> DAO -> Service -> Strategy -> Controller -> UI -> Tests -> Docs
```

This prevents fragile features where the UI exists but the architecture is not ready.

## Phase 0 — Repository Readiness

Status: DONE

- README
- Architecture docs
- Design pattern docs
- UML docs
- Component audit
- Sprint audit
- Presentation outline
- Professor Q&A

## Phase 1 — Stable Core Productivity System

Status: MOSTLY DONE

- Dashboard
- Task CRUD
- Goal definition
- Auth flow
- Central navigation
- Central design system
- DAO abstraction
- Service layer
- Strategy layer

Next hardening:
- Run full test suite before presentation.
- Confirm database configuration instructions on target machine.

## Phase 2 — Temporal Intelligence

Status: IMPLEMENTED

- Chronotype and energy profile
- Time blocks
- Deep-work windows
- Temporal task compatibility
- Strategy-based temporal recommendations

Next hardening:
- Add edit/delete UI for time blocks.
- Learn energy profile from actual completion/focus history.

## Phase 3 — Student Persona Engine

Status: PLANNED

Goal:
- Adapt recommendations based on behavior mode, without medical labels or shame.

Candidate modes:
- Anti-Procrastination
- Anti-Scrolling
- Deep Work
- Short Focus

Needed layers:
- `PersonaStrategy`
- user persona persistence
- UI selection persistence
- recommendation adjustments

## Phase 4 — Habit Engine

Status: PLANNED

Goal:
- Track sleep, study consistency, and scrolling reduction ethically.

Needed layers:
- Habit models
- Habit DAO
- Habit service
- habit analytics
- empty state replacement with real data

## Phase 5 — Deep Work Engine

Status: PLANNED

Goal:
- Turn Deep Work from a focus timer into a real focus-session system.

Needed layers:
- Focus session DAO
- session start/end persistence
- interruption log
- deep work analytics
- streaks based on consistency, not volume

## Phase 6 — Advanced Recommendation Engine

Status: PLANNED

Goal:
- Improve suggestions using feedback, goal contribution, deadlines, energy, historical completion, and user context.

Potential upgrades:
- feedback loop
- confidence scoring
- recommendation explanation view
- "why this task" transparency

## Phase 7 — Distraction Management

Status: PLANNED

Goal:
- Track and reduce digital distractions gradually.

Ethical boundaries:
- no guilt language
- no forced blocking
- no manipulative streak loss
- user-controlled reduction plan

## Phase 8 — Smart Schedule Generator

Status: PARTIAL / PLANNED

Goal:
- Build realistic day plans using task duration, priority, deadline, temporal fit, and available time.

Future:
- conflict detection
- drag-and-drop schedule edits
- calendar export/import

## Phase 9 — AI Coach

Status: PARTIAL / PLANNED

Goal:
- Provide explainable coaching suggestions from real user data.

Future:
- rule-based first
- optional LLM integration later
- privacy-first summaries
- always explain recommendation rationale

## Phase 10 — Multi-User / Cloud Evolution

Status: FUTURE

Goal:
- Support cloud synchronization, devices, and optional collaborative study/productivity contexts.

Architecture path:
- keep services JavaFX-independent
- replace DAO implementations with API adapters
- add authentication/session hardening
- add sync conflict strategy

