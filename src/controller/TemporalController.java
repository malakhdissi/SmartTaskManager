package controller;

import model.Chronotype;
import model.DayPeriod;
import model.EnergyLevel;
import model.TemporalProfile;
import model.TimeBlock;
import service.ServiceLocator;
import service.Session;

import java.time.LocalTime;
import java.util.UUID;

/**
 * TemporalController — UI-facing write actions for the Temporal Intelligence
 * screen. Keeps the view free of persistence logic; reads happen through the
 * services directly, writes funnel through here with toast feedback.
 */
public class TemporalController {

    private final NavigationController nav;

    public TemporalController(NavigationController nav) {
        this.nav = nav;
    }

    /** Persists the user's energy-rhythm profile. */
    public void saveProfile(EnergyLevel morning, EnergyLevel afternoon, EnergyLevel evening,
                            Chronotype chronotype, DayPeriod bestDeepWork, DayPeriod fatigue) {
        String uid = ServiceLocator.session().currentUserId();
        TemporalProfile profile = new TemporalProfile(uid, morning, afternoon, evening,
                chronotype, bestDeepWork, fatigue);
        ServiceLocator.temporalPlanningService().saveProfile(profile);
        nav.notifySuccess("Energy profile saved.");
        nav.showTemporal();
    }

    /** Adds a custom time block. */
    public void addBlock(LocalTime start, LocalTime end, EnergyLevel energy, int availableMinutes, String label) {
        if (start == null || end == null || !end.isAfter(start)) {
            nav.notifyWarning("End time must be after start time.");
            return;
        }
        String uid = ServiceLocator.session().currentUserId();
        TimeBlock block = new TimeBlock(UUID.randomUUID().toString().substring(0, 8), uid,
                start, end, energy, availableMinutes, label == null || label.isBlank() ? "Custom block" : label.trim());
        ServiceLocator.temporalPlanningService().addBlock(block);
        nav.notifySuccess("Time block added.");
        nav.showTemporal();
    }
}
