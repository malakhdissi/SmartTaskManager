package strategy;

import model.Persona;

/**
 * PersonaStrategy — adapts the product's behavior to the active persona.
 *
 * <p>Persona influences default focus duration, suggested task ordering,
 * notification frequency, and tone. We extract it as a strategy so future
 * personas (Calm, High-Intensity, Recovering, ...) can be added without
 * touching screens.</p>
 */
public interface PersonaStrategy {

    /** The persona this strategy applies to. */
    Persona persona();

    /** Default Deep Work block length in minutes for this persona. */
    int defaultDeepWorkMinutes();

    /** Suggested first action label shown when no tasks exist yet. */
    String firstActionHint();
}
