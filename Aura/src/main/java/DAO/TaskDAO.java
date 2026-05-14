package DAO;

import Interface.DataAccessObject;
import Model.Task;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO implements DataAccessObject<Task> {

    @Override
    public void add(Task task) {
        String sql = "INSERT INTO tasks (user_id, title, priority, status, scheduled_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, task.getUserId());
            stmt.setString(2, task.getTitle());
            stmt.setString(3, task.getPriority());
            stmt.setString(4, task.getStatus());
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(task.getScheduledAt() + " 00:00:00"));
            stmt.executeUpdate();
            System.out.println("Tâche ajoutée !");

        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    @Override
    public List<Task> getAll(int userId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(new Task(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("priority"),
                        rs.getString("status"),
                        rs.getString("scheduled_at")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
        return tasks;
    }

    @Override
    public void update(int taskId, String newStatus) {
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, taskId);
            stmt.executeUpdate();
            System.out.println("Tâche mise à jour !");

        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    @Override
    public void delete(int taskId) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            stmt.executeUpdate();
            System.out.println("Tâche supprimée !");

        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}