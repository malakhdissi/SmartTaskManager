# Architecture

## Purpose

Smart Task Manager is structured as an evolutive JavaFX application. The current target is a demonstrable desktop system, but the code is organized so it can later support stronger persistence, richer analytics, recommendation engines, AI coaching, synchronization, and multi-user behavior.

## Architectural Style

The project uses a layered MVC-style architecture:

```text
Presentation Layer
  JavaFX views and reusable components

Controller Layer
  Navigation and UI action orchestration

Service Layer
  Business use cases and application rules

Strategy Layer
  Replaceable algorithms for scoring, recommendation, temporal matching

DAO Layer
  Persistence abstraction

Model Layer
  Domain entities and enums

Utility Layer
  Formatting, constants, security helpers, shared values
```

## Package Responsibilities

| Package | Responsibility | Examples |
|---|---|---|
| `view` | Screens and JavaFX layout composition | `DashboardView`, `TaskListView`, `TemporalIntelligenceView` |
| `view.components` | Reusable UI controls | `Sidebar`, `TopBar`, `TaskCard`, `NotificationToast` |
| `controller` | UI action orchestration and navigation | `NavigationController`, `TaskUiController`, `AuthUiController` |
| `service` | Use cases and business workflows | `TaskServiceImpl`, `DashboardService`, `AuthService` |
| `strategy` | Swappable algorithms | `DefaultScoringStrategy`, `BasicTemporalRecommendationStrategy` |
| `dao` | Persistence contracts and implementations | `TaskDao`, `JdbcTaskDao`, `InMemoryTaskDao` |
| `model` | Domain objects | `Task`, `Goal`, `TemporalProfile`, `TimeBlock` |
| `util` | Cross-cutting helpers | `Formatter`, `PasswordHasher`, `Constants` |

## Runtime Flow

1. `Main` starts JavaFX, creates the root `StackPane`, loads CSS, and delegates to `NavigationController`.
2. `NavigationController` renders screens and owns toast overlay behavior.
3. Views collect input and delegate actions to UI controllers.
4. UI controllers validate user-facing input and call services.
5. Services use DAOs and strategies to execute business behavior.
6. DAOs either persist to JDBC/MySQL or fall back to in-memory implementations, depending on startup availability.

## Dependency Direction

Allowed direction:

```text
View -> Controller -> Service -> DAO / Strategy -> Model
```

Important constraints:
- Views may read from services for display, but should not directly touch SQL/JDBC.
- Controllers coordinate UI actions; they should not become God objects.
- Services contain application decisions; they should be testable without JavaFX.
- DAOs hide storage details.
- Strategies isolate algorithms so future AI or recommendation logic can replace current heuristics.

## Navigation

`NavigationController` is the central screen router.

It manages:
- Welcome/Login/Signup/Forgot Password
- Dashboard
- Task List/Add/Edit/Details
- Insights
- Goals and Settings
- Recommendations
- Deep Work
- Timeline
- Habits
- Distractions
- Leaderboard
- Persona
- AI Coach
- Smart Schedule
- Temporal Intelligence

This prevents scattered screen construction and makes future route guards possible, such as requiring authentication before showing dashboard content.

## Persistence

The DAO layer supports two modes:

| Mode | Purpose |
|---|---|
| JDBC/MySQL | Real persisted data when database configuration is available |
| In-memory fallback | Keeps the app demonstrable when MySQL is unavailable |

The UI does not know which mode is active. It talks through controllers and services.

## Temporal Intelligence

Temporal Intelligence is structured as a full vertical slice:

```text
TemporalIntelligenceView
    ↓
TemporalController
    ↓
TemporalPlanningService + TemporalRecommendationService
    ↓
TimeBlockDAO + TemporalProfileDAO + TemporalRecommendationStrategy
    ↓
TimeBlock + TemporalProfile + TaskTemporalType
```

This is a good example of how future phases should be added: model first, DAO/service/strategy next, then controller and UI.

## Scalability Decisions

| Decision | Why it helps |
|---|---|
| Separate `view.components` | Reduces duplicated layout and keeps UI style consistent |
| `ServiceLocator` wiring point | Makes demo setup simple and centralizes dependency construction |
| DAO interfaces | Allows MySQL, SQLite, PostgreSQL, or cloud APIs later |
| Strategy interfaces | Allows algorithm upgrades without rewriting UI |
| Central CSS | Enables visual redesign without changing every screen |
| Tests in `test/` | Keeps service/strategy logic verifiable outside JavaFX |

## Known Architectural Tradeoffs

| Tradeoff | Current state | Future improvement |
|---|---|---|
| `ServiceLocator` is static | Simple for student/demo project | Replace with dependency injection if project grows |
| JavaFX views can still read services | Practical for presentation screens | Move more read models into controllers/view models |
| Flat source layout | Required by original project structure | Standard Maven layout could be adopted later |
| Desktop-only UI | Good for JavaFX course/demo | Service/DAO/strategy layers can support web/mobile later |

## Readiness Verdict

The architecture is presentation-ready and extensible. It is not a temporary script: it has clear layers, reusable components, persistence abstraction, strategy-based intelligence, and testable business logic.

