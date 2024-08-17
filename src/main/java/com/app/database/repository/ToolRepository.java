package com.app.database.repository;

import com.app.database.dao.ToolDao;
import com.app.database.dao.ToolTypeChargesDao;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface ToolRepository {
    @RegisterFieldMapper(ToolDao.class)
    @SqlQuery("SELECT * FROM tools WHERE code = :code")
    ToolDao findToolByCode(@Bind("code") String code);

    @RegisterFieldMapper(ToolTypeChargesDao.class)
    @SqlQuery("SELECT * FROM tool_type_charges WHERE tool_type = :type")
    ToolTypeChargesDao getChargeDetailsByType(@Bind("type") String type);
}
