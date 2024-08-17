package com.app;

import com.app.database.Database;
import com.app.services.ToolRentalService;
import org.apache.commons.cli.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Application {
    private static final int NORMAL_EXIT = 0;
    private static final int ERROR_EXIT = 1;

    static {
        Database.setup();
    }

    public static void main(String[] args) {
        int statusCode = runApplication(args);
        Database.closeConnection();
        System.exit(statusCode);
    }

    public static int runApplication(String[] args) {
        Options options = getOptions();

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("checkout")) {
                if (hasCheckoutArgs(line)) {
                    checkout(line.getOptionValue("tool-code"), line.getOptionValue("rental-days"),
                            line.getOptionValue("discount-percent"), line.getOptionValue("checkout-date"));
                } else {
                    System.out.println("Missing required options for checkout.");
                    printUsage(options);
                }
            } else if (line.hasOption("view")) {
                if (line.hasOption("agreement-id")) {
                    viewRentalAgreement(line.getOptionValue("agreement-id"));
                } else {
                    System.out.println("Missing required options for view.");
                    printUsage(options);
                }
            } else {
                printUsage(options);
            }
        } catch (NumberFormatException ex) {
            return processError("Error converting value to number: " + ex.getMessage());
        } catch (DateTimeParseException ex) {
            return processError("Error parsing date: " + ex.getMessage());
        } catch (Exception ex) {
            return processError("Error: " + ex.getMessage());
        }

        return NORMAL_EXIT;
    }

    private static int processError(String message) {
        System.err.println(message);
        printUsage(getOptions());

        return ERROR_EXIT;
    }

    private static Options getOptions() {
        Options options = new Options();

        Option checkout = new Option("c", "checkout", false, "Perform a checkout");
        options.addOption(checkout);

        Option toolCode = new Option("tc", "tool-code", true, "Required for checkout: Tool code");
        options.addOption(toolCode);

        Option rentalDays = new Option("rd", "rental-days", true, "Required for checkout: Number of rental days (number, minimum of 1)");
        options.addOption(rentalDays);

        Option discount = new Option("dp", "discount-percent", true, "Required for checkout: Discount percent (number, from 0-100)");
        options.addOption(discount);

        Option checkoutDate = new Option("cd", "checkout-date", true, "Required for checkout: Checkout date (mm/dd/yy)");
        options.addOption(checkoutDate);

        Option view = new Option("v", "view", false, "View a rental agreement");
        options.addOption(view);

        Option agreementId = new Option("id", "agreement-id", true, "Required for view: id of rental agreement to view");
        options.addOption(agreementId);

        return options;
    }

    private static boolean hasCheckoutArgs(CommandLine line) {
        return line.hasOption("tool-code") && line.hasOption("rental-days") &&
                line.hasOption("discount-percent") && line.hasOption("checkout-date");
    }

    private static void checkout(String toolCode, String rentalDays, String discountPercent, String checkoutDate) {
        String agreement = ToolRentalService.checkout(toolCode, Integer.parseInt(rentalDays),
                Integer.parseInt(discountPercent), formatDate(checkoutDate));
        System.out.println(agreement);
    }

    private static void viewRentalAgreement(String id) {
        String agreement = ToolRentalService.getRentalAgreement(id);
        System.out.println(agreement);
    }

    private static void printUsage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        String header = """

                Usage guidelines:
                  -c, -tc, -rd, -dp, -cd must be used together.
                  -v, -id must be used together.
                """;
        String customUsage = "app [-c] [-tc -rd -dp -cd] | app [-v] [-id]";

        System.out.println("\n\nGenerated Help:");
        formatter.setWidth(100);
        formatter.printHelp(customUsage, header, options, null, false);
    }

    private static LocalDate formatDate(String date) {
        String[] formats = {"M/d/yy", "M/d/yyyy"};

        for (String format : formats) {
            try {
                return LocalDate.parse(date, DateTimeFormatter.ofPattern(format));
            } catch (DateTimeParseException _) {
            }
        }
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(formats[0]));
    }
}
