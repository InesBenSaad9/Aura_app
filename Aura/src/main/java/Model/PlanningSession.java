package Model;

public class PlanningSession {
    private int id;
    private int userId;
    private int moodId;
    private String generatedPlan;
    private String date;

    public PlanningSession(int id, int userId, int moodId, String generatedPlan, String date) {
        this.id = id;
        this.userId = userId;
        this.moodId = moodId;
        this.generatedPlan = generatedPlan;
        this.date = date;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getMoodId() { return moodId; }
    public String getGeneratedPlan() { return generatedPlan; }
    public String getDate() { return date; }

    public void setGeneratedPlan(String generatedPlan) { this.generatedPlan = generatedPlan; }
}