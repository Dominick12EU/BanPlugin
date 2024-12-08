package it.dominick.devroom.commands;

import it.dominick.devroom.manager.BanManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

public class UnbanCommand implements CommandExecutor {
    private BanManager banManager;
    private FileConfiguration config;

    public UnbanCommand(BanManager banManager, FileConfiguration config) {
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
            sender.sendMessage(msg(config.getString("messages.usageUnban")));
            return true;
        }

        String playerName = args[0];
        UUID playerUUID = Bukkit.getOfflinePlayer(playerName).getUniqueId();

        if (!banManager.isBanned(playerUUID)) {
            sender.sendMessage(msg(config.getString("messages.playerNotBanned")).replace("{player}", playerName));
            return true;
        }

        String staffName = sender.getName();
        String reason = "No Reason";

        banManager.unbanPlayer(playerUUID, playerName, staffName, reason);

        sender.sendMessage(msg(config.getString("messages.playerUnbanned")).replace("{player}", playerName));

        return true;
    }

    public static String msg(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
