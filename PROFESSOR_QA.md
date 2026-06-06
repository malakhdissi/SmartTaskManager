# Professor Q&A

## Is this just a to-do list?

No. A to-do list mainly stores tasks. This system guides decisions. It includes recommendations, goal contribution, temporal intelligence, analytics, deep work separation, and ethical feedback. The task list is only one part of the system.

## Why JavaFX?

JavaFX is appropriate for this project because it supports a rich desktop UI with custom CSS, reusable components, and screen navigation. It also lets the architecture remain pure Java for services, DAO, strategy, and model layers.

## Where is the business logic?

Business logic belongs in the service and strategy layers:
- services coordinate use cases,
- strategies calculate scores and recommendations,
- DAOs persist or retrieve data.

Views should mostly compose UI and collect inputs.

## Why use DAO interfaces?

DAO interfaces prevent the application from depending directly on MySQL, JDBC, or a specific database. This makes it easier to change storage later.

Example:
- today: JDBC/MySQL,
- demo fallback: in-memory,
- future: SQLite, PostgreSQL, cloud API.

## Why does the project have in-memory fallback?

It makes the app demonstrable even when the database is not configured on the presentation machine. This is useful for a desktop demo, but the architecture still supports real persistence.

## What design patterns are used?

Main patterns:
- MVC / layered architecture,
- Service Layer,
- DAO,
- Strategy,
- centralized navigation/controller,
- component composition,
- empty state pattern,
- in-memory fallback.

## How is the app scalable?

It is scalable because each concern has a package:
- UI screens are in `view`,
- reusable UI controls are in `view.components`,
- actions are in `controller`,
- business use cases are in `service`,
- persistence contracts are in `dao`,
- algorithms are in `strategy`,
- domain data is in `model`.

Future features can be added as vertical slices without rewriting the whole app.

## How do you prevent toxic gamification?

The product avoids shame language, forced competition, and guilt-based alerts. Leaderboard ideas are framed as optional and ethical. Progress rewards consistency and starting, not only high volume.

## What makes Temporal Intelligence important?

A task's priority is not enough. A deep-work task may be a bad recommendation during low-energy time, while a flexible small task may be appropriate. Temporal Intelligence matches task type, energy level, time blocks, and urgency.

## Why is Deep Work separate from the normal layout?

Planning mode and execution mode have different cognitive needs. Planning can use navigation and context; Deep Work should remove distractions. That is why Deep Work does not use the sidebar.

## How would you add a new feature?

Use a vertical slice:

1. Add or extend models.
2. Add DAO interface and implementation if data must persist.
3. Add service use case.
4. Add strategy if algorithmic behavior is needed.
5. Add controller methods for UI actions.
6. Add or update JavaFX view/components.
7. Add tests.
8. Update docs.

## What is the biggest current limitation?

Some future-facing screens are not full final systems yet. They should be presented as roadmap phases unless their data is fully implemented. The architecture is ready for them, but honest product communication matters.

## What would you improve next?

The best next improvement is to complete one more vertical slice, such as Focus Session persistence or Habit Engine, rather than adding many visual-only features.

