package ch.framedev.marketplace.commands;



/*
 * ch.framedev.marketplace.commands
 * =============================================
 * This File was Created by FrameDev.
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 15.04.2025 19:35
 */

import ch.framedev.marketplace.database.DatabaseHelper;
import ch.framedev.marketplace.item.Item;
import ch.framedev.marketplace.main.Main;
import ch.framedev.marketplace.transactions.Transaction;
import ch.framedev.marketplace.utils.ConfigVariables;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TransactionCommand implements CommandExecutor {

    private final DatabaseHelper databaseHelper;
    private final CommandUtils commandUtils;

    private final Main plugin;

    public TransactionCommand(Main plugin, DatabaseHelper databaseHelper) {
        this.plugin = plugin;
        this.databaseHelper = databaseHelper;
        this.commandUtils = new CommandUtils();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("transactions")) return false;

        if (!(sender instanceof Player player)) {
            sender.sendMessage(commandUtils.getOnlyPlayerMessage());
            return true;
        }

        if (commandUtils.hasNotPermission(sender, ConfigVariables.TRANSACTIONS_COMMAND_PERMISSION)) return true;

        if(!ConfigVariables.SETTINGS_TRANSACTION_USE_HISTORY) {
            player.sendMessage("§cThis command is disabled in the config.yml");
            return true;
        }

        if (ConfigVariables.SETTINGS_TRANSACTION_USE_GUI) {
            plugin.getTransactionGUI().showInventory(player);
        } else {
            if (databaseHelper.getTransaction(player.getUniqueId()).isPresent()) {
                Transaction transaction = databaseHelper.getTransaction(player.getUniqueId()).get();
                int id = transaction.getId();
                List<Integer> itemsForSale = transaction.getItemsForSale();
                List<Integer> itemsSold = transaction.getItemsSold();
                Map<Integer, UUID> receivers = transaction.getReceivers();

                List<String> historyTextList = plugin.getReplacementUtils().getTransactionHistoryList();
                for (String text : historyTextList) {
                    text = commandUtils.translateColor(text);
                    text = text.replace("{id}", String.valueOf(id));
                    if (text.contains("%itemForSaleList%")) {
                        StringBuilder itemForSaleList = new StringBuilder();
                        for (int itemId : itemsForSale) {
                            Item item = databaseHelper.getTypeItem(itemId);
                            if (item != null && !item.isSold()) {
                                for (String itemText : plugin.getReplacementUtils().getItemsForSaleList()) {
                                    itemText = commandUtils.translateColor(itemText);
                                    itemText = itemText.replace("{itemId}", String.valueOf(item.getId()));
                                    itemText = itemText.replace("{itemName}", item.getName());
                                    itemText = itemText.replace("{itemType}", item.getItemStack().getType().name());
                                    itemText = itemText.replace("{amount}", String.valueOf(item.getAmount()));
                                    itemText = itemText.replace("{price}", String.valueOf(item.getPrice()));
                                    String discountText = item.isDiscount() ? " §7| §7Discount Price: §6" + item.getDiscountPrice() : "";
                                    itemText = itemText.replace("{hasDiscount}", item.isDiscount() + discountText);
                                    itemForSaleList.append(itemText).append("\n");
                                }
                                itemForSaleList.append("\n").append("---");
                            }
                        }
                        text = text.replace("%itemForSaleList%", itemForSaleList.toString());
                    }
                    if (text.contains("%itemSoldList%")) {
                        StringBuilder itemSoldList = new StringBuilder();
                        for (int itemId : itemsSold) {
                            Item item = databaseHelper.getTypeItem(itemId);
                            if (item != null && item.isSold()) {
                                for (String itemText : plugin.getReplacementUtils().getItemsSoldList()) {
                                    itemText = commandUtils.translateColor(itemText);
                                    itemText = itemText.replace("{itemId}", String.valueOf(item.getId()));
                                    itemText = itemText.replace("{itemName}", item.getName());itemText = itemText.replace("%itemName%", item.getName());
                                    itemText = itemText.replace("{itemType}", item.getItemStack().getType().name());
                                    itemText = itemText.replace("{amount}", String.valueOf(item.getAmount()));
                                    itemText = itemText.replace("{price}", String.valueOf(item.getPrice()));
                                    String discountText = item.isDiscount() ? " §7| §7Discount Price: §6" + item.getDiscountPrice() : "";
                                    itemText = itemText.replace("{hasDiscount}", item.isDiscount() + discountText);
                                    itemText = itemText.replace("{seller}", Objects.requireNonNull(Bukkit.getOfflinePlayer(item.getPlayerUUID()).getName()));
                                    itemText = itemText.replace("{receiver}", Objects.requireNonNull(Bukkit.getOfflinePlayer(receivers.get(itemId)).getName()));
                                    itemSoldList.append(itemText).append("\n");
                                }
                                itemSoldList.append("---").append("\n");
                            }
                        }
                        text = text.replace("%itemSoldList%", itemSoldList.toString());
                    }
                    player.sendMessage(text);
                }
            } else {
                player.sendMessage("No Transactions found!");
            }
        }
        return true;
    }
}
