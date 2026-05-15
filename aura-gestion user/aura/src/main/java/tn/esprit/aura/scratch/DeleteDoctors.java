package tn.esprit.aura.scratch;

import tn.esprit.aura.utils.DBConnection;
import java.sql.Connection;
import java.sql.Statement;

public class DeleteDoctors {
    public static void main(String[] args) {
        try {
            Connection conn = DBConnection.getInstance().getConnection();
            Statement st = conn.createStatement();
            
            // Delete the first 4 doctors by ID or just delete by names provided
            String sql = "DELETE FROM docteurs WHERE nom IN (" +
                         "'Dr. Sonia Ben Salah Modifiée', 'Dr. Karim Zied', 'Dr. Sonia Ben Salah', 'Dr. Nadia Chouchane', " +
                         "'Sonia Ben Salah Modifiée', 'Karim Zied', 'Sonia Ben Salah', 'Nadia Chouchane') " +
                         "LIMIT 4";
            
            int rows = st.executeUpdate(sql);
            System.out.println("Deleted " + rows + " doctors.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
