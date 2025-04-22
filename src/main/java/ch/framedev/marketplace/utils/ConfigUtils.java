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
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;

/**
 * ConfigUtils is a utility class that handles the configuration settings for the Marketplace plugin.
 * It provides methods to retrieve configuration values and set default values if they are not present.
 */
public class ConfigUtils {

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
        plugin.getConfig().options().copyDefaults(true);
        containsOrAdd("settings.blackmarket.useConfirmation", true);
        containsOrAdd("settings.blackmarket.maxDiscountItems", 5);
        containsOrAdd("settings.transaction.useGUI", false);

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

        containsOrAdd("messages.sell.itemAdded", "&6You have successfully added the Item {itemName} {amount}x for the price {price} to the Marketplace!");
        containsOrAdd("messages.sell.argumentMissing", "&cUsage: /sell <item>");
        containsOrAdd("messages.sell.missingItemInHand", "&cYou must hold an item in your hand to sell it.");
        containsOrAdd("messages.sell.wrongNumberFormat", "&cThe price must be a number. &6Your input: {input}");
        containsOrAdd("messages.sell.itemSold", "&6You have sold {amount}x {itemName} for {price}.");
        containsOrAdd("messages.sell.boughtItem", "&6You bought the item {itemName} from {playerName} {amount}x.");

        containsOrAdd("messages.error.sell", "&cThere was an error while selling the Item &6{itemName}&c!");
        containsOrAdd("messages.error.buy", "&cThere was an error while buying the Item &6{itemName}&c!");
        containsOrAdd("messages.error.updatingTransaction", "&cThere was an error while updating Transaction! {id}");
        containsOrAdd("messages.error.addTransaction", "&cThere was an error while adding new Transaction! {id}");
        containsOrAdd("messages.error.itemMetaNotFound", "&cItemMeta for &6{itemName} &c not found!");
        containsOrAdd("messages.error.moneyNotEnough", "&cYou do not have enough money to buy this item!");

        containsOrAdd("permissions.commands.sell", "marketplace.sell");
        containsOrAdd("permissions.commands.marketplace", "marketplace.marketplace");
        containsOrAdd("permissions.commands.blackmarket", "marketplace.blackmarket");
        containsOrAdd("permissions.commands.transactions", "marketplace.history");

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

        Map<String, Object> yourItems = new HashMap<>();
        yourItems.put("name", "&6Your Items");
        yourItems.put("item", "DIAMOND_BLOCK");
        yourItems.put("slot", 2);
        containsOrAdd("gui.marketplace.navigation.yourItems", yourItems);

        Map<String, Object> item = new HashMap<>();
        item.put("name", "&6{itemName}");
        item.put("lore", new String[]{
                "&7Price: &6{price}",
                "&7Amount: &6{amount}",
                "&7Item Type: &6{itemType}",
                "&7Seller: &6{seller}"
        });
        item.put("discount", "&6DISCOUNT 50% new price {newPrice}");
        containsOrAdd("gui.marketplace.item", item);

        containsOrAdd("gui.adminDeeper.title", "&6Admin Deeper GUI");

        setupForBlackmarket();
        setupForUpdateGUI();
        setupForAdminGUI();
        plugin.saveConfig();
    }

    private void setupForBlackmarket() {
        containsOrAdd("gui.blackmarket.title", "&6Blackmarket");
        containsOrAdd("gui.blackmarket.rowSize", 3);

        Map<String, Object> previous = new HashMap<>();
        previous.put("name", "&cPrevious Page");
        previous.put("item", "ARROW");
        previous.put("slot", 0);
        containsOrAdd("gui.blackmarket.navigation.previous", previous);

        Map<String, Object> next = new HashMap<>();
        next.put("name", "&aNext Page");
        next.put("item", "ARROW");
        next.put("slot", 8);
        containsOrAdd("gui.blackmarket.navigation.next", next);

        Map<String, Object> back = new HashMap<>();
        back.put("name", "&cBack");
        back.put("item", "ARROW");
        back.put("slot", 4);
        containsOrAdd("gui.blackmarket.navigation.back", back);

        Map<String, Object> page = new HashMap<>();
        page.put("name", "&6Page {page}");
        page.put("item", "BOOK");
        page.put("slot", 1);
        containsOrAdd("gui.blackmarket.navigation.page", page);

        Map<String, Object> yourItems = new HashMap<>();
        yourItems.put("name", "&6Your Items");
        yourItems.put("item", "DIAMOND_BLOCK");
        yourItems.put("slot", 2);
        containsOrAdd("gui.blackmarket.navigation.yourItems", yourItems);

        Map<String, Object> item = new HashMap<>();
        item.put("name", "&6{itemName}");
        item.put("lore", new String[]{
                "&7Price: &6{price}",
                "&7Amount: &6{amount}",
                "&7Item Type: &6{itemType}",
                "&7Seller: &6{seller}"
        });
        item.put("discount", "&6DISCOUNT 50% new price {newPrice}");
        containsOrAdd("gui.blackmarket.item", item);
    }

    private void setupForUpdateGUI() {
        containsOrAdd("gui.update.title", "&6Update Item");
        containsOrAdd("gui.update.rowSize", 3);

        Map<String, Object> previous = new HashMap<>();
        previous.put("name", "&cPrevious Page");
        previous.put("item", "ARROW");
        previous.put("slot", 0);
        containsOrAdd("gui.update.navigation.previous", previous);

        Map<String, Object> next = new HashMap<>();
        next.put("name", "&aNext Page");
        next.put("item", "ARROW");
        next.put("slot", 8);
        containsOrAdd("gui.update.navigation.next", next);

        Map<String, Object> back = new HashMap<>();
        back.put("name", "&cBack");
        back.put("item", "ARROW");
        back.put("slot", 4);
        containsOrAdd("gui.update.navigation.back", back);

        Map<String, Object> page = new HashMap<>();
        page.put("name", "&6Page {page}");
        page.put("item", "BOOK");
        page.put("slot", 1);
        containsOrAdd("gui.update.navigation.page", page);

        Map<String, Object> yourItems = new HashMap<>();
        yourItems.put("name", "&6Your Items");
        yourItems.put("item", "DIAMOND_BLOCK");
        yourItems.put("slot", 2);
        containsOrAdd("gui.update.navigation.yourItems", yourItems);

        Map<String, Object> item = new HashMap<>();
        item.put("name", "&6{itemName}");
        item.put("lore", new String[]{
                "&7Price: &6{price}",
                "&7Amount: &6{amount}",
                "&7Item Type: &6{itemType}",
                "&7Seller: &6{seller}",
                "&7Sold: &6{sold}"
        });
        item.put("discount", "&6DISCOUNT 50% new price {newPrice}");
        containsOrAdd("gui.update.item", item);
        containsOrAdd("gui.updateDeeper.title", "&6Update Item Deeper");

        Map<String, Object> renameItem = new HashMap<>();
        renameItem.put("name", "&6Rename Item");
        renameItem.put("item", "NAME_TAG");
        containsOrAdd("gui.updateDeeper.renameItem", renameItem);

        Map<String, Object> changePrice = new HashMap<>();
        changePrice.put("name", "&6Change Price");
        changePrice.put("item", "EMERALD");
        containsOrAdd("gui.updateDeeper.changePrice", changePrice);

        Map<String, Object> deleteItem = new HashMap<>();
        deleteItem.put("name", "&6Delete Item");
        deleteItem.put("item", "BARRIER");
        containsOrAdd("gui.updateDeeper.deleteItem", deleteItem);

        Map<String, Object> backItem = new HashMap<>();
        backItem.put("name", "&6Back");
        backItem.put("item", "ARROW");
        containsOrAdd("gui.updateDeeper.back", backItem);
    }

    private void containsOrAdd(String key, Object value) {
        if (!Main.getInstance().getConfig().contains(key)) {
            Main.getInstance().getConfig().set(key, value);
            Main.getInstance().saveConfig(); // Save changes
            Main.getInstance().reloadConfig(); // Reload to ensure changes are applied
        }
    }

    private void setupForAdminGUI() {
        containsOrAdd("gui.admin.title", "&6Admin GUI");

        Map<String, Object> previous = new HashMap<>();
        previous.put("name", "&cPrevious Page");
        previous.put("item", "ARROW");
        previous.put("slot", 0);
        containsOrAdd("gui.admin.navigation.previous", previous);

        Map<String, Object> next = new HashMap<>();
        next.put("name", "&aNext Page");
        next.put("item", "ARROW");
        next.put("slot", 8);
        containsOrAdd("gui.admin.navigation.next", next);

        Map<String, Object> back = new HashMap<>();
        back.put("name", "&cBack");
        back.put("item", "ARROW");
        back.put("slot", 4);
        containsOrAdd("gui.admin.navigation.back", back);

        Map<String, Object> page = new HashMap<>();
        page.put("name", "&6Page {page}");
        page.put("item", "BOOK");
        page.put("slot", 1);
        containsOrAdd("gui.admin.navigation.page", page);

        Map<String, Object> item = new HashMap<>();
        item.put("name", "&6{itemName}");
        item.put("lore", new String[]{
                "&7Sold: &6{sold}",
                "&7Seller: &6{seller}",
                "&7Receiver: &6{receiver}"
        });
        containsOrAdd("gui.admin.item", item);
    }

    public static String translateColor(String message, String defaultMessage) {
        if(message == null) {
            message = defaultMessage;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String getItemMetaNotFoundMessage(String itemName) {
        String itemMetaNotFoundMessage = ConfigVariables.ERROR_ITEM_META_NOT_FOUND;
        itemMetaNotFoundMessage = ConfigUtils.translateColor(itemMetaNotFoundMessage, "&cItemMeta for &6{itemName} &c not found!");
        itemMetaNotFoundMessage = itemMetaNotFoundMessage.replace("{itemName}", itemName);
        return itemMetaNotFoundMessage;
    }
}
