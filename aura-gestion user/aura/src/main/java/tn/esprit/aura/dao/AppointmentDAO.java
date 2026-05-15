package tn.esprit.aura.dao;

import tn.esprit.aura.entities.Appointment;
import tn.esprit.aura.utils.DBConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    public List<Appointment> getAppointmentsByPatient(int patientId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, d.nom as docteur_name, d.specialite " +
                "FROM appointments a JOIN docteurs d ON a.docteur_id = d.id " +
                "WHERE a.patient_id = ? ORDER BY a.date_time DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAppointment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("GetAppointmentsByPatient error: " + e.getMessage());
        }
        return list;
    }

    public List<Appointment> getAppointmentsByDocteur(int docteurId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, d.nom as docteur_name, d.specialite " +
                "FROM appointments a JOIN docteurs d ON a.docteur_id = d.id " +
                "WHERE a.docteur_id = ? ORDER BY a.date_time DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, docteurId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAppointment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("GetAppointmentsByDocteur error: " + e.getMessage());
        }
        return list;
    }

    public boolean cancelAppointment(int appointmentId) {
        String sql = "UPDATE appointments SET status = 'Annulé' WHERE id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("CancelAppointment error: " + e.getMessage());
            return false;
        }
    }

    private Appointment mapAppointment(ResultSet rs) throws SQLException {
        LocalDateTime dt = rs.getTimestamp("date_time") != null ?
                rs.getTimestamp("date_time").toLocalDateTime() : null;
        return new Appointment(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getInt("docteur_id"),
                rs.getString("docteur_name"),
                rs.getString("specialite"),
                dt,
                rs.getString("status")
        );
    }
}
