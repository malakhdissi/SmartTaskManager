package controller;

import model.AvailableTimeSlot;
import model.CoachAvoidItem;
import model.CoachContext;
import model.CoachInsight;
import model.CoachRecommendation;
import model.EnergyLevel;
import service.ProductivityCoachService;
import service.ServiceLocator;

import java.util.List;

/**
 * CoachController — mediates between the Coach UI and
 * {@link ProductivityCoachService}. Holds no UI; the view calls these methods
 * and re-renders. Energy/time selection lives in the service so it persists
 * across re-renders.
 */
public class CoachController {

    private final ProductivityCoachService coach = ServiceLocator.productivityCoachService();

    public void setEnergy(EnergyLevel energy) { coach.setEnergy(energy); }
    public void setTime(AvailableTimeSlot slot) { coach.setAvailableTime(slot); }
    public EnergyLevel energy() { return coach.getEnergy(); }
    public AvailableTimeSlot time() { return coach.getAvailableTime(); }

    public CoachContext situation() { return coach.analyzeCurrentSituation(); }
    public CoachRecommendation best() { return coach.recommendNextAction(); }
    public List<CoachRecommendation> alternatives() { return coach.generateAlternatives(); }
    public List<CoachAvoidItem> avoid() { return coach.generateAvoidList(); }
    public List<CoachInsight> insights() { return coach.generateInsights(); }
}
