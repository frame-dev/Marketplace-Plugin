package ch.framedev.marketplace.utils;



/*
 * ch.framedev.marketplace.utils
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 15.04.2025 19:31
 */

import ch.framedev.marketplace.main.Main;

public class ConfigUtils {

    public static final String MONGODB_URI = Main.getInstance().getConfig().getString("mongodb.uri", "mongodb://localhost:27017");
    public static final String MONGODB_HOST = Main.getInstance().getConfig().getString("mongodb.host", "localhost");
    public static final int MONGODB_PORT = Main.getInstance().getConfig().getInt("mongodb.port", 27017);
    public static final String MONGODB_DATABASE = Main.getInstance().getConfig().getString("mongodb.database", "marketplace");
    public static final String MONGODB_COLLECTION = Main.getInstance().getConfig().getString("mongodb.collection", "marketplace");
    public static final String MONGODB_USERNAME = Main.getInstance().getConfig().getString("mongodb.username", "username");
    public static final String MONGODB_PASSWORD = Main.getInstance().getConfig().getString("mongodb.password", "password");
    public static final boolean MONGODB_USE_URI = Main.getInstance().getConfig().getBoolean("mongodb.useUri", false);

    public static final String ONLY_PLAYER_MESSAGE = Main.getInstance().getConfig().getString("messages.onlyPlayer", "&cThis command can only be used by players.");
    public static final String NO_PERMISSION_MESSAGE = Main.getInstance().getConfig().getString("messages.noPermission", "&cYou do not have permission to use this command.");
    public static final String SELL_ARGUMENT_MISSING = Main.getInstance().getConfig().getString("messages.argumentMissingSell", "&cUsage: /sell <item>");
    public static final String SELL_MISSING_ITEM = Main.getInstance().getConfig().getString("messages.sellMissingItemInHand", "&cYou must hold an item in your hand to sell it.");
    public static final String WRONG_NUMBER_FORMAT = Main.getInstance().getConfig().getString("messages.wrongNumberFormat", "&cThe price must be a number.");

    public static final String SELL_COMMAND_PERMISSION = "permissions.commands.sell";

    public ConfigUtils(Main plugin) {
        createDefaultConfig(plugin);
    }

    private void createDefaultConfig(Main plugin) {
        plugin.getConfig().options().copyDefaults(true);
        plugin.getConfig().addDefault("mongodb.uri", "mongodb://localhost:27017");
        plugin.getConfig().addDefault("mongodb.host", "localhost");
        plugin.getConfig().addDefault("mongodb.port", 27017);
        plugin.getConfig().addDefault("mongodb.database", "marketplace");
        plugin.getConfig().addDefault("mongodb.collection", "marketplace");
        plugin.getConfig().addDefault("mongodb.username", "username");
        plugin.getConfig().addDefault("mongodb.password", "password");
        plugin.getConfig().addDefault("mongodb.useUri", false);
        plugin.getConfig().addDefault("messages.onlyPlayer", "&cThis command can only be used by players.");
        plugin.saveConfig();
    }

}
