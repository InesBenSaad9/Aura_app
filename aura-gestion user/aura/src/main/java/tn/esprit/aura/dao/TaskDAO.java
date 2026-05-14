package tn.esprit.aura.dao;

import tn.esprit.aura.entities.Task;
import tn.esprit.aura.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access object for tasks.
 * Migrated from the Aura AI-core module (DAO.TaskDAO).
 * Now uses the unified DBConnection singleton.
 */
public class TaskDAO implements DataAccessObject<Task> {

    private Connection getConn() {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public void add(Task task) {
        String sql = "INSERT INTO tasks (user_id, title, priority, status, scheduled_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = getConn().prepareStatement(sql)) {
            stmt.setInt(1, task.getUserId());
            stmt.setString(2, task.getTitle());
            stmt.setString(3, task.getPriority());
            stmt.setString(4, task.getStatus());
            stmt.setTimestamp(5, Timestamp.valueOf(task.getScheduledAt() + " 00:00:00"));
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("TaskDAO.add error: " + e.getMessage());
        }
    }

    @Override
    public List<Task> getAll(int userId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE user_id = ?";
        try (PreparedStatement stmt = getConn().prepareStatement(sql)) {
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
            System.err.println("TaskDAO.getAll error: " + e.getMessage());
        }
        return tasks;
    }

    @Override
    public void update(int taskId, String newStatus) {
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = getConn().prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, taskId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("TaskDAO.update error: " + e.getMessage());
        }
    }

    @Override
    public void delete(int taskId) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (PreparedStatement stmt = getConn().prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("TaskDAO.delete error: " + e.getMessage());
        }
    }

    /** Returns all tasks for a user on a given date (yyyy-MM-dd). */
    public List<Task> getByDate(int userId, String date) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE user_id = ? AND DATE(scheduled_at) = ?";
        try (PreparedStatement stmt = getConn().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, date);
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
            System.err.println("TaskDAO.getByDate error: " + e.getMessage());
        }
        return tasks;
    }
}
