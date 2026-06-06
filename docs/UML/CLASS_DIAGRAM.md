# Class Diagram

```mermaid
classDiagram
    class Main {
        +start(Stage stage)
        +main(String[] args)
    }

    class NavigationController {
        -Stage stage
        -Scene scene
        -StackPane root
        +showWelcome()
        +showLogin()
        +showDashboard()
        +showTaskList()
        +showAddTask()
        +showTaskDetails(String id)
        +showDeepWork()
        +notifySuccess(String text)
    }

    class TaskUiController {
        +createTask(...)
        +updateTask(...)
        +markDone(String id)
        +delete(String id)
    }

    class AuthUiController {
        +login(String email, char[] password, Consumer onError)
        +signup(String name, String email, char[] password, Consumer onError)
        +guest()
    }

    class TaskService {
        <<interface>>
        +getAll()
        +getById(String id)
        +save(Task task)
        +markDone(String id)
        +delete(String id)
    }

    class TaskServiceImpl

    class TaskDao {
        <<interface>>
    }

    class JdbcTaskDao
    class InMemoryTaskDao

    class ScoringStrategy {
        <<interface>>
        +score(Task task)
    }

    class DefaultScoringStrategy

    class Task {
        +String id
        +String title
        +Priority priority
        +TaskStatus status
        +TaskType type
        +TaskTemporalType temporalType
    }

    Main --> NavigationController
    NavigationController --> TaskUiController
    NavigationController --> AuthUiController
    TaskUiController --> TaskService
    TaskService <|.. TaskServiceImpl
    TaskServiceImpl --> TaskDao
    TaskDao <|.. JdbcTaskDao
    TaskDao <|.. InMemoryTaskDao
    TaskServiceImpl --> ScoringStrategy
    ScoringStrategy <|.. DefaultScoringStrategy
    TaskService --> Task
```

## Notes

This is a simplified class diagram focused on the main task and navigation path. The repository contains additional services for analytics, goals, temporal intelligence, AI coach, and smart schedule.

