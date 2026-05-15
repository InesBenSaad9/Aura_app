package org.example.dao;

import org.example.entities.AppointmentRequest;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentRequestDAO {
    private Connection conn = DatabaseConnection.getConnection();

    public boolean createRequest(int patientId, int docteurId) {
        String sql = "INSERT INTO appointment_requests (patient_id, docteur_id, status, created_at) VALUES (?, ?, 'PENDING', NOW())";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, docteurId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("CreateRequest error: " + e.getMessage());
            return false;
        }
    }

    public boolean createRequest(int patientId, int docteurId, LocalDateTime appointmentDate) {
        String sql = "INSERT INTO appointment_requests (patient_id, docteur_id, status, created_at, appointment_date) " +
                "VALUES (?, ?, 'PENDING', NOW(), ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, docteurId);
            ps.setTimestamp(3, Timestamp.valueOf(appointmentDate));
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("CreateRequest error: " + e.getMessage());
            return false;
        }
    }

    public List<AppointmentRequest> getRequestsByDocteur(int docteurId) {
        List<AppointmentRequest> list = new ArrayList<>();
        String sql = "SELECT ar.*, u.username as patient_username FROM appointment_requests ar " +
                "JOIN users u ON ar.patient_id = u.id " +
                "WHERE ar.docteur_id = ? ORDER BY ar.created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, docteurId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRequest(rs));
            }
        } catch (SQLException e) {
            System.err.println("GetRequestsByDocteur error: " + e.getMessage());
        }
        return list;
    }

    public List<AppointmentRequest> getRequestsByPatient(int patientId) {
        List<AppointmentRequest> list = new ArrayList<>();
        String sql = "SELECT ar.*, u.username as patient_username FROM appointment_requests ar " +
                "JOIN users u ON ar.patient_id = u.id " +
                "WHERE ar.patient_id = ? ORDER BY ar.created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRequest(rs));
            }
        } catch (SQLException e) {
            System.err.println("GetRequestsByPatient error: " + e.getMessage());
        }
        return list;
    }

    public boolean updateStatus(int requestId, String status) {
        String sql = "UPDATE appointment_requests SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, requestId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("UpdateStatus error: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePendingRequest(int requestId, int patientId, int docteurId, LocalDateTime appointmentDate) {
        String sql = "UPDATE appointment_requests SET docteur_id = ?, appointment_date = ? " +
                "WHERE id = ? AND patient_id = ? AND status = 'PENDING'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, docteurId);
            ps.setTimestamp(2, Timestamp.valueOf(appointmentDate));
            ps.setInt(3, requestId);
            ps.setInt(4, patientId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UpdatePendingRequest error: " + e.getMessage());
            return false;
        }
    }

    public boolean deletePendingRequest(int requestId, int patientId) {
        String sql = "DELETE FROM appointment_requests WHERE id = ? AND patient_id = ? AND status = 'PENDING'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.setInt(2, patientId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DeletePendingRequest error: " + e.getMessage());
            return false;
        }
    }

    public boolean acceptAndSchedule(int requestId, int patientId, int docteurId, LocalDateTime dateTime) {
        Connection c = conn;
        try {
            c.setAutoCommit(false);
            // Update request status
            String sql1 = "UPDATE appointment_requests SET status = 'ACCEPTED', appointment_date = ? WHERE id = ?";
            PreparedStatement ps1 = c.prepareStatement(sql1);
            ps1.setTimestamp(1, Timestamp.valueOf(dateTime));
            ps1.setInt(2, requestId);
            ps1.executeUpdate();

            // Create appointment
            String sql2 = "INSERT INTO appointments (patient_id, docteur_id, date_time, status) VALUES (?, ?, ?, 'Confirmé')";
            PreparedStatement ps2 = c.prepareStatement(sql2);
            ps2.setInt(1, patientId);
            ps2.setInt(2, docteurId);
            ps2.setTimestamp(3, Timestamp.valueOf(dateTime));
            ps2.executeUpdate();

            c.commit();
            return true;
        } catch (SQLException e) {
            try { c.rollback(); } catch (SQLException ex) {}
            System.err.println("AcceptAndSchedule error: " + e.getMessage());
            return false;
        } finally {
            try { c.setAutoCommit(true); } catch (SQLException e) {}
        }
    }

    private AppointmentRequest mapRequest(ResultSet rs) throws SQLException {
        LocalDateTime createdAt = rs.getTimestamp("created_at") != null ?
                rs.getTimestamp("created_at").toLocalDateTime() : null;
        Timestamp apptTs = rs.getTimestamp("appointment_date");
        LocalDateTime apptDate = apptTs != null ? apptTs.toLocalDateTime() : null;

        AppointmentRequest req = new AppointmentRequest(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getInt("docteur_id"),
                rs.getString("patient_username"),
                rs.getString("status"),
                createdAt,
                apptDate
        );
        return req;
    }
}
