package tn.esprit.aura.dao;

import tn.esprit.aura.entities.User;
import tn.esprit.aura.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private Connection conn = DBConnection.getInstance().getConnection();

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setNom(rs.getString("nom"));
        // "users" table doesn't seem to store prenom; keep null
        u.setEmail(rs.getString("email"));
        // Map legacy column "password" to User.motDePasse
        u.setMotDePasse(rs.getString("password"));
        u.setRole(rs.getString("role"));
        u.setActive(true);
        return u;
    }

    public User login(String nom, String password) {
        String sql = "SELECT * FROM users WHERE nom = ? AND password = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }
        return null;
    }

    public boolean register(String nom, String password, String role, String email) {
        String sql = "INSERT INTO users (nom, password, role, email) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, password);
            ps.setString(3, role);
            ps.setString(4, email);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Register error: " + e.getMessage());
            return false;
        }
    }

    public User findBynom(String nom) {
        String sql = "SELECT * FROM users WHERE nom = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nom);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("FindBynom error: " + e.getMessage());
        }
        return null;
    }

    public List<User> getDoctors() {
        List<User> doctors = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'MEDECIN'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                doctors.add(mapUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("getDoctors error: " + e.getMessage());
        }
        return doctors;
    }
}
