CREATE TABLE IF NOT EXISTS tools (
     id INTEGER PRIMARY KEY AUTOINCREMENT,
     code TEXT NOT NULL UNIQUE,
     type TEXT NOT NULL,
     brand TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS tool_type_charges (
     id INTEGER PRIMARY KEY AUTOINCREMENT,
     tool_type TEXT NOT NULL,
     daily_charge_in_cents INTEGER NOT NULL,
     weekday_charge INTEGER NOT NULL,
     weekend_charge INTEGER NOT NULL,
     holiday_charge INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS rental_agreements (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tool_id INTEGER NOT NULL,
    rental_days INTEGER NOT NULL,
    checkout_date INTEGER NOT NULL,
    due_date INTEGER NOT NULL,
    daily_rental_charge_in_cents INTEGER NOT NULL,
    charge_days INTEGER NOT NULL,
    prediscount_charge_in_cents INTEGER NOT NULL,
    discount_percent INTEGER NOT NULL,
    discount_amount_in_cents INTEGER NOT NULL,
    final_charge_in_cents INTEGER NOT NULL,
    FOREIGN KEY (tool_id) REFERENCES tools(id)
);

INSERT INTO tools (code, type, brand) VALUES ('CHNS', 'Chainsaw', 'Stihl');
INSERT INTO tools (code, type, brand) VALUES ('LADW', 'Ladder', 'Werner');
INSERT INTO tools (code, type, brand) VALUES ('JAKD', 'Jackhammer', 'DeWalt');
INSERT INTO tools (code, type, brand) VALUES ('JAKR', 'Jackhammer', 'Ridgid');

INSERT INTO tool_type_charges (tool_type, daily_charge_in_cents, weekday_charge, weekend_charge, holiday_charge) VALUES ('Chainsaw', 149, true, false, true);
INSERT INTO tool_type_charges (tool_type, daily_charge_in_cents, weekday_charge, weekend_charge, holiday_charge) VALUES ('Ladder', 199, true, true, false );
INSERT INTO tool_type_charges (tool_type, daily_charge_in_cents, weekday_charge, weekend_charge, holiday_charge) VALUES ('Jackhammer', 299, true, false, false );