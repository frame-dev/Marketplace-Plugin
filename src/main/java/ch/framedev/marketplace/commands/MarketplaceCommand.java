package ch.framedev.marketplace.commands;



/*
 * ch.framedev.marketplace.commands
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 15.04.2025 19:34
 */

import ch.framedev.marketplace.guis.MarketplaceGUI;
import ch.framedev.marketplace.main.Main;
import ch.framedev.marketplace.utils.ConfigVariables;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MarketplaceCommand implements CommandExecutor {

    private final CommandUtils commandUtils;
    private final MarketplaceGUI marketplaceGUI;

    public MarketplaceCommand(Main plugin) {
        this.commandUtils = new CommandUtils();
        this.marketplaceGUI = plugin.getMarketplaceGUI();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the command is "marketplace"
        if(!command.getName().equalsIgnoreCase("marketplace")) {
            return false;
        }
        // Check if the sender is a player
        if(!(sender instanceof Player player)) {
            sender.sendMessage(commandUtils.getOnlyPlayerMessage());
            return true;
        }

        // Check if the player has the permission to use the command
        if(!commandUtils.hasPermission(player, ConfigVariables.MARKETPLACE_COMMAND_PERMISSION)) {
            return true;
        }

        // Command logic for opening the marketplace
        // Open the marketplace GUI
        marketplaceGUI.showMarketplace(player);
        return true;
    }
}
