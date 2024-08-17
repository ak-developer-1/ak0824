package com.app.database.dao;

import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.time.LocalDate;

public class RentalAgreementDao {
    @ColumnName("id")
    public int id;
    @Nested("tool")
    public ToolDao tool;
    @ColumnName("rental_days")
    public int rentalDays;
    @ColumnName("checkout_date")
    public LocalDate checkoutDate;
    @ColumnName("due_date")
    public LocalDate dueDate;
    @ColumnName("daily_rental_charge_in_cents")
    public int dailyRentalChargeInCents;
    @ColumnName("charge_days")
    public int chargeDays;
    @ColumnName("prediscount_charge_in_cents")
    public int preDiscountChargeInCents;
    @ColumnName("discount_percent")
    public int discountPercent;
    @ColumnName("discount_amount_in_cents")
    public int discountAmountInCents;
    @ColumnName("final_charge_in_cents")
    public int finalChargeInCents;
}
