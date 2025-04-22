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
import ch.framedev.marketplace.utils.DiscordWebhook;
import ch.framedev.marketplace.utils.InventoryBuilder;
import ch.framedev.marketplace.vault.VaultManager;
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

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class ConfirmationGUI implements Listener {

    private final Main plugin;

    private final Inventory inventory;
    private final String title;

    private final int[] slots;

    private final DatabaseHelper databaseHelper;

    private final Map<Player, Item> playerItems = new HashMap<>();
    private final VaultManager vaultManager;

    public ConfirmationGUI(Main plugin, DatabaseHelper databaseHelper) {
        this.plugin = plugin;
        this.vaultManager = plugin.getVaultManager();
        this.databaseHelper = databaseHelper;
        this.title = "Confirmation";
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
        if (itemMeta != null) {
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
                        if (item.getItemStack().getItemMeta() == null) return;
                        // Remove the item from the inventory
                        if (!vaultManager.getEconomy().has(player, item.getPrice())) {
                            String notEnough = ConfigVariables.MONEY_NOT_ENOUGH;
                            notEnough = ConfigUtils.translateColor(notEnough, "&cYou don't have enough money to buy this item!");
                            player.sendMessage(notEnough);
                            return;
                        }

                        Player itemSeller = Bukkit.getPlayer(item.getPlayerUUID());
                        if (itemSeller != null) {
                            String sellerMessage = ConfigVariables.ITEM_SOLD;
                            sellerMessage = ConfigUtils.translateColor(sellerMessage, "&6You have sold {amount}x {itemName} for {price} to the Player {playerName}.");
                            sellerMessage = sellerMessage.replace("{itemName}", ChatColor.RESET + item.getName());
                            sellerMessage = sellerMessage.replace("{price}", String.valueOf(item.getPrice()));
                            sellerMessage = sellerMessage.replace("{amount}", String.valueOf(item.getAmount()));
                            sellerMessage = sellerMessage.replace("{playerName}", player.getName());
                            itemSeller.sendMessage(sellerMessage);
                        }

                        vaultManager.getEconomy().withdrawPlayer(player, item.getPrice());
                        int sellerMultiplier = item.isDiscount() ? 4 : 2;
                        vaultManager.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(item.getPlayerUUID()), item.getPrice() * sellerMultiplier);

                        player.getInventory().addItem(item.getItemStack());
                        player.closeInventory();
                        Main.getInstance().getBlackmarketGUI().getGui().remove(event.getCurrentItem());
                        Main.getInstance().getBlackmarketGUI().removeFromCache(item);

                        String receiverMessage = ConfigVariables.ITEM_BOUGHT;
                        receiverMessage = ConfigUtils.translateColor(receiverMessage, "&6You have bought {amount}x {itemName} for {price} from the Player {playerName}.");
                        receiverMessage = receiverMessage.replace("{itemName}", ChatColor.RESET + item.getName());
                        receiverMessage = receiverMessage.replace("{price}", String.valueOf(item.getPrice()));
                        receiverMessage = receiverMessage.replace("{amount}", String.valueOf(item.getAmount()));
                        OfflinePlayer offlineReceiver = Bukkit.getOfflinePlayer(item.getPlayerUUID());
                        receiverMessage = receiverMessage.replace("{playerName}", offlineReceiver.hasPlayedBefore() ? Objects.requireNonNull(offlineReceiver.getName()) : "Unknown");
                        player.sendMessage(receiverMessage);
                        if (!databaseHelper.soldItem(item, player)) {
                            String error = ConfigVariables.ERROR_BUY;
                            error = ConfigUtils.translateColor(error, "&cThere was an error buying the Item &6{itemName}&c!");
                            player.sendMessage(error.replace("{itemName}", item.getItemStack().getItemMeta().getDisplayName()));
                            return;
                        }
                        if (plugin.getConfig().getBoolean("discord.enabled")) {
                            sendDiscordWebhook(item.getName(), player, itemSeller, item.isDiscount() ? item.getPrice() / 2 : item.getPrice());
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

    private void sendDiscordWebhook(String itemName, Player player, OfflinePlayer itemSeller, double price) {
        String url = ConfigVariables.DISCORD_WEBHOOK_URL;
        if (url != null && !url.isEmpty()) {
            DiscordWebhook webhook = new DiscordWebhook(url);
            DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
            embed.setTitle(plugin.getConfig().getString("discord.embed.title"));
            String description = plugin.getConfig().getString("discord.embed.description");
            if(description == null) description = "Item bought from the Blackmarket by {playerName} for {price} from {sellerName}";
            if(description.contains("{playerName}")) {
                description = description.replace("{playerName}", player.getName());
            }
            if(description.contains("{itemName}")) {
                description = description.replace("{itemName}", itemName);
            }
            if(description.contains("{sellerName}") && itemSeller != null && itemSeller.hasPlayedBefore()) {
                description = description.replace("{sellerName}", Objects.requireNonNull(itemSeller.getName()));
            } else {
                description = description.replace("{sellerName}", "Unknown");
            }
            if(description.contains("{price}")) {
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
            webhook.setAvatarUrl(plugin.getConfig().getString("discord.avatar_url"));
            webhook.setContent(plugin.getConfig().getString("discord.content"));
            try {
                webhook.execute();
            } catch (IOException e) {
                String errorMessage = ConfigVariables.ERROR_EXECUTE_DISCORD_WEBHOOK;
                if(errorMessage == null) errorMessage = "There was an error while executing Discord Webhook!";
                plugin.getLogger().log(Level.SEVERE, errorMessage, e);
            }
        } else {
            plugin.getLogger().warning("Discord Webhook URL is not set.");
        }
    }
}
