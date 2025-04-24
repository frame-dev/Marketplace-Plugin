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
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

/**
 * Item is a class that represents an item that a player wants to sell.
 * It contains information about the item, such as its ID, player UUID, item stack, amount, and price.
 * It also provides methods to get the item's name, serialize the item stack, and send the item to the database.
 */
public class Item {

    private UUID id;
    private final UUID playerUUID;
    private ItemStack itemStack;
    private final int amount;
    private double price;
    private double discountPrice;
    private boolean sold;
    private boolean discount;
    private String itemName;

    public Item(UUID id, UUID playerUUID, ItemStack itemStack, double price, boolean sold, boolean discount, String itemName, double discountPrice) {
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
        this.id = UUID.randomUUID();
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

    public void setId(UUID id) {
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

    public UUID getId() {
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Item item)) return false;
        return getAmount() == item.getAmount() && Double.compare(getPrice(), item.getPrice()) == 0 && Double.compare(getDiscountPrice(), item.getDiscountPrice()) == 0 && isSold() == item.isSold() && isDiscount() == item.isDiscount() && Objects.equals(getId(), item.getId()) && Objects.equals(getPlayerUUID(), item.getPlayerUUID()) && Objects.equals(getItemStack(), item.getItemStack()) && Objects.equals(itemName, item.itemName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getPlayerUUID(), getItemStack(), getAmount(), getPrice(), getDiscountPrice(), isSold(), isDiscount(), itemName);
    }
}
