package ch.framedev.marketplace.guis;



/*
 * ch.framedev.marketplace.guis
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 18.04.2025 17:35
 */

import ch.framedev.marketplace.database.DatabaseHelper;
import ch.framedev.marketplace.item.Item;
import ch.framedev.marketplace.utils.ConfigUtils;
import ch.framedev.marketplace.utils.ConfigVariables;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class TransactionGUI implements Listener {

    private final DatabaseHelper databaseHelper;

    private final String title;
    private final int rowSize;

    public TransactionGUI(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        title = ConfigUtils.translateColor(ConfigVariables.TRANSACTIONS_GUI_TITLE, "&6Transaction");
        rowSize = ConfigVariables.TRANSACTIONS_GUI_ROW_SIZE;
    }

    public void showInventory(Player player) {
        player.openInventory(createGUI(player));
    }

    private Inventory createGUI(Player player) {
        return Bukkit.createInventory(null, rowSize * 9, title + ", Main");
    }

    private Inventory createGUISold(Player player, List<Item> itemsSold) {
        Inventory soldInventory = Bukkit.createInventory(null, rowSize * 9, title + ", Sold");
        for (Item item : itemsSold) {
            soldInventory.addItem(item.getItemStack());
        }
        return soldInventory;
    }

    @EventHandler
    public void onClickItem(InventoryClickEvent event) {

    }
}
