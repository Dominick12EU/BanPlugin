package it.dominick.devroom.manager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BanManager {

    private final DatabaseManager databaseManager;

    public BanManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void banPlayer(UUID playerUUID, String playerName, String reason, Timestamp expiration, String staffName, String staffAction) {
        int playerId = getOrCreatePlayerId(playerUUID, playerName);
        if (playerId == -1) {
            System.out.println("Error: Could not find or create player.");
            return;
        }

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO bans (player_id, reason, expiration, staff_name, staff_action, is_active) VALUES (?, ?, ?, ?, ?, TRUE)")) {
            statement.setInt(1, playerId);
            statement.setString(2, reason);
            statement.setTimestamp(3, expiration);
            statement.setString(4, staffName);
            statement.setString(5, staffAction);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while banning player: " + e.getMessage());
        }
    }


    public void unbanPlayer(UUID playerUUID, String playerName, String staffName, String unbanReason) {
        int playerId = getOrCreatePlayerId(playerUUID, playerName);
        if (playerId == -1) {
            System.out.println("Error: Could not find or create player.");
            return;
        }

        try (Connection connection = databaseManager.getConnection()) {
            String staffAction = "UNBAN";

            String query = "UPDATE bans SET is_active = FALSE, unban_reason = ?, unban_date = ?, staff_name = ?, staff_action = ? " +
                    "WHERE player_id = ? AND is_active = TRUE";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, unbanReason);
                statement.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                statement.setString(3, staffName);
                statement.setString(4, staffAction); // Azione "UNBAN" automatica
                statement.setInt(5, playerId);

                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated == 0) {
                    System.out.println("No active ban found for player.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error while unbanning player: " + e.getMessage());
        }
    }



    public boolean isBanned(UUID playerUUID) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM bans WHERE player_uuid = ? AND is_active = TRUE")) {
            statement.setString(1, playerUUID.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            System.out.println("Error checking ban: " + e.getMessage());
        }
        return false;
    }

    public int getOrCreatePlayerId(UUID playerUUID, String playerName) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement checkStatement = connection.prepareStatement("SELECT id FROM players WHERE player_uuid = ?")) {
            checkStatement.setString(1, playerUUID.toString());
            try (ResultSet resultSet = checkStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }

            try (PreparedStatement insertStatement = connection.prepareStatement(
                    "INSERT INTO players (player_uuid, player_name) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                insertStatement.setString(1, playerUUID.toString());
                insertStatement.setString(2, playerName);
                insertStatement.executeUpdate();

                try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error managing player data: " + e.getMessage());
        }
        return -1;
    }

    public Timestamp getBanExpiration(UUID playerUUID) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT expiration FROM bans WHERE player_uuid = ? AND is_active = TRUE")) {
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
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT player_name, reason, ban_date, expiration, staff_name, staff_action, is_active " +
                             "FROM bans WHERE player_uuid = ?")) {
            statement.setString(1, playerUUID.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String playerName = resultSet.getString("player_name");
                    String reason = resultSet.getString("reason");
                    Timestamp startTime = resultSet.getTimestamp("ban_date");
                    Timestamp expiration = resultSet.getTimestamp("expiration");
                    String staffName = resultSet.getString("staff_name");
                    String staffAction = resultSet.getString("staff_action");
                    boolean isActive = resultSet.getBoolean("is_active");

                    BanData banData = new BanData(playerName, playerUUID, reason, startTime, expiration, staffName, staffAction, isActive);
                    banHistory.add(banData);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving ban history: " + e.getMessage());
        }

        return banHistory;
    }



    public void close() {
        databaseManager.close();
    }
}
