import com.app.Application;
import com.app.database.Database;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utilities.TestUtilities.*;

public class ApplicationTest {
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    @BeforeEach
    public void setup() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void reset() {
        System.setOut(originalOut);
        System.setErr(originalErr);

        deleteAllRentalAgreements();
    }

    @AfterAll
    static void tearDown() {
        Database.closeConnection();
        new File("test.db").delete();
    }

    @ParameterizedTest
    @CsvSource({
            "JAKR, invalid_date_format, 5, 10, Error parsing date",
            "JAKR, 9/3/15, invalid_rental_day_format, 10, Error converting value to number",
            "JAKR, 9/3/15, 5, invalid_discount_format, Error converting value to number"
    })
    @DisplayName("Should error when input arg cannot be parsed")
    void shouldGenerateErrorWhenInputArgCannotBeParsed(String code, String date, String days,
                                                         String discount, String errorMessage) {
        errorOnCheckout(code, date, days, discount);

        assertTrue(errContent.toString().contains(errorMessage));
    }

    @DisplayName("Should error when rental day count is less than minimum")
    @Test
    void shouldGenerateErrorWhenRentalDayCountIsBelowMinimum() {
        String code = "JAKR";
        String date = "9/3/15";
        String invalidDays = "0";
        String discount = "10";

        errorOnCheckout(code, date, invalidDays, discount);

        assertTrue(errContent.toString().contains("Rental day count must be at least 1."));
    }

    @ParameterizedTest
    @CsvSource({
            "JAKR, 9/3/15, 5, 101",
            "JAKR, 9/3/15, 5, -1"
    })
    @DisplayName("Should error when discount is not within 0-100 range - includes Test 1")
    void shouldGenerateErrorForInvalidDiscount(String code, String date, String days, String invalidDiscount) {
        errorOnCheckout(code, date, days, invalidDiscount);

        assertTrue(errContent.toString().contains("Discount percent must be a number between 0-100."));
    }

    @DisplayName("Should error when tool code is not found")
    @Test
    void shouldGenerateErrorWhenToolCodeCannotBeFound() {
        String invalidCode = "INVALID_TOOL_CODE";
        String date = "9/3/15";
        String days = "1";
        String discount = "10";

        errorOnCheckout(invalidCode, date, days, discount);

        assertTrue(errContent.toString().contains("Tool code INVALID_TOOL_CODE could not be found."));
    }

    @DisplayName("Should handle ladder checkout over 4th of July weekend - holiday observed on Friday - Test 2")
    @Test
    void shouldHandleLadderCheckoutOverHolidayWeekendObservedFriday() {
        String code = "LADW";
        String date = "7/2/20";
        String days = "3";
        String discount = "10";

        String id = checkout(code, date, days, discount);

        String expected =
                        "Id: " + id + "\n" +
                        "Tool code: " + code + "\n" +
                        "Tool type: Ladder" + "\n" +
                        "Tool brand: Werner" + "\n" +
                        "Rental days: " + days + "\n" +
                        "Checkout date: 07/02/20" + "\n" +
                        "Due date: 07/05/20" + "\n" +
                        "Daily rental charge: $1.99" + "\n" +
                        "Charge days: 2" + "\n" +
                        "Pre-discount charge: $3.98" + "\n" +
                        "Discount percent: " + discount + "%" + "\n" +
                        "Discount amount: $0.40" + "\n" +
                        "Final charge: $3.58";

        assertTrue(outContent.toString().contains(expected));
    }

    @DisplayName("Should handle chainsaw checkout over 4th of July weekend - holiday observed on Friday - Test 3")
    @Test
    void shouldHandleChainsawCheckoutOverHolidayWeekendObservedFriday() {
        String code = "CHNS";
        String date = "7/2/15";
        String days = "5";
        String discount = "25";

        String id = checkout(code, date, days, discount);

        String expected =
                "Id: " + id + "\n" +
                "Tool code: " + code + "\n" +
                "Tool type: Chainsaw" + "\n" +
                "Tool brand: Stihl" + "\n" +
                "Rental days: " + days + "\n" +
                "Checkout date: 07/02/15" + "\n" +
                "Due date: 07/07/15" + "\n" +
                "Daily rental charge: $1.49" + "\n" +
                "Charge days: 3" + "\n" +
                "Pre-discount charge: $4.47" + "\n" +
                "Discount percent: " + discount + "%" + "\n" +
                "Discount amount: $1.12" + "\n" +
                "Final charge: $3.35";

        assertTrue(outContent.toString().contains(expected));
    }

    @DisplayName("Should handle jackhammer checkout over Labor Day - Test 4")
    @Test
    void shouldHandleJackhammerCheckoutOverLaborDay() {
        String code = "JAKD";
        String date = "9/3/15";
        String days = "6";
        String discount = "0";

        String id = checkout(code, date, days, discount);

        String expected =
                "Id: " + id + "\n" +
                "Tool code: " + code + "\n" +
                "Tool type: Jackhammer" + "\n" +
                "Tool brand: DeWalt" + "\n" +
                "Rental days: " + days + "\n" +
                "Checkout date: 09/03/15" + "\n" +
                "Due date: 09/09/15" + "\n" +
                "Daily rental charge: $2.99" + "\n" +
                "Charge days: 3" + "\n" +
                "Pre-discount charge: $8.97" + "\n" +
                "Discount percent: " + discount + "%" + "\n" +
                "Discount amount: $0.00" + "\n" +
                "Final charge: $8.97";

        assertTrue(outContent.toString().contains(expected));
    }

    @DisplayName("Should handle jackhammer checkout over holiday weekend and non-holiday weekend - holiday observed on Friday - Test 5")
    @Test
    void shouldHandleJackhammerCheckoutOverMultipleWeekends() {
        String code = "JAKR";
        String date = "7/2/15";
        String days = "9";
        String discount = "0";

        String id = checkout(code, date, days, discount);

        String expected =
                "Id: " + id + "\n" +
                "Tool code: " + code + "\n" +
                "Tool type: Jackhammer" + "\n" +
                "Tool brand: Ridgid" + "\n" +
                "Rental days: " + days + "\n" +
                "Checkout date: 07/02/15" + "\n" +
                "Due date: 07/11/15" + "\n" +
                "Daily rental charge: $2.99" + "\n" +
                "Charge days: 5" + "\n" +
                "Pre-discount charge: $14.95" + "\n" +
                "Discount percent: " + discount + "%" + "\n" +
                "Discount amount: $0.00" + "\n" +
                "Final charge: $14.95";

        assertTrue(outContent.toString().contains(expected));
    }

    @DisplayName("Should handle jackhammer checkout over 4th of July weekend - holiday observed on Friday - Test 6")
    @Test
    void shouldHandleJackhammerCheckoutOverHolidayWeekendObservedFriday() {
        String code = "JAKR";
        String date = "7/2/20";
        String days = "4";
        String discount = "50";

        String id = checkout(code, date, days, discount);

        String expected =
                "Id: " + id + "\n" +
                "Tool code: " + code + "\n" +
                "Tool type: Jackhammer" + "\n" +
                "Tool brand: Ridgid" + "\n" +
                "Rental days: " + days + "\n" +
                "Checkout date: 07/02/20" + "\n" +
                "Due date: 07/06/20" + "\n" +
                "Daily rental charge: $2.99" + "\n" +
                "Charge days: 1" + "\n" +
                "Pre-discount charge: $2.99" + "\n" +
                "Discount percent: " + discount + "%" + "\n" +
                "Discount amount: $1.50" + "\n" +
                "Final charge: $1.49";

        assertTrue(outContent.toString().contains(expected));
    }

    @DisplayName("Should handle ladder checkout over 4th of July weekend - holiday observed on Monday")
    @Test
    void shouldHandleLadderCheckoutOverHolidayWeekendObservedMonday() {
        String code = "LADW";
        String date = "7/1/21";
        String days = "6";
        String discount = "0";

        String id = checkout(code, date, days, discount);

        String expected =
                "Id: " + id + "\n" +
                "Tool code: " + code + "\n" +
                "Tool type: Ladder" + "\n" +
                "Tool brand: Werner" + "\n" +
                "Rental days: " + days + "\n" +
                "Checkout date: 07/01/21" + "\n" +
                "Due date: 07/07/21" + "\n" +
                "Daily rental charge: $1.99" + "\n" +
                "Charge days: 5" + "\n" +
                "Pre-discount charge: $9.95" + "\n" +
                "Discount percent: " + discount + "%" + "\n" +
                "Discount amount: $0.00" + "\n" +
                "Final charge: $9.95";

        assertTrue(outContent.toString().contains(expected));
    }

    @DisplayName("Should handle ladder checkout when 4th of July lands on weekday")
    @Test
    void shouldHandleLadderCheckoutWhenObservedHolidayLandsOnWeekday() {
        String code = "LADW";
        String date = "7/1/24";
        String days = "4";
        String discount = "0";

        String id = checkout(code, date, days, discount);

        String expected =
                "Id: " + id + "\n" +
                "Tool code: " + code + "\n" +
                "Tool type: Ladder" + "\n" +
                "Tool brand: Werner" + "\n" +
                "Rental days: " + days + "\n" +
                "Checkout date: 07/01/24" + "\n" +
                "Due date: 07/05/24" + "\n" +
                "Daily rental charge: $1.99" + "\n" +
                "Charge days: 3" + "\n" +
                "Pre-discount charge: $5.97" + "\n" +
                "Discount percent: " + discount + "%" + "\n" +
                "Discount amount: $0.00" + "\n" +
                "Final charge: $5.97";

        assertTrue(outContent.toString().contains(expected));
    }

    @DisplayName("Should be able to view previously generated agreement")
    @Test
    void shouldBeAbleToViewPreviouslyGeneratedAgreement() {
        String code = "JAKR";
        String date = "7/2/20";
        String days = "4";
        String discount = "50";

        String id = checkout(code, date, days, discount);
        String agreementFromCheckout = outContent.toString();

        outContent.reset();

        assertEquals("", outContent.toString());

        viewAgreement(id);
        String agreementFromView = outContent.toString();

        assertEquals(agreementFromCheckout, agreementFromView);
    }

    @DisplayName("Should error when agreement cannot be found")
    @Test
    void shouldGenerateErrorWhenRentalAgreementCannotBeFound() {
        String id = "invalid_id";

        viewAgreement(id);

        String errorMessage = String.format("Rental agreement with id %s could not be found.", id);
        assertTrue(errContent.toString().contains(errorMessage));
    }

    private static String checkout(String code, String date, String days, String discount) {
        String[] checkoutArgs = getCheckoutArgs(code, days, discount, date);

        int statusCode = Application.runApplication(checkoutArgs);
        assertEquals(0, statusCode);

        verifyRentalAgreementCount(1);

        return getFirstRentalAgreementId();
    }

    private static void errorOnCheckout(String code, String date, String days, String discount) {
        String[] checkoutArgs = getCheckoutArgs(code, days, discount, date);
        int statusCode = Application.runApplication(checkoutArgs);
        assertEquals(1, statusCode);
    }

    private static void viewAgreement(String id) {
        String[] viewArgs = {"-v", "-id", id};

        Application.runApplication(viewArgs);
    }

    private static String[] getCheckoutArgs(String code, String days, String discount, String date) {
        return new String[]{"-c", "-tc", code, "-rd", days, "-dp", discount, "-cd", date};
    }
}
