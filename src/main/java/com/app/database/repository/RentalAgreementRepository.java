package com.app.database.repository;

import com.app.database.dao.RentalAgreementDao;
import com.app.database.dao.ToolDao;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterFieldMapper(value = RentalAgreementDao.class)
@RegisterFieldMapper(value = ToolDao.class)
public interface RentalAgreementRepository {
    @SqlUpdate("INSERT INTO rental_agreements (tool_id, rental_days, " +
            "checkout_date, due_date, daily_rental_charge_in_cents, charge_days, prediscount_charge_in_cents, " +
            "discount_percent, discount_amount_in_cents, final_charge_in_cents) VALUES (:tool.id, :rentalDays, " +
            ":checkoutDate, :dueDate, :dailyRentalChargeInCents, :chargeDays, :preDiscountChargeInCents, " +
            ":discountPercent, :discountAmountInCents, :finalChargeInCents)")
    @GetGeneratedKeys
    int insert(@BindFields RentalAgreementDao rentalAgreement);

    @SqlQuery("SELECT r.*, t.code AS tool_code, t.type AS tool_type, t.brand AS tool_brand " +
            "FROM rental_agreements r JOIN tools t ON r.tool_id = t.id " +
            "WHERE r.id = :id")
    RentalAgreementDao findAgreementById(@Bind("id") String id);
}
