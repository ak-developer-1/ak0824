package com.app.database.dao;

import org.jdbi.v3.core.mapper.reflect.ColumnName;

public class ToolDao {
    @ColumnName("id")
    public int id;
    @ColumnName("code")
    public String code;
    @ColumnName("type")
    public String type;
    @ColumnName("brand")
    public String brand;
}
