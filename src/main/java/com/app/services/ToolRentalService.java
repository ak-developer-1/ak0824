package com.app.services;

import com.app.database.Database;
import com.app.database.dao.RentalAgreementDao;
import com.app.database.dao.ToolDao;
import com.app.database.dao.ToolTypeChargesDao;
import com.app.database.repository.RentalAgreementRepository;
import com.app.database.repository.ToolRepository;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ToolRentalService {
    private static final int MINIMUM_RENTAL_DAYS = 1;

    public static String checkout(String toolCode, int rentalDays, int discountPercent, LocalDate checkoutDate) {
        if (rentalDays < MINIMUM_RENTAL_DAYS) {
            throw new IllegalArgumentException("Rental day count must be at least 1.");
        }

        if (discountPercent < 0 || discountPercent > 100) {
            throw new IllegalArgumentException("Discount percent must be a number between 0-100.");
        }

        ToolRepository toolRepository = Database.jdbi.onDemand(ToolRepository.class);
        ToolDao toolDao = toolRepository.findToolByCode(toolCode);
        if (toolDao == null) {
            throw new IllegalArgumentException(String.format("Tool code %s could not be found.", toolCode));
        }

        return generateRentalAgreement(toolDao, rentalDays, discountPercent, checkoutDate);
    }

    public static String getRentalAgreement(String id) {
        RentalAgreementRepository repository = Database.jdbi.onDemand(RentalAgreementRepository.class);
        RentalAgreementDao agreementDao = repository.findAgreementById(id);

        if (agreementDao == null) {
            throw new IllegalArgumentException(String.format("Rental agreement with id %s could not be found.", id));
        }

        return formatRentalAgreement(agreementDao);
    }

    private static String generateRentalAgreement(ToolDao tool, int rentalDays, int discountPercent, LocalDate checkoutDate) {
        ToolRepository toolRepository = Database.jdbi.onDemand(ToolRepository.class);
        ToolTypeChargesDao charges = toolRepository.getChargeDetailsByType(tool.type);

        LocalDate dueDate = getDueDate(checkoutDate, rentalDays);
        int chargeDays = getChargeDays(charges, checkoutDate, dueDate);
        int preDiscountCharge = getPreDiscountChargeInCents(chargeDays, charges.dailyChargeInCents);
        int discountAmount = getDiscountAmountInCents(preDiscountCharge, discountPercent);

        RentalAgreementDao agreementDao = new RentalAgreementDao();
        agreementDao.tool = tool;
        agreementDao.rentalDays = rentalDays;
        agreementDao.checkoutDate = checkoutDate;
        agreementDao.dueDate = dueDate;
        agreementDao.dailyRentalChargeInCents = charges.dailyChargeInCents;
        agreementDao.chargeDays = chargeDays;
        agreementDao.preDiscountChargeInCents = preDiscountCharge;
        agreementDao.discountPercent = discountPercent;
        agreementDao.discountAmountInCents = discountAmount;
        agreementDao.finalChargeInCents = preDiscountCharge - discountAmount;

        RentalAgreementRepository repository = Database.jdbi.onDemand(RentalAgreementRepository.class);
        agreementDao.id = repository.insert(agreementDao);

        return formatRentalAgreement(agreementDao);
    }

    private static String formatRentalAgreement(RentalAgreementDao dao) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        return "Id: " + dao.id + "\n" +
               "Tool code: " + dao.tool.code + "\n" +
               "Tool type: " + dao.tool.type + "\n" +
               "Tool brand: " + dao.tool.brand + "\n" +
               "Rental days: " + dao.rentalDays + "\n" +
               "Checkout date: " + dao.checkoutDate.format(formatter) + "\n" +
               "Due date: " + dao.dueDate.format(formatter) + "\n" +
               "Daily rental charge: " + currencyFormat.format(dao.dailyRentalChargeInCents * 0.01) + "\n" +
               "Charge days: " + dao.chargeDays + "\n" +
               "Pre-discount charge: " + currencyFormat.format(dao.preDiscountChargeInCents * 0.01) + "\n" +
               "Discount percent: " + dao.discountPercent + "%" + "\n" +
               "Discount amount: " + currencyFormat.format(dao.discountAmountInCents * 0.01) + "\n" +
               "Final charge: " + currencyFormat.format(dao.finalChargeInCents * 0.01);
    }

    private static LocalDate getDueDate(LocalDate checkoutDate, int rentalDays) {
        return checkoutDate.plusDays(rentalDays);
    }

    private static int getChargeDays(ToolTypeChargesDao charges, LocalDate checkoutDate, LocalDate dueDate) {
        List<LocalDate> dates = getDatesInRentalPeriod(checkoutDate, dueDate);

        int chargeDays = 0;
        for (LocalDate date : dates) {
            if (CalendarUtility.isHoliday(date)) {
                if (charges.hasHolidayCharge) chargeDays += 1;
            } else if (CalendarUtility.isWeekend(date) && charges.hasWeekendCharge) {
                chargeDays += 1;
            } else if (CalendarUtility.isWeekday(date) && charges.hasWeekdayCharge) {
                chargeDays += 1;
            }
        }

        return chargeDays;
    }

    private static List<LocalDate> getDatesInRentalPeriod(LocalDate checkoutDate, LocalDate dueDate) {
        return Stream.iterate(checkoutDate.plusDays(1), date -> date.plusDays(1))
                .limit(checkoutDate.until(dueDate).getDays())
                .collect(Collectors.toList());
    }

    private static int getPreDiscountChargeInCents(int days, int charge) {
        return days * charge;
    }

    private static int getDiscountAmountInCents(int charge, int discount) {
        double discountAsDecimal = discount / 100.0;

        return (int) Math.round(charge * discountAsDecimal);
    }
}
