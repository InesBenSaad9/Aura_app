package tn.esprit.aura.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBInitializer {

    public static void initialize() {
        Connection connection = DBConnection.getInstance().getConnection();
        if (connection == null) {
            System.err.println("No database connection available.");
            return;
        }

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS utilisateurs (
                        id INT NOT NULL AUTO_INCREMENT,
                        nom VARCHAR(100) NOT NULL,
                        prenom VARCHAR(100),
                        email VARCHAR(150) NOT NULL UNIQUE,
                        mot_de_passe VARCHAR(255) NOT NULL,
                        photo_profil VARCHAR(255),
                        telephone VARCHAR(30),
                        date_naissance DATE,
                        genre VARCHAR(20),
                        ville VARCHAR(100),
                        bio VARCHAR(255),
                        role VARCHAR(50) NOT NULL DEFAULT 'USER',
                        active TINYINT(1) NOT NULL DEFAULT 1,
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (id)
                    )
                    """);

            // Keep old databases in sync when table already exists.
            stmt.execute("ALTER TABLE utilisateurs ADD COLUMN IF NOT EXISTS prenom VARCHAR(100) AFTER nom");
            stmt.execute("ALTER TABLE utilisateurs ADD COLUMN IF NOT EXISTS photo_profil VARCHAR(255) AFTER mot_de_passe");
            stmt.execute("ALTER TABLE utilisateurs ADD COLUMN IF NOT EXISTS telephone VARCHAR(30) AFTER photo_profil");
            stmt.execute("ALTER TABLE utilisateurs ADD COLUMN IF NOT EXISTS date_naissance DATE AFTER telephone");
            stmt.execute("ALTER TABLE utilisateurs ADD COLUMN IF NOT EXISTS genre VARCHAR(20) AFTER date_naissance");
            stmt.execute("ALTER TABLE utilisateurs ADD COLUMN IF NOT EXISTS ville VARCHAR(100) AFTER genre");
            stmt.execute("ALTER TABLE utilisateurs ADD COLUMN IF NOT EXISTS bio VARCHAR(255) AFTER ville");
            stmt.execute("ALTER TABLE utilisateurs ADD COLUMN IF NOT EXISTS role VARCHAR(50) NOT NULL DEFAULT 'USER' AFTER bio");
            stmt.execute("ALTER TABLE utilisateurs ADD COLUMN IF NOT EXISTS face_data TEXT AFTER role");
            stmt.execute("ALTER TABLE utilisateurs ADD COLUMN IF NOT EXISTS active TINYINT(1) NOT NULL DEFAULT 1 AFTER face_data");
            stmt.execute("ALTER TABLE utilisateurs ADD COLUMN IF NOT EXISTS created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
            stmt.execute("ALTER TABLE utilisateurs ADD COLUMN IF NOT EXISTS updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");

            // Ensure column width for existing databases (older schema used VARCHAR(20)).
            try {
                stmt.execute("ALTER TABLE utilisateurs MODIFY role VARCHAR(50) NOT NULL DEFAULT 'USER'");
            } catch (SQLException ignored) {
                // Some MySQL versions/permissions may reject MODIFY; code-level normalization still prevents truncation.
            }

            System.out.println("Table utilisateurs initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Erreur initializing utilisateurs table: " + e.getMessage());
        }
    }
}
