package tn.esprit.aura.dao;

import tn.esprit.aura.entities.Event;
import tn.esprit.aura.utils.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventDAO implements GenericDAO<Event, Integer>, EventSpecificDAO {

    private final DBConnection dbConnection;
    private final Connection connection;
    private final String recommendedMoodColumn;
    private final String imageUrlColumn;
    private final String organizerColumn;
    private final String priceColumn;
    private final String paymentMethodColumn;
    private final String discountPercentColumn;
    private final String participationEventColumn;
    private final String participationUserColumn;
    private final String participationStatusColumn;
    private final String participationRoleColumn;
    private final String participationDateColumn;
    private boolean autoCommit = true;

    public record ParticipationRow(
            int userId,
            String status,
            String role,
            String registrationDate
    ) {
    }

    public record FeedbackRow(
            int userId,
            int rating,
            String comment,
            String createdAt
    ) {
    }

    public record OrganizerRow(
            int idOrganizer,
            String fullName,
            String email,
            String phone,
            String bio,
            boolean active
    ) {
    }

    public EventDAO() {
        this.dbConnection = DBConnection.getInstance();
        this.connection = dbConnection.getConnection();
        ensureOptionalColumns();
        this.recommendedMoodColumn = findColumnName("event", "recommendedMood", "recommended_mood");
        this.imageUrlColumn = findColumnName("event", "imageUrl", "imageUrl");
        this.organizerColumn = findColumnName("event", "idOrganiser", "idOrganizer");
        this.priceColumn = findColumnName("event", "price", "prix", "eventPrice");
        this.paymentMethodColumn = findColumnName("event", "paymentMethod", "payment_method", "paiement");
        this.discountPercentColumn = findColumnName("event", "discountPercent", "discount_percent", "reduction");
        this.participationEventColumn = findColumnName("participation", "idEvent", "eventId", "id_event", "event_id");
        this.participationUserColumn = findColumnName("participation", "idUser", "userId", "id_user", "user_id");
        this.participationStatusColumn = findColumnName("participation", "status", "participationStatus", "etat", "state");
        this.participationRoleColumn = findColumnName("participation", "participationRole", "role", "userRole");
        this.participationDateColumn = findColumnName("participation", "registrationDate", "registeredAt", "createdAt", "dateParticipation");
    }

    @Override
    public Event insert(Event event) {
        List<String> columns = new ArrayList<>(List.of("title", "description", "eventDate", "eventTime", "location", "capacity"));
        if (organizerColumn != null) columns.add(organizerColumn);
        if (recommendedMoodColumn != null) columns.add(recommendedMoodColumn);
        if (imageUrlColumn != null) columns.add(imageUrlColumn);
        if (priceColumn != null) columns.add(priceColumn);
        if (paymentMethodColumn != null) columns.add(paymentMethodColumn);
        if (discountPercentColumn != null) columns.add(discountPercentColumn);

        String placeholders = String.join(", ", columns.stream().map(c -> "?").toList());
        String sql = "INSERT INTO event (" + String.join(", ", columns) + ") VALUES (" + placeholders + ")";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int index = 1;
            ps.setString(index++, event.getTitle());
            ps.setString(index++, event.getDescription());
            ps.setDate(index++, Date.valueOf(event.getEventDate()));
            ps.setString(index++, event.getEventTime());
            ps.setString(index++, event.getLocation());
            ps.setInt(index++, event.getMaxParticipants());
            if (organizerColumn != null) ps.setInt(index++, event.getCreatedBy() == 0 ? 1 : event.getCreatedBy());
            if (recommendedMoodColumn != null) ps.setString(index++, normalizeMood(event.getRecommendedMood()));
            if (imageUrlColumn != null) ps.setString(index++, event.getImageUrl());
            if (priceColumn != null) ps.setDouble(index++, event.getPrice());
            if (paymentMethodColumn != null) ps.setString(index++, normalizePaymentMethod(event));
            if (discountPercentColumn != null) ps.setInt(index, event.getDiscountPercent());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) event.setIdEvent(keys.getInt(1));
                }
            }
            return event;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur insertion événement: " + e.getMessage(), e);
        }
    }

    @Override
    public Event save(Event event) {
        return (event.getIdEvent() == 0 || !exists(event.getIdEvent())) ? insert(event) : update(event);
    }

    @Override
    public Optional<Event> findById(Integer id) {
        String sql = "SELECT * FROM event WHERE idEvent = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findById: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Event> findAll() {
        return findAll(1000, 0);
    }

    @Override
    public List<Event> findAll(int limit, int offset) {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM event ORDER BY eventDate ASC, eventTime ASC LIMIT ? OFFSET ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAll: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean exists(Integer id) {
        String sql = "SELECT 1 FROM event WHERE idEvent = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur exists: " + e.getMessage(), e);
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM event";
        try (Statement s = connection.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur count: " + e.getMessage(), e);
        }
    }

    @Override
    public Event update(Event event) {
        List<String> assignments = new ArrayList<>(List.of(
                "title=?", "description=?", "eventDate=?", "eventTime=?", "location=?", "capacity=?"
        ));
        if (organizerColumn != null) assignments.add(organizerColumn + "=?");
        if (recommendedMoodColumn != null) assignments.add(recommendedMoodColumn + "=?");
        if (imageUrlColumn != null) assignments.add(imageUrlColumn + "=?");
        if (priceColumn != null) assignments.add(priceColumn + "=?");
        if (paymentMethodColumn != null) assignments.add(paymentMethodColumn + "=?");
        if (discountPercentColumn != null) assignments.add(discountPercentColumn + "=?");
        String sql = "UPDATE event SET " + String.join(", ", assignments) + " WHERE idEvent=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int index = 1;
            ps.setString(index++, event.getTitle());
            ps.setString(index++, event.getDescription());
            ps.setDate(index++, Date.valueOf(event.getEventDate()));
            ps.setString(index++, event.getEventTime());
            ps.setString(index++, event.getLocation());
            ps.setInt(index++, event.getMaxParticipants());
            if (organizerColumn != null) ps.setInt(index++, event.getCreatedBy() == 0 ? 1 : event.getCreatedBy());
            if (recommendedMoodColumn != null) ps.setString(index++, normalizeMood(event.getRecommendedMood()));
            if (imageUrlColumn != null) ps.setString(index++, event.getImageUrl());
            if (priceColumn != null) ps.setDouble(index++, event.getPrice());
            if (paymentMethodColumn != null) ps.setString(index++, normalizePaymentMethod(event));
            if (discountPercentColumn != null) ps.setInt(index++, event.getDiscountPercent());
            ps.setInt(index, event.getIdEvent());
            ps.executeUpdate();
            return event;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur update: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        // Supprimer les participations liées d'abord
        try {
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM participation WHERE idEvent = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        } catch (SQLException ignored) {}
        try {
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM event_feedback WHERE idEvent = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        } catch (SQLException ignored) {}

        String sql = "DELETE FROM event WHERE idEvent = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur deleteById: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(Event event) { return deleteById(event.getIdEvent()); }

    @Override
    public int deleteAll() {
        try (Statement s = connection.createStatement()) {
            return s.executeUpdate("DELETE FROM event");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur deleteAll: " + e.getMessage(), e);
        }
    }

    @Override
    public void beginTransaction() {
        try { autoCommit = connection.getAutoCommit(); connection.setAutoCommit(false); }
        catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void commitTransaction() {
        try { connection.commit(); connection.setAutoCommit(autoCommit); }
        catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void rollbackTransaction() {
        try { connection.rollback(); connection.setAutoCommit(autoCommit); }
        catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void close() { dbConnection.closeConnection(); }

    // ─── EventSpecificDAO ─────────────────────────────────────────────────────

    @Override
    public List<Event> findByMood(String mood) {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM event ORDER BY eventDate ASC";
        try (Statement s = connection.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                Event e = mapRow(rs);
                if (mood == null || mood.equalsIgnoreCase(e.getRecommendedMood())) list.add(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public List<Event> findByType(String type) { return findAll(); }

    @Override
    public List<Event> findByCreator(int userId) {
        List<Event> list = new ArrayList<>();
        if (organizerColumn == null) return list;
        String sql = "SELECT * FROM event WHERE " + organizerColumn + " = ? ORDER BY eventDate ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public List<Event> findByDateRange(LocalDate start, LocalDate end) {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM event WHERE eventDate BETWEEN ? AND ? ORDER BY eventDate ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public List<Event> findUpcomingEvents() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM event WHERE eventDate >= CURDATE() ORDER BY eventDate ASC";
        try (Statement s = connection.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public List<Event> findPastEvents() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM event WHERE eventDate < CURDATE() ORDER BY eventDate DESC";
        try (Statement s = connection.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public List<Event> findByCompatibleMood(String userMood) { return findUpcomingEvents(); }

    @Override
    public List<Event> findEventsWithAvailableSlots() {
        List<Event> list = new ArrayList<>();
        String eventColumn = participationEventColumn == null ? "idEvent" : participationEventColumn;
        String statusColumn = participationStatusColumn == null ? "status" : participationStatusColumn;
        String sql = "SELECT e.* FROM event e " +
                "LEFT JOIN (SELECT " + eventColumn + " AS idEvent, COUNT(*) as cnt FROM participation " +
                "WHERE " + acceptedStatusCondition(statusColumn) + " GROUP BY " + eventColumn + ") p ON e.idEvent = p.idEvent " +
                "WHERE COALESCE(p.cnt,0) < e.capacity AND e.eventDate >= CURDATE() " +
                "ORDER BY e.eventDate ASC";
        try (Statement s = connection.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public boolean addParticipant(int eventId, int userId) {
        if (!canJoin(eventId)) return false;
        if (participationExists(eventId, userId)) {
            return updateParticipantStatus(eventId, userId, "accepted");
        }
        String eventColumn = participationColumn(participationEventColumn, "idEvent");
        String userColumn = participationColumn(participationUserColumn, "idUser");
        String dateColumn = participationColumn(participationDateColumn, "registrationDate");
        String statusColumn = participationColumn(participationStatusColumn, "status");
        String roleColumn = participationColumn(participationRoleColumn, "participationRole");
        String sql = "INSERT INTO participation (" + eventColumn + ", " + userColumn + ", " + dateColumn + ", " + statusColumn + ", " + roleColumn + ") VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            setRegistrationDate(ps, 3);
            ps.setString(4, participationStatusValue("accepted"));
            ps.setString(5, participationRoleValue());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean removeParticipant(int eventId, int userId) {
        String eventColumn = participationColumn(participationEventColumn, "idEvent");
        String userColumn = participationColumn(participationUserColumn, "idUser");
        String sql = "DELETE FROM participation WHERE " + eventColumn + "=? AND " + userColumn + "=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eventId); ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Integer> getParticipants(int eventId) {
        return getParticipationRows(eventId).stream()
                .filter(row -> isAcceptedStatusValue(row.status()))
                .map(ParticipationRow::userId)
                .toList();
    }

    @Override
    public int getCurrentParticipantCount(int eventId) {
        return getParticipants(eventId).size();
    }

    @Override
    public boolean canJoin(int eventId) {
        Optional<Event> opt = findById(eventId);
        if (opt.isEmpty()) return false;
        Event ev = opt.get();
        return getCurrentParticipantCount(eventId) < ev.getMaxParticipants()
                && !ev.getEventDate().isBefore(LocalDate.now());
    }

    public List<ParticipationRow> getParticipationRows(int eventId) {
        List<ParticipationRow> list = new ArrayList<>();
        String eventColumn = participationColumn(participationEventColumn, "idEvent");
        String userColumn = participationColumn(participationUserColumn, "idUser");
        String statusColumn = participationColumn(participationStatusColumn, "status");
        String roleSelect = participationRoleColumn == null ? "''" : participationRoleColumn;
        String dateSelect = participationDateColumn == null ? "''" : participationDateColumn;
        String orderBy = participationDateColumn == null ? userColumn + " ASC" : participationDateColumn + " DESC, " + userColumn + " ASC";
        String sql = "SELECT " + userColumn + " AS participantUserId, "
                + statusColumn + " AS participantStatus, "
                + roleSelect + " AS participantRole, "
                + dateSelect + " AS participantDate "
                + "FROM participation WHERE " + eventColumn + "=? ORDER BY " + orderBy;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ParticipationRow(
                        rs.getInt("participantUserId"),
                        rs.getString("participantStatus"),
                        rs.getString("participantRole"),
                        rs.getString("participantDate")
                ));
            }
        } catch (SQLException e) {
            // Participation table might not exist yet.
        }
        return list;
    }

    public boolean updateParticipantStatus(int eventId, int userId, String status) {
        String eventColumn = participationColumn(participationEventColumn, "idEvent");
        String userColumn = participationColumn(participationUserColumn, "idUser");
        String statusColumn = participationColumn(participationStatusColumn, "status");
        String sql = "UPDATE participation SET " + statusColumn + "=? WHERE " + eventColumn + "=? AND " + userColumn + "=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, participationStatusValue(status));
            ps.setInt(2, eventId);
            ps.setInt(3, userId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                return true;
            }
            throw new RuntimeException("Aucune participation trouvee pour event=" + eventId + " et user=" + userId + ".");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur mise a jour participant: " + e.getMessage(), e);
        }
    }

    public Optional<ParticipationRow> findParticipationRow(int eventId, int userId) {
        return getParticipationRows(eventId).stream()
                .filter(row -> row.userId() == userId)
                .findFirst();
    }

    public boolean participationExists(int eventId, int userId) {
        String eventColumn = participationColumn(participationEventColumn, "idEvent");
        String userColumn = participationColumn(participationUserColumn, "idUser");
        String sql = "SELECT 1 FROM participation WHERE " + eventColumn + "=? AND " + userColumn + "=? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur verification participation: " + e.getMessage(), e);
        }
    }

    public boolean saveFeedback(int eventId, int userId, int rating, String comment) {
        String updateSql = "UPDATE event_feedback SET rating=?, comment=?, createdAt=CURRENT_TIMESTAMP "
                + "WHERE idEvent=? AND idUser=?";
        try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
            ps.setInt(1, rating);
            ps.setString(2, comment);
            ps.setInt(3, eventId);
            ps.setInt(4, userId);
            if (ps.executeUpdate() > 0) {
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur mise a jour feedback: " + e.getMessage(), e);
        }

        String insertSql = "INSERT INTO event_feedback (idEvent, idUser, rating, comment, createdAt) "
                + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            ps.setInt(3, rating);
            ps.setString(4, comment);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur ajout feedback: " + e.getMessage(), e);
        }
    }

    public List<FeedbackRow> getFeedbackRows(int eventId) {
        List<FeedbackRow> list = new ArrayList<>();
        String sql = "SELECT idUser, rating, comment, createdAt FROM event_feedback "
                + "WHERE idEvent=? ORDER BY createdAt DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new FeedbackRow(
                        rs.getInt("idUser"),
                        rs.getInt("rating"),
                        rs.getString("comment"),
                        rs.getString("createdAt")
                ));
            }
        } catch (SQLException e) {
            // Feedback table might not exist yet.
        }
        return list;
    }

    public Optional<OrganizerRow> findOrganizerById(int organizerId) {
        String sql = "SELECT idOrganizer, fullName, email, phone, bio, isActive "
                + "FROM event_organizer WHERE idOrganizer=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, organizerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new OrganizerRow(
                        rs.getInt("idOrganizer"),
                        rs.getString("fullName"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("bio"),
                        rs.getBoolean("isActive")
                ));
            }
        } catch (SQLException ignored) {
            // Organizer table might not exist in older databases.
        }
        return Optional.empty();
    }

    @Override
    public int countEventsByCreator(int userId) { return findByCreator(userId).size(); }

    @Override
    public int countEventsByType(String type) { return 0; }

    // ─── Mapping ──────────────────────────────────────────────────────────────
    private Event mapRow(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setIdEvent(rs.getInt("idEvent"));
        e.setTitle(rs.getString("title"));
        e.setDescription(rs.getString("description"));
        Date d = rs.getDate("eventDate");
        if (d != null) e.setEventDate(d.toLocalDate());
        e.setEventTime(rs.getString("eventTime"));
        e.setLocation(rs.getString("location"));
        e.setMaxParticipants(rs.getInt("capacity"));
        if (organizerColumn != null && hasColumn(rs, organizerColumn)) {
            e.setCreatedBy(rs.getInt(organizerColumn));
        }
        if (recommendedMoodColumn != null && hasColumn(rs, recommendedMoodColumn)) {
            e.setRecommendedMood(normalizeMood(rs.getString(recommendedMoodColumn)));
        }
        if (imageUrlColumn != null && hasColumn(rs, imageUrlColumn)) {
            e.setImageUrl(rs.getString(imageUrlColumn));
        }
        if (priceColumn != null && hasColumn(rs, priceColumn)) {
            e.setPrice(rs.getDouble(priceColumn));
        }
        if (paymentMethodColumn != null && hasColumn(rs, paymentMethodColumn)) {
            e.setPaymentMethod(rs.getString(paymentMethodColumn));
        }
        if (discountPercentColumn != null && hasColumn(rs, discountPercentColumn)) {
            e.setDiscountPercent(rs.getInt(discountPercentColumn));
        }
        e.setParticipants(getParticipants(e.getIdEvent()));
        return e;
    }

    private void ensureOptionalColumns() {
        try (Statement statement = connection.createStatement()) {
            // Create event table if not exists
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS event (
                        idEvent INT AUTO_INCREMENT PRIMARY KEY,
                        title VARCHAR(255) NOT NULL,
                        description TEXT,
                        eventDate DATE NOT NULL,
                        eventTime VARCHAR(50),
                        location VARCHAR(255),
                        capacity INT NOT NULL DEFAULT 10,
                        idOrganizer INT DEFAULT 1,
                        recommendedMood VARCHAR(50) DEFAULT 'neutre',
                        imageUrl VARCHAR(1024),
                        price DECIMAL(10,2) DEFAULT 0,
                        paymentMethod VARCHAR(50) DEFAULT 'Gratuit',
                        discountPercent INT DEFAULT 0
                    )
                    """);

            // Create participation table if not exists
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS participation (
                        idEvent INT NOT NULL,
                        idUser INT NOT NULL,
                        registrationDate DATETIME DEFAULT CURRENT_TIMESTAMP,
                        status VARCHAR(50) DEFAULT 'accepted',
                        participationRole VARCHAR(50) DEFAULT 'participant',
                        PRIMARY KEY (idEvent, idUser),
                        FOREIGN KEY (idEvent) REFERENCES event(idEvent) ON DELETE CASCADE,
                        FOREIGN KEY (idUser) REFERENCES utilisateurs(id) ON DELETE CASCADE
                    )
                    """);
        } catch (SQLException e) {
            System.err.println("Database initialization warning (tables): " + e.getMessage());
        }

        try (Statement statement = connection.createStatement()) {
            // Ensure all columns exist (for migration)
            String[] alters = {
                    "ALTER TABLE event ADD COLUMN IF NOT EXISTS recommendedMood VARCHAR(50) DEFAULT 'neutre'",
                    "ALTER TABLE event ADD COLUMN IF NOT EXISTS imageUrl VARCHAR(1024) NULL",
                    "ALTER TABLE event ADD COLUMN IF NOT EXISTS price DECIMAL(10,2) NOT NULL DEFAULT 0",
                    "ALTER TABLE event ADD COLUMN IF NOT EXISTS paymentMethod VARCHAR(50) NOT NULL DEFAULT 'Gratuit'",
                    "ALTER TABLE event ADD COLUMN IF NOT EXISTS discountPercent INT NOT NULL DEFAULT 0",
                    "ALTER TABLE event ADD COLUMN IF NOT EXISTS idOrganizer INT DEFAULT 1"
            };
            for (String sql : alters) {
                try { statement.executeUpdate(sql); } catch (SQLException ignored) {}
            }
        } catch (SQLException ignored) {}

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS event_feedback (
                        idFeedback INT AUTO_INCREMENT PRIMARY KEY,
                        idEvent INT NOT NULL,
                        idUser INT NOT NULL,
                        rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
                        comment TEXT,
                        createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (idEvent) REFERENCES event(idEvent) ON DELETE CASCADE
                    )
                    """);
        } catch (SQLException ignored) {
            // Current database user might not be allowed to create tables.
        }
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS event_organizer (
                        idOrganizer INT AUTO_INCREMENT PRIMARY KEY,
                        fullName VARCHAR(100) NOT NULL,
                        email VARCHAR(150) NOT NULL UNIQUE,
                        password VARCHAR(255) NOT NULL,
                        phone VARCHAR(20),
                        bio TEXT,
                        createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
                        isActive BOOLEAN DEFAULT TRUE
                    )
                    """);
        } catch (SQLException ignored) {
            // Current database user might not be allowed to create tables.
        }
    }

    private boolean hasColumn(String tableName, String columnName) {
        try (ResultSet rs = connection.getMetaData().getColumns(null, null, tableName, columnName)) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private String findColumnName(String tableName, String... columnNames) {
        for (String columnName : columnNames) {
            if (hasColumn(tableName, columnName)) {
                return columnName;
            }
        }
        return null;
    }

    private String acceptedStatusCondition(String columnName) {
        return "LOWER(COALESCE(" + columnName + ",'accepted')) IN "
                + "('accepted','accept','accepte','confirm','confirmed','confirme','approved','approve','1')";
    }

    private boolean isAcceptedStatusValue(String status) {
        String normalized = normalizeEnumText(status);
        if (isNegativeConfirmationStatus(normalized)) {
            return false;
        }
        return normalized.contains("accept")
                || normalized.contains("confirm")
                || normalized.contains("valid")
                || normalized.contains("approv")
                || "1".equals(normalized);
    }

    private String participationStatusValue(String status) {
        String statusColumn = participationColumn(participationStatusColumn, "status");
        List<String> allowed = getEnumValues("participation", statusColumn);
        String[] preferred = switch (normalizeStatusIntent(status)) {
            case "rejected" -> new String[]{"non confirmé", "non confirme", "not confirmed", "unconfirmed", "rejected", "refused", "refuse", "refusé", "refusée", "rejete", "rejeté", "rejetée", "declined", "denied", "cancelled", "canceled", "annule", "annulé", "annulée"};
            case "pending" -> new String[]{"pending", "en_attente", "en attente", "waiting", "requested", "attente"};
            default -> new String[]{"accepted", "accept", "accepte", "accepté", "acceptée", "confirmed", "confirm", "confirme", "confirmé", "confirmée", "approved", "approve", "approuvé", "validated", "valide", "validé", "validée"};
        };

        if (allowed.isEmpty()) {
            return preferred[0];
        }
        for (String candidate : preferred) {
            for (String value : allowed) {
                if (normalizeEnumText(candidate).equals(normalizeEnumText(value))) {
                    return value;
                }
            }
        }
        return bestFallbackStatus(allowed, normalizeStatusIntent(status));
    }

    private String normalizeStatusIntent(String status) {
        if (status == null) {
            return "accepted";
        }
        String normalized = normalizeEnumText(status);
        if (normalized.contains("reject") || normalized.contains("refus") || normalized.contains("rejet")
                || normalized.contains("declin") || normalized.contains("denied")
                || normalized.contains("cancel") || normalized.contains("annul")) {
            return "rejected";
        }
        if (normalized.contains("pend") || normalized.contains("attente") || normalized.contains("wait") || normalized.contains("request")) {
            return "pending";
        }
        return "accepted";
    }

    private String bestFallbackStatus(List<String> allowed, String intent) {
        if (allowed.isEmpty()) {
            return "";
        }
        for (String value : allowed) {
            String normalized = normalizeEnumText(value);
            if ("rejected".equals(intent) && isNegativeConfirmationStatus(normalized)) {
                return value;
            }
            if ("accepted".equals(intent) && isNegativeConfirmationStatus(normalized)) {
                continue;
            }
            if ("accepted".equals(intent)
                    && (normalized.contains("accept") || normalized.contains("confirm")
                    || normalized.contains("approv") || normalized.contains("valid"))) {
                return value;
            }
            if ("rejected".equals(intent)
                    && (normalized.contains("reject") || normalized.contains("refus")
                    || normalized.contains("rejet") || normalized.contains("declin")
                    || normalized.contains("denied") || normalized.contains("cancel")
                    || normalized.contains("annul"))) {
                return value;
            }
            if ("pending".equals(intent)
                    && (normalized.contains("pending") || normalized.contains("attente") || normalized.contains("wait") || normalized.contains("request"))) {
                return value;
            }
        }
        return allowed.get(0);
    }

    private String normalizeEnumText(String value) {
        if (value == null) {
            return "";
        }
        return java.text.Normalizer.normalize(value.trim().toLowerCase(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    private boolean isNegativeConfirmationStatus(String normalized) {
        return normalized.contains("non confirme")
                || normalized.contains("not confirmed")
                || normalized.contains("unconfirmed");
    }

    private String participationRoleValue() {
        String roleColumn = participationColumn(participationRoleColumn, "participationRole");
        List<String> allowed = getEnumValues("participation", roleColumn);
        String[] preferred = {"participant", "attendee", "user", "member", "guest"};
        if (allowed.isEmpty()) {
            return preferred[0];
        }
        for (String candidate : preferred) {
            for (String value : allowed) {
                if (candidate.equalsIgnoreCase(value)) {
                    return value;
                }
            }
        }
        return allowed.get(0);
    }

    private String participationColumn(String detectedColumn, String fallbackColumn) {
        return detectedColumn == null ? fallbackColumn : detectedColumn;
    }

    private List<String> getEnumValues(String tableName, String columnName) {
        String sql = "SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return parseEnumValues(rs.getString("COLUMN_TYPE"));
            }
        } catch (SQLException ignored) {
        }
        return List.of();
    }

    private List<String> parseEnumValues(String columnType) {
        List<String> values = new ArrayList<>();
        if (columnType == null || !columnType.toLowerCase().startsWith("enum(")) {
            return values;
        }
        String content = columnType.substring(columnType.indexOf('(') + 1, columnType.lastIndexOf(')'));
        for (String raw : content.split(",")) {
            String value = raw.trim();
            if (value.length() >= 2 && value.startsWith("'") && value.endsWith("'")) {
                values.add(value.substring(1, value.length() - 1).replace("''", "'"));
            }
        }
        return values;
    }

    private void setRegistrationDate(PreparedStatement ps, int parameterIndex) throws SQLException {
        ColumnInfo columnInfo = getColumnInfo("participation", "registrationDate");
        LocalDate today = LocalDate.now();
        if (columnInfo == null) {
            ps.setDate(parameterIndex, Date.valueOf(today));
            return;
        }

        switch (columnInfo.sqlType()) {
            case Types.DATE -> ps.setDate(parameterIndex, Date.valueOf(today));
            case Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE -> ps.setTimestamp(parameterIndex, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            case Types.TIME, Types.TIME_WITH_TIMEZONE -> ps.setTime(parameterIndex, java.sql.Time.valueOf(LocalDateTime.now().toLocalTime()));
            case Types.TINYINT, Types.SMALLINT, Types.INTEGER, Types.BIGINT, Types.NUMERIC, Types.DECIMAL -> {
                int value = columnInfo.size() <= 4 ? today.getYear() : Integer.parseInt(today.format(DateTimeFormatter.BASIC_ISO_DATE));
                ps.setInt(parameterIndex, value);
            }
            default -> ps.setString(parameterIndex, today.toString());
        }
    }

    private ColumnInfo getColumnInfo(String tableName, String columnName) {
        try (ResultSet rs = connection.getMetaData().getColumns(null, null, tableName, columnName)) {
            if (rs.next()) {
                return new ColumnInfo(rs.getInt("DATA_TYPE"), rs.getInt("COLUMN_SIZE"));
            }
        } catch (SQLException ignored) {
        }
        return null;
    }

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            if (columnName.equalsIgnoreCase(metaData.getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }

    private String normalizeMood(String mood) {
        return mood == null || mood.isBlank() ? "neutre" : mood.trim().toLowerCase();
    }

    private String normalizePaymentMethod(Event event) {
        if (!event.isPaidEvent()) {
            return "Gratuit";
        }
        String paymentMethod = event.getPaymentMethod();
        return paymentMethod == null || paymentMethod.isBlank() ? "Sur place" : paymentMethod.trim();
    }

    private record ColumnInfo(int sqlType, int size) {
    }
}
