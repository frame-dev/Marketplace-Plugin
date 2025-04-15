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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MarketplaceGUI implements Listener {

    private String title;
    private final int size;
    private final DatabaseHelper databaseHelper;

    private String previousPageName;
    private String backName;
    private String nextPageName;

    private final CommandUtils commandUtils;

    public MarketplaceGUI() {
        this.title = ConfigUtils.MARKETPLACE_GUI_TITLE;
        this.size = ConfigUtils.MARKETPLACE_GUI_ROW_SIZE;

        this.databaseHelper = new DatabaseHelper();
        this.commandUtils = new CommandUtils();
    }

    public Inventory createGui(int page) {
        title = title.replace("{page}", String.valueOf(page + 1));
        title = commandUtils.translateColor(title);
        Inventory gui = Bukkit.createInventory(null, size * 9, title);

        // Filter, sort, and search the materials
        List<SellItem> sellItems = databaseHelper.getAllSellItems();

        // 5 rows for items, 1 row for navigation
        final int ITEMS_PER_PAGE = size - 1;
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, sellItems.size());

        for (int i = startIndex; i < endIndex; i++) {
            SellItem dataMaterial = sellItems.get(i);
            Map<String, Object> previousPage = Main.getInstance().getConfig().getConfigurationSection("gui.marketplace.item").getValues(false);
            String itemName = (String) previousPage.get("name");
            itemName = commandUtils.translateColor(itemName);
            itemName = itemName.replace("{itemName}", dataMaterial.getName());
            @SuppressWarnings("unchecked") List<String> lore = (List<String>) previousPage.get("lore");
            List<String> newLore = new ArrayList<>();
            for(String loreText : lore) {
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
            ItemStack itemStack = dataMaterial.getItemStack();
            itemStack.setAmount(dataMaterial.getAmount());
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                itemMeta.setDisplayName(itemName);
                itemMeta.setLore(newLore);
                itemStack.setItemMeta(itemMeta);
            }
            gui.setItem(i - startIndex, itemStack);
        }

        // Navigation items
        int sizeForNavigation = size * 9 - 9;
        if (page > 0) {
            Map<String, Object> previousPage = Main.getInstance().getConfig().getConfigurationSection("gui.marketplace.navigation.previousPage").getValues(false);
            previousPageName = (String) previousPage.get("name");
            previousPageName = commandUtils.translateColor(previousPageName);
            Material material = Material.valueOf(((String) previousPage.get("item")).toUpperCase());
            int slot = (int) previousPage.get("slot");
            gui.setItem(sizeForNavigation + slot, createGuiItem(material, previousPageName));
        }
        if (endIndex < sellItems.size()) {
            Map<String, Object> nextPage = Main.getInstance().getConfig().getConfigurationSection("gui.marketplace.navigation.nextPage").getValues(false);
            nextPageName = (String) nextPage.get("name");
            nextPageName = commandUtils.translateColor(nextPageName);
            Material material = Material.valueOf(((String) nextPage.get("item")).toUpperCase());
            int slot = (int) nextPage.get("slot");
            gui.setItem(sizeForNavigation + slot, createGuiItem(material, nextPageName));
        }
        Map<String, Object> back = Main.getInstance().getConfig().getConfigurationSection("gui.marketplace.navigation.back").getValues(false);
        backName = (String) back.get("name");
        backName = commandUtils.translateColor(backName);
        Material material = Material.valueOf(((String) back.get("item")).toUpperCase());
        int slot = (int) back.get("slot");
        gui.setItem(sizeForNavigation + slot, createGuiItem(material, backName));

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
    }

    @EventHandler
    private void handleAddMaterialsClick(InventoryClickEvent event) {

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR || event.getCurrentItem().getItemMeta() == null) return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        if (title.contains(this.title)) {
            event.setCancelled(true);
            String materialName = event.getCurrentItem().getItemMeta().getDisplayName();
            ItemStack item = event.getCurrentItem();

            int page = getPageFromTitle(title);

            if (materialName.equalsIgnoreCase(backName)) {
                player.closeInventory();
                return;
            }
            if (materialName.equalsIgnoreCase(previousPageName)) {
                player.openInventory(createGui(page - 1));
                return;
            }
            if (materialName.equalsIgnoreCase(nextPageName)) {
                player.openInventory(createGui(page + 1));
                return;
            }
        } else {
            event.setCancelled(false);
        }
    }

    private int getPageFromTitle(String title) {
        String[] parts = title.split(" ");
        try {
            return Integer.parseInt(parts[parts.length - 1]) - 1;
        } catch (NumberFormatException e) {
            return 0; // Default to page 0 if parsing fails
        }
    }
}
