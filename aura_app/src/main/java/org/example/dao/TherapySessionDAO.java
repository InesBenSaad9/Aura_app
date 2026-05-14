package org.example.dao;

import org.example.entities.TherapySession;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TherapySessionDAO {
    private Connection conn = DatabaseConnection.getConnection();

    public List<TherapySession> getSessionsByPatient(int patientId) {
        List<TherapySession> list = new ArrayList<>();
        String sql = "SELECT * FROM therapy_sessions WHERE patient_id = ? ORDER BY session_date DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapSession(rs));
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
                rs.getInt("docteur_id"),
                rs.getString("notes"),
                dt,
                rs.getString("status")
        );
    }
}
