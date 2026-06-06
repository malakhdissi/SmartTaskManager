package model;

import java.time.LocalDateTime;

/**
 * TimelineEvent — generic productivity timeline entry.
 *
 * <p>Used by the Productivity Timeline screen to render a mixed stream of
 * "task completed", "focus session", "habit kept" events in one column.</p>
 */
public class TimelineEvent {

    public enum Kind { TASK_COMPLETED, FOCUS_SESSION, HABIT_KEPT, DISTRACTION_REDUCED }

    private final Kind kind;
    private final String title;
    private final String subtitle;
    private final LocalDateTime when;

    public TimelineEvent(Kind kind, String title, String subtitle, LocalDateTime when) {
        this.kind = kind;
        this.title = title;
        this.subtitle = subtitle;
        this.when = when;
    }

    public Kind getKind() { return kind; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public LocalDateTime getWhen() { return when; }
}
