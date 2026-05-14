package Model;

public class Task {
    private int id;
    private int userId;
    private String title;
    private String priority;   // "haute", "moyenne", "basse"
    private String status;     // "à faire", "en cours", "terminé"
    private String scheduledAt;

    public Task(int id, int userId, String title, String priority, String status, String scheduledAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.priority = priority;
        this.status = status;
        this.scheduledAt = scheduledAt;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
    public String getScheduledAt() { return scheduledAt; }

    public void setStatus(String status) { this.status = status; }
    public void setPriority(String priority) { this.priority = priority; }
    public void setScheduledAt(String scheduledAt) { this.scheduledAt = scheduledAt; }
}
