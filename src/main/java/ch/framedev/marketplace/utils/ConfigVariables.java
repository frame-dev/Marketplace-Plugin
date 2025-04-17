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

public class ConfigVariables {

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
    public static final String BLACKMARKET_COMMAND_PERMISSION = Main.getInstance().getConfig().getString("permissions.commands.blackmarket", "marketplace.blackmarket");
    public static final String TRANSACTIONS_COMMAND_PERMISSION = Main.getInstance().getConfig().getString("permissions.commands.transactions", "marketplace.history");

    public static final String MARKETPLACE_GUI_TITLE = Main.getInstance().getConfig().getString("gui.marketplace.title", "&6Marketplace Page - {page}");
    public static final int MARKETPLACE_GUI_ROW_SIZE = Main.getInstance().getConfig().getInt("gui.marketplace.rowSize", 3);

    public static final String ERROR_SELL = Main.getInstance().getConfig().getString("messages.error.sell", "&cThere was while error selling the Item &6{itemName}&c!");
    public static final String ERROR_BUY = Main.getInstance().getConfig().getString("messages.error.buy", "&cThere was while error buying the Item &6{itemName}&c!");
    public static final String ERROR_UPDATING_TRANSACTION = Main.getInstance().getConfig().getString("messages.error.updatingTransaction", "&cThere was an error while updating Transaction! {id}");
    public static final String ERROR_ADD_TRANSACTION = Main.getInstance().getConfig().getString("messages.error.addTransaction", "&cThere was an error while adding Transaction! {id}");
    public static final String ERROR_ITEM_META_NOT_FOUND = Main.getInstance().getConfig().getString("messages.error.itemMetaNotFound", "&cItemMeta for &6{itemName} &c not found!");
}
