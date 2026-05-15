package org.example.entities;

import java.time.LocalDateTime;

public class Appointment {
    private int id;
    private int patientId;
    private int docteurId;
    private String docteurName;
    private String specialite;
    private LocalDateTime dateTime;
    private String status; // Confirmé, En attente, Annulé

    public Appointment() {}

    public Appointment(int id, int patientId, int docteurId, String docteurName,
                       String specialite, LocalDateTime dateTime, String status) {
        this.id = id;
        this.patientId = patientId;
        this.docteurId = docteurId;
        this.docteurName = docteurName;
        this.specialite = specialite;
        this.dateTime = dateTime;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getDocteurId() { return docteurId; }
    public void setDocteurId(int docteurId) { this.docteurId = docteurId; }

    public String getDocteurName() { return docteurName; }
    public void setDocteurName(String docteurName) { this.docteurName = docteurName; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
