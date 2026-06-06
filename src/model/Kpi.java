package model;

/**
 * Kpi — a single dashboard metric (label + value + optional delta hint).
 *
 * <p>Kept as a plain value object so KpiCard never has to know whether a
 * KPI is computed live, fetched from a service, or mocked.</p>
 */
public class Kpi {

    private final String label;
    private final String value;
    private final String delta;   // e.g. "+3% vs yesterday" — may be null
    private final String accent;  // CSS modifier class, e.g. "success", "warning"

    public Kpi(String label, String value, String delta, String accent) {
        this.label = label;
        this.value = value;
        this.delta = delta;
        this.accent = accent;
    }

    public String getLabel()  { return label; }
    public String getValue()  { return value; }
    public String getDelta()  { return delta; }
    public String getAccent() { return accent; }
}
