package it.dominick.devroom.manager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BanManager {

    private DatabaseManager databaseManager;

    public BanManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void banPlayer(UUID playerUUID, String playerName, String reason, Timestamp expiration) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO bans (player_uuid, player_name, reason, expiration) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, playerName);
            statement.setString(3, reason);
            statement.setTimestamp(4, expiration);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while banning player: " + e.getMessage());
        }
    }

    public void addBanToHistory(UUID playerUUID, String playerName, String reason, Timestamp expiration, String staffName, String staffAction) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO ban_history (player_uuid, player_name, reason, expiration, staff_name, staff_action) VALUES (?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, playerName);
            statement.setString(3, reason);
            statement.setTimestamp(4, expiration);
            statement.setString(5, staffName);
            statement.setString(6, staffAction);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while adding ban to history: " + e.getMessage());
        }
    }

    public void unbanPlayer(UUID playerUUID) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM bans WHERE player_uuid = ?")) {
            statement.setString(1, playerUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error unblocking player: " + e.getMessage());
        }
    }

    public boolean isBanned(UUID playerUUID) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM bans WHERE player_uuid = ?")) {
            statement.setString(1, playerUUID.toString());
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("Error checking ban: " + e.getMessage());
        }
        return false;
    }

    public Timestamp getBanExpiration(UUID playerUUID) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT expiration FROM bans WHERE player_uuid = ?")) {
            statement.setString(1, playerUUID.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getTimestamp("expiration");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving ban expiration: " + e.getMessage());
        }
        return null;
    }

    public List<BanData> getBanHistory(UUID playerUUID) {
        List<BanData> banHistory = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM ban_history WHERE player_uuid = ?")) {
            statement.setString(1, playerUUID.toString());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String playerName = resultSet.getString("player_name");
                String reason = resultSet.getString("reason");
                Timestamp startTime = resultSet.getTimestamp("ban_date");
                Timestamp expiration = resultSet.getTimestamp("expiration");
                String staffName = resultSet.getString("staff_name");
                String staffAction = resultSet.getString("staff_action");

                BanData banData = new BanData(playerName, playerUUID, reason, startTime, expiration, staffName, staffAction);
                banHistory.add(banData);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving ban history: " + e.getMessage());
        }

        return banHistory;
    }

    public int getBanHistoryCount() {
        try (Connection connection = databaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            String query = "SELECT COUNT(*) FROM ban_history";
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving ban history count: " + e.getMessage());
        }
        return 0;
    }

    private Timestamp calculateExpirationDate(String duration) {
        if (duration.isEmpty()) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        long seconds = parseDuration(duration);
        LocalDateTime expirationDateTime = now.plusSeconds(seconds);

        return Timestamp.valueOf(expirationDateTime);
    }

    private long parseDuration(String duration) {
        int minutes = Integer.parseInt(duration);
        return minutes * 60;
    }

    public void close() {
        databaseManager.close();
    }
}

