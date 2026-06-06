package model;

import java.time.LocalTime;

/**
 * ScheduleBlock — a single time-slot in the Smart Schedule Generator.
 *
 * <p>Used by the auto-planning mock to render a day plan as a vertical
 * timeline (start → end + suggested task + block type).</p>
 */
public class ScheduleBlock {

    private final LocalTime start;
    private final LocalTime end;
    private final String label;
    private final TaskType blockType;
    private final String accent; // CSS modifier: primary / intel / success

    public ScheduleBlock(LocalTime start, LocalTime end, String label, TaskType blockType, String accent) {
        this.start = start;
        this.end = end;
        this.label = label;
        this.blockType = blockType;
        this.accent = accent;
    }

    public LocalTime getStart() { return start; }
    public LocalTime getEnd()   { return end; }
    public String getLabel()    { return label; }
    public TaskType getBlockType() { return blockType; }
    public String getAccent()   { return accent; }
}
