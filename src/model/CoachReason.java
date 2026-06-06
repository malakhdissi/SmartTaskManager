package model;

/**
 * CoachReason — one explainable factor behind a recommendation.
 *
 * @param lens       which lens produced it (e.g. "Deadline")
 * @param score      normalized 0..1 contribution from that lens
 * @param text       human-readable explanation
 * @param realData   true if backed by real data; false if degraded/neutral
 * @param confidence 0..1 confidence in this factor
 */
public record CoachReason(String lens, double score, String text, boolean realData, double confidence) {}
