package org.example.dao;

import org.example.entities.Prescription;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDAO {
    private Connection conn = DatabaseConnection.getConnection();

    public boolean createPrescription(int patientId, int docteurId, String medicament, String dosage, String instructions) {
        String sql = "INSERT INTO prescriptions (id_user, patient_id, docteur_id, nom_med, medicament, dosage, dosage_simple, instructions, date_debut, date_fin, date_creation) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 MONTH), CURDATE())";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, patientId);
            ps.setInt(3, docteurId);
            ps.setString(4, medicament);
            ps.setString(5, medicament);
            ps.setString(6, dosage);
            ps.setString(7, dosage);
            ps.setString(8, instructions);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return createPrescriptionSimple(patientId, docteurId, medicament, dosage, instructions);
        }
    }

    private boolean createPrescriptionSimple(int patientId, int docteurId, String medicament, String dosage, String instructions) {
        String sql = "INSERT INTO prescriptions (patient_id, docteur_id, medicament, dosage, instructions, date_creation) " +
                "VALUES (?, ?, ?, ?, ?, CURDATE())";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, docteurId);
            ps.setString(3, medicament);
            ps.setString(4, dosage);
            ps.setString(5, instructions);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("CreatePrescription error: " + e.getMessage());
            return false;
        }
    }

    public List<Prescription> getPrescriptionsByPatient(int patientId) {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT p.*, u.username as patient_username, d.nom as docteur_username " +
                "FROM prescriptions p " +
                "JOIN users u ON p.id_user = u.id " +
                "JOIN docteurs d ON p.docteur_id = d.id " +
                "WHERE p.id_user = ? ORDER BY p.date_creation DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapPrescription(rs));
        } catch (SQLException e) {
            list = getPrescriptionsByPatientSimple(patientId);
        }
        return list;
    }

    private List<Prescription> getPrescriptionsByPatientSimple(int patientId) {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT p.*, u.username as patient_username, d.nom as docteur_username " +
                "FROM prescriptions p " +
                "JOIN users u ON p.patient_id = u.id " +
                "JOIN docteurs d ON p.docteur_id = d.id " +
                "WHERE p.patient_id = ? ORDER BY p.date_creation DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapPrescription(rs));
        } catch (SQLException e) {
            System.err.println("GetPrescriptionsByPatient error: " + e.getMessage());
        }
        return list;
    }

    public List<Prescription> getPrescriptionsByDocteur(int docteurId) {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT p.*, u.username as patient_username, d.nom as docteur_username " +
                "FROM prescriptions p " +
                "JOIN users u ON p.id_user = u.id " +
                "JOIN docteurs d ON p.docteur_id = d.id " +
                "WHERE p.docteur_id = ? ORDER BY p.date_creation DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, docteurId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapPrescription(rs));
        } catch (SQLException e) {
            list = getPrescriptionsByDocteurSimple(docteurId);
        }
        return list;
    }

    private List<Prescription> getPrescriptionsByDocteurSimple(int docteurId) {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT p.*, u.username as patient_username, d.nom as docteur_username " +
                "FROM prescriptions p " +
                "JOIN users u ON p.patient_id = u.id " +
                "JOIN docteurs d ON p.docteur_id = d.id " +
                "WHERE p.docteur_id = ? ORDER BY p.date_creation DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, docteurId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapPrescription(rs));
        } catch (SQLException e) {
            System.err.println("GetPrescriptionsByDocteur error: " + e.getMessage());
        }
        return list;
    }

    public boolean updatePrescription(int prescriptionId, int docteurId, String medicament, String dosage, String instructions) {
        String sql = "UPDATE prescriptions SET nom_med = ?, medicament = ?, dosage = ?, dosage_simple = ?, instructions = ? " +
                "WHERE id = ? AND docteur_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, medicament);
            ps.setString(2, medicament);
            ps.setString(3, dosage);
            ps.setString(4, dosage);
            ps.setString(5, instructions);
            ps.setInt(6, prescriptionId);
            ps.setInt(7, docteurId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return updatePrescriptionSimple(prescriptionId, docteurId, medicament, dosage, instructions);
        }
    }

    private boolean updatePrescriptionSimple(int prescriptionId, int docteurId, String medicament, String dosage, String instructions) {
        String sql = "UPDATE prescriptions SET medicament = ?, dosage = ?, instructions = ? WHERE id = ? AND docteur_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, medicament);
            ps.setString(2, dosage);
            ps.setString(3, instructions);
            ps.setInt(4, prescriptionId);
            ps.setInt(5, docteurId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UpdatePrescription error: " + e.getMessage());
            return false;
        }
    }

    public boolean deletePrescription(int prescriptionId, int docteurId) {
        String sql = "DELETE FROM prescriptions WHERE id = ? AND docteur_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, prescriptionId);
            ps.setInt(2, docteurId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DeletePrescription error: " + e.getMessage());
            return false;
        }
    }

    public List<String> getPatientNamesForDocteur(int docteurId) {
        List<String> names = new ArrayList<>();
        String sql = "SELECT DISTINCT u.username FROM appointment_requests ar " +
                "JOIN users u ON ar.patient_id = u.id " +
                "WHERE ar.docteur_id = ? AND ar.status = 'ACCEPTED'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, docteurId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) names.add(rs.getString("username"));
        } catch (SQLException e) {
            System.err.println("GetPatientNamesForDocteur error: " + e.getMessage());
        }
        return names;
    }

    private int getInt(ResultSet rs, String primaryColumn, String fallbackColumn) throws SQLException {
        try {
            return rs.getInt(primaryColumn);
        } catch (SQLException e) {
            return rs.getInt(fallbackColumn);
        }
    }

    private Prescription mapPrescription(ResultSet rs) throws SQLException {
        // Essaie d'abord la colonne "medicament", sinon "nom_med"
        String medicament;
        try { medicament = rs.getString("medicament"); }
        catch (SQLException e) { medicament = rs.getString("nom_med"); }
        if (medicament == null) medicament = rs.getString("nom_med");

        String dosage;
        try { dosage = rs.getString("dosage_simple"); }
        catch (SQLException e) { dosage = rs.getString("dosage"); }
        if (dosage == null) dosage = rs.getString("dosage");

        LocalDate dateCreation = rs.getDate("date_creation") != null ?
                rs.getDate("date_creation").toLocalDate() : LocalDate.now();

        return new Prescription(
                rs.getInt("id"),
                getInt(rs, "id_user", "patient_id"),
                rs.getInt("docteur_id"),
                rs.getString("patient_username"),
                rs.getString("docteur_username"),
                medicament,
                dosage,
                rs.getString("instructions"),
                dateCreation
        );
    }
}
