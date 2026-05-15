package tn.esprit.aura.dao;

import tn.esprit.aura.entities.Prescription;
import tn.esprit.aura.utils.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDAO {


    public boolean createPrescription(int patientId, int docteurId, String medicament, String dosage, String instructions) {
        String sql = "INSERT INTO prescriptions (patient_id, docteur_id, medicament, nom_med, dosage, instructions, date_creation, date_debut, date_fin, id_user) " +
                "VALUES (?, ?, ?, ?, ?, ?, CURDATE(), CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setInt(2, docteurId);
            ps.setString(3, medicament);
            ps.setString(4, medicament); // nom_med is required
            ps.setString(5, dosage);
            ps.setString(6, instructions);
            ps.setInt(7, patientId); // id_user
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("CreatePrescription error: " + e.getMessage());
            return false;
        }
    }

    public List<Prescription> getPrescriptionsByPatient(int patientId) {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT p.*, u.nom as patient_username, d.nom as docteur_username " +
                "FROM prescriptions p " +
                "JOIN utilisateurs u ON p.patient_id = u.id " +
                "JOIN docteurs d ON p.docteur_id = d.id " +
                "WHERE p.patient_id = ? ORDER BY p.date_creation DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPrescription(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("GetPrescriptionsByPatient error: " + e.getMessage());
        }
        return list;
    }

    public List<Prescription> getPrescriptionsByDocteur(int docteurId) {
        List<Prescription> list = new ArrayList<>();
        String sql = "SELECT p.*, u.nom as patient_username, d.nom as docteur_username " +
                "FROM prescriptions p " +
                "JOIN utilisateurs u ON p.patient_id = u.id " +
                "JOIN docteurs d ON p.docteur_id = d.id " +
                "WHERE p.docteur_id = ? ORDER BY p.date_creation DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, docteurId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPrescription(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("GetPrescriptionsByDocteur error: " + e.getMessage());
        }
        return list;
    }

    public boolean updatePrescription(int prescriptionId, int docteurId, String medicament, String dosage, String instructions) {
        String sql = "UPDATE prescriptions SET medicament = ?, nom_med = ?, dosage = ?, instructions = ? WHERE id = ? AND docteur_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, medicament);
            ps.setString(2, medicament);
            ps.setString(3, dosage);
            ps.setString(4, instructions);
            ps.setInt(5, prescriptionId);
            ps.setInt(6, docteurId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UpdatePrescription error: " + e.getMessage());
            return false;
        }
    }

    public boolean deletePrescription(int prescriptionId, int docteurId) {
        String sql = "DELETE FROM prescriptions WHERE id = ? AND docteur_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, prescriptionId);
            ps.setInt(2, docteurId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DeletePrescription error: " + e.getMessage());
            return false;
        }
    }

    // API aliases expected by some controllers
    public List<Prescription> getPrescriptionsByDoctor(int doctorId) {
        return getPrescriptionsByDocteur(doctorId);
    }

    /**
     * Returns patient names (utilisateurs.nom) that have had prescriptions with a given doctor.
     * Used to populate the doctor prescription form combo box.
     */
    public List<String> getPatientNamesForDoctor(int doctorId) {
        List<String> names = new ArrayList<>();
        String sql = "SELECT DISTINCT u.nom " +
                "FROM prescriptions p " +
                "JOIN utilisateurs u ON p.patient_id = u.id " +
                "WHERE p.docteur_id = ? " +
                "ORDER BY u.nom ASC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString(1);
                    if (name != null) names.add(name);
                }
            }
        } catch (SQLException e) {
            System.err.println("GetPatientNamesForDoctor error: " + e.getMessage());
        }
        return names;
    }

    private Prescription mapPrescription(ResultSet rs) throws SQLException {
        LocalDate dateCreation = rs.getDate("date_creation") != null ?
                rs.getDate("date_creation").toLocalDate() : LocalDate.now();

        return new Prescription(
                rs.getInt("id"),
                rs.getInt("patient_id"),
                rs.getInt("docteur_id"),
                rs.getString("patient_username"),
                rs.getString("docteur_username"),
                rs.getString("medicament"),
                rs.getString("dosage"),
                rs.getString("instructions"),
                dateCreation
        );
    }
}
