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

    public void createPlayerTable() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            String query = "CREATE TABLE IF NOT EXISTS players (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "player_uuid VARCHAR(36) NOT NULL UNIQUE, " +
                    "player_name VARCHAR(255) NOT NULL)";
            statement.executeUpdate(query);
        } catch (SQLException e) {
            System.out.println("Error creating players table: " + e.getMessage());
        }
    }

    public void createBansTable() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            String query = "CREATE TABLE IF NOT EXISTS bans (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "player_id INT NOT NULL, " +
                    "reason VARCHAR(255) NOT NULL, " +
                    "expiration DATETIME(6), " +
                    "ban_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "staff_name VARCHAR(255) NOT NULL, " +
                    "staff_action VARCHAR(255) NOT NULL, " +
                    "is_active BOOLEAN DEFAULT TRUE, " +
                    "FOREIGN KEY (player_id) REFERENCES players(id) " +
                    "ON DELETE CASCADE ON UPDATE CASCADE)";
            statement.executeUpdate(query);
        } catch (SQLException e) {
            System.out.println("Error creating bans table: " + e.getMessage());
        }
    }


    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        dataSource.close();
    }
}
