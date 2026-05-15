package tn.esprit.aura.entities;

import java.time.LocalDateTime;

public class AppointmentRequest {
    private int id;
    private int patientId;
    private int docteurId;
    private String patientUsername;
    private String docteurName;
    private String specialite;
    private String status; // PENDING, ACCEPTED, REFUSED
    private LocalDateTime createdAt;
    private LocalDateTime appointmentDate;

    public AppointmentRequest() {}

    public AppointmentRequest(int id, int patientId, int docteurId, String patientUsername,
                              String status, LocalDateTime createdAt, LocalDateTime appointmentDate) {
        this.id = id;
        this.patientId = patientId;
        this.docteurId = docteurId;
        this.patientUsername = patientUsername;
        this.status = status;
        this.createdAt = createdAt;
        this.appointmentDate = appointmentDate;
    }

    public String getDocteurName() { return docteurName; }
    public void setDocteurName(String docteurName) { this.docteurName = docteurName; }
    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getDocteurId() { return docteurId; }
    public int getDoctorId() { return docteurId; }
    public void setDocteurId(int docteurId) { this.docteurId = docteurId; }

    public String getPatientUsername() { return patientUsername; }
    public String getPatientNom() { return patientUsername; }
    public void setPatientUsername(String patientUsername) { this.patientUsername = patientUsername; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }
}
