package model;

import java.util.List;

/**
 * CoachRecommendation — a scored task with its full reasoning and display-ready
 * metadata. Used for the best action and for alternatives.
 *
 * @param task             the real task
 * @param score            0..1 overall fit
 * @param confidence       0..1 confidence
 * @param reasons          per-lens explanations
 * @param goalContribution display string ("12% to your goal" or "Not linked to a goal yet")
 * @param durationLabel    e.g. "1h30" or "—"
 * @param priorityLabel    e.g. "High"
 * @param deadlineLabel    e.g. "Due Jun 6" or "No deadline"
 */
public record CoachRecommendation(Task task, double score, double confidence,
                                  List<CoachReason> reasons, String goalContribution,
                                  String durationLabel, String priorityLabel, String deadlineLabel) {}
