package tn.esprit.aura.dao;

import tn.esprit.aura.entities.Mood;
import tn.esprit.aura.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access object for moods.
 * Migrated from the Aura AI-core module (DAO.MoodDAO).
 * Now uses the unified DBConnection singleton.
 */
public class MoodDAO implements DataAccessObject<Mood> {

    private Connection getConn() {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public void add(Mood mood) {
        String sql = "INSERT INTO moods (user_id, label) VALUES (?, ?)";
        try (PreparedStatement stmt = getConn().prepareStatement(sql)) {
            stmt.setInt(1, mood.getUserId());
            stmt.setString(2, mood.getLabel());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("MoodDAO.add error: " + e.getMessage());
        }
    }

    @Override
    public List<Mood> getAll(int userId) {
        List<Mood> moods = new ArrayList<>();
        String sql = "SELECT * FROM moods WHERE user_id = ? ORDER BY detected_at DESC";
        try (PreparedStatement stmt = getConn().prepareStatement(sql)) {
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
            System.err.println("MoodDAO.getAll error: " + e.getMessage());
        }
        return moods;
    }

    @Override
    public void update(int moodId, String newLabel) {
        String sql = "UPDATE moods SET label = ? WHERE id = ?";
        try (PreparedStatement stmt = getConn().prepareStatement(sql)) {
            stmt.setString(1, newLabel);
            stmt.setInt(2, moodId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("MoodDAO.update error: " + e.getMessage());
        }
    }

    @Override
    public void delete(int moodId) {
        String sql = "DELETE FROM moods WHERE id = ?";
        try (PreparedStatement stmt = getConn().prepareStatement(sql)) {
            stmt.setInt(1, moodId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("MoodDAO.delete error: " + e.getMessage());
        }
    }

    /** Returns the most recent mood record for a user. */
    public Mood getLastMood(int userId) {
        String sql = "SELECT * FROM moods WHERE user_id = ? ORDER BY detected_at DESC LIMIT 1";
        try (PreparedStatement stmt = getConn().prepareStatement(sql)) {
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
            System.err.println("MoodDAO.getLastMood error: " + e.getMessage());
        }
        return null;
    }
}
