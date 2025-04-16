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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarketplaceGUI implements Listener {

    private String title;
    private final int size;
    private final DatabaseHelper databaseHelper;
    private final Inventory gui;

    private final CommandUtils commandUtils;

    public MarketplaceGUI() {
        this.commandUtils = new CommandUtils();
        this.title = ConfigUtils.MARKETPLACE_GUI_TITLE;
        this.size = ConfigUtils.MARKETPLACE_GUI_ROW_SIZE;

        if (title == null || title.isEmpty()) {
            title = "Marketplace";
        }
        title = commandUtils.translateColor(title);
        gui = Bukkit.createInventory(null, size * 9, title);

        this.databaseHelper = new DatabaseHelper();
    }

    public String getNavigationName(String key) {
        Map<String, Object> navigation = Main.getInstance().getConfig().getConfigurationSection("gui.marketplace.navigation." + key).getValues(false);
        return (String) navigation.get("name");
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

        // 5 rows for items, 1 row for navigation
        final int ITEMS_PER_PAGE = size - 1;
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, sellItems.size());

        for (int i = startIndex; i < endIndex; i++) {
            SellItem dataMaterial = sellItems.get(i);
            Map<String, Object> item = Main.getInstance().getConfig().getConfigurationSection("gui.marketplace.item").getValues(false);
            String itemName = (String) item.get("name");
            itemName = commandUtils.translateColor(itemName);
            itemName = itemName.replace("{itemName}", dataMaterial.getName());
            @SuppressWarnings("unchecked") List<String> lore = (List<String>) item.get("lore");
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
            gui.setItem(sizeForNavigation + getSlot("previous"), createGuiItem(getNavigationMaterial("previous"), getNavigationName("previous")));
        }
        if (endIndex < sellItems.size()) {
            gui.setItem(sizeForNavigation + getSlot("next"), createGuiItem(getNavigationMaterial("next"), getNavigationName("next")));
        }
        gui.setItem(sizeForNavigation + getSlot("back"), createGuiItem(getNavigationMaterial("back"), getNavigationName("back")));
        gui.setItem(sizeForNavigation  + getSlot("page"), createGuiItem(getNavigationMaterial("page"), getNavigationName("page").replace("{page}", String.valueOf(page + 1))));

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
    }

    private int getPageFromTitle(String displayName) {
        try {
            // Use a regular expression to find the first number in the title
            Matcher matcher = Pattern.compile("\\d+").matcher(title);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group()) - 1;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0; // Default to page 0 if no number is found or parsing fails
    }
}
