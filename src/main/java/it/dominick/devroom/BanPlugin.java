package it.dominick.devroom;

import it.dominick.devroom.commands.BanCommand;
import it.dominick.devroom.commands.HistoryCommand;
import it.dominick.devroom.commands.UnbanCommand;
import it.dominick.devroom.events.JoinPlayerListener;
import it.dominick.devroom.manager.BanManager;
import it.dominick.devroom.manager.DatabaseManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class BanPlugin extends JavaPlugin {

    private FileConfiguration config;
    private DatabaseManager databaseManager;
    private BanManager banManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        try {
            String host = config.getString("database.host");
            int port = config.getInt("database.port");
            String database = config.getString("database.name");
            String username = config.getString("database.username");
            String password = config.getString("database.password");

            databaseManager = new DatabaseManager(host, port, database, username, password);
            databaseManager.connect();
            databaseManager.createBansTable();

            banManager = new BanManager(databaseManager);


            getCommand("ban").setExecutor(new BanCommand(banManager, config));
            getCommand("unban").setExecutor(new UnbanCommand(banManager, config));
            getCommand("history").setExecutor(new HistoryCommand(banManager, config));

            getServer().getPluginManager().registerEvents(new JoinPlayerListener(banManager, config), this);

        } catch (Exception e) {
            getLogger().severe("Error activating the plugin: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        databaseManager.close();
    }
}
