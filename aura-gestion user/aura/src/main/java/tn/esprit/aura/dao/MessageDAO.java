package tn.esprit.aura.dao;

import tn.esprit.aura.entities.Message;
import tn.esprit.aura.utils.DBConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    public boolean sendMessage(int senderId, String senderRole, int receiverId, String receiverRole, String content) {
        String sql = "INSERT INTO messages (sender_id, sender_role, receiver_id, content, sent_at) VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, senderId);
            ps.setString(2, senderRole);
            ps.setInt(3, receiverId);
            ps.setString(4, content);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("SendMessage error: " + e.getMessage());
            return false;
        }
    }

    public List<Message> getConversation(int patientId, int docteurId) {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE " +
                "((sender_id = ? AND sender_role = 'patient' AND receiver_id = ?) OR " +
                " (sender_id = ? AND sender_role = 'doctor'  AND receiver_id = ?)) " +
                "ORDER BY sent_at ASC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, docteurId);
            ps.setInt(3, docteurId);
            ps.setInt(4, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapMessage(rs));
            }
        } catch (SQLException e) {
            System.err.println("GetConversation error: " + e.getMessage());
        }
        return list;
    }

    public List<Integer> getPatientPartnersForDocteur(int docteurId) {
        return getPatientPartnersForDoctor(docteurId);
    }

    public List<Integer> getPatientPartnersForDoctor(int docteurId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT DISTINCT " +
                "CASE WHEN sender_role='patient' THEN sender_id ELSE receiver_id END as pat_id " +
                "FROM messages WHERE " +
                "(sender_id = ? AND sender_role = 'doctor') OR " +
                "(receiver_id = ? AND sender_role = 'patient')";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, docteurId);
            ps.setInt(2, docteurId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("pat_id"));
            }
        } catch (SQLException e) {
            System.err.println("GetPatientPartnersForDocteur error: " + e.getMessage());
        }
        return ids;
    }

    public List<Integer> getDocteurPartnersForPatient(int patientId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT DISTINCT " +
                "CASE WHEN sender_role='doctor' THEN sender_id ELSE receiver_id END as doc_id " +
                "FROM messages WHERE " +
                "(sender_id = ? AND sender_role = 'patient') OR " +
                "(receiver_id = ? AND sender_role = 'doctor')";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("doc_id"));
            }
        } catch (SQLException e) {
            System.err.println("GetDocteurPartnersForPatient error: " + e.getMessage());
        }
        return ids;
    }

    public String resolveNameById(int id, String role) {
        if ("doctor".equals(role)) {
            String sql = "SELECT nom FROM docteurs WHERE id = ?";
            try (Connection conn = DBConnection.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getString("nom");
                }
            } catch (SQLException e) {}
        } else {
            // Try users table
            String sqlUsers = "SELECT nom FROM users WHERE id = ?";
            try (Connection conn = DBConnection.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlUsers)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getString("nom");
                }
            } catch (SQLException e) {}
            
            // Try utilisateurs table as fallback
            String sqlUtils = "SELECT nom FROM utilisateurs WHERE id = ?";
            try (Connection conn = DBConnection.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlUtils)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getString("nom");
                }
            } catch (SQLException e) {}
        }
        return "Inconnu";
    }

    private Message mapMessage(ResultSet rs) throws SQLException {
        LocalDateTime sentAt = rs.getTimestamp("sent_at") != null ?
                rs.getTimestamp("sent_at").toLocalDateTime() : null;
        return new Message(
                rs.getInt("id"),
                rs.getInt("sender_id"),
                rs.getInt("receiver_id"),
                rs.getString("sender_role"),
                rs.getString("content"),
                sentAt
        );
    }
}
