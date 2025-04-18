package ch.framedev.marketplace.commands;



/*
 * ch.framedev.marketplace.commands
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 15.04.2025 19:35
 */

import ch.framedev.marketplace.database.DatabaseHelper;
import ch.framedev.marketplace.transactions.Transaction;
import ch.framedev.marketplace.utils.ConfigVariables;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class TransactionCommand implements CommandExecutor {

    private final DatabaseHelper databaseHelper;
    private final CommandUtils commandUtils;

    public TransactionCommand(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        this.commandUtils = new CommandUtils();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!command.getName().equalsIgnoreCase("transaction")) return false;

        if(!(sender instanceof Player player)) {
            sender.sendMessage(commandUtils.getOnlyPlayerMessage());
            return true;
        }

        if(!commandUtils.hasPermission(sender, ConfigVariables.TRANSACTIONS_COMMAND_PERMISSION)) return true;
        if(ConfigVariables.SETTINGS_TRANSACTION_USE_GUI) {
            throw new UnsupportedOperationException("Not yet implemented");
        } else {
            if(databaseHelper.getTransaction(player.getUniqueId()).isPresent()) {
                Transaction transaction = databaseHelper.getTransaction(player.getUniqueId()).get();
                int id = transaction.getId();
                List<Integer> itemsForSale = transaction.getItemsForSale();
                List<Integer> itemsSold = transaction.getItemsSold();
                List<UUID> receivers = transaction.getReceivers();
                // TODO: Setup output
                player.sendMessage("Test output!");
                player.sendMessage("Transaction ID: " + id);
                player.sendMessage("Items for Sale: " + itemsForSale);
                player.sendMessage("Items Sold: " + itemsSold);
                player.sendMessage("Receivers: " + receivers);
            } else {
                // TODO: Add message
                player.sendMessage("No Transactions found!");
            }
        }
        return false;
    }
}
