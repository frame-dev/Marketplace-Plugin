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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class UpdateDeeperGUI implements Listener {

    private final DatabaseHelper databaseHelper;
    private Item item;
    private final Map<Player, Item> renameMap = new HashMap<>();
    private final Map<Player, Item> changeMap = new HashMap<>();

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
        if(itemName.equalsIgnoreCase("§6Back")) {
            Main.getInstance().getUpdateGUI().showUpdateGUI(player);
            return;
        }
        if(itemName.equalsIgnoreCase("§6Change Price")) {
            changeMap.put(player, item);
            player.sendMessage("Please type a new price for the item!");
            player.closeInventory();
            return;
        }
        if(itemName.equalsIgnoreCase("§6Rename Item")) {
            renameMap.put(player, item);
            player.sendMessage("Please type a new name for the item!");
            player.closeInventory();
            return;
        }
        if(itemName.equalsIgnoreCase("§6Delete Item")) {
            if(item == null) return;
            databaseHelper.removeItem(item);
            player.sendMessage("Document Deleted!");
        }
    }

    @EventHandler
    public void onTypeChat(AsyncPlayerChatEvent event) {
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
                event.getPlayer().sendMessage("§cWrong Number format!");
            }
        }
    }
}
