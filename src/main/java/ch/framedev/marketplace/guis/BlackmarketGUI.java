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
import ch.framedev.marketplace.item.Item;
import ch.framedev.marketplace.utils.ConfigUtils;
import ch.framedev.marketplace.utils.ConfigVariables;
import ch.framedev.marketplace.vault.VaultManager;
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
    private final Map<Integer, Item> cacheItems = new HashMap<>();
    private final List<Item> saleItems = new ArrayList<>();
    private final Set<Integer> persistentDiscountedIndices = new HashSet<>();

    private final Set<Player> viewers = new HashSet<>();

    private final CommandUtils commandUtils;
    private final VaultManager vaultManager;

    public BlackmarketGUI(Main plugin, DatabaseHelper databaseHelper) {
        this.commandUtils = new CommandUtils();
        this.title = ConfigVariables.BLACKMARKET_GUI_TITLE;
        this.size = ConfigVariables.BLACKMARKET_GUI_ROW_SIZE;

        if (title == null || title.isEmpty()) {
            title = "Marketplace";
        }
        title = commandUtils.translateColor(title);
        gui = Bukkit.createInventory(null, size * 9, title);

        this.databaseHelper = databaseHelper;
        this.vaultManager = plugin.getVaultManager();

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
        Map<String, Object> navigation = Main.getInstance().getConfig().getConfigurationSection("gui.blackmarket.navigation." + key).getValues(false);
        return commandUtils.translateColor(((String) navigation.get("name")));
    }

    public Material getNavigationMaterial(String key) {
        Map<String, Object> navigation = Main.getInstance().getConfig().getConfigurationSection("gui.blackmarket.navigation." + key).getValues(false);
        return Material.valueOf(((String) navigation.get("item")).toUpperCase());
    }

    public int getSlot(String key) {
        Map<String, Object> navigation = Main.getInstance().getConfig().getConfigurationSection("gui.blackmarket.navigation." + key).getValues(false);
        return (int) navigation.get("slot");
    }

    public Inventory createGui(int page) {
        // Filter, sort, and search the materials
        List<Item> items = databaseHelper.getAllItems();

        // Randomly select items for sale at half-price (only if not already selected)
        Random random = new Random();
        int maxDiscountItems = ConfigVariables.SETTINGS_BLACKMARKET_MAX_DISCOUNT_ITEMS;
        int currentDiscountedItems = databaseHelper.discountItemSize(); // Check how many items are already discounted

        if (currentDiscountedItems < maxDiscountItems) {
            int discountCount = Math.min(maxDiscountItems - currentDiscountedItems, items.size());
            int attempts = 0;
            int maxAttempts = items.size() * 2; // Prevent infinite loop
            
            while (persistentDiscountedIndices.size() < discountCount && attempts < maxAttempts) {
                attempts++;
                int randomIndex = random.nextInt(items.size());
                
                // Skip if this index is already in the persistent set
                if (persistentDiscountedIndices.contains(randomIndex)) {
                    continue;
                }
                
                Item item = items.get(randomIndex);
                
                // Add to persistent set and apply discount
                persistentDiscountedIndices.add(randomIndex);
                item.setDiscount(true);
                item.setPrice(item.getPrice() / 2); // Halve the price
                databaseHelper.updateSellItem(item); // Update the database with the discount
            }
            
            // Log if we couldn't find enough items to discount
            if (persistentDiscountedIndices.size() < discountCount) {
                Main.getInstance().getLogger().warning("Could only find " + persistentDiscountedIndices.size() + 
                    " items to discount out of " + discountCount + " requested");
            }
        }

        // 5 rows for items, 1 row for navigation
        final int ITEMS_PER_PAGE = (size - 1) * 9;
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, items.size());

        // Clear the inventory before populating it
        gui.clear();
        
        // Clear the cache for this page
        for (int i = startIndex; i < endIndex; i++) {
            cacheItems.remove(i);
        }

        for (int i = startIndex; i < endIndex; i++) {
            Item dataMaterial = items.get(i);
            cacheItems.put(i, dataMaterial);
            
            Map<String, Object> item = Main.getInstance().getConfig().getConfigurationSection("gui.blackmarket.item").getValues(true);
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
            
            if (dataMaterial.isDiscount()) {
                String discountText = item.get("discount").toString();
                discountText = commandUtils.translateColor(discountText);
                newLore.add(discountText); // Add discount indicator
            }

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
            
            gui.setItem(i - startIndex, itemStack);
        }

        // Navigation items
        int sizeForNavigation = size * 9 - 9;
        if (page > 0) {
            gui.setItem(sizeForNavigation + getSlot("previous"), createGuiItem(getNavigationMaterial("previous"), getNavigationName("previous")));
        }
        if (endIndex < items.size()) {
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

    public void removeFromCache(Item item) {
        for (Map.Entry<Integer, Item> entry : cacheItems.entrySet()) {
            if (entry.getValue().equals(item)) {
                cacheItems.remove(entry.getKey());
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
        Item item = cacheItems.get(event.getSlot());
        if (itemStack.getType() == Material.AIR) return;
        if (ConfigVariables.SETTINGS_BLACKMARKET_USE_CONFIRMATION) {
            Main.getInstance().getBuyGUI().showInventory(player, item);
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

    public List<Item> getSaleItems() {
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
                    List<Item> items = databaseHelper.getAllItems();

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
                    int totalPages = (int) Math.ceil((double) items.size() / ITEMS_PER_PAGE);

                    // Ensure currentPage is within valid range
                    if (currentPage < 0) currentPage = 0;
                    if (currentPage >= totalPages) currentPage = totalPages - 1;

                    int startIndex = currentPage * ITEMS_PER_PAGE;
                    int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, items.size());

                    // Clear current cache and rebuild it
                    cacheItems.clear();

                    // Clear the inventory
                    openInventory.clear();

                    // Populate the inventory with updated items
                    for (int i = startIndex; i < endIndex; i++) {
                        Item dataMaterial = items.get(i);
                        cacheItems.put(i - startIndex, dataMaterial);

                        Map<String, Object> item = Main.getInstance().getConfig().getConfigurationSection("gui.blackmarket.item").getValues(true);
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

                        String discountText = item.get("discount").toString();
                        discountText = commandUtils.translateColor(discountText);
                        if (dataMaterial.isDiscount())
                            newLore.add(discountText);

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
                    if (endIndex < items.size()) {
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
