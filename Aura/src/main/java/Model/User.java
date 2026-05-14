package Model;

public class User {
    private int id;
    private String username;
    private String email;
    private String password;
    private String city;

    // Constructeur
    public User(int id, String username, String email, String password, String city) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.city = city;
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getCity() { return city; }

    // Setters
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setCity(String city) { this.city = city; }
}
