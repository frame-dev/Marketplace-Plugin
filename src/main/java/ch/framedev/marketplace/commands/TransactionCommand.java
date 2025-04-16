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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TransactionCommand implements CommandExecutor {

    private final DatabaseHelper databaseHelper;

    public TransactionCommand(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }
}
