package tn.esprit.aura.scratch;

import tn.esprit.aura.utils.DBConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.ResultSetMetaData;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            Statement stmt = conn.createStatement();
            
            // 1. Describe table
            System.out.println("--- PRESCRIPTIONS TABLE COLUMNS ---");
            ResultSet rs = stmt.executeQuery("DESCRIBE prescriptions");
            while(rs.next()) {
                System.out.println(rs.getString("Field") + " | " + rs.getString("Type") + " | Null: " + rs.getString("Null") + " | Default: " + rs.getString("Default"));
            }
            
            // 2. Check foreign keys
            System.out.println("\n--- FOREIGN KEYS ---");
            ResultSet rsFk = stmt.executeQuery(
                "SELECT TABLE_NAME, COLUMN_NAME, CONSTRAINT_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME " +
                "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                "WHERE REFERENCED_TABLE_SCHEMA = 'aura_db' AND TABLE_NAME = 'prescriptions'"
            );
            while(rsFk.next()) {
                System.out.println(rsFk.getString("COLUMN_NAME") + " -> " + rsFk.getString("REFERENCED_TABLE_NAME") + "." + rsFk.getString("REFERENCED_COLUMN_NAME"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
