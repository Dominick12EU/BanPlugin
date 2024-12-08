package it.dominick.devroom.commands;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import it.dominick.devroom.manager.BanData;
import it.dominick.devroom.manager.BanManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HistoryCommand implements CommandExecutor {
    private BanManager banManager;
    private FileConfiguration config;

    public HistoryCommand(BanManager banManager, FileConfiguration config) {
        this.banManager = banManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banplugin.admin")) {
            sender.sendMessage(msg(config.getString("messages.noPermission")));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(msg(config.getString("messages.onlyPlayer")));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(msg(config.getString("messages.usageHistory")));
            return true;
        }

        String targetPlayerName = args[0];
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetPlayerName);
        Player player = ((Player) sender).getPlayer();

        if (targetPlayer == null) {
            sender.sendMessage(msg(config.getString("messages.playerNotOnline")).replace("{player}", targetPlayerName));
            return true;
        }

        UUID targetPlayerUUID = targetPlayer.getUniqueId();

        List<BanData> banHistory = banManager.getBanHistory(targetPlayerUUID);

        if (banHistory.isEmpty()) {
            sender.sendMessage(msg(config.getString("messages.noPunish")));
            return true;
        }

        // Create GUI
        PaginatedGui historyGui = Gui.paginated()
                .title(Component.text(config.getString("gui.title")))
                .rows(6)
                .pageSize(28)
                .disableAllInteractions()
                .create();

        // Next/Previous
        String materialFillName = config.getString("gui.materialFill");
        Material materialFill = Material.matchMaterial(materialFillName);
        String materialPageName = config.getString("gui.materialPage");
        Material materialPage = Material.matchMaterial(materialPageName);

        // FillBorder
        historyGui.getFiller().fillBorder(ItemBuilder.from(materialFill).setName("").asGuiItem());

        // Next Page
        historyGui.setItem(6, 6, ItemBuilder.from(materialPage).setName(msg(config.getString("gui.nextPage"))).asGuiItem(event -> {
            historyGui.next();
            historyGui.update();
        }));

        // Previous Page
        historyGui.setItem(6, 4, ItemBuilder.from(materialPage).setName(msg(config.getString("gui.previousPage"))).asGuiItem(event -> {
            historyGui.previous();
            historyGui.update();
        }));

        // Add BanItem
        for (BanData banData : banHistory) {
            ItemStack banItem = createBanItem(banData);
            historyGui.addItem(ItemBuilder.from(banItem).asGuiItem());
        }

        historyGui.open(player);

        return true;
    }

    private Material getMaterialFromConfig() {
        String materialName = config.getString("gui.punishMaterial");
        Material material = Material.matchMaterial(materialName);

        if (material == null) {
            material = Material.BARRIER;
        }

        return material;
    }

    private ItemStack createBanItem(BanData banData) {
        Material material = getMaterialFromConfig();
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lore = getItemLoreFromConfig();

        itemMeta.setDisplayName(msg(config.getString("gui.titleBook")));

        List<String> formattedLore = new ArrayList<>();

        for (String line : lore) {
            line = line.replace("{banDate}", String.valueOf(banData.getStartTime()))
                    .replace("{expiration}", String.valueOf(banData.getExpiration()))
                    .replace("{isActive}", String.valueOf(banData.isActive()))
                    .replace("{reason}", banData.getReason())
                    .replace("{staffer}", banData.getStaffName())
                    .replace("{player}", banData.getPlayerName())
                    .replace("{action}", banData.getStaffAction());

            formattedLore.add(line);
        }

        itemMeta.setLore(formattedLore);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    private List<String> getItemLoreFromConfig() {
        List<String> lore = config.getStringList("gui.punishLore");
        List<String> formattedLore = new ArrayList<>();

        for (String line : lore) {
            formattedLore.add(msg(line));
        }

        return formattedLore;
    }

    public static String msg(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }
}
