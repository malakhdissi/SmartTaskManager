package model;

/**
 * CoachAvoidItem — a task/activity the Coach suggests deferring right now, with
 * an honest reason (too long for the slot, low impact, poor energy fit, …).
 */
public record CoachAvoidItem(String title, String reason) {}
