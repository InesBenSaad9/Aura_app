package org.example.dao;

import org.example.entities.Message;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {
    private Connection conn = DatabaseConnection.getConnection();

    // senderId/receiverId = vrais IDs de leurs tables respectives
    // senderRole = "doctor" ou "patient"
    public boolean sendMessage(int senderId, String senderRole, int receiverId, String receiverRole, String content) {
        String sql = "INSERT INTO messages (sender_id, sender_role, receiver_id, content, sent_at) VALUES (?, ?, ?, ?, NOW())";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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

    // Récupère la conversation entre un patient et un docteur
    public List<Message> getConversation(int patientId, int docteurId) {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE " +
                "((sender_id = ? AND sender_role = 'patient' AND receiver_id = ?) OR " +
                " (sender_id = ? AND sender_role = 'doctor'  AND receiver_id = ?)) " +
                "ORDER BY sent_at ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, docteurId);
            ps.setInt(3, docteurId);
            ps.setInt(4, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapMessage(rs));
        } catch (SQLException e) {
            System.err.println("GetConversation error: " + e.getMessage());
        }
        return list;
    }

    // Pour un patient : retourne la liste des docteurs avec qui il a échangé
    public List<Integer> getDocteurPartnersForPatient(int patientId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT DISTINCT " +
                "CASE WHEN sender_role='doctor' THEN sender_id ELSE receiver_id END as doc_id " +
                "FROM messages WHERE " +
                "(sender_id = ? AND sender_role = 'patient') OR " +
                "(receiver_id = ? AND sender_role = 'doctor')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt("doc_id"));
        } catch (SQLException e) {
            System.err.println("GetDocteurPartnersForPatient error: " + e.getMessage());
        }
        return ids;
    }

    // Pour un docteur : retourne la liste des patients avec qui il a échangé
    public List<Integer> getPatientPartnersForDocteur(int docteurId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT DISTINCT " +
                "CASE WHEN sender_role='patient' THEN sender_id ELSE receiver_id END as pat_id " +
                "FROM messages WHERE " +
                "(sender_id = ? AND sender_role = 'doctor') OR " +
                "(receiver_id = ? AND sender_role = 'patient')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, docteurId);
            ps.setInt(2, docteurId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt("pat_id"));
        } catch (SQLException e) {
            System.err.println("GetPatientPartnersForDocteur error: " + e.getMessage());
        }
        return ids;
    }

    public String resolveNameById(int id, String role) {
        try {
            if ("doctor".equals(role)) {
                PreparedStatement ps = conn.prepareStatement("SELECT nom FROM docteurs WHERE id = ?");
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getString("nom");
            } else {
                PreparedStatement ps = conn.prepareStatement("SELECT username FROM users WHERE id = ?");
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getString("username");
            }
        } catch (SQLException e) {
            System.err.println("ResolveNameById error: " + e.getMessage());
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
                null,
                rs.getString("content"),
                sentAt
        );
    }
}