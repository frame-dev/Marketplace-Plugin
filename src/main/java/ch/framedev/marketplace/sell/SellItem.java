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
import ch.framedev.marketplace.utils.ItemHelper;
import org.bukkit.inventory.ItemStack;

import java.util.Random;
import java.util.UUID;

/**
 * SellItem is a class that represents an item that a player wants to sell.
 * It contains information about the item, such as its ID, player UUID, item stack, amount, and price.
 * It also provides methods to get the item's name, serialize the item stack, and send the item to the database.
 */
public class SellItem {

    private int id;
    private final UUID playerUUID;
    private final ItemStack itemStack;
    private final int amount;
    private final double price;

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
