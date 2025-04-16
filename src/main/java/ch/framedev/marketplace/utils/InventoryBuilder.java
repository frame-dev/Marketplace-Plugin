package ch.framedev.marketplace.utils;



/*
 * ch.framedev.marketplace.utils
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 16.04.2025 16:51
 */

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryBuilder {

    Inventory inventory;

    String titleName;
    int size;

    public InventoryBuilder() {
        this.titleName = "Inventory";
        this.size = 54;
    }

    public InventoryBuilder(String titleName) {
        this.titleName = titleName;
        this.size = 54;
    }

    public InventoryBuilder(String titleName, int size) {
        this.titleName = titleName;
        this.size = size;
    }

    public String getTitleName() {
        return titleName;
    }

    public InventoryBuilder setTitleName(String titleName) {
        this.titleName = titleName;
        return this;
    }

    public int getSize() {
        return size;
    }

    public InventoryBuilder setSize(int size) {
        this.size = size;
        return this;
    }

    public InventoryBuilder build() {
        // Create the inventory with the specified title and size
        this.inventory = org.bukkit.Bukkit.createInventory(null, size, titleName);
        return this;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public InventoryBuilder setItem(int slot, ItemStack item) {
        if(inventory == null) {
            throw new IllegalStateException("Inventory is not built yet. Call build() before setting items.");
        }
        // Add item to the inventory at the specified slot
        inventory.setItem(slot, item);
        return this;
    }

    public InventoryBuilder addItem(ItemStack item) {
        if(inventory == null) {
            throw new IllegalStateException("Inventory is not built yet. Call build() before adding items.");
        }
        // Add item to the inventory
        inventory.addItem(item);
        return this;
    }

    public InventoryBuilder fillNull() {
        if(inventory == null) {
            throw new IllegalStateException("Inventory is not built yet. Call build() before filling.");
        }

        // Fill the inventory with null items
        for (int i = 0; i < size; i++) {
            ItemStack itemStack = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                itemMeta.setDisplayName(" ");
                itemStack.setItemMeta(itemMeta);
            }
            inventory.setItem(i, itemStack);
        }
        return this;
    }
}
