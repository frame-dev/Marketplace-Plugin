package ch.framedev.marketplace.guis;



/*
 * ch.framedev.marketplace.guis
 * =============================================
 * This File was Created by FrameDev.
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
import ch.framedev.marketplace.utils.DiscordWebhook;
import ch.framedev.marketplace.vault.VaultManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlackmarketGUI implements Listener {

    private final Main plugin;

    // Inventory Title
    private String title;
    // Inventory Size
    private final int size;
    // Database Helper for database stuff
    private final DatabaseHelper databaseHelper;
    // Inventory for the GUI
    private final Inventory gui;
    // Cache for items
    private final Map<Integer, Item> cacheItems = new HashMap<>();
    // List of items for sale
    private final List<Item> saleItems = new ArrayList<>();
    // Set of discounted item indices to persist across sessions
    private final Set<Integer> persistentDiscountedIndices = new HashSet<>();

    // Set of players currently viewing the GUI
    private final Set<Player> viewers = new HashSet<>();

    // CommandUtils for utility functions
    private final CommandUtils commandUtils;
    // VaultManager for economy operations
    private final VaultManager vaultManager;

    public BlackmarketGUI(Main plugin, DatabaseHelper databaseHelper) {
        this.plugin = plugin;
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
        Map<String, Object> navigation = Objects.requireNonNull(Main.getInstance().getConfig().getConfigurationSection("gui.blackmarket.navigation." + key)).getValues(false);
        return commandUtils.translateColor(((String) navigation.get("name")));
    }

    public Material getNavigationMaterial(String key) {
        Map<String, Object> navigation = Objects.requireNonNull(Main.getInstance().getConfig().getConfigurationSection("gui.blackmarket.navigation." + key)).getValues(false);
        return Material.valueOf(((String) navigation.get("item")).toUpperCase());
    }

    public int getSlot(String key) {
        Map<String, Object> navigation = Objects.requireNonNull(Main.getInstance().getConfig().getConfigurationSection("gui.blackmarket.navigation." + key)).getValues(false);
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

                // Add to a persistent set and apply discount
                persistentDiscountedIndices.add(randomIndex);
                if (!item.isSold()) {
                    item.setDiscount(true);
                    item.setDiscountPrice(item.getPrice() / 2); // Halve the price
                    databaseHelper.updateSellItem(item); // Update the database with the discount
                } else {
                    item.setDiscount(false);
                    databaseHelper.updateSellItem(item);
                }
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

            Map<String, Object> item = Objects.requireNonNull(Main.getInstance().getConfig().getConfigurationSection("gui.blackmarket.item")).getValues(true);
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

        int page = getPageFromItemName(materialName);

        if (materialName.equalsIgnoreCase(getNavigationName("back"))) {
            player.closeInventory();
            viewers.remove(player); // Remove player from viewers list
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

        int sizeForNavigation = size * 9 - 9;
        if (materialName.equalsIgnoreCase(getNavigationName("page")) ||
            event.getSlot() == sizeForNavigation + getSlot("page")) {
            event.setCancelled(true);
            return;
        }

        ItemStack itemStack = event.getCurrentItem();
        Item item = cacheItems.get(event.getSlot());
        if (itemStack.getType() == Material.AIR) return;
        if (ConfigVariables.SETTINGS_BLACKMARKET_USE_CONFIRMATION) {
            Main.getInstance().getBuyGUI().showInventory(player, item);
        } else {
            if (item != null) {
                if (item.getItemStack().getItemMeta() == null) return;
                // Remove the item from the inventory
                if (!vaultManager.getEconomy().has(player, item.getPrice())) {
                    String notEnough = ConfigVariables.MONEY_NOT_ENOUGH;
                    notEnough = ConfigUtils.translateColor(notEnough, "&cYou don't have enough money to buy this item!");
                    String message = ConfigVariables.SETTINGS_USE_PREFIX ? ConfigUtils.getPrefix() + notEnough :
                            notEnough;
                    player.sendMessage(message);
                    return;
                }

                Player itemSeller = Bukkit.getPlayer(item.getPlayerUUID());
                if (itemSeller != null) {
                    String sellerMessage = ConfigVariables.ITEM_SOLD;
                    sellerMessage = ConfigUtils.translateColor(sellerMessage, "&6You have sold {amount}x {itemName} for {price} to the Player {playerName}.");
                    sellerMessage = sellerMessage.replace("{itemName}", item.getName());
                    sellerMessage = sellerMessage.replace("{price}", String.valueOf(item.getPrice()));
                    sellerMessage = sellerMessage.replace("{amount}", String.valueOf(item.getAmount()));
                    sellerMessage = sellerMessage.replace("{playerName}", player.getName());
                    String message = ConfigVariables.SETTINGS_USE_PREFIX ? ConfigUtils.getPrefix() + sellerMessage :
                            sellerMessage;
                    itemSeller.sendMessage(message);
                }

                vaultManager.getEconomy().withdrawPlayer(player, item.getPrice());
                int sellerMultiplier = item.isDiscount() ? 4 : 2;
                vaultManager.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(item.getPlayerUUID()), item.getPrice() * sellerMultiplier);

                player.getInventory().addItem(item.getItemStack());
                player.closeInventory();
                plugin.getBlackmarketGUI().getGui().remove(event.getCurrentItem());
                plugin.getBlackmarketGUI().removeFromCache(item);

                String receiverMessage = ConfigVariables.ITEM_BOUGHT;
                receiverMessage = ConfigUtils.translateColor(receiverMessage, "&6You have bought {amount}x {itemName} for {price} from the Player {playerName}.");
                receiverMessage = receiverMessage.replace("{itemName}", item.getName());
                receiverMessage = receiverMessage.replace("{price}", String.valueOf(item.getPrice()));
                receiverMessage = receiverMessage.replace("{amount}", String.valueOf(item.getAmount()));
                OfflinePlayer offlineReceiver = Bukkit.getOfflinePlayer(item.getPlayerUUID());
                receiverMessage = receiverMessage.replace("{playerName}", offlineReceiver.hasPlayedBefore() ? Objects.requireNonNull(offlineReceiver.getName()) : "Unknown");
                String messagePrefix = ConfigVariables.SETTINGS_USE_PREFIX ? ConfigUtils.getPrefix() + receiverMessage :
                        receiverMessage;
                player.sendMessage(messagePrefix);
                if (databaseHelper.notSoldItem(item, player)) {
                    String error = ConfigVariables.ERROR_BUY;
                    error = ConfigUtils.translateColor(error, "&cThere was an error buying the Item &6{itemName}&c!");
                    player.sendMessage(error.replace("{itemName}", item.getItemStack().getItemMeta().getDisplayName()));
                    return;
                }
                if (plugin.getConfig().getBoolean("discord.enabled")) {
                    sendDiscordWebhook(item.getName(), player, itemSeller, item.getPrice(), item.getDiscountPrice(), item.isDiscount());
                }
            } else {
                player.sendMessage("You don't have any items to buy.");
            }
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

    @EventHandler
    private void handleInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (event.getView().getTitle().contains(this.title)) {
            viewers.remove(player); // Remove player from the viewer list when they close the GUI
        }
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
                        // If the page item doesn't exist, recreate the GUI with page 1
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
                    }

                    int ITEMS_PER_PAGE = (size - 1) * 9;
                    int totalPages = (int) Math.ceil((double) items.size() / ITEMS_PER_PAGE);

                    // Ensure currentPage is within valid range
                    if (currentPage < 0) currentPage = 0;
                    if (currentPage >= totalPages) currentPage = totalPages - 1;

                    int startIndex = currentPage * ITEMS_PER_PAGE;
                    int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, items.size());

                    // Clear the current cache and rebuild it
                    cacheItems.clear();

                    // Clear the inventory
                    openInventory.clear();

                    // Populate the inventory with updated items
                    for (int i = startIndex; i < endIndex; i++) {
                        Item dataMaterial = items.get(i);
                        cacheItems.put(i - startIndex, dataMaterial);

                        Map<String, Object> item = Objects.requireNonNull(Main.getInstance().getConfig().getConfigurationSection("gui.blackmarket.item")).getValues(true);
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

                        String discountText = item.get("discount").toString();
                        discountText = discountText.replace("{newPrice}", String.valueOf(dataMaterial.getDiscountPrice()));
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

    private void sendDiscordWebhook(String itemName, Player player, OfflinePlayer itemSeller, double price, double discountPrice, boolean isDiscount) {
        String url = ConfigVariables.DISCORD_WEBHOOK_URL;
        if (url != null && !url.isEmpty()) {
            DiscordWebhook webhook = new DiscordWebhook(url);
            DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
            embed.setTitle(plugin.getConfig().getString("discord.embed.title"));
            String description = plugin.getConfig().getString("discord.embed.description");
            if (description == null)
                description = "Item bought from the Blackmarket by {playerName} for {price} from {sellerName}";
            if (description.contains("{playerName}")) {
                description = description.replace("{playerName}", player.getName());
            }
            if (description.contains("{itemName}")) {
                description = description.replace("{itemName}", itemName);
            }
            if (description.contains("{sellerName}") && itemSeller != null && itemSeller.hasPlayedBefore()) {
                description = description.replace("{sellerName}", Objects.requireNonNull(itemSeller.getName()));
            } else {
                description = description.replace("{sellerName}", "Unknown");
            }
            if (description.contains("{price/discount}")) {
                String discount = isDiscount ? " | Discount Price: " + discountPrice : "";
                description = description.replace("{price/discount}", String.valueOf(price) + discount);
            } else if (description.contains("{price}")) {
                description = description.replace("{price}", String.valueOf(price));
            }
            embed.setDescription(description);
            embed.setColor(Color.getColor(plugin.getConfig().getString("discord.embed.color"))); // Green color
            embed.setFooter(plugin.getConfig().getString("discord.embed.footer.text"),
                    plugin.getConfig().getString("discord.embed.footer.icon_url"));
            embed.setThumbnail(plugin.getConfig().getString("discord.embed.thumbnail.url"));
            embed.setImage(plugin.getConfig().getString("discord.embed.image.url"));
            webhook.addEmbed(embed);
            webhook.setUsername(plugin.getConfig().getString("discord.username"));
            String avatarUrl = plugin.getConfig().getString("discord.avatarUrl");
            if (avatarUrl == null) avatarUrl = "https://example.com/avatar.png"; // Default avatar URL
            webhook.setAvatarUrl(avatarUrl);
            webhook.setContent(plugin.getConfig().getString("discord.content"));
            try {
                webhook.execute();
            } catch (IOException e) {
                String errorMessage = ConfigVariables.ERROR_EXECUTE_DISCORD_WEBHOOK;
                if (errorMessage == null) errorMessage = "There was an error while executing Discord Webhook!";
                plugin.getLogger().log(Level.SEVERE, errorMessage, e);
            }
        } else {
            plugin.getLogger().warning("Discord Webhook URL is not set.");
        }
    }
}
