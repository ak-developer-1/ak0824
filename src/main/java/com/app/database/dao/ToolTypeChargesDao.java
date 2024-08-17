package com.app.database.dao;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

public class ToolTypeChargesDao {
    @ColumnName("tool_type")
    public String toolType;
    @ColumnName("daily_charge_in_cents")
    public int dailyChargeInCents;
    @ColumnName("weekday_charge")
    public boolean hasWeekdayCharge;
    @ColumnName("weekend_charge")
    public boolean hasWeekendCharge;
    @ColumnName("holiday_charge")
    public boolean hasHolidayCharge;
}
