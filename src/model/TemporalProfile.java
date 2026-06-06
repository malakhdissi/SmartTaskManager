package model;

/**
 * TemporalProfile — the user's personal energy rhythm. Drives where the engine
 * suggests hard cognitive work versus light tasks.
 *
 * <p>One profile per user. {@link #defaultFor(String)} provides a sensible
 * starting profile so the engine works before the user customizes anything.</p>
 */
public class TemporalProfile {

    private final String userId;
    private EnergyLevel preferredMorningEnergy;
    private EnergyLevel preferredAfternoonEnergy;
    private EnergyLevel preferredEveningEnergy;
    private Chronotype chronotype;
    private DayPeriod bestDeepWorkPeriod;
    private DayPeriod fatiguePeriod;

    public TemporalProfile(String userId,
                           EnergyLevel preferredMorningEnergy,
                           EnergyLevel preferredAfternoonEnergy,
                           EnergyLevel preferredEveningEnergy,
                           Chronotype chronotype,
                           DayPeriod bestDeepWorkPeriod,
                           DayPeriod fatiguePeriod) {
        this.userId = userId;
        this.preferredMorningEnergy = preferredMorningEnergy;
        this.preferredAfternoonEnergy = preferredAfternoonEnergy;
        this.preferredEveningEnergy = preferredEveningEnergy;
        this.chronotype = chronotype;
        this.bestDeepWorkPeriod = bestDeepWorkPeriod;
        this.fatiguePeriod = fatiguePeriod;
    }

    /** A balanced default profile (morning-leaning), used until the user edits it. */
    public static TemporalProfile defaultFor(String userId) {
        return new TemporalProfile(userId,
                EnergyLevel.HIGH, EnergyLevel.MEDIUM, EnergyLevel.LOW,
                Chronotype.INTERMEDIATE, DayPeriod.MORNING, DayPeriod.EVENING);
    }

    public String getUserId() { return userId; }
    public EnergyLevel getPreferredMorningEnergy() { return preferredMorningEnergy; }
    public EnergyLevel getPreferredAfternoonEnergy() { return preferredAfternoonEnergy; }
    public EnergyLevel getPreferredEveningEnergy() { return preferredEveningEnergy; }
    public Chronotype getChronotype() { return chronotype; }
    public DayPeriod getBestDeepWorkPeriod() { return bestDeepWorkPeriod; }
    public DayPeriod getFatiguePeriod() { return fatiguePeriod; }

    public void setPreferredMorningEnergy(EnergyLevel e) { this.preferredMorningEnergy = e; }
    public void setPreferredAfternoonEnergy(EnergyLevel e) { this.preferredAfternoonEnergy = e; }
    public void setPreferredEveningEnergy(EnergyLevel e) { this.preferredEveningEnergy = e; }
    public void setChronotype(Chronotype c) { this.chronotype = c; }
    public void setBestDeepWorkPeriod(DayPeriod p) { this.bestDeepWorkPeriod = p; }
    public void setFatiguePeriod(DayPeriod p) { this.fatiguePeriod = p; }

    /** Expected energy for a given part of the day, per this profile. */
    public EnergyLevel energyFor(DayPeriod period) {
        return switch (period) {
            case MORNING -> preferredMorningEnergy;
            case AFTERNOON -> preferredAfternoonEnergy;
            case EVENING -> preferredEveningEnergy;
        };
    }
}
