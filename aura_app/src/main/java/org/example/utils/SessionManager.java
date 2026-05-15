package org.example.utils;

public class SessionManager {
    private static SessionManager instance;
    private int userId;
    private String username;
    private String role; // "patient" or "doctor"

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isDoctor() { return "doctor".equals(role); }
    public boolean isPatient() { return "patient".equals(role); }

    public void clear() {
        userId = 0;
        username = null;
        role = null;
    }
}
