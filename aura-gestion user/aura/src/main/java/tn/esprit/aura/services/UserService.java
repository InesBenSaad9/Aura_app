package tn.esprit.aura.services;

import tn.esprit.aura.entities.User;
import tn.esprit.aura.interfaces.IService;
import tn.esprit.aura.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<User> {
    private final Connection connection;

    public UserService() {
        this.connection = DBConnection.getInstance().getConnection();
    }

    @Override
    public void add(User user) throws SQLException {
        String sql = "INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, photo_profil, telephone, date_naissance, genre, ville, bio, role, face_data) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getMotDePasse());
            ps.setString(5, user.getPhotoProfil());
            ps.setString(6, user.getTelephone());
            if (user.getDateNaissance() != null) {
                ps.setDate(7, Date.valueOf(user.getDateNaissance()));
            } else {
                ps.setNull(7, Types.DATE);
            }
            ps.setString(8, user.getGenre());
            ps.setString(9, user.getVille());
            ps.setString(10, user.getBio());
            ps.setString(11, user.getRole() != null ? user.getRole() : "USER");
            ps.setString(12, user.getFaceData());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(User user) throws SQLException {
        String sql = "UPDATE utilisateurs SET nom = ?, prenom = ?, email = ?, mot_de_passe = ?, photo_profil = ?, telephone = ?, date_naissance = ?, genre = ?, ville = ?, bio = ?, role = ?, face_data = ?, active = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getMotDePasse());
            ps.setString(5, user.getPhotoProfil());
            ps.setString(6, user.getTelephone());
            if (user.getDateNaissance() != null) {
                ps.setDate(7, Date.valueOf(user.getDateNaissance()));
            } else {
                ps.setNull(7, Types.DATE);
            }
            ps.setString(8, user.getGenre());
            ps.setString(9, user.getVille());
            ps.setString(10, user.getBio());
            ps.setString(11, user.getRole());
            ps.setString(12, user.getFaceData());
            ps.setBoolean(13, user.isActive());
            ps.setInt(14, user.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM utilisateurs WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public User getById(int id) throws SQLException {
        String sql = "SELECT * FROM utilisateurs WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<User> getAll() throws SQLException {
        String sql = "SELECT * FROM utilisateurs ORDER BY id DESC";
        List<User> users = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    // ──── Authentication ────
    public User authenticate(String email, String password) throws SQLException {
        String sql = "SELECT * FROM utilisateurs WHERE email = ? AND mot_de_passe = ? AND active = 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    public User getByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM utilisateurs WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    public boolean emailExists(String email) throws SQLException {
        return getByEmail(email) != null;
    }

    public void updatePassword(String email, String newPassword) throws SQLException {
        String sql = "UPDATE utilisateurs SET mot_de_passe = ? WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, email);
            ps.executeUpdate();
        }
    }

    // ──── Statistics ────
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM utilisateurs";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countByRole(String role) throws SQLException {
        String sql = "SELECT COUNT(*) FROM utilisateurs WHERE role = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public int countActive() throws SQLException {
        String sql = "SELECT COUNT(*) FROM utilisateurs WHERE active = 1";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countNewToday() throws SQLException {
        String sql = "SELECT COUNT(*) FROM utilisateurs WHERE DATE(created_at) = CURDATE()";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        Date dateNaissanceSql = rs.getDate("date_naissance");
        return new User(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("email"),
                rs.getString("mot_de_passe"),
                rs.getString("photo_profil"),
                rs.getString("telephone"),
                dateNaissanceSql != null ? dateNaissanceSql.toLocalDate() : null,
                rs.getString("genre"),
                rs.getString("ville"),
                rs.getString("bio"),
                rs.getString("role"),
                rs.getString("face_data"),
                rs.getBoolean("active"),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("updated_at")
        );
    }
}
