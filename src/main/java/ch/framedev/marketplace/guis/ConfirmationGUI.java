package ch.framedev.marketplace.guis;



/*
 * ch.framedev.marketplace.guis
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 16.04.2025 16:51
 */

import ch.framedev.marketplace.database.DatabaseHelper;
import ch.framedev.marketplace.main.Main;
import ch.framedev.marketplace.item.Item;
import ch.framedev.marketplace.utils.ConfigUtils;
import ch.framedev.marketplace.utils.ConfigVariables;
import ch.framedev.marketplace.utils.InventoryBuilder;
import ch.framedev.marketplace.vault.VaultManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class ConfirmationGUI implements Listener {

    private final Inventory inventory;
    private final String title;

    private final int[] slots;

    private final DatabaseHelper databaseHelper;

    private final Map<Player, Item> playerItems = new HashMap<>();
    private final VaultManager vaultManager;

    public ConfirmationGUI(Main plugin, DatabaseHelper databaseHelper) {
        this.vaultManager = plugin.getVaultManager();
        this.databaseHelper = databaseHelper;
        this.title = "Buy GUI";
        InventoryBuilder inventoryBuilder = new InventoryBuilder(title, 3 * 9).
                build().fillNull();
        this.inventory = inventoryBuilder.getInventory();

        int rows = inventory.getSize() / 9;
        int middleRowStart = (rows / 2) * 9; // Start index of the middle row
        slots = getSpecificSlots(middleRowStart + 3, middleRowStart + 5);
        if (slots.length >= 2) { // Ensure the slot array has at least two elements
            for (int i = 0; i < slots.length; i++) {
                int slot = slots[i];
                ItemStack item = new ItemStack(Material.DIAMOND); // Example item
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("Item " + (i + 1));
                    item.setItemMeta(meta);
                }
                inventory.setItem(slot, item);
            }

            ItemStack acceptItem = createItemStack(Material.GREEN_STAINED_GLASS_PANE, "§aAccept");
            ItemStack denyItem = createItemStack(Material.RED_STAINED_GLASS_PANE, "§cDeny");
            inventory.setItem(slots[0], denyItem);
            inventory.setItem(slots[1], acceptItem);
        } else {
            throw new IllegalStateException("Slots array must have at least two elements.");
        }
    }

    private ItemStack createItemStack(Material material, String displayName) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta != null) {
            itemMeta.setDisplayName(displayName);
            itemMeta.setItemName(displayName);
            itemStack.setItemMeta(itemMeta);
        } else {
            String itemMetaNotFoundMessage = ConfigVariables.ERROR_ITEM_META_NOT_FOUND;
            itemMetaNotFoundMessage = ConfigUtils.translateColor(itemMetaNotFoundMessage, "&cItemMeta for &6{itemName} &c not found!");
            itemMetaNotFoundMessage = itemMetaNotFoundMessage.replace("{itemName}", displayName);
            Main.getInstance().getLogger().severe(itemMetaNotFoundMessage);
        }
        return itemStack;
    }

    public void showInventory(Player player, Item item) {
        player.openInventory(inventory);
        System.out.println(item.getName());
        playerItems.put(player, item);
    }

    public int[] getSpecificSlots(int... slots) {
        return slots;
    }

    public Material getNavigationMaterial(String key) {
        Map<String, Object> navigation = Main.getInstance().getConfig().getConfigurationSection("gui.marketplace.navigation." + key).getValues(false);
        return Material.valueOf(((String) navigation.get("item")).toUpperCase());
    }

    @EventHandler
    public void onClickItem(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(title)) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                Player player = (Player) event.getWhoClicked();
                if (event.getSlot() == slots[1] && event.getCurrentItem().getType() != getNavigationMaterial("back") &&
                    event.getCurrentItem().getType() != getNavigationMaterial("next") &&
                    event.getCurrentItem().getType() != getNavigationMaterial("page") &&
                    event.getCurrentItem().getType() != getNavigationMaterial("previous")) {
                    Item item = databaseHelper.getItem(playerItems.get(player).getId());
                    if (item != null) {
                        if(item.getItemStack().getItemMeta() == null) return;
                        // Remove the item from the inventory
                        if (!databaseHelper.soldItem(item, player)) {
                            String error = ConfigVariables.ERROR_BUY;
                            error = ConfigUtils.translateColor(error, "&cThere was an error buying the Item &6{itemName}&c!");
                            player.sendMessage(error.replace("{itemName}", item.getItemStack().getItemMeta().getDisplayName()));
                            return;
                        } else {
                            if(item.isDiscount()) {
                                if(!vaultManager.getEconomy().has(player, item.getPrice())) {
                                    // Not enough messages
                                    return;
                                } else {
                                    vaultManager.getEconomy().withdrawPlayer(player, item.getPrice());
                                    vaultManager.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(item.getPlayerUUID()), item.getPrice() * 2);
                                    player.getInventory().addItem(item.getItemStack());
                                    player.closeInventory();
                                    Main.getInstance().getBlackmarketGUI().getGui().remove(event.getCurrentItem());
                                    Main.getInstance().getBlackmarketGUI().removeFromCache(item);
                                }
                            } else {
                                vaultManager.getEconomy().withdrawPlayer(player, item.getPrice());
                                vaultManager.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(item.getPlayerUUID()), item.getPrice());
                                player.getInventory().addItem(item.getItemStack());
                                player.closeInventory();
                                Main.getInstance().getBlackmarketGUI().getGui().remove(event.getCurrentItem());
                                Main.getInstance().getBlackmarketGUI().removeFromCache(item);
                            }
                        }
                    } else {
                        player.sendMessage("You don't have any items to buy.");
                    }
                } else if (event.getSlot() == slots[0]) {
                    Main.getInstance().getBlackmarketGUI().showMarketplace(player);
                }
            }
        }
    }
}
