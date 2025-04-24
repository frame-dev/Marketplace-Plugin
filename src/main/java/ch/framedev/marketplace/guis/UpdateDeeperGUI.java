package ch.framedev.marketplace.guis;



/*
 * ch.framedev.marketplace.guis
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 19.04.2025 18:02
 */

import ch.framedev.marketplace.database.DatabaseHelper;
import ch.framedev.marketplace.item.Item;
import ch.framedev.marketplace.main.Main;
import ch.framedev.marketplace.utils.ConfigUtils;
import ch.framedev.marketplace.utils.ConfigVariables;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class UpdateDeeperGUI implements Listener {
    
    private final Main plugin;

    private final DatabaseHelper databaseHelper;
    private Item item;
    private final Map<Player, Item> renameMap = new HashMap<>();
    private final Map<Player, Item> changeMap = new HashMap<>();

    private final String title;

    @SuppressWarnings("DataFlowIssue")
    public UpdateDeeperGUI(Main plugin, DatabaseHelper databaseHelper) {
        this.plugin = plugin;
        this.databaseHelper = databaseHelper;

        // Set the title for the GUI
        this.title = ChatColor.translateAlternateColorCodes('&',ConfigVariables.UPDATE_DEEPER_GUI_TITLE);
    }

    private String getName(String key) {
        return ChatColor.translateAlternateColorCodes('&',plugin.getConfig().getString("gui.updateDeeper." + key + ".name", "&6" + key));
    }

    private Material getItem(String key) {
        String materialName = plugin.getConfig().getString("gui.updateDeeper." + key + ".item", "STONE");
        Material material = Material.getMaterial(materialName.toUpperCase());
        if (material == null) {
            Bukkit.getLogger().warning("Invalid material name in config: " + materialName);
            return Material.STONE; // Default to STONE if invalid
        }
        return material;
    }

    public Inventory createGUI(Item item) {
        this.item = item;
        Inventory inventory = Bukkit.createInventory(null, 9, title);
        inventory.setItem(0, createGuiItem(getItem("renameItem"), getName("renameItem")));
        inventory.setItem(1, createGuiItem(getItem("changePrice"), getName("changePrice")));
        inventory.setItem(2, createGuiItem(getItem("deleteItem"), getName("deleteItem")));
        inventory.setItem(8, createGuiItem(getItem("back"), getName("back")));
        return inventory;
    }

    private ItemStack createGuiItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClickItem(InventoryClickEvent event) {
        if(event.getCurrentItem() == null) return;
        if(event.getCurrentItem().getItemMeta() == null) return;
        if(!event.getView().getTitle().equalsIgnoreCase(title)) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
        if(itemName.equalsIgnoreCase(getName("back"))) {
            plugin.getUpdateGUI().showUpdateGUI(player);
            return;
        }
        if(itemName.equalsIgnoreCase(getName("changePrice"))) {
            changeMap.put(player, item);
            player.sendMessage("Please type a new price for the item!");
            player.closeInventory();
            return;
        }
        if(itemName.equalsIgnoreCase(getName("renameItem"))) {
            renameMap.put(player, item);
            player.sendMessage("Please type a new name for the item!");
            player.closeInventory();
            return;
        }
        if(itemName.equalsIgnoreCase(getName("deleteItem"))) {
            if(item == null) return;
            databaseHelper.removeItem(item);
            String message = "§cThis makes the Item only invisible for you but still remains in the Database!";
            String messagePrefix = ConfigVariables.SETTINGS_USE_PREFIX ? ConfigUtils.getPrefix() + message :
                    message;
            player.sendMessage(messagePrefix);
        }
    }

    @EventHandler
    public void onTypeChat(AsyncPlayerChatEvent event) {
        // Logic for rename the item
        if(renameMap.containsKey(event.getPlayer())) {
            event.setCancelled(true);
            String newName = event.getMessage();
            newName = newName.replace("&", "§");
            Item renameItem = renameMap.get(event.getPlayer());
            ItemStack itemStack = renameItem.getItemStack();
            ItemMeta itemMeta = itemStack.getItemMeta();
            if(itemMeta == null) return;
            itemMeta.setDisplayName(newName);
            itemStack.setItemMeta(itemMeta);
            item.setItemName(newName);
            item.setItemStack(itemStack);
            databaseHelper.updateSellItem(item);
            event.getPlayer().sendMessage("§aItem has been renamed to §6" + newName);
            renameMap.remove(event.getPlayer());
        }
        // Logic for change the Price of the item
        if(changeMap.containsKey(event.getPlayer())) {
            event.setCancelled(true);
            try {
                double price = Double.parseDouble(event.getMessage());
                Item changedPrice = changeMap.get(event.getPlayer());
                if(changedPrice.isDiscount()) {
                    double discountPrice = changedPrice.isDiscount() ? price / 2 : price;
                    changedPrice.setDiscountPrice(discountPrice);
                    changedPrice.setPrice(price);
                    databaseHelper.updateSellItem(changedPrice);
                    event.getPlayer().sendMessage("§aPrice successfully changed to §6" + price + " §aDiscount Price §6" + discountPrice);
                    changeMap.remove(event.getPlayer());
                } else {
                    changedPrice.setPrice(price);
                    databaseHelper.updateSellItem(changedPrice);
                    event.getPlayer().sendMessage("§aPrice successfully changed to §6" + price);
                    changeMap.remove(event.getPlayer());
                }
            } catch (NumberFormatException e) {
                // Prints an error if the number format is wrong.
                String wrongNumberFormat = ConfigVariables.WRONG_NUMBER_FORMAT;
                if(wrongNumberFormat == null)
                    wrongNumberFormat = "&cThe price must be a number. &6Your input: {input}";
                wrongNumberFormat = ChatColor.translateAlternateColorCodes('&', wrongNumberFormat);
                wrongNumberFormat = wrongNumberFormat.replace("{input}", event.getMessage());
                String message = ConfigVariables.SETTINGS_USE_PREFIX ? ConfigUtils.getPrefix() + wrongNumberFormat :
                        wrongNumberFormat;
                event.getPlayer().sendMessage(message);
            }
        }
    }
}
