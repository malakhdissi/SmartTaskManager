-- =============================================================================
-- Smart Task Manager — schema bootstrap (idempotent)
-- Executed automatically by dao.Database on first connection.
-- =============================================================================

CREATE TABLE IF NOT EXISTS users (
    id            VARCHAR(36)  PRIMARY KEY,
    display_name  VARCHAR(80)  NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    streak_days   INT          NOT NULL DEFAULT 0,
    persona       VARCHAR(40),
    is_guest      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS goals (
    id          VARCHAR(36)  PRIMARY KEY,
    user_id     VARCHAR(36)  NOT NULL,
    title       VARCHAR(160) NOT NULL,
    description TEXT,
    progress    DOUBLE       NOT NULL DEFAULT 0,
    active      BOOLEAN      NOT NULL DEFAULT FALSE,
    category    VARCHAR(20),
    importance  INT          NOT NULL DEFAULT 3,
    target_date DATE,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_goal_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tasks (
    id                VARCHAR(36)  PRIMARY KEY,
    user_id           VARCHAR(36)  NOT NULL,
    title             VARCHAR(200) NOT NULL,
    description       TEXT,
    priority          VARCHAR(20)  NOT NULL,
    status            VARCHAR(20)  NOT NULL,
    type              VARCHAR(30)  NOT NULL,
    deadline          DATE,
    est_minutes       INT          NOT NULL DEFAULT 30,
    score             DOUBLE       NOT NULL DEFAULT 0,
    goal_contribution DOUBLE       NOT NULL DEFAULT 0,
    temporal_type     VARCHAR(20),
    goal_id           VARCHAR(36),
    CONSTRAINT fk_task_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ===== Phase 1: Temporal Intelligence =====

CREATE TABLE IF NOT EXISTS time_blocks (
    id                VARCHAR(36)  PRIMARY KEY,
    user_id           VARCHAR(36)  NOT NULL,
    start_time        TIME         NOT NULL,
    end_time          TIME         NOT NULL,
    energy_level      VARCHAR(10)  NOT NULL,
    available_minutes INT          NOT NULL DEFAULT 0,
    label             VARCHAR(120),
    CONSTRAINT fk_block_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS temporal_profiles (
    user_id               VARCHAR(36) PRIMARY KEY,
    morning_energy        VARCHAR(10) NOT NULL,
    afternoon_energy      VARCHAR(10) NOT NULL,
    evening_energy        VARCHAR(10) NOT NULL,
    chronotype            VARCHAR(20) NOT NULL,
    best_deep_work_period VARCHAR(15) NOT NULL,
    fatigue_period        VARCHAR(15) NOT NULL,
    CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
