package tn.esprit.aura.dao;

import tn.esprit.aura.entities.AppointmentRequest;
import tn.esprit.aura.utils.DBConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentRequestDAO {

    public boolean createRequest(int patientId, int docteurId) {
        String sql = "INSERT INTO appointment_requests (patient_id, docteur_id, status, created_at) VALUES (?, ?, 'PENDING', NOW())";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
        return getRequestsByDoctor(docteurId);
    }

    public List<AppointmentRequest> getRequestsByDoctor(int docteurId) {
        List<AppointmentRequest> list = new ArrayList<>();
        String sql = "SELECT ar.*, IFNULL(u.nom, CONCAT('Patient #', ar.patient_id)) as patient_username " +
                "FROM appointment_requests ar " +
                "LEFT JOIN utilisateurs u ON ar.patient_id = u.id " +
                "WHERE ar.docteur_id = ? ORDER BY ar.created_at DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, docteurId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRequest(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("GetRequestsByDocteur error: " + e.getMessage());
        }
        return list;
    }

    public List<AppointmentRequest> getRequestsByPatient(int patientId) {
        List<AppointmentRequest> list = new ArrayList<>();
        String sql = "SELECT ar.*, COALESCE(u1.nom, u2.nom, 'Inconnu') as patient_username, d.nom as docteur_nom, d.specialite " +
                "FROM appointment_requests ar " +
                "LEFT JOIN users u1 ON ar.patient_id = u1.id " +
                "LEFT JOIN utilisateurs u2 ON ar.patient_id = u2.id " +
                "LEFT JOIN docteurs d ON ar.docteur_id = d.id " +
                "WHERE ar.patient_id = ? ORDER BY ar.created_at DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRequest(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("GetRequestsByPatient error: " + e.getMessage());
        }
        return list;
    }

    public boolean updateStatus(int requestId, String status) {
        String sql = "UPDATE appointment_requests SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.setInt(2, patientId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DeletePendingRequest error: " + e.getMessage());
            return false;
        }
    }

    public boolean acceptAndSchedule(int requestId, int patientId, int docteurId, LocalDateTime dateTime) {
        String sql1 = "UPDATE appointment_requests SET status = 'ACCEPTED', appointment_date = ? WHERE id = ?";
        String sql2 = "INSERT INTO appointments (patient_id, docteur_id, date_time, status) VALUES (?, ?, ?, 'Confirmé')";
        
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(sql1);
                 PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                
                ps1.setTimestamp(1, Timestamp.valueOf(dateTime));
                ps1.setInt(2, requestId);
                ps1.executeUpdate();

                ps2.setInt(1, patientId);
                ps2.setInt(2, docteurId);
                ps2.setTimestamp(3, Timestamp.valueOf(dateTime));
                ps2.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("AcceptAndSchedule error: " + e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("AcceptAndSchedule connection error: " + e.getMessage());
            return false;
        }
    }

    private AppointmentRequest mapRequest(ResultSet rs) throws SQLException {
        LocalDateTime createdAt = rs.getTimestamp("created_at") != null ?
                rs.getTimestamp("created_at").toLocalDateTime() : null;
        Timestamp appointmentTimestamp = rs.getTimestamp("appointment_date");
        LocalDateTime appointmentDate = appointmentTimestamp != null ? appointmentTimestamp.toLocalDateTime() : null;

        AppointmentRequest req = new AppointmentRequest(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getInt("docteur_id"),
                rs.getString("patient_username"),
                rs.getString("status"),
                createdAt,
                appointmentDate
        );
        
        // Fetch doctor info if available in result set
        try {
            req.setDocteurName(rs.getString("docteur_nom"));
            req.setSpecialite(rs.getString("specialite"));
        } catch (SQLException e) {
            // Optional columns not present in all queries (e.g. getRequestsByDocteur might not join with docteurs)
        }
        
        return req;
    }
}
