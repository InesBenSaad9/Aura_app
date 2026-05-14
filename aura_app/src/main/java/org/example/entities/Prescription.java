package org.example.entities;

import java.time.LocalDate;

public class Prescription {
    private int id;
    private int patientId;
    private int docteurId;
    private String patientUsername;
    private String docteurUsername;
    private String medicament;
    private String dosage;
    private String instructions;
    private LocalDate dateCreation;

    public Prescription() {}

    public Prescription(int id, int patientId, int docteurId, String patientUsername,
                        String docteurUsername, String medicament, String dosage,
                        String instructions, LocalDate dateCreation) {
        this.id = id;
        this.patientId = patientId;
        this.docteurId = docteurId;
        this.patientUsername = patientUsername;
        this.docteurUsername = docteurUsername;
        this.medicament = medicament;
        this.dosage = dosage;
        this.instructions = instructions;
        this.dateCreation = dateCreation;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getDocteurId() { return docteurId; }
    public void setDocteurId(int docteurId) { this.docteurId = docteurId; }

    public String getPatientUsername() { return patientUsername; }
    public void setPatientUsername(String patientUsername) { this.patientUsername = patientUsername; }

    public String getDocteurUsername() { return docteurUsername; }
    public void setDocteurUsername(String docteurUsername) { this.docteurUsername = docteurUsername; }

    public String getMedicament() { return medicament; }
    public void setMedicament(String medicament) { this.medicament = medicament; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }
}
