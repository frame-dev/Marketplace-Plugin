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

import java.util.HashMap;
import java.util.Map;

/**
 * ConfigUtils is a utility class that handles the configuration settings for the Marketplace plugin.
 * It provides methods to retrieve configuration values and set default values if they are not present.
 */
public class ConfigUtils {

    public static final Boolean SETTINGS_BLACKMARKET_USE_CONFIRMATION = Main.getInstance().getConfig().getBoolean("settings.blackmarket.useConfirmation", true);

    public static final String MONGODB_URI = Main.getInstance().getConfig().getString("mongodb.uri", "mongodb://localhost:27017");
    public static final String MONGODB_HOST = Main.getInstance().getConfig().getString("mongodb.host", "localhost");
    public static final int MONGODB_PORT = Main.getInstance().getConfig().getInt("mongodb.port", 27017);
    public static final String MONGODB_DATABASE = Main.getInstance().getConfig().getString("mongodb.database", "marketplace");
    public static final String MONGODB_COLLECTION = Main.getInstance().getConfig().getString("mongodb.collection", "marketplace");
    public static final String MONGODB_USERNAME = Main.getInstance().getConfig().getString("mongodb.username", "username");
    public static final String MONGODB_PASSWORD = Main.getInstance().getConfig().getString("mongodb.password", "password");
    public static final boolean MONGODB_USE_URI = Main.getInstance().getConfig().getBoolean("mongodb.useUri", false);

    public static final String ONLY_PLAYER_MESSAGE = Main.getInstance().getConfig().getString("messages.onlyPlayer", "&cThis command can only be used by players.");
    public static final String NO_PERMISSION_MESSAGE = Main.getInstance().getConfig().getString("messages.noPermission", "&6You have sold {amount}x {itemName} for {price}.");

    // Sell Command Messages
    public static final String SELL_ARGUMENT_MISSING = Main.getInstance().getConfig().getString("messages.sell.argumentMissing", "&cUsage: /sell <item>");
    public static final String SELL_MISSING_ITEM = Main.getInstance().getConfig().getString("messages.sell.missingItemInHand", "&cYou must hold an item in your hand to sell it.");
    public static final String WRONG_NUMBER_FORMAT = Main.getInstance().getConfig().getString("messages.sell.wrongNumberFormat", "&cThe price must be a number. &6Your input: {input}");
    public static final String ITEM_SOLD = Main.getInstance().getConfig().getString("messages.sell.itemSold", "&aYou have successfully sold the item for &6{price}.");

    public static final String SELL_COMMAND_PERMISSION = Main.getInstance().getConfig().getString("permissions.commands.sell", "marketplace.sell");
    public static final String MARKETPLACE_COMMAND_PERMISSION = Main.getInstance().getConfig().getString("permissions.commands.marketplace", "marketplace.marketplace");

    public static final String MARKETPLACE_GUI_TITLE = Main.getInstance().getConfig().getString("gui.marketplace.title", "&6Marketplace Page - {page}");
    public static final int MARKETPLACE_GUI_ROW_SIZE = Main.getInstance().getConfig().getInt("gui.marketplace.rowSize", 3);

    public static final String ERROR_SELL = Main.getInstance().getConfig().getString("messages.error.sell", "&cThere was while error selling the Item &6{itemName}&c!");
    public static final String ERROR_BUY = Main.getInstance().getConfig().getString("messages.error.buy", "&cThere was while error buying the Item &6{itemName}&c!");
    public static final String ERROR_UPDATING_TRANSACTION = Main.getInstance().getConfig().getString("messages.error.updatingTransaction", "&cThere was an error while updating Transaction!");
    public static final String ERROR_ADD_TRANSACTION = Main.getInstance().getConfig().getString("messages.error.addTransaction", "&cThere was an error while adding Transaction!");

    public ConfigUtils(Main plugin) {
        createDefaultConfig(plugin);
    }

    /**
     * Creates the default configuration file for the plugin if it does not exist.
     * It sets default values for MongoDB connection settings and messages.
     *
     * @param plugin The main plugin instance.
     */
    private void createDefaultConfig(Main plugin) {
        containsOrAdd("settings.blackmarket.useConfirmation", true);

        containsOrAdd("mongodb.uri", "mongodb://localhost:27017");
        containsOrAdd("mongodb.host", "localhost");
        containsOrAdd("mongodb.port", 27017);
        containsOrAdd("mongodb.database", "marketplace");
        containsOrAdd("mongodb.collection", "marketplace");
        containsOrAdd("mongodb.username", "username");
        containsOrAdd("mongodb.password", "password");
        containsOrAdd("mongodb.useUri", false);

        containsOrAdd("messages.onlyPlayer", "&cThis command can only be used by players.");
        containsOrAdd("messages.noPermission", "&cYou do not have permission to use this command.");

        containsOrAdd("messages.sell.argumentMissing", "&cUsage: /sell <item>");
        containsOrAdd("messages.sell.missingItemInHand", "&cYou must hold an item in your hand to sell it.");
        containsOrAdd("messages.sell.wrongNumberFormat", "&cThe price must be a number. &6Your input: {input}");
        containsOrAdd("messages.noPermission", "&6You have sold {amount}x {itemName} for {price}.");

        containsOrAdd("messages.error.sell", "&cThere was while error selling the Item &6{itemName}&c!");
        containsOrAdd("messages.error.buy", "&cThere was while error buying the Item &6{itemName}&c!");
        containsOrAdd("messages.error.updatingTransaction", "&cThere was an error while updating Transaction!");
        containsOrAdd("messages.error.addTransaction", "&cThere was an error while adding new Transaction!");

        containsOrAdd("permissions.commands.sell", "marketplace.sell");
        containsOrAdd("permissions.commands.marketplace", "marketplace.marketplace");

        containsOrAdd("gui.marketplace.title", "&6Marketplace");
        containsOrAdd("gui.marketplace.rowSize", 3);

        Map<String, Object> previous = new HashMap<>();
        previous.put("name", "&cPrevious Page");
        previous.put("item", "ARROW");
        previous.put("slot", 0);
        containsOrAdd("gui.marketplace.navigation.previous", previous);

        Map<String, Object> next = new HashMap<>();
        next.put("name", "&aNext Page");
        next.put("item", "ARROW");
        next.put("slot", 8);
        containsOrAdd("gui.marketplace.navigation.next", next);

        Map<String, Object> back = new HashMap<>();
        back.put("name", "&cBack");
        back.put("item", "ARROW");
        back.put("slot", 4);
        containsOrAdd("gui.marketplace.navigation.back", back);

        Map<String, Object> page = new HashMap<>();
        page.put("name", "&6Page {page}");
        page.put("item", "BOOK");
        page.put("slot", 1);
        containsOrAdd("gui.marketplace.navigation.page", page);

        Map<String, Object> item = new HashMap<>();
        item.put("name", "&6{itemName}");
        item.put("lore", new String[]{
                "&7Price: &6{price}",
                "&7Amount: &6{amount}",
                "&7Item Type: &6{itemType}",
                "&7Seller: &6{seller}"
        });
        containsOrAdd("gui.marketplace.item", item);
        plugin.saveConfig();
    }
    
    private void containsOrAdd(String key, Object value) {
        if (!Main.getInstance().getConfig().contains(key)) {
            Main.getInstance().getConfig().set(key, value);
        }
    }
}
