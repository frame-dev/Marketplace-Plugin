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
import ch.framedev.marketplace.main.Main;
import ch.framedev.marketplace.transactions.Transaction;
import ch.framedev.marketplace.utils.ConfigUtils;
import ch.framedev.marketplace.utils.ConfigVariables;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransactionGUI implements Listener {

    private final DatabaseHelper databaseHelper;

    private final String title;
    private final int rowSize;

    private final Map<Integer, Item> cacheSoldItems = new HashMap<>();
    private final Map<Integer, Item> cacheSellItems = new HashMap<>();

    public TransactionGUI(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        title = ConfigUtils.translateColor(ConfigVariables.TRANSACTIONS_GUI_TITLE, "&6Transaction");
        rowSize = ConfigVariables.TRANSACTIONS_GUI_ROW_SIZE;
    }

    public void showInventory(Player player) {
        player.openInventory(createGUI(player));
    }

    private Inventory createGUI(Player player) {
        Inventory inventory = Bukkit.createInventory(null, rowSize * 9, title + ", Main");
        inventory.setItem(0, createGuiItem(Material.CHEST, "Items For Sale"));
        inventory.setItem(1, createGuiItem(Material.CHEST, "Items Sold"));
        return inventory;
    }

    private Inventory createGUISold(Player player, List<Item> itemsSold, int page) {
        Inventory soldInventory = Bukkit.createInventory(null, rowSize * 9, title + ", Sold");
        // 5 rows for items, 1 row for navigation
        final int ITEMS_PER_PAGE = soldInventory.getSize() - 9;
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, itemsSold.size());
        // Clear the cache for this page
        for (int i = startIndex; i < endIndex; i++) {
            cacheSoldItems.remove(i);
        }
        // Clear the inventory before populating it
        soldInventory.clear();
        for (int i = startIndex; i < endIndex; i++) {
            Item item = itemsSold.get(i);
            cacheSoldItems.put(i, item);
            soldInventory.setItem(i, createGuiItem(item.getItemStack().getType(), item.getName()));
        }
        int sizeForNavigation = soldInventory.getSize() - 9;
        soldInventory.setItem(sizeForNavigation + 1, createGuiItem(Material.ARROW, "Previous Page"));
        soldInventory.setItem(sizeForNavigation + 7, createGuiItem(Material.ARROW, "Next Page"));
        soldInventory.setItem(sizeForNavigation + 8, createGuiItem(Material.BARRIER, "§6Close"));
        soldInventory.setItem(sizeForNavigation + 2, createGuiItem(Material.BOOK, "§6Page - {page}".replace("{page}", String.valueOf(page + 1))));
        return soldInventory;
    }

    private Inventory createGUIForSale(Player player, List<Item> itemsForSale, int page) {
        Inventory forSaleInventory = Bukkit.createInventory(null, rowSize * 9, title + ", For Sale");
        // 5 rows for items, 1 row for navigation
        final int ITEMS_PER_PAGE = forSaleInventory.getSize() - 9;
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, itemsForSale.size());
        // Clear the cache for this page
        for (int i = startIndex; i < endIndex; i++) {
            cacheSellItems.remove(i);
        }
        // Clear the inventory before populating it
        forSaleInventory.clear();
        for (int i = startIndex; i < endIndex; i++) {
            Item item = itemsForSale.get(i);
            cacheSellItems.put(i, item);
            forSaleInventory.setItem(i, createGuiItem(item.getItemStack().getType(), item.getName()));
        }
        int sizeForNavigation = forSaleInventory.getSize() - 9;
        forSaleInventory.setItem(sizeForNavigation + 1, createGuiItem(Material.ARROW, "Previous Page"));
        forSaleInventory.setItem(sizeForNavigation + 7, createGuiItem(Material.ARROW, "Next Page"));
        forSaleInventory.setItem(sizeForNavigation + 8, createGuiItem(Material.BARRIER, "§6Close"));
        forSaleInventory.setItem(sizeForNavigation + 2, createGuiItem(Material.BOOK, "§6Page - {page}".replace("{page}", String.valueOf(page + 1))));
        return forSaleInventory;
    }

    public Inventory createGUIItem(Player player, Item item) {
        Inventory inventory = Bukkit.createInventory(null, 9, title + ", Information");
        ItemStack itemStack = item.getItemStack();
        Transaction transaction = databaseHelper.getTransaction(player.getUniqueId()).orElse(null);
        if(transaction == null) {
            return inventory;
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&',"&7Item ID: &6" + item.getId()));
            lore.add(ChatColor.translateAlternateColorCodes('&',"&7Item Name: &6" + item.getName()));
            lore.add(ChatColor.translateAlternateColorCodes('&',"&7Item Type: &6" + itemStack.getType().name()));
            lore.add(ChatColor.translateAlternateColorCodes('&',"&7Item Price: &6" + item.getPrice()));
            lore.add(ChatColor.translateAlternateColorCodes('&',"&7Item Amount: &6" + item.getAmount()));
            lore.add(ChatColor.translateAlternateColorCodes('&',"&7Item Discount: &6" + item.isDiscount()));
            if (item.isDiscount()) {
                lore.add(ChatColor.translateAlternateColorCodes('&',"&7Item Discount Price: &6" + item.getDiscountPrice()));
            }
            if(item.isSold()) {
                lore.add(ChatColor.translateAlternateColorCodes('&', "&7Receiver: &6" + Bukkit.getOfflinePlayer(transaction.getReceivers().get(item.getId())).getName()));
            }
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
        inventory.setItem(0, itemStack);
        inventory.setItem(8, createGuiItem(Material.BARRIER, "§6Close"));
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
    public void onClickItemMain(InventoryClickEvent event) {
        if(!event.getView().getTitle().equalsIgnoreCase(title + ", Main")) return;
        if(event.getCurrentItem() == null) return;
        if(event.getCurrentItem().getItemMeta() == null) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
        if(itemName.equalsIgnoreCase("Items For Sale")) {
            List<Item> itemsForSale = databaseHelper.getItemsByPlayer(player.getUniqueId()).stream().filter(item -> !item.isSold()).toList();
            if(itemsForSale.isEmpty()) {
                player.sendMessage(ChatColor.RED + "You have no items for sale.");
                return;
            }
            Inventory inventory = createGUIForSale(player, itemsForSale, 0);
            player.openInventory(inventory);
        } else if(itemName.equalsIgnoreCase("Items Sold")) {
            List<Item> itemsSold = databaseHelper.getItemsByPlayer(player.getUniqueId()).stream().filter(Item::isSold).toList();
            if(itemsSold.isEmpty()) {
                player.sendMessage(ChatColor.RED + "You have no items sold.");
                return;
            }
            Inventory inventory = createGUISold(player, itemsSold, 0);
            player.openInventory(inventory);
        } else if(itemName.equalsIgnoreCase("§6Close"))
            player.closeInventory();
    }

    @EventHandler
    public void onClickSale(InventoryClickEvent event) {
        if(!event.getView().getTitle().equalsIgnoreCase(title + ", For Sale")) return;
        if(event.getCurrentItem() == null) return;
        if(event.getCurrentItem().getItemMeta() == null) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
        int sizeForNavigation = event.getClickedInventory().getSize() - 9;
        ItemStack pageItem = event.getClickedInventory().getItem(sizeForNavigation + 2);
        int page = getPageFromItemName(pageItem.getItemMeta().getDisplayName());
        List<Item> itemsForSale = databaseHelper.getItemsByPlayer(player.getUniqueId()).stream().filter(item -> !item.isSold()).toList();
        if(itemName.equalsIgnoreCase("Previous Page")) {
            Inventory inventory = createGUIForSale(player, itemsForSale, page);
            player.openInventory(inventory);
        } else if(itemName.equalsIgnoreCase("Next Page")) {
            Inventory inventory = createGUIForSale(player, itemsForSale, page + 1);
            player.openInventory(inventory);
        } else if(itemName.equalsIgnoreCase("§6Close")) {
            player.closeInventory();
        } else {
            Item item = cacheSellItems.get(event.getSlot());
            if(item != null) {
                Inventory inventory = createGUIItem(player, item);
                player.openInventory(inventory);
            }
        }
    }

    @EventHandler
    public void onClickSold(InventoryClickEvent event) {
        if(!event.getView().getTitle().equalsIgnoreCase(title + ", Sold")) return;
        if(event.getCurrentItem() == null) return;
        if(event.getCurrentItem().getItemMeta() == null) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
        int sizeForNavigation = event.getClickedInventory().getSize() - 9;
        ItemStack pageItem = event.getClickedInventory().getItem(sizeForNavigation + 2);
        int page = getPageFromItemName(pageItem.getItemMeta().getDisplayName());
        List<Item> itemsSold = databaseHelper.getItemsByPlayer(player.getUniqueId()).stream().filter(Item::isSold).toList();
        if(itemName.equalsIgnoreCase("Previous Page")) {
            Inventory inventory = createGUISold(player, itemsSold, page);
            player.openInventory(inventory);
        } else if(itemName.equalsIgnoreCase("Next Page")) {
            Inventory inventory = createGUISold(player, itemsSold, page + 1);
            player.openInventory(inventory);
        } else if(itemName.equalsIgnoreCase("§6Close")) {
            player.closeInventory();
        } else {
            Item item = cacheSoldItems.get(event.getSlot());
            if(item != null) {
                Inventory inventory = createGUIItem(player, item);
                player.openInventory(inventory);
            }
        }
    }

    @EventHandler
    public void onClickItem(InventoryClickEvent event) {
        if(event.getCurrentItem() == null) return;
        if(event.getCurrentItem().getItemMeta() == null) return;
        if(!event.getView().getTitle().equalsIgnoreCase(title + ", Information")) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
        if(itemName.equalsIgnoreCase("§6Close")) {
            player.closeInventory();
        }
    }

    private int getPageFromItemName(String displayName) {
        try {
            // Use a regular expression to find the number after the word "Page"
            Matcher matcher = Pattern.compile("Page\\s+-?\\d+").matcher(displayName);
            if (matcher.find()) {
                String match = matcher.group(); // e.g., "Page 2"
                String number = match.replaceAll("[^\\d-]", ""); // Extract only the number
                return Integer.parseInt(number);
            }
        } catch (NumberFormatException e) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Failed to parse page number from title: " + displayName, e);
        }
        return 0; // Default to page 0 if no number is found or parsing fails
    }
}
