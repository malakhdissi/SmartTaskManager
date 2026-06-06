# Smart Task Manager / Productivity Intelligence System

Smart Task Manager is a JavaFX desktop productivity platform designed to help a user decide what to do next, protect deep work, understand progress, and reduce distraction without shame-based UX.

This project is intentionally not a classic to-do list. It uses task scoring, recommendation strategies, temporal intelligence, goal contribution, analytics, and calm UI patterns to create a guided productivity experience.

## Current Readiness Status

| Area | Status |
|---|---|
| JavaFX presentation layer | Ready for demo |
| Centralized navigation | Implemented |
| Reusable UI components | Implemented |
| MVC separation | Implemented |
| DAO abstraction | Implemented |
| MySQL persistence | Implemented with in-memory fallback |
| Temporal intelligence | Implemented |
| Auth flow | Implemented |
| Tests | Present for service/strategy/util flows |
| Professor/client docs | Prepared |

## How to Run

Prerequisites:
- JDK 17+; the project is configured for Java 17 source/target.
- Maven.
- Optional: MySQL if you want persisted data. If the database is unavailable, the app falls back to in-memory storage.

Run the app:

```bash
mvn javafx:run
```

Run tests:

```bash
mvn test
```

## Project Structure

```text
src/
├── controller/   User actions, navigation orchestration, UI-facing controllers
├── dao/          Persistence interfaces plus JDBC and in-memory implementations
├── model/        Domain entities and enums
├── service/      Business logic and application use cases
├── strategy/     Replaceable scoring, recommendation, and temporal algorithms
├── util/         Constants, formatting, password helpers, theme values
├── view/         JavaFX screens
│   └── components/  Reusable JavaFX UI components
└── Main.java     Thin JavaFX entry point

resources/
├── css/style.css
├── db.properties
├── db/schema.sql
└── images/

docs/
├── ARCHITECTURE.md
├── DESIGN_PATTERNS.md
├── ARCHITECTURE_AUDIT.md
├── SPRINT_AUDIT.md
├── COMPONENT_AUDIT.md
└── UML/
```

## Core Screens

MVP screens:
- Welcome
- Login / Signup / Forgot Password
- Dashboard
- Task List
- Add Task
- Task Details
- Edit Task
- Insights
- Settings
- Goal Definition

V1+ and future-facing screens:
- Recommendation Engine
- Deep Work Session
- Productivity Timeline
- Habit Tracking
- Distraction Management
- Leaderboard
- Persona Adaptation
- AI Coach
- Smart Schedule Generator
- Temporal Intelligence

## Architecture Summary

The application follows a layered desktop architecture:

```text
JavaFX Views
    ↓
UI Controllers
    ↓
Services
    ↓
Strategies + DAOs
    ↓
Models + Persistence
```

Important decisions:
- Views do not run SQL.
- Views do not own business algorithms.
- Navigation is centralized in `NavigationController`.
- Services expose use cases and keep logic testable.
- DAOs hide persistence details behind interfaces.
- Strategies make recommendation/scoring algorithms replaceable.
- CSS centralizes the visual design system.

## Demo Scenario

1. Start at Welcome and explain the product problem: students often know they have work, but not what to do next.
2. Log in or continue as guest.
3. Show Dashboard and the best next action.
4. Add a task.
5. Return to Task List and show the task.
6. Open Task Details and mark a task done.
7. Open Insights and Temporal Intelligence.
8. Open Deep Work Mode and explain distraction reduction.
9. Conclude with architecture: JavaFX UI now, DAO/service/strategy layers ready for growth.

## Documentation Map

- [Architecture](docs/ARCHITECTURE.md)
- [Design Patterns](docs/DESIGN_PATTERNS.md)
- [Architecture Audit](docs/ARCHITECTURE_AUDIT.md)
- [Sprint Audit](docs/SPRINT_AUDIT.md)
- [Component Audit](docs/COMPONENT_AUDIT.md)
- [Roadmap](ROADMAP.md)
- [Presentation Outline](PRESENTATION_OUTLINE.md)
- [Professor Q&A](PROFESSOR_QA.md)
- [Appendix Code Snippets](docs/APPENDIX_CODE_SNIPPETS.md)
- [UML Diagrams](docs/UML/README.md)

## Ethical UX Position

The product avoids toxic gamification and shame-based productivity. It rewards starting, consistency, and better choices, not only high task volume. The UI uses calm feedback, explainable recommendations, and progressive disclosure to reduce cognitive load.

