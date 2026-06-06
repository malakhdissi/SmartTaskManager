package util;

/**
 * Theme — exposes design-token constants that Java code (not CSS) needs.
 *
 * <p>Role: when JavaFX code must use a color programmatically (for example
 * to tint a custom shape that CSS cannot reach), it should pull the value
 * from this file rather than hardcoding a hex string in a view class.</p>
 *
 * <p>Scalability note: switching themes (light/dark/contrast) later means
 * replacing this class behind a {@code Theme} interface — no view changes.</p>
 */
public final class Theme {

    /* Background scale */
    public static final String BG_BASE       = "#0F172A";
    public static final String BG_SURFACE    = "#1E293B";
    public static final String BG_CARD       = "#334155";

    /* Accent scale */
    public static final String PRIMARY_BLUE  = "#3B82F6";
    public static final String SUCCESS_GREEN = "#22C55E";
    public static final String WARNING_AMBER = "#F59E0B";
    public static final String DANGER_RED    = "#EF4444";
    public static final String INTEL_VIOLET  = "#8B5CF6";

    /* Text scale */
    public static final String TEXT_PRIMARY   = "#F8FAFC";
    public static final String TEXT_SECONDARY = "#94A3B8";
    public static final String TEXT_MUTED     = "#64748B";

    private Theme() {}
}
