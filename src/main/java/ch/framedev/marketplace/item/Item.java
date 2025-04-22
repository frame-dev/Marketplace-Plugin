package ch.framedev.marketplace.item;



/*
 * ch.framedev.marketplace.sell
 * =============================================
 * This File was Created by FrameDev.
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 15.04.2025 20:15
 */

import ch.framedev.marketplace.database.DatabaseHelper;
import ch.framedev.marketplace.main.Main;
import ch.framedev.marketplace.utils.ItemHelper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Item is a class that represents an item that a player wants to sell.
 * It contains information about the item, such as its ID, player UUID, item stack, amount, and price.
 * It also provides methods to get the item's name, serialize the item stack, and send the item to the database.
 */
public class Item {

    private int id;
    private final UUID playerUUID;
    private ItemStack itemStack;
    private final int amount;
    private double price;
    private double discountPrice;
    private boolean sold;
    private boolean discount;
    private String itemName;

    public Item(int id, UUID playerUUID, ItemStack itemStack, double price, boolean sold, boolean discount, String itemName, double discountPrice) {
        this.id = id;
        this.playerUUID = playerUUID;
        this.itemName = itemName;
        this.itemStack = itemStack;
        this.amount = itemStack.getAmount();
        this.price = price;
        this.sold = sold;
        this.discount = discount;
        this.discountPrice = discountPrice;
    }

    public Item(UUID playerUUID, ItemStack itemStack, double price) {
        this.id = new Random().nextInt(0, 100000000);
        List<Integer> ids = Main.getInstance().getDatabaseHelper().getAllItems().stream().map(Item::getId).toList();
        while (ids.contains(id)) {
            id = new Random().nextInt(0, 100000000);
        }
        this.playerUUID = playerUUID;
        this.itemStack = itemStack;
        this.amount = itemStack.getAmount();
        this.price = price;
        ItemMeta itemMeta = itemStack.getItemMeta();
        this.itemName = itemMeta != null && itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : itemStack.getType().name();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDiscountPrice(double discountPrice) {
        this.discountPrice = discountPrice;
    }

    public double getDiscountPrice() {
        return discountPrice;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getAmount() {
        return amount;
    }

    public int getId() {
        return id;
    }

    public void setSold(boolean sold) {
        this.sold = sold;
    }

    public boolean isSold() {
        return sold;
    }

    public void setDiscount(boolean discount) {
        this.discount = discount;
    }

    public boolean isDiscount() {
        return discount;
    }

    public String getName() {
        return itemName;
    }

    public String serializedItemStack() {
        return ItemHelper.toBase64(itemStack);
    }

    public boolean sendToDatabase(DatabaseHelper databaseHelper) {
        return databaseHelper.sellItem(this);
    }
}
