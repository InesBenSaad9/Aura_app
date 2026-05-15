package tn.esprit.aura.dao;

import tn.esprit.aura.entities.TherapySession;
import tn.esprit.aura.utils.DBConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TherapySessionDAO {

    public List<TherapySession> getSessionsByPatient(int patientId) {
        List<TherapySession> list = new ArrayList<>();
        String sql = "SELECT * FROM therapy_sessions WHERE patient_id = ? ORDER BY session_date DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapSession(rs));
            }
        } catch (SQLException e) {
            System.err.println("GetSessionsByPatient error: " + e.getMessage());
        }
        return list;
    }

    private TherapySession mapSession(ResultSet rs) throws SQLException {
        LocalDateTime dt = rs.getTimestamp("session_date") != null ?
                rs.getTimestamp("session_date").toLocalDateTime() : null;
        return new TherapySession(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getInt("doctor_id"),
                rs.getString("notes"),
                dt,
                "Confirmé" // Status par défaut si non présent dans la table
        );
    }
}
