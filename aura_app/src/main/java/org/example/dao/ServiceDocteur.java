package org.example.dao;

import org.example.entities.docteur;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDocteur {
    private Connection conn = DatabaseConnection.getConnection();

    public List<docteur> getAllDocteurs() {
        List<docteur> list = new ArrayList<>();
        String sql = "SELECT * FROM docteurs";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapDocteur(rs));
        } catch (SQLException e) {
            System.err.println("GetAllDocteurs error: " + e.getMessage());
        }
        return list;
    }

    public List<docteur> searchDocteurs(String keyword) {
        List<docteur> list = new ArrayList<>();
        String sql = "SELECT * FROM docteurs WHERE nom LIKE ? OR type_praticien LIKE ? OR adresse LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            ps.setString(1, k); ps.setString(2, k); ps.setString(3, k);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapDocteur(rs));
        } catch (SQLException e) {
            System.err.println("SearchDocteurs error: " + e.getMessage());
        }
        return list;
    }

    public docteur getByUsername(String username) {
        String sql = "SELECT * FROM docteurs WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapDocteur(rs);
        } catch (SQLException e) {
            System.err.println("GetByUsername error: " + e.getMessage());
        }
        return null;
    }

    public docteur getById(int id) {
        String sql = "SELECT * FROM docteurs WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapDocteur(rs);
        } catch (SQLException e) {
            System.err.println("GetById error: " + e.getMessage());
        }
        return null;
    }

    private docteur mapDocteur(ResultSet rs) throws SQLException {
        // Lecture défensive : utilise nom/type_praticien/adresse (vraie DB)
        // + username/specialite/ville si les colonnes ont été ajoutées
        String nom        = getStringSafe(rs, "nom");
        String username   = getStringSafe(rs, "username");
        String password   = getStringSafe(rs, "password");
        String specialite = getStringSafe(rs, "specialite");
        if (specialite == null) specialite = getStringSafe(rs, "type_praticien");
        String ville      = getStringSafe(rs, "ville");
        if (ville == null) ville = getStringSafe(rs, "adresse");
        String telephone  = getStringSafe(rs, "telephone");
        double rating     = getRatingSafe(rs);

        return new docteur(rs.getInt("id"), nom, username, password,
                specialite, ville, telephone, rating);
    }

    private String getStringSafe(ResultSet rs, String col) {
        try { return rs.getString(col); } catch (SQLException e) { return null; }
    }

    private double getRatingSafe(ResultSet rs) {
        try { return rs.getDouble("rating"); } catch (SQLException e) { return 4.5; }
    }
}