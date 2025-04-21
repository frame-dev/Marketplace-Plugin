package ch.framedev.marketplace.guis;



/*
 * ch.framedev.marketplace.guis
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 21.04.2025 13:10
 */

import ch.framedev.marketplace.database.DatabaseHelper;
import ch.framedev.marketplace.item.Item;
import ch.framedev.marketplace.utils.ConfigVariables;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AdminDeeperGUI implements Listener {

    // Inventory Title
    private final String title;
    private Item item;

    // Database Helper for database stuff
    private final DatabaseHelper databaseHelper;

    public AdminDeeperGUI(DatabaseHelper databaseHelper) {
        // Retrieve Title from Config.yml
        this.title = ConfigVariables.ADMIN_DEEPER_GUI_TITLE;

        this.databaseHelper = databaseHelper;
    }

    private Inventory createGUI() {
        Inventory inventory = Bukkit.createInventory(null, 3 * 9, title);
        // Setup Admin Items for the Inventory
        inventory.setItem(0, createGuiItem(Material.PAPER, "§6Delete from Database"));
        inventory.setItem(1, createGuiItem(Material.PAPER, "§6Change Price for Item"));
        // Returns the updated Inventory
        return inventory;
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
        // Check if the Inventory is the Admin Deeper Inventory otherwise skip
        if (!event.getView().getTitle().equalsIgnoreCase(title)) return;
        event.setCancelled(true);
        // Check if item is valid
        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getItemMeta() == null ||
            event.getCurrentItem().getItemMeta().hasDisplayName()) return;
        Player player = (Player) event.getWhoClicked();
        String materialName = event.getCurrentItem().getItemMeta().getDisplayName();

        // Check if item is Delete Item
        if (materialName.equalsIgnoreCase("§6Delete from Database")) {
            // Delete the Item from the Database
            databaseHelper.deleteDocument(new Document("id", item.getId()));
            player.sendMessage("§aItem successfully deleted from the Database!");
            return;
        }
    }
}
