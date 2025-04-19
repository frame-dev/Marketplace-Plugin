package ch.framedev.marketplace.commands;



/*
 * ch.framedev.marketplace.commands
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 15.04.2025 19:34
 */

import ch.framedev.marketplace.database.DatabaseHelper;
import ch.framedev.marketplace.item.Item;
import ch.framedev.marketplace.utils.ConfigVariables;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Require Testing (Not completed)
 */
public class SellCommand implements CommandExecutor {

    private final CommandUtils commandUtils;
    private final DatabaseHelper databaseHelper;

    public SellCommand(DatabaseHelper databaseHelper) {
        this.commandUtils = new CommandUtils();
        this.databaseHelper = databaseHelper;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the command is "sell"
        if(command.getName().equalsIgnoreCase("sell")) {
            // Check if the sender is not a player
            if(!(sender instanceof Player player)) {
                String onlyPlayerMessage = ConfigVariables.ONLY_PLAYER_MESSAGE;
                if(onlyPlayerMessage == null || onlyPlayerMessage.isEmpty()) {
                    sender.sendMessage("§cThis command can only be used by players.");
                    return true;
                }
                onlyPlayerMessage = ChatColor.translateAlternateColorCodes('&', onlyPlayerMessage);
                sender.sendMessage(onlyPlayerMessage);
                return true;
            }
            // Check if the player has the permission to use the command
            if(!commandUtils.hasPermission(player, ConfigVariables.SELL_COMMAND_PERMISSION)) {
                return true;
            }

            // Check if the player has provided the correct number of arguments
            if(args.length < 1) {
                player.sendMessage(commandUtils.translateColor(ConfigVariables.SELL_ARGUMENT_MISSING));
                return true;
            }

            // Handle the sell command
            // Check if the player has an item in their main hand
            if(player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                String missingItem = ConfigVariables.SELL_MISSING_ITEM;
                missingItem = commandUtils.translateColor(missingItem);
                player.sendMessage(missingItem);
                return true;
            }
            double price;
            try {
                price = Double.parseDouble(args[0]);
            } catch (NumberFormatException ex) {
                String wrongNumberFormat = ConfigVariables.WRONG_NUMBER_FORMAT;
                wrongNumberFormat = commandUtils.translateColor(wrongNumberFormat);
                wrongNumberFormat = wrongNumberFormat.replace("{input}", args[0]);
                player.sendMessage(wrongNumberFormat);
                return true;
            }

            // Get the item in the player's main hand
            ItemStack itemStack = player.getInventory().getItemInMainHand();

            // Sell the item (Test) Temporary
            Item item = new Item(player.getUniqueId(), itemStack, price);
            if(item.sendToDatabase(databaseHelper)) {
                // TODO: Updated messages
                String itemAddedMessage = ConfigVariables.ITEM_ADDED;
                itemAddedMessage = commandUtils.translateColor(itemAddedMessage);
                itemAddedMessage = itemAddedMessage.replace("{price}", String.valueOf(price));
                itemAddedMessage = itemAddedMessage.replace("{amount}", String.valueOf(itemStack.getAmount()));
                itemAddedMessage = itemAddedMessage.replace("{itemName}", itemStack.getType().name());
                player.sendMessage(itemAddedMessage);
                player.getInventory().removeItem(itemStack);
                return true;
            } else {
                String error = ConfigVariables.ERROR_SELL;
                if(error == null)
                    error = "&cThere was an error while selling the Item &6{itemName}&c!";
                error = error.replace("{itemName}", itemStack.getType().name());
                error = ChatColor.translateAlternateColorCodes('&', error);
                player.sendMessage(error);
                return true;
            }
        }
        return false;
    }
}
