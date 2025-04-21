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

    private final String title;
    private Item item;

    private final DatabaseHelper databaseHelper;

    public AdminDeeperGUI(DatabaseHelper databaseHelper) {
        this.title = ConfigVariables.ADMIN_DEEPER_GUI_TITLE;

        this.databaseHelper = databaseHelper;
    }

    private Inventory createGUI() {
        Inventory inventory = Bukkit.createInventory(null, 3 * 9, title);
        inventory.setItem(0, createGuiItem(Material.PAPER, "§6Delete from Database"));
        inventory.setItem(1, createGuiItem(Material.PAPER, "§6Change Price for Item"));
        return inventory;
    }

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
        if (!event.getView().getTitle().equalsIgnoreCase(title)) return;
        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getItemMeta() == null ||
            event.getCurrentItem().getItemMeta().hasDisplayName()) return;
        Player player = (Player) event.getWhoClicked();
        String materialName = event.getCurrentItem().getItemMeta().getDisplayName();
        if (materialName.equalsIgnoreCase("§6Delete from Database")) {
            databaseHelper.deleteDocument(new Document("id", item.getId()));
            player.sendMessage("§aItem successfully deleted from the Database!");
            return;
        }
    }
}
