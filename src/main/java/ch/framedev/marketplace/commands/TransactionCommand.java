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
import ch.framedev.marketplace.utils.ConfigVariables;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

        if(!(sender instanceof Player)) {
            sender.sendMessage(commandUtils.getOnlyPlayerMessage());
            return true;
        }

        if(!commandUtils.hasPermission(sender, ConfigVariables.TRANSACTIONS_COMMAND_PERMISSION)) return true;
        return false;
    }
}
