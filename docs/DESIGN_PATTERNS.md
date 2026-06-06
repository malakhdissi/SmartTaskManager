# Design Patterns

## 1. MVC / Layered Architecture

The app separates UI composition, user actions, business logic, persistence, and domain data.

```text
View -> Controller -> Service -> DAO -> Model
```

Benefit:
- The JavaFX layer stays understandable.
- Business logic can be tested without launching JavaFX.
- Persistence can change without rewriting screens.

## 2. Service Layer

Services represent application use cases:
- `TaskService`
- `DashboardService`
- `AuthService`
- `GoalService`
- `AnalyticsService`
- `TemporalPlanningService`
- `TemporalRecommendationService`
- `AICoachService`
- `SmartScheduleService`

Benefit:
- Keeps behavior out of views.
- Gives future APIs or mobile clients a reusable backend core.

## 3. DAO Pattern

DAO interfaces hide persistence details:
- `TaskDao`
- `UserDao`
- `GoalDao`
- `TimeBlockDAO`
- `TemporalProfileDAO`

Each contract has JDBC and/or in-memory implementations.

Benefit:
- The UI and services do not depend on a specific database.
- MySQL can later become SQLite, PostgreSQL, MongoDB, or an API adapter.

## 4. Strategy Pattern

Algorithms are replaceable through interfaces:
- `ScoringStrategy`
- `RecommendationStrategy`
- `TemporalRecommendationStrategy`
- `PersonaStrategy`

Benefit:
- A heuristic recommendation engine can later become a learning system.
- Temporal scoring can evolve independently from UI.
- Student demos can explain intelligence as modular and ethical.

## 5. Centralized Navigation Controller

`NavigationController` owns screen rendering and toasts.

Benefit:
- Screens do not instantiate each other directly.
- Future guarded routes, transitions, breadcrumbs, or role-based access can be added centrally.
- Runtime rendering failures are caught and shown as readable fallback screens.

## 6. Component Composition

Repeated UI patterns live in `view.components`.

Examples:
- `Sidebar`
- `TopBar`
- `ActionButton`
- `TaskCard`
- `KpiCard`
- `RecommendationCard`
- `ProgressCard`
- `EmptyState`
- `NotificationToast`
- `PasswordBox`
- `BrandMark`

Benefit:
- The UI feels like one product, not separate academic screens.
- Future screens can be built quickly and consistently.

## 7. In-Memory Fallback

`ServiceLocator` chooses JDBC DAOs if persistence is available, otherwise in-memory DAOs.

Benefit:
- The application remains demonstrable even without a configured database.
- The same service code works in both modes.

## 8. Empty State Pattern

Screens that do not yet have real data show honest empty states instead of fake analytics.

Benefit:
- Avoids misleading the user.
- Keeps the demo professional.
- Supports ethical UX.

## 9. Progressive Disclosure

The UI does not show every detail at once. The Dashboard emphasizes the best next action; deeper detail is available through task details, insights, temporal intelligence, and recommendation screens.

Benefit:
- Reduces cognitive load.
- Fits the product goal: guide action, not overwhelm.

## 10. Calm Feedback Pattern

Actions produce non-blocking toasts instead of disruptive dialogs for routine events.

Benefit:
- User gets feedback without being interrupted.
- Supports focus-oriented workflow.

