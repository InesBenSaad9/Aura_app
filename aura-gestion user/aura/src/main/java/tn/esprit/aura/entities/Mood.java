package tn.esprit.aura.entities;

/**
 * Represents a detected mood entry.
 * Migrated from the Aura AI-core module (Model.Mood).
 */
public class Mood {
    private int id;
    private int userId;
    private String label;       // "calme", "stressé", "fatigué", "énergisé", "focalisé"
    private String detectedAt;  // datetime string

    public Mood(int id, int userId, String label, String detectedAt) {
        this.id = id;
        this.userId = userId;
        this.label = label;
        this.detectedAt = detectedAt;
    }

    public int getId()              { return id; }
    public int getUserId()          { return userId; }
    public String getLabel()        { return label; }
    public String getDetectedAt()   { return detectedAt; }

    public void setLabel(String label) { this.label = label; }
}
