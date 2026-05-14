package tn.esprit.aura.entities;

import java.sql.Timestamp;
import java.time.LocalDate;

public class User {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String photoProfil;
    private String telephone;
    private LocalDate dateNaissance;
    private String genre;
    private String ville;
    private String bio;
    private String role; // "ADMIN" or "USER"
    private String faceData; // Base64 or JSON face embeddings
    private boolean active;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public User() {
        this.role = "USER";
    }

    public User(String nom, String prenom, String email, String motDePasse, String role) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role != null ? role : "USER";
        this.active = true;
    }

    public User(String nom, String prenom, String email, String motDePasse, String photoProfil,
                String telephone, LocalDate dateNaissance, String genre, String ville, String bio, boolean active) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.photoProfil = photoProfil;
        this.telephone = telephone;
        this.dateNaissance = dateNaissance;
        this.genre = genre;
        this.ville = ville;
        this.bio = bio;
        this.active = active;
        this.role = "USER";
    }

    public User(int id, String nom, String prenom, String email, String motDePasse, String photoProfil,
                String telephone, LocalDate dateNaissance, String genre, String ville, String bio,
                String role, boolean active, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.photoProfil = photoProfil;
        this.telephone = telephone;
        this.dateNaissance = dateNaissance;
        this.genre = genre;
        this.ville = ville;
        this.bio = bio;
        this.role = role != null ? role : "USER";
        this.faceData = null;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public User(int id, String nom, String prenom, String email, String motDePasse, String photoProfil,
                String telephone, LocalDate dateNaissance, String genre, String ville, String bio,
                String role, String faceData, boolean active, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.photoProfil = photoProfil;
        this.telephone = telephone;
        this.dateNaissance = dateNaissance;
        this.genre = genre;
        this.ville = ville;
        this.bio = bio;
        this.role = role != null ? role : "USER";
        this.faceData = faceData;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getPhotoProfil() { return photoProfil; }
    public void setPhotoProfil(String photoProfil) { this.photoProfil = photoProfil; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFaceData() { return faceData; }
    public void setFaceData(String faceData) { this.faceData = faceData; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getFullName() {
        String first = (prenom != null ? prenom : "");
        String last = (nom != null ? nom : "");
        return (first + " " + last).trim();
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                '}';
    }
}
