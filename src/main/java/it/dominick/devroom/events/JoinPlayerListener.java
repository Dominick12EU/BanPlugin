package it.dominick.devroom.events;

import it.dominick.devroom.manager.BanManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class JoinPlayerListener implements Listener {

    private BanManager banManager;
    private FileConfiguration config;

    public JoinPlayerListener(BanManager banManager, FileConfiguration config) {
        this.banManager = banManager;
        this.config = config;
    }

    @EventHandler
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        UUID playerUUID = event.getUniqueId();

        LocalDateTime currentDateTime = LocalDateTime.now();
        Timestamp expiration = banManager.getBanExpiration(playerUUID);

        if (expiration != null && currentDateTime.isAfter(expiration.toLocalDateTime())) {
            banManager.unbanPlayer(playerUUID);
        } else if (banManager.isBanned(playerUUID)) {
            List<String> banDisplayList = config.getStringList("messages.banDisplay");
            event.setLoginResult(null);
            event.setKickMessage(msg(String.join("\n", banDisplayList).replace("{expiration}", expiration.toString())));
        }
    }

    public static String msg(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
