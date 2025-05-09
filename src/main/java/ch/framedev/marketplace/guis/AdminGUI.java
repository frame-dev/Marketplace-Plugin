package ch.framedev.marketplace.guis;



/*
 * ch.framedev.marketplace.guis
 * =============================================
 * This File was Created by FrameDev.
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 20.04.2025 13:39
 */

import ch.framedev.marketplace.commands.CommandUtils;
import ch.framedev.marketplace.database.DatabaseHelper;
import ch.framedev.marketplace.item.Item;
import ch.framedev.marketplace.main.Main;
import ch.framedev.marketplace.utils.ConfigUtils;
import ch.framedev.marketplace.utils.ConfigVariables;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("DataFlowIssue")
public class AdminGUI implements Listener {
    
    private final Main plugin;

    private final Inventory gui;
    private final DatabaseHelper databaseHelper;
    private final CommandUtils commandUtils = new CommandUtils();
    private String title;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<Integer, Item> cacheItems = new HashMap<>();
    private final List<Item> saleItems = new ArrayList<>();

    public AdminGUI(Main plugin, DatabaseHelper databaseHelper) {
        this.plugin = plugin;
        this.databaseHelper = databaseHelper;

        this.title = ConfigVariables.ADMIN_GUI_TITLE;
        this.title = ConfigUtils.translateColor(title, "&6Admin GUI");
        this.gui = Bukkit.createInventory(null, 3 * 9, title);
    }

    public String getNavigationName(String key) {
        Map<String, Object> navigation = plugin.getConfig().getConfigurationSection("gui.marketplace.navigation." + key).getValues(false);
        return commandUtils.translateColor(((String) navigation.get("name")));
    }

    public Material getNavigationMaterial(String key) {
        Map<String, Object> navigation = plugin.getConfig().getConfigurationSection("gui.marketplace.navigation." + key).getValues(false);
        return Material.valueOf(((String) navigation.get("item")).toUpperCase());
    }

    public int getSlot(String key) {
        Map<String, Object> navigation = plugin.getConfig().getConfigurationSection("gui.marketplace.navigation." + key).getValues(false);
        return (int) navigation.get("slot");
    }

    public Inventory createGui(int page) {
        // Filter, sort, and search the materials
        List<Item> items = databaseHelper.getAllItemsSoldSell();

        // 5 rows for items, 1 row for navigation
        final int ITEMS_PER_PAGE = gui.getSize() - 9;
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

            Map<String, Object> item = plugin.getConfig().getConfigurationSection("gui.blackmarket.item").getValues(true);
            String itemName = (String) item.get("name");
            itemName = commandUtils.translateColor(itemName);
            itemName = itemName.replace("{itemName}", ChatColor.RESET + dataMaterial.getName());

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
                discountText = discountText.replace("{newPrice}", String.valueOf(dataMaterial.getDiscountPrice()));
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
        int sizeForNavigation = gui.getSize() - 9;
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

    public void showAdminInventory(Player player) {
        player.openInventory(createGui(0));
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
    private void handleAddMaterialsClick(InventoryClickEvent event) {

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR || event.getCurrentItem().getItemMeta() == null)
            return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        if (!title.contains(this.title)) return;
        event.setCancelled(true);
        String materialName = event.getCurrentItem().getItemMeta().getDisplayName();

        int page = getPageFromItemName(materialName);

        if (materialName.equalsIgnoreCase(getNavigationName("back"))) {
            player.closeInventory();
            return;
        }
        if (materialName.equalsIgnoreCase(getNavigationName("previous"))) {
            player.openInventory(createGui(page));
            return;
        }
        if (materialName.equalsIgnoreCase(getNavigationName("next"))) {
            player.openInventory(createGui(page + 1));
            return;
        }

        // Open the Deeper GUI with the clicked item
        Item item = databaseHelper.getItemByName(materialName);
        /*
          Check if the item is null with has been retrieved from the Database.
         */
        if (item == null) {
            player.sendMessage("§cItem could not be found in Database §6" + materialName + "!");
            return;
        }
        // Opens the GUI with the Item
        plugin.getAdminDeeperGUI().showInventory(player, item);
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
            plugin.getLogger().log(Level.SEVERE, "Failed to parse page number from title: " + displayName, e);
        }
        return 0; // Default to page 0 if no number is found or parsing fails
    }
}
