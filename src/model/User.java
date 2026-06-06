package model;

/**
 * User — the person using the product.
 *
 * <p>Currently only stores the minimum needed for the UI greeting and the
 * Settings screen. Later this will hold preferences, persona, goals, etc.</p>
 */
public class User {

    private final String id;
    private String displayName;
    private String email;
    /** Salted BCrypt hash — never the raw password. May be null for transient users. */
    private String passwordHash;
    private int currentStreakDays;
    private Persona persona;
    /** True for an anonymous guest session (real but flagged, not credential-backed). */
    private boolean guest;

    /** Full constructor (used by the persistence layer). */
    public User(String id, String displayName, String email, String passwordHash,
                int currentStreakDays, Persona persona) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.currentStreakDays = currentStreakDays;
        this.persona = persona;
    }

    /** Convenience constructor for callers that don't deal with credentials. */
    public User(String id, String displayName, String email, int currentStreakDays, Persona persona) {
        this(id, displayName, email, null, currentStreakDays, persona);
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public int getCurrentStreakDays() { return currentStreakDays; }
    public Persona getPersona() { return persona; }
    public boolean isGuest() { return guest; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setEmail(String email)             { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setCurrentStreakDays(int days)     { this.currentStreakDays = days; }
    public void setPersona(Persona persona)        { this.persona = persona; }
    public void setGuest(boolean guest)            { this.guest = guest; }
}
