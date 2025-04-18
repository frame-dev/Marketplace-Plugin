package ch.framedev.marketplace.guis;



/*
 * ch.framedev.marketplace.guis
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 15.04.2025 20:49
 */

import ch.framedev.marketplace.commands.CommandUtils;
import ch.framedev.marketplace.database.DatabaseHelper;
import ch.framedev.marketplace.main.Main;
import ch.framedev.marketplace.sell.SellItem;
import ch.framedev.marketplace.utils.ConfigUtils;
import ch.framedev.marketplace.utils.ConfigVariables;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Require Testing (Not completed)
 * TODO: Require sold item functionality
 * TODO: Random Item selection
 */
public class BlackmarketGUI implements Listener {

    private String title;
    private final int size;
    private final DatabaseHelper databaseHelper;
    private final Inventory gui;
    private Map<Integer, SellItem> cacheSellItems = new HashMap<>();
    private List<SellItem> saleItems = new ArrayList<>();
    private final Set<Integer> persistentDiscountedIndices = new HashSet<>();

    private final Set<Player> viewers = new HashSet<>();

    private final CommandUtils commandUtils;

    public BlackmarketGUI(DatabaseHelper databaseHelper) {
        this.commandUtils = new CommandUtils();
        this.title = ConfigVariables.MARKETPLACE_GUI_TITLE;
        this.size = ConfigVariables.MARKETPLACE_GUI_ROW_SIZE;

        if (title == null || title.isEmpty()) {
            title = "Marketplace";
        }
        title = commandUtils.translateColor(title);
        gui = Bukkit.createInventory(null, size * 9, title);

        this.databaseHelper = databaseHelper;

        new BukkitRunnable() {
            @Override
            public void run() {
                updateGuiForViewers();
            }
        }.runTaskTimer(Main.getInstance(), 0L, 600L); // 600 ticks = 30 seconds

    }

    public Inventory getGui() {
        return gui;
    }

    public String getNavigationName(String key) {
        Map<String, Object> navigation = Main.getInstance().getConfig().getConfigurationSection("gui.marketplace.navigation." + key).getValues(false);
        return commandUtils.translateColor(((String) navigation.get("name")));
    }

    public Material getNavigationMaterial(String key) {
        Map<String, Object> navigation = Main.getInstance().getConfig().getConfigurationSection("gui.marketplace.navigation." + key).getValues(false);
        return Material.valueOf(((String) navigation.get("item")).toUpperCase());
    }

    public int getSlot(String key) {
        Map<String, Object> navigation = Main.getInstance().getConfig().getConfigurationSection("gui.marketplace.navigation." + key).getValues(false);
        return (int) navigation.get("slot");
    }

    public Inventory createGui(int page) {
        // Filter, sort, and search the materials
        List<SellItem> sellItems = databaseHelper.getAllSellItems();

        // Randomly select items for sale at half-price (only if not already selected)
        Random random = new Random();
        int maxDiscountItems = ConfigVariables.SETTINGS_BLACKMARKET_MAX_DISCOUNT_ITEMS;
        int currentDiscountedItems = databaseHelper.discountItemSize(); // Check how many items are already discounted

        if (currentDiscountedItems < maxDiscountItems) {
            int discountCount = Math.min(maxDiscountItems - currentDiscountedItems, sellItems.size());
            while (persistentDiscountedIndices.size() < discountCount) {
                int randomIndex = random.nextInt(sellItems.size());
                SellItem sellItem = sellItems.get(randomIndex);

                // Skip items that already have a discount
                if (sellItem.isDiscount()) {
                    continue;
                }

                persistentDiscountedIndices.add(randomIndex);
                sellItem.setDiscount(true);
                sellItem.setPrice(sellItem.getPrice() / 2); // Halve the price
                databaseHelper.updateSellItem(sellItem); // Update the database with the discount
            }
        }

        // 5 rows for items, 1 row for navigation
        final int ITEMS_PER_PAGE = (size - 1) * 9;
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, sellItems.size());

        for (int i = startIndex; i < endIndex; i++) {
            SellItem dataMaterial = sellItems.get(i);
            if (!cacheSellItems.containsKey(i))
                cacheSellItems.put(i, dataMaterial);
            Map<String, Object> item = Main.getInstance().getConfig().getConfigurationSection("gui.marketplace.item").getValues(true);
            String itemName = (String) item.get("name");
            itemName = commandUtils.translateColor(itemName);
            itemName = itemName.replace("{itemName}", dataMaterial.getName());
            @SuppressWarnings("unchecked") List<String> lore = (List<String>) item.get("lore");
            List<String> newLore = new ArrayList<>();
            for (String loreText : lore) {
                if (dataMaterial.isDiscount()) {
                    loreText = loreText.replace("{price}", String.valueOf(dataMaterial.getPrice()));
                } else {
                    loreText = loreText.replace("{price}", String.valueOf(dataMaterial.getPrice()));
                }
                loreText = loreText.replace("{amount}", String.valueOf(dataMaterial.getAmount()));
                loreText = loreText.replace("{itemType}", dataMaterial.getItemStack().getType().name());
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(dataMaterial.getPlayerUUID());
                if (offlinePlayer.hasPlayedBefore() && offlinePlayer.getName() != null) {
                    loreText = loreText.replace("{seller}", offlinePlayer.getName());
                } else {
                    loreText = loreText.replace("{seller}", "Unknown");
                }
                loreText = commandUtils.translateColor(loreText);
                newLore.add(loreText);
            }
            if (dataMaterial.isDiscount()) {
                newLore.add(commandUtils.translateColor("&aDiscounted: 50% OFF!")); // Add discount indicator
                databaseHelper.updateSellItem(dataMaterial);
            }

            // Apply discount if the item is selected
            if (persistentDiscountedIndices.contains(i)) {
                dataMaterial.setPrice(dataMaterial.getPrice() / 2); // Halve the price
            }

            ItemStack itemStack = dataMaterial.getItemStack().clone();
            itemStack.setAmount(dataMaterial.getAmount());
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                itemMeta.setItemName(itemName);
                itemMeta.setDisplayName(itemName);
                itemMeta.setLore(newLore);
                itemStack.setItemMeta(itemMeta);
                saleItems.add(dataMaterial);
            }
            gui.setItem(i - startIndex, itemStack);
        }

        // Navigation items
        int sizeForNavigation = size * 9 - 9;
        if (page > 0) {
            gui.setItem(sizeForNavigation + getSlot("previous"), createGuiItem(getNavigationMaterial("previous"), getNavigationName("previous")));
        }
        if (endIndex < sellItems.size()) {
            gui.setItem(sizeForNavigation + getSlot("next"), createGuiItem(getNavigationMaterial("next"), getNavigationName("next")));
        }
        gui.setItem(sizeForNavigation + getSlot("back"), createGuiItem(getNavigationMaterial("back"), getNavigationName("back")));
        gui.setItem(sizeForNavigation + getSlot("page"), createGuiItem(getNavigationMaterial("page"), getNavigationName("page").replace("{page}", String.valueOf(page + 1))));

        return gui;
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

    public void showMarketplace(Player player) {
        player.openInventory(createGui(0));
        viewers.add(player); // Add player to the viewers list
    }

    public void removeFromCache(SellItem sellItem) {
        for (Map.Entry<Integer, SellItem> entry : cacheSellItems.entrySet()) {
            if (entry.getValue().equals(sellItem)) {
                cacheSellItems.remove(entry.getKey());
                break;
            }
        }
    }

    @EventHandler
    private void handleAddMaterialsClick(InventoryClickEvent event) {

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR || event.getCurrentItem().getItemMeta() == null)
            return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        if (!title.contains(this.title)) return;
        event.setCancelled(true);
        String materialName = event.getCurrentItem().getItemMeta().getDisplayName();

        int page = getPageFromTitle(materialName);

        if (materialName.equalsIgnoreCase(getNavigationName("back"))) {
            player.closeInventory();
            viewers.remove(player); // Remove player from viewers list
            return;
        }
        if (materialName.equalsIgnoreCase(getNavigationName("previous"))) {
            player.openInventory(createGui(page - 1));
            return;
        }
        if (materialName.equalsIgnoreCase(getNavigationName("next"))) {
            player.openInventory(createGui(page + 1));
            return;
        }
        if (materialName.equalsIgnoreCase(getNavigationName("page"))) {
            event.setCancelled(true);
            return;
        }

        ItemStack itemStack = event.getCurrentItem();
        SellItem sellItem = cacheSellItems.get(event.getSlot());
        if (itemStack.getType() == Material.AIR) return;
        if (ConfigVariables.SETTINGS_BLACKMARKET_USE_CONFIRMATION) {
            Main.getInstance().getBuyGUI().showInventory(player, sellItem);
        } else {
            // Handle the item purchase logic here
            player.sendMessage("You bought: " + sellItem.getItemStack().getItemMeta().getDisplayName());
            // Remove the item from the inventory
            if (!databaseHelper.soldItem(sellItem, player)) {
                String error = ConfigVariables.ERROR_BUY;
                error = ConfigUtils.translateColor(error, "&cThere was an error buying the Item &6{itemName}&c!");
                player.sendMessage(error.replace("{itemName}", sellItem.getItemStack().getItemMeta().getDisplayName()));
                return;
            }
            player.getInventory().addItem(sellItem.getItemStack());
            player.closeInventory();
            gui.removeItem(event.getCurrentItem());
            Main.getInstance().getBlackmarketGUI().removeFromCache(sellItem);
        }
    }

    private int getPageFromTitle(String displayName) {
        try {
            // Use a regular expression to find the first number in the displayName
            Matcher matcher = Pattern.compile("\\d+").matcher(displayName);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group()) - 1;
            }
        } catch (NumberFormatException e) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Failed to parse page number from title: " + displayName, e);
        }
        return 0; // Default to page 0 if no number is found or parsing fails
    }

    @EventHandler
    private void handleInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (event.getView().getTitle().contains(this.title)) {
            viewers.remove(player); // Remove player from the viewer list when they close the GUI
        }
    }

    public List<SellItem> getSaleItems() {
        return saleItems;
    }

    private void updateGuiForViewers() {
        for (Player player : new HashSet<>(viewers)) {
            if (!player.isOnline()) {
                viewers.remove(player);
                continue;
            }

            Inventory openInventory = player.getOpenInventory().getTopInventory();
            if (openInventory.getHolder() == null && openInventory.getSize() == gui.getSize()) {
                try {
                    // Update the inventory in place
                    List<SellItem> sellItems = databaseHelper.getAllSellItems();
                    
                    int sizeForNavigation = size * 9 - 9;
                    ItemStack pageItem = openInventory.getItem(sizeForNavigation + getSlot("page"));
                    
                    // Check if pageItem exists
                    if (pageItem == null || pageItem.getItemMeta() == null) {
                        // If page item doesn't exist, recreate the GUI with page 1
                        player.openInventory(createGui(0));
                        continue;
                    }
                    
                    // Extract page number from the page item's display name
                    String pageDisplayName = pageItem.getItemMeta().getDisplayName();
                    int currentPage = 0;
                    try {
                        // Extract the number from the page display name (e.g., "Page 1" -> 1)
                        Matcher matcher = Pattern.compile("\\d+").matcher(pageDisplayName);
                        if (matcher.find()) {
                            currentPage = Integer.parseInt(matcher.group()) - 1; // Convert to 0-based index
                        }
                    } catch (NumberFormatException e) {
                        Main.getInstance().getLogger().warning("Failed to parse page number from: " + pageDisplayName);
                        currentPage = 0;
                    }
                    
                    int ITEMS_PER_PAGE = (size - 1) * 9;
                    int totalPages = (int) Math.ceil((double) sellItems.size() / ITEMS_PER_PAGE);
                    
                    // Ensure currentPage is within valid range
                    if (currentPage < 0) currentPage = 0;
                    if (currentPage >= totalPages) currentPage = totalPages - 1;
                    
                    int startIndex = currentPage * ITEMS_PER_PAGE;
                    int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, sellItems.size());

                    // Clear current cache and rebuild it
                    cacheSellItems.clear();
                    
                    // Clear the inventory
                    openInventory.clear();

                    // Populate the inventory with updated items
                    for (int i = startIndex; i < endIndex; i++) {
                        SellItem dataMaterial = sellItems.get(i);
                        cacheSellItems.put(i - startIndex, dataMaterial);
                        
                        Map<String, Object> item = Main.getInstance().getConfig().getConfigurationSection("gui.marketplace.item").getValues(true);
                        String itemName = (String) item.get("name");
                        itemName = commandUtils.translateColor(itemName);
                        itemName = itemName.replace("{itemName}", dataMaterial.getName());
                        
                        @SuppressWarnings("unchecked") 
                        List<String> lore = (List<String>) item.get("lore");
                        List<String> newLore = new ArrayList<>();
                        for (String loreText : lore) {
                            loreText = loreText.replace("{price}", String.valueOf(dataMaterial.getPrice()));
                            loreText = loreText.replace("{amount}", String.valueOf(dataMaterial.getAmount()));
                            loreText = loreText.replace("{itemType}", dataMaterial.getItemStack().getType().name());
                            
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(dataMaterial.getPlayerUUID());
                            if (offlinePlayer.hasPlayedBefore() && offlinePlayer.getName() != null) {
                                loreText = loreText.replace("{seller}", offlinePlayer.getName());
                            } else {
                                loreText = loreText.replace("{seller}", "Unknown");
                            }
                            loreText = commandUtils.translateColor(loreText);
                            newLore.add(loreText);
                        }
                        
                        if (dataMaterial.isDiscount())
                            newLore.add(commandUtils.translateColor("&aDiscounted: 50% OFF!"));

                        ItemStack itemStack = dataMaterial.getItemStack().clone();
                        itemStack.setAmount(dataMaterial.getAmount());
                        ItemMeta itemMeta = itemStack.getItemMeta();
                        if (itemMeta != null) {
                            itemMeta.setDisplayName(itemName);
                            itemMeta.setLore(newLore);
                            itemStack.setItemMeta(itemMeta);
                            if (!saleItems.contains(dataMaterial)) {
                                saleItems.add(dataMaterial);
                            }
                        }
                        openInventory.setItem(i - startIndex, itemStack);
                    }

                    // Add navigation items
                    if (currentPage > 0) {
                        openInventory.setItem(sizeForNavigation + getSlot("previous"), createGuiItem(getNavigationMaterial("previous"), getNavigationName("previous")));
                    }
                    if (endIndex < sellItems.size()) {
                        openInventory.setItem(sizeForNavigation + getSlot("next"), createGuiItem(getNavigationMaterial("next"), getNavigationName("next")));
                    }
                    openInventory.setItem(sizeForNavigation + getSlot("back"), createGuiItem(getNavigationMaterial("back"), getNavigationName("back")));
                    openInventory.setItem(sizeForNavigation + getSlot("page"), createGuiItem(getNavigationMaterial("page"), getNavigationName("page").replace("{page}", String.valueOf(currentPage + 1))));
                } catch (Exception e) {
                    Main.getInstance().getLogger().severe("Error updating marketplace GUI for " + player.getName() + ": " + e.getMessage());
                    viewers.remove(player);
                }
            }
        }
    }
}
