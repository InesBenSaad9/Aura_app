package DAO;

import Interface.DataAccessObject;
import Model.Mood;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MoodDAO implements DataAccessObject<Mood> {

    @Override
    public void add(Mood mood) {
        String sql = "INSERT INTO moods (user_id, label) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mood.getUserId());
            stmt.setString(2, mood.getLabel());
            stmt.executeUpdate();
            System.out.println("Humeur ajoutée : " + mood.getLabel());

        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    @Override
    public List<Mood> getAll(int userId) {
        List<Mood> moods = new ArrayList<>();
        String sql = "SELECT * FROM moods WHERE user_id = ? ORDER BY detected_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                moods.add(new Mood(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("label"),
                        rs.getString("detected_at")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
        return moods;
    }

    @Override
    public void update(int moodId, String newLabel) {
        String sql = "UPDATE moods SET label = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newLabel);
            stmt.setInt(2, moodId);
            stmt.executeUpdate();
            System.out.println("Humeur mise à jour !");

        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    @Override
    public void delete(int moodId) {
        String sql = "DELETE FROM moods WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, moodId);
            stmt.executeUpdate();
            System.out.println("Humeur supprimée !");

        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    // Méthode spéciale — récupère la dernière humeur
    public Mood getLastMood(int userId) {
        String sql = "SELECT * FROM moods WHERE user_id = ? ORDER BY detected_at DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Mood(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("label"),
                        rs.getString("detected_at")
                );
            }
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
        return null;
    }
}