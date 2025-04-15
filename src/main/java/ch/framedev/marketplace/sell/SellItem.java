package ch.framedev.marketplace.sell;



/*
 * ch.framedev.marketplace.sell
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 15.04.2025 20:15
 */

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Random;

public class SellItem {

    private int id;
    private Player player;
    private ItemStack itemStack;
    private int amount;
    private double price;

    public SellItem(Player player, ItemStack itemStack, double price) {
        this.id = new Random().nextInt(0, 1000000);
        this.player = player;
        this.itemStack = itemStack;
        this.amount = itemStack.getAmount();
        this.price = price;
    }

    public Player getPlayer() {
        return player;
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

    public Map<String, Object> serializedItemStack() {
        return itemStack.serialize();
    }
}
