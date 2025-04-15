package ch.framedev.marketplace.main;

import ch.framedev.marketplace.commands.MarketplaceCommand;
import ch.framedev.marketplace.commands.SellCommand;
import ch.framedev.marketplace.guis.MarketplaceGUI;
import ch.framedev.marketplace.vault.VaultManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main instance;

    private VaultManager vaultManager;
    private MarketplaceGUI marketplaceGUI;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.vaultManager = new VaultManager();

        this.marketplaceGUI = new MarketplaceGUI();
        getServer().getPluginManager().registerEvents(marketplaceGUI, this);

        getCommand("sell").setExecutor(new SellCommand());
        getCommand("marketplace").setExecutor(new MarketplaceCommand(this));

    }

    @Override
    public void onDisable() {

    }

    public VaultManager getVaultManager() {
        return vaultManager;
    }

    public MarketplaceGUI getMarketplaceGUI() {
        return marketplaceGUI;
    }

    public static Main getInstance() {
        return instance;
    }
}
