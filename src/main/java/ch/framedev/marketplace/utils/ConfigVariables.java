package ch.framedev.marketplace.utils;



/*
 * ch.framedev.marketplace.utils
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 15.04.2025 19:35
 */

import ch.framedev.marketplace.main.Main;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigVariables {

    private static final FileConfiguration config;
    
    static {
        config = Main.getInstance().getConfig();
    }

    public static final Boolean SETTINGS_BLACKMARKET_USE_CONFIRMATION = config.getBoolean("settings.blackmarket.useConfirmation", true);
    public static final int SETTINGS_BLACKMARKET_MAX_DISCOUNT_ITEMS = config.getInt("settings.blackmarket.maxDiscountItems", 5);
    public static final Boolean SETTINGS_TRANSACTION_USE_GUI = config.getBoolean("settings.transactions.useGui", false);

    public static final String MONGODB_URI = config.getString("mongodb.uri", "mongodb://localhost:27017");
    public static final String MONGODB_HOST = config.getString("mongodb.host", "localhost");
    public static final int MONGODB_PORT = config.getInt("mongodb.port", 27017);
    public static final String MONGODB_DATABASE = config.getString("mongodb.database", "marketplace");
    public static final String MONGODB_COLLECTION = config.getString("mongodb.collection", "marketplace");
    public static final String MONGODB_USERNAME = config.getString("mongodb.username", "username");
    public static final String MONGODB_PASSWORD = config.getString("mongodb.password", "password");
    public static final boolean MONGODB_USE_URI = config.getBoolean("mongodb.useUri", false);

    public static final String ONLY_PLAYER_MESSAGE = config.getString("messages.onlyPlayer", "&cThis command can only be used by players.");
    public static final String NO_PERMISSION_MESSAGE = config.getString("messages.noPermission", "&6You have sold {amount}x {itemName} for {price}.");

    // Sell Command Messages
    public static final String ITEM_ADDED = config.getString("messages.sell.itemAdded", "&6You have successfully added the Item {itemName} to the Marketplace!");
    public static final String SELL_ARGUMENT_MISSING = config.getString("messages.sell.argumentMissing", "&cUsage: /sell <item>");
    public static final String SELL_MISSING_ITEM = config.getString("messages.sell.missingItemInHand", "&cYou must hold an item in your hand to sell it.");
    public static final String WRONG_NUMBER_FORMAT = config.getString("messages.sell.wrongNumberFormat", "&cThe price must be a number. &6Your input: {input}");
    public static final String ITEM_SOLD = config.getString("messages.sell.itemSold", "&aYou have successfully sold the item for &6{price}.");
    public static final String ITEM_BOUGHT = config.getString("messages.sell.boughtItem","&6You bought the item {itemName} from {playerName} {amount}x.");

    public static final String SELL_COMMAND_PERMISSION = config.getString("permissions.commands.sell", "marketplace.sell");
    public static final String MARKETPLACE_COMMAND_PERMISSION = config.getString("permissions.commands.marketplace", "marketplace.marketplace");
    public static final String BLACKMARKET_COMMAND_PERMISSION = config.getString("permissions.commands.blackmarket", "marketplace.blackmarket");
    public static final String TRANSACTIONS_COMMAND_PERMISSION = config.getString("permissions.commands.transactions", "marketplace.history");
    public static final String ADMIN_COMMAND_PERMISSION = config.getString("permissions.commands.admin", "marketplace.admin");

    public static final String MARKETPLACE_GUI_TITLE = config.getString("gui.marketplace.title", "&6Marketplace");
    public static final int MARKETPLACE_GUI_ROW_SIZE = config.getInt("gui.marketplace.rowSize", 3);

    public static final String BLACKMARKET_GUI_TITLE = config.getString("gui.blackmarket.title", "&6Blackmarket");
    public static final int BLACKMARKET_GUI_ROW_SIZE = config.getInt("gui.blackmarket.rowSize", 3);

    public static final String TRANSACTIONS_GUI_TITLE = config.getString("gui.transaction.title", "&6Transaction");
    public static final int TRANSACTIONS_GUI_ROW_SIZE = config.getInt("gui.transaction.rowSize", 3);

    public static final String UPDATE_GUI_TITLE = config.getString("gui.update.title", "&6Update Item");
    public static final int UPDATE_GUI_ROW_SIZE = config.getInt("gui.update.rowSize", 3);

    public static final String UPDATE_DEEPER_GUI_TITLE = config.getString("gui.updateDeeper.title", "&6Update Item Deeper");

    public static final String ADMIN_GUI_TITLE = config.getString("gui.admin.title", "&6Admin GUI");

    public static final String ADMIN_DEEPER_GUI_TITLE = config.getString("gui.adminDeeper.title", "&6Admin Deeper GUI");

    public static final String ERROR_SELL = config.getString("messages.error.sell", "&cThere was an error while selling the Item &6{itemName}&c!");
    public static final String ERROR_BUY = config.getString("messages.error.buy", "&cThere was an error while buying the Item &6{itemName}&c!");
    public static final String ERROR_UPDATING_TRANSACTION = config.getString("messages.error.updatingTransaction", "&cThere was an error while updating Transaction! {id}");
    public static final String ERROR_ADD_TRANSACTION = config.getString("messages.error.addTransaction", "&cThere was an error while adding Transaction! {id}");
    public static final String ERROR_ITEM_META_NOT_FOUND = config.getString("messages.error.itemMetaNotFound", "&cItemMeta for &6{itemName} &c not found!");

    public static final String MONEY_NOT_ENOUGH = config.getString("messages.error.moneyNotEnough", "&cYou don't have enough money to buy this item!");

    public static final String DISCORD_WEBHOOK_URL = config.getString("discord.webhookUrl", "https://discord.com/api/webhooks/1234567890/abcdefghijklmnopqrstuvwxyz");
}
