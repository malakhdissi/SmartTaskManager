package model;

/**
 * EnergyLevel — coarse cognitive-energy bands used to match tasks to the right
 * time of day. {@link #weight()} gives an orderable value for comparisons.
 */
public enum EnergyLevel {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3);

    private final String label;
    private final int weight;

    EnergyLevel(String label, int weight) {
        this.label = label;
        this.weight = weight;
    }

    public String label() { return label; }
    public int weight() { return weight; }
}
