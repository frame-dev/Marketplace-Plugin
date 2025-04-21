package ch.framedev.marketplace.commands;



/*
 * ch.framedev.marketplace.commands
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 20.04.2025 13:24
 */

import ch.framedev.marketplace.database.DatabaseHelper;
import ch.framedev.marketplace.main.Main;
import ch.framedev.marketplace.utils.ConfigVariables;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdminCommand implements CommandExecutor {

    private final Main plugin;
    private final CommandUtils commandUtils;
    private final DatabaseHelper databaseHelper;

    public AdminCommand(Main plugin, DatabaseHelper databaseHelper) {
        this.plugin = plugin;
        this.databaseHelper = databaseHelper;
        this.commandUtils = new CommandUtils();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("marketplace-admin")) return false;

        if (!commandUtils.hasPermission(sender, ConfigVariables.ADMIN_COMMAND_PERMISSION)) return true;
        if(!(sender instanceof Player player)) {
            sender.sendMessage(commandUtils.getOnlyPlayerMessage());
            return true;
        }

        plugin.getAdminGUI().showAdminInventory(player);
        return true;
    }
}
