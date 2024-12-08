package it.dominick.devroom.commands;

import it.dominick.devroom.manager.BanManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BanCommand implements CommandExecutor {
    private BanManager banManager;
    private FileConfiguration config;

    public BanCommand(BanManager banManager, FileConfiguration config) {
        this.banManager = banManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banplugin.admin")) {
            sender.sendMessage(msg(config.getString("messages.noPermission")));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(msg(config.getString("messages.usageBan")));
            return true;
        }

        String playerName = args[0];
        Player player = Bukkit.getPlayer(playerName);
        String staffName = sender.getName();

        if (player == null) {
            sender.sendMessage(msg(config.getString("messages.playerNotOnline")));
            return true;
        }

        String duration = args.length >= 2 ? args[1] : "permanent";
        String reason = args.length >= 3 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "No Reason";
        String staffAction = "BAN";

        if (!duration.equalsIgnoreCase("permanent") && !duration.matches("\\d+[smhdw]")) {
            reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            duration = "permanent";
        } else if (args.length >= 3) {
            reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        }

        Timestamp expiration = calculateExpirationDate(duration);

        UUID playerUUID = player.getUniqueId();
        banManager.banPlayer(playerUUID, playerName, reason, expiration, staffName, staffAction);

        player.kickPlayer(msg(config.getString("messages.playerKicked").replace("{reason}", reason)));

        sender.sendMessage(msg(config.getString("messages.confirmedBan")));

        sendBanNotification(player, sender.getName(), reason);

        return true;
    }

    private Timestamp calculateExpirationDate(String duration) {
        if (duration.equalsIgnoreCase("permanent") || !duration.matches("\\d+[smhdw]")) {
            return Timestamp.valueOf("9999-12-31 23:59:59");
        }

        String[] durationParts = duration.toLowerCase().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

        int value = Integer.parseInt(durationParts[0]);
        String unit = durationParts[1];

        long durationMillis;

        switch (unit) {
            case "s":
                durationMillis = value * 1000;
                break;
            case "m":
                durationMillis = value * 1000 * 60;
                break;
            case "h":
                durationMillis = value * 1000 * 60 * 60;
                break;
            case "d":
                durationMillis = value * 1000 * 60 * 60 * 24;
                break;
            case "w":
                durationMillis = value * 1000 * 60 * 60 * 24 * 7;
                break;
            case "mo":
                durationMillis = value * 1000 * 60 * 60 * 24 * 30;
                break;
            default:
                return Timestamp.valueOf(LocalDateTime.MAX);
        }

        LocalDateTime expirationDateTime = LocalDateTime.now().plus(Duration.ofMillis(durationMillis));
        return Timestamp.valueOf(expirationDateTime);
    }

    private void sendBanNotification(Player bannedPlayer, String senderName, String reason) {
        List<String> banNotification = config.getStringList("messages.banNotification");
        String message = msg(String.join("\n", banNotification)
                .replace("{player}", bannedPlayer.getName())
                .replace("{sender}", senderName)
                .replace("{reason}", reason));

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("banplugin.notify")) {
                player.sendMessage(message);
            }
        }
    }

    public static String msg(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
