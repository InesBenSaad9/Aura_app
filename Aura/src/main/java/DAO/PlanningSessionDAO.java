package DAO;

import java.sql.*;

public class PlanningSessionDAO {

    public void save(int userId, String mood, String plan) {
        String sql = "INSERT INTO planning_sessions (user_id, generated_plan) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, plan);
            stmt.executeUpdate();
            System.out.println("✅ Planning sauvegardé !");

        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}