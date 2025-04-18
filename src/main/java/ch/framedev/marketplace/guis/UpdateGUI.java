package ch.framedev.marketplace.guis;



/*
 * ch.framedev.marketplace.guis
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 16.04.2025 17:03
 */

import ch.framedev.marketplace.database.DatabaseHelper;
import ch.framedev.marketplace.sell.SellItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class UpdateGUI {

    private final DatabaseHelper databaseHelper;

    public UpdateGUI(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public Inventory createGUI(Player player) {
        List<SellItem> playersSellItem = databaseHelper.getSellItemsByPlayer(player.getUniqueId());
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void showUpdateGUI(Player player) {
    }
}
