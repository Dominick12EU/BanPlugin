package it.dominick.devroom.manager;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class BanData {
    private final String playerName;
    private final UUID playerUUID;
    private final String reason;
    private final Timestamp startTime;
    private final Timestamp expiration;
    private final String staffName;
    private final String staffAction;
    private final boolean isActive;

    public BanData(String playerName, UUID playerUUID, String reason, Timestamp startTime, Timestamp expiration, String staffName, String staffAction, boolean isActive) {
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.reason = reason;
        this.startTime = startTime;
        this.expiration = expiration;
        this.staffName = staffName;
        this.staffAction = staffAction;
        this.isActive = isActive;
    }

    public boolean isActive() {
        return isActive;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getReason() {
        return reason;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public Timestamp getExpiration() {
        return expiration;
    }

    public String getStaffName() {
        return staffName;
    }

    public String getStaffAction() {
        return staffAction;
    }

    public Duration getBanDuration() {
        if (expiration == null) {
            return null;
        }
        return Duration.between(startTime.toLocalDateTime(), expiration.toLocalDateTime());
    }

    public boolean isPermanent() {
        return expiration == null;
    }

}
