package it.dominick.devroom.manager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final HikariDataSource dataSource;

    public DatabaseManager(String host, int port, String database, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);
        dataSource = new HikariDataSource(config);
    }


    public void connect() {
        try {
            dataSource.getConnection();
        } catch (SQLException e) {
            System.out.println("Error connecting to database: " + e.getMessage());
        }
    }

    public void createBansTable() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            String query = "CREATE TABLE IF NOT EXISTS bans (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "player_name VARCHAR(255) NOT NULL," +
                    "player_uuid VARCHAR(36) NOT NULL," +
                    "reason VARCHAR(255) NOT NULL," +
                    "expiration TIMESTAMP)";
            statement.executeUpdate(query);

            String alterQuery = "ALTER TABLE bans MODIFY COLUMN expiration DATETIME(6)";
            statement.executeUpdate(alterQuery);
        } catch (SQLException e) {
            System.out.println("Error creating ban table: " + e.getMessage());
        }
    }

    public void createHistoryTable() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            String query = "CREATE TABLE IF NOT EXISTS ban_history (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "player_name VARCHAR(255) NOT NULL," +
                    "player_uuid VARCHAR(36) NOT NULL," +
                    "reason VARCHAR(255) NOT NULL," +
                    "ban_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "expiration DATETIME(6), " +
                    "staff_name VARCHAR(255) NOT NULL, " +
                    "staff_action VARCHAR(255) NOT NULL)";
            statement.executeUpdate(query);

            String alterQuery = "ALTER TABLE ban_history MODIFY COLUMN expiration DATETIME(6)";
            statement.executeUpdate(alterQuery);
        } catch (SQLException e) {
            System.out.println("Error creating history table: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        dataSource.close();
    }
}

