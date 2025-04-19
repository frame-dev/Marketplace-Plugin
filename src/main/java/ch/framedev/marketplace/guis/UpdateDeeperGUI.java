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

public class UpdateDeeperGUI implements Listener {

    private final DatabaseHelper databaseHelper;
    private Item item;

    public UpdateDeeperGUI(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public Inventory createGUI(Item item) {
        this.item = item;
        Inventory inventory = Bukkit.createInventory(null, 9, "Update Deeper");
        inventory.setItem(0, createGuiItem(Material.NAME_TAG, "§6Rename Item"));
        inventory.setItem(1, createGuiItem(Material.EMERALD, "§6Change Price"));
        inventory.setItem(2, createGuiItem(Material.BARRIER, "§6Delete Item"));
        inventory.setItem(8, createGuiItem(Material.ARROW, "§6Back"));
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
        if(!event.getView().getTitle().equalsIgnoreCase("Update Deeper")) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
        ItemStack itemStack = event.getCurrentItem();
        if(itemName.equalsIgnoreCase("§6Back")) {
            Main.getInstance().getUpdateGUI().showUpdateGUI(player);
            return;
        }
        if(itemName.equalsIgnoreCase("§6Delete Item")) {
            if(item == null) return;
            databaseHelper.deleteDocument(new Document("player", player.getUniqueId().toString()).append("id", item.getId()));
            player.sendMessage("Document Deleted!");
        }
    }
}
