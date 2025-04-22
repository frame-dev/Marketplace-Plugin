package ch.framedev.marketplace.guis;



/*
 * ch.framedev.marketplace.guis
 * =============================================
 * This File was Created by FrameDev.
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 21.04.2025 13:10
 */

import ch.framedev.marketplace.database.DatabaseHelper;
import ch.framedev.marketplace.item.Item;
import ch.framedev.marketplace.utils.ConfigVariables;
import org.bson.Document;
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
import java.util.Objects;

public class AdminDeeperGUI implements Listener {

    // Inventory Title
    private final String title;
    private Item item;

    // Database Helper for database stuff
    private final DatabaseHelper databaseHelper;

    private final Map<Player, Item> changePriceMap = new HashMap<>();

    public AdminDeeperGUI(DatabaseHelper databaseHelper) {
        // Retrieve Title from Config.yml
        this.title = ConfigVariables.ADMIN_DEEPER_GUI_TITLE;

        this.databaseHelper = databaseHelper;
    }

    private Inventory createGUI() {
        Inventory inventory = Bukkit.createInventory(null, 3 * 9, title);
        // Set up Admin Items for the Inventory
        inventory.setItem(0, createGuiItem(Material.BARRIER, "§6Delete from Database"));
        inventory.setItem(1, createGuiItem(Material.PAPER, "§6Change Price for Item"));
        // Returns the updated Inventory
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

    /**
     * Opens the Inventory and passthrough the Item to update
     *
     * @param player Player
     * @param item the Item to parse through
     */
    public void showInventory(Player player, Item item) {
        this.item = item;
        player.openInventory(createGUI());
    }

    @EventHandler
    public void onClickItem(InventoryClickEvent event) {
        // Check if the Inventory is the Admin Deeper Inventory otherwise skip
        if (!event.getView().getTitle().equalsIgnoreCase(title)) return;
        event.setCancelled(true);
        // Check if item is valid
        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getItemMeta() == null ||
            event.getCurrentItem().getItemMeta().hasDisplayName()) return;
        Player player = (Player) event.getWhoClicked();
        String materialName = event.getCurrentItem().getItemMeta().getDisplayName();

        // Check if the item is Delete Item
        if (materialName.equalsIgnoreCase("§6Delete from Database")) {
            // Delete the Item from the Database
            databaseHelper.deleteDocument(new Document("id", item.getId()));
            player.sendMessage("§aItem successfully deleted from the Database!");
            return;
        }

        // Check if the item is Change Price Item
        if (materialName.equalsIgnoreCase("§6Change Price for Item")) {
            changePriceMap.put(player, item);
            player.closeInventory();
            player.sendMessage("§aType the new Price in the Chat!");
        }
    }

    @EventHandler
    public void onAsyncChatEvent(AsyncPlayerChatEvent event) {
        // Check if player is in changePriceMap
        if(changePriceMap.containsKey(event.getPlayer())) {
            event.setCancelled(true);
            // Chat event for the price change
            try {
                double price = Double.parseDouble(event.getMessage());
                Item changedPrice = changePriceMap.get(event.getPlayer());
                if(changedPrice.isDiscount()) {
                    double discountPrice = changedPrice.isDiscount() ? price / 2 : price;
                    changedPrice.setDiscountPrice(discountPrice);
                    changedPrice.setPrice(price);
                    databaseHelper.updateSellItem(changedPrice);
                    event.getPlayer().sendMessage("§aPrice successfully changed to §6" + price + " §aDiscount Price §6" + discountPrice);
                    changePriceMap.remove(event.getPlayer());
                } else {
                    changedPrice.setPrice(price);
                    databaseHelper.updateSellItem(changedPrice);
                    event.getPlayer().sendMessage("§aPrice successfully changed to §6" + price);
                    changePriceMap.remove(event.getPlayer());
                }
                event.getPlayer().openInventory(createGUI());
            } catch (NumberFormatException e) {
                String wrongNumberFormat = ConfigVariables.WRONG_NUMBER_FORMAT;
                wrongNumberFormat = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(wrongNumberFormat));
                wrongNumberFormat = wrongNumberFormat.replace("{input}", event.getMessage());
                event.getPlayer().sendMessage(wrongNumberFormat);
            }
        }
    }
}
