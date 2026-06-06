package util;

/**
 * Constants — application-wide compile-time values.
 *
 * <p>Role: a single, calm place where strings, numeric defaults, and
 * style-class names live. Keeping constants here means screens never
 * "drift" because someone retypes a magic string slightly differently.</p>
 *
 * <p>Scalability note: when this product is later translated (i18n) or
 * driven by a remote config, only this file changes — views remain stable.</p>
 */
public final class Constants {

    /** Application identity */
    public static final String APP_NAME    = "Smart Task Manager";
    public static final String APP_TAGLINE = "Know What To Do Next.";
    public static final String APP_VERSION = "1.0";

    /** Path to the brand monogram on the classpath. */
    public static final String LOGO_PATH = "/images/stm-monogram.png";

    /** Default window size — chosen so the dark SaaS layout breathes. */
    public static final double WINDOW_WIDTH  = 1280;
    public static final double WINDOW_HEIGHT = 800;

    /** CSS classpath location — referenced from Main when building scenes. */
    public static final String CSS_PATH = "/css/style.css";

    /** Deep Work default uninterrupted duration in minutes. */
    public static final int DEEP_WORK_DEFAULT_MINUTES = 90;

    /** Top-N tasks shown on the dashboard. Keeps cognitive load low. */
    public static final int DASHBOARD_TOP_TASKS = 3;

    /** Private constructor — this class is purely a static holder. */
    private Constants() {}
}
