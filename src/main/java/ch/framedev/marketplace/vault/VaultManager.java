package ch.framedev.marketplace.vault;



/*
 * ch.framedev.marketplace.vault
 * =============================================
 * This File was Created by FrameDev.
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 15.04.2025 19:34
 */

import ch.framedev.marketplace.main.Main;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

import static org.bukkit.Bukkit.getServer;

/**
 * VaultManager is a class that manages the Vault economy system.
 * It checks if Vault is present and sets up the economy provider.
 * It also provides a method to get the economy instance.
 */
public class VaultManager {

    private Economy economy;

    public VaultManager() {
        if(!setupEconomy()) {
            getServer().getPluginManager().disablePlugin(Main.getInstance());
            getServer().getConsoleSender().sendMessage("§cVault not found! Disabling plugin...");
        } else {
            getServer().getConsoleSender().sendMessage("§aVault found! Economy system is enabled.");
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    public Economy getEconomy() {
        return economy;
    }
}
