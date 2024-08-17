package com.app.database;

import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Database {
    public static Connection connection;
    public static Jdbi jdbi;
    private static String url;
    private static final String pathToMigrationFile = "filesystem:src/main/resources/db/migration";

    public static void setup() {
        try {
            Properties properties = new Properties();
            InputStream inputStream = Database.class.getClassLoader().getResourceAsStream("application.properties");
            properties.load(inputStream);
            url = properties.getProperty("db.url");

            connection = DriverManager.getConnection(url);
            connection.createStatement().execute("PRAGMA foreign_keys = ON;");

            migrateDatabase();

            jdbi = Jdbi.create(url, "", "");
            jdbi.installPlugin(new SqlObjectPlugin());
        } catch (SQLException | IOException e) {
            System.out.println("Error: " + e.getMessage());
            throw new RuntimeException("Error during database setup: ", e);
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.out.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

    private static void migrateDatabase() {
        Flyway flyway = Flyway.configure()
                .dataSource(url, "", "")
                .locations(pathToMigrationFile)
                .load();

        flyway.migrate();
    }
}
