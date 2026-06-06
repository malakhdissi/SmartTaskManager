# Package Diagram

```mermaid
flowchart TD
    Main["Main.java"]
    View["view<br/>JavaFX screens"]
    Components["view.components<br/>Reusable UI components"]
    Controller["controller<br/>Navigation + UI actions"]
    Service["service<br/>Business use cases"]
    Strategy["strategy<br/>Scoring + recommendations"]
    Dao["dao<br/>Persistence abstraction"]
    Model["model<br/>Domain objects"]
    Util["util<br/>Helpers + constants"]
    Resources["resources<br/>CSS, DB config, images"]

    Main --> Controller
    Main --> Resources
    View --> Components
    View --> Controller
    Controller --> Service
    Service --> Dao
    Service --> Strategy
    Service --> Model
    Strategy --> Model
    Dao --> Model
    View --> Util
    Service --> Util
```

## Notes

- The direction points from higher-level orchestration to lower-level dependencies.
- The UI layer is JavaFX-specific.
- Service, DAO, strategy, and model layers are mostly JavaFX-independent.

