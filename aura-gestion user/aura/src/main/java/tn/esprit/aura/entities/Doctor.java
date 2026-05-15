package tn.esprit.aura.entities;

public class Doctor {
    private int id;
    private String nom;        // Full name displayed
    private String username;
    private String password;
    private String specialite;
    private String ville;
    private String telephone;
    private double rating;

    public Doctor() {}

    public Doctor(int id, String nom, String username, String password,
                   String specialite, String ville, String telephone, double rating) {
        this.id = id;
        this.nom = nom;
        this.username = username;
        this.password = password;
        this.specialite = specialite;
        this.ville = ville;
        this.telephone = telephone;
        this.rating = rating;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }
    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    @Override
    public String toString() { return nom != null ? nom : username; }
}
