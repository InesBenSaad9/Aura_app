package tn.esprit.aura.entities;

import java.time.LocalDateTime;

public class TherapySession {
    private int id;
    private int patientId;
    private int docteurId;
    private String notes;
    private LocalDateTime sessionDate;
    private String status;

    public TherapySession() {}

    public TherapySession(int id, int patientId, int docteurId, String notes,
                          LocalDateTime sessionDate, String status) {
        this.id = id;
        this.patientId = patientId;
        this.docteurId = docteurId;
        this.notes = notes;
        this.sessionDate = sessionDate;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getDocteurId() { return docteurId; }
    public void setDocteurId(int docteurId) { this.docteurId = docteurId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDateTime sessionDate) { this.sessionDate = sessionDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
