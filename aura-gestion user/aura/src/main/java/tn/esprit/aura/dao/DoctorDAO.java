package tn.esprit.aura.dao;

import tn.esprit.aura.entities.Doctor;
import tn.esprit.aura.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {

    public List<Doctor> getAllDoctors() {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT * FROM docteurs";
        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapDoctor(rs));
        } catch (SQLException e) {
            System.err.println("GetAllDoctors error: " + e.getMessage());
        }
        return list;
    }

    public List<Doctor> searchDoctors(String keyword) {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT * FROM docteurs WHERE nom LIKE ? OR specialite LIKE ? OR ville LIKE ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            ps.setString(1, k); 
            ps.setString(2, k); 
            ps.setString(3, k);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapDoctor(rs));
            }
        } catch (SQLException e) {
            System.err.println("SearchDoctors error: " + e.getMessage());
        }
        return list;
    }

    public Doctor getByUsername(String username) {
        String sql = "SELECT * FROM docteurs WHERE username = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapDoctor(rs);
            }
        } catch (SQLException e) {
            System.err.println("GetByUsername error: " + e.getMessage());
        }
        return null;
    }

    public Doctor getById(int id) {
        String sql = "SELECT * FROM docteurs WHERE id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapDoctor(rs);
            }
        } catch (SQLException e) {
            System.err.println("GetById error: " + e.getMessage());
        }
        return null;
    }

    public void addDoctor(Doctor doctor) {
        String sql1 = "INSERT INTO docteurs (nom, username, password, specialite, ville, telephone, rating) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sql2 = "INSERT INTO docteurs (nom, username, password, type_praticien, adresse, telephone, rating) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql1)) {
                ps.setString(1, doctor.getNom());
                ps.setString(2, doctor.getUsername());
                ps.setString(3, doctor.getPassword());
                ps.setString(4, doctor.getSpecialite());
                ps.setString(5, doctor.getVille());
                ps.setString(6, doctor.getTelephone());
                ps.setDouble(7, doctor.getRating());
                ps.executeUpdate();
                return; // Success
            } catch (SQLException e1) {
                // If it failed because of missing columns, try sql2
                try (PreparedStatement ps = conn.prepareStatement(sql2)) {
                    ps.setString(1, doctor.getNom());
                    ps.setString(2, doctor.getUsername());
                    ps.setString(3, doctor.getPassword());
                    ps.setString(4, doctor.getSpecialite());
                    ps.setString(5, doctor.getVille());
                    ps.setString(6, doctor.getTelephone());
                    ps.setDouble(7, doctor.getRating());
                    ps.executeUpdate();
                } catch (SQLException e2) {
                    System.err.println("AddDoctor error (fallback): " + e2.getMessage());
                }
            }
        } catch (SQLException e) {
             System.err.println("AddDoctor connection error: " + e.getMessage());
        }
    }

    private Doctor mapDoctor(ResultSet rs) throws SQLException {
        String nom        = getStringSafe(rs, "nom");
        String username   = getStringSafe(rs, "username");
        String password   = getStringSafe(rs, "password");
        String specialite = getStringSafe(rs, "specialite");
        if (specialite == null) specialite = getStringSafe(rs, "type_praticien");
        String ville      = getStringSafe(rs, "ville");
        if (ville == null) ville = getStringSafe(rs, "adresse");
        String telephone  = getStringSafe(rs, "telephone");
        double rating     = getRatingSafe(rs);

        return new Doctor(rs.getInt("id"), nom, username, password,
                specialite, ville, telephone, rating);
    }

    private String getStringSafe(ResultSet rs, String col) {
        try { return rs.getString(col); } catch (SQLException e) { return null; }
    }

    private double getRatingSafe(ResultSet rs) {
        try { return rs.getDouble("rating"); } catch (SQLException e) { return 4.5; }
    }
}
