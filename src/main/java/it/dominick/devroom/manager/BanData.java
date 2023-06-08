package it.dominick.devroom.manager;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class BanData {
    private String playerName;
    private UUID playerUUID;
    private String reason;
    private Timestamp startTime;
    private Timestamp expiration;
    private String staffName;
    private String staffAction;

    public BanData(String playerName, UUID playerUUID, String reason, Timestamp startTime, Timestamp expiration, String staffName, String action) {
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.reason = reason;
        this.startTime = startTime;
        this.expiration = expiration;
        this.staffName = staffName;
        this.staffAction = action;
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

    public String getAction() {
        return staffAction;
    }
}

