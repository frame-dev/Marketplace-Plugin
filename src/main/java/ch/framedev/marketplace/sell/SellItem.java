package ch.framedev.marketplace.sell;



/*
 * ch.framedev.marketplace.sell
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 15.04.2025 20:15
 */

import ch.framedev.marketplace.database.DatabaseHelper;
import ch.framedev.marketplace.main.Main;
import ch.framedev.marketplace.utils.ItemHelper;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class SellItem {

    private int id;
    private UUID playerUUID;
    private ItemStack itemStack;
    private int amount;
    private double price;

    public SellItem(int id, UUID playerUUID, ItemStack itemStack, double price) {
        this.id = id;
        this.playerUUID = playerUUID;
        this.itemStack = itemStack;
        this.amount = itemStack.getAmount();
        this.price = price;
    }

    public SellItem(UUID playerUUID, ItemStack itemStack, double price) {
        this.id = new Random().nextInt(0, 1000000);
        this.playerUUID = playerUUID;
        this.itemStack = itemStack;
        this.amount = itemStack.getAmount();
        this.price = price;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public double getPrice() {
        return price;
    }

    public int getAmount() {
        return amount;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        if(itemStack.getItemMeta() == null) {
            return itemStack.getType().name();
        }
        return itemStack.getItemMeta().getDisplayName();
    }

    public String serializedItemStack() {
        return ItemHelper.toBase64(itemStack);
    }

    public void sendToDatabase(DatabaseHelper databaseHelper) {
        databaseHelper.sellItem(this);
    }
}
