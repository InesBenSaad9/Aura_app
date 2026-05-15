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

            // Medical Tables
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS docteurs (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        nom VARCHAR(255),
                        email VARCHAR(255) UNIQUE,
                        password VARCHAR(255),
                        specialite VARCHAR(255),
                        ville VARCHAR(255),
                        telephone VARCHAR(20),
                        rating DOUBLE DEFAULT 4.5
                    )
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS appointments (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        patient_id INT,
                        docteur_id INT,
                        date_time DATETIME,
                        status VARCHAR(50),
                        FOREIGN KEY (patient_id) REFERENCES utilisateurs(id),
                        FOREIGN KEY (docteur_id) REFERENCES docteurs(id)
                    )
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS appointment_requests (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        patient_id INT,
                        docteur_id INT,
                        status VARCHAR(50),
                        created_at DATETIME,
                        appointment_date DATETIME,
                        FOREIGN KEY (patient_id) REFERENCES utilisateurs(id),
                        FOREIGN KEY (docteur_id) REFERENCES docteurs(id)
                    )
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS prescriptions (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        id_user INT,
                        patient_id INT,
                        docteur_id INT,
                        medicament VARCHAR(255),
                        dosage VARCHAR(255),
                        instructions TEXT,
                        date_creation DATE,
                        FOREIGN KEY (patient_id) REFERENCES utilisateurs(id),
                        FOREIGN KEY (docteur_id) REFERENCES docteurs(id)
                    )
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS messages (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        sender_id INT,
                        sender_role VARCHAR(50),
                        receiver_id INT,
                        content TEXT,
                        sent_at DATETIME DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS therapy_sessions (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        patient_id INT,
                        doctor_id INT,
                        session_date DATETIME,
                        notes TEXT,
                        FOREIGN KEY (patient_id) REFERENCES utilisateurs(id),
                        FOREIGN KEY (doctor_id) REFERENCES docteurs(id)
                    )
                    """);

            System.out.println("All tables initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }
}
