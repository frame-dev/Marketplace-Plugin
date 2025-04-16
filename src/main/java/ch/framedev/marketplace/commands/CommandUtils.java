package ch.framedev.marketplace.commands;



/*
 * ch.framedev.marketplace.commands
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 15.04.2025 20:03
 */

import ch.framedev.marketplace.utils.ConfigUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Utility class for command-related operations.
 * Provides methods for translating color codes, checking permissions,
 * and sending messages to players.
 * Require Testing
 */
public class CommandUtils {

    public String translateColor(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getOnlyPlayerMessage() {
        String onlyPlayerMessage = ConfigUtils.ONLY_PLAYER_MESSAGE;
        if(onlyPlayerMessage == null || onlyPlayerMessage.isEmpty()) {
            return "§cThis command can only be used by players.";
        }
        return ChatColor.translateAlternateColorCodes('&', onlyPlayerMessage);
    }

    public String getNoPermissionMessage() {
        String noPermissionMessage = ConfigUtils.NO_PERMISSION_MESSAGE;
        if(noPermissionMessage == null || noPermissionMessage.isEmpty()) {
            return "§cYou do not have permission to use this command.";
        }
        return ChatColor.translateAlternateColorCodes('&', noPermissionMessage);
    }

    public String getOnlyPlayerMessage(CommandSender sender) {
        String onlyPlayerMessage = ConfigUtils.ONLY_PLAYER_MESSAGE;
        if(onlyPlayerMessage == null || onlyPlayerMessage.isEmpty()) {
            return "§cThis command can only be used by players.";
        }
        return ChatColor.translateAlternateColorCodes('&', onlyPlayerMessage);
    }

    public boolean hasPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        } else {
            sender.sendMessage(getNoPermissionMessage());
            return false;
        }
    }
}
