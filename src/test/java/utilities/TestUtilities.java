package utilities;

import com.app.database.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUtilities {
    public static void verifyRentalAgreementCount(int count) {
        assertEquals(count, getRentalAgreementRowCount());
    }

    public static int getRentalAgreementRowCount() {
        int count = -1;
        try (Statement stmt = Database.connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM rental_agreements")) {
            if (rs.next()) {
                count = rs.getInt("total");
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving rental agreement count: " + e.getMessage());
        }
        return count;
    }

    public static String getFirstRentalAgreementId() {
        String id = "";
        try (Statement stmt = Database.connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id FROM rental_agreements")) {
            if (rs.next()) {
                id = rs.getString("id");
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving ID from rental_agreements table: " + e.getMessage());
        }
        return id;
    }

    public static void deleteAllRentalAgreements() {
        try (Statement stmt = Database.connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM rental_agreements");
        } catch (SQLException e) {
            System.out.println("Error deleting data from rental_agreements table: " + e.getMessage());
        }
    }
}
