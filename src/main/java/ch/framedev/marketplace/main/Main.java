package ch.framedev.marketplace.main;

import ch.framedev.marketplace.commands.BlackmarketCommand;
import ch.framedev.marketplace.commands.MarketplaceCommand;
import ch.framedev.marketplace.commands.SellCommand;
import ch.framedev.marketplace.database.DatabaseHelper;
import ch.framedev.marketplace.guis.BlackmarketGUI;
import ch.framedev.marketplace.guis.BuyGUI;
import ch.framedev.marketplace.guis.MarketplaceGUI;
import ch.framedev.marketplace.vault.VaultManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main instance;

    private VaultManager vaultManager;
    private MarketplaceGUI marketplaceGUI;
    private BlackmarketGUI blackmarketGUI;
    private BuyGUI buyGUI;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.vaultManager = new VaultManager();

        DatabaseHelper databaseHelper = new DatabaseHelper();

        this.marketplaceGUI = new MarketplaceGUI(databaseHelper);
        getServer().getPluginManager().registerEvents(marketplaceGUI, this);

        this.blackmarketGUI = new BlackmarketGUI(databaseHelper);
        getServer().getPluginManager().registerEvents(blackmarketGUI, this);

        this.buyGUI = new BuyGUI(databaseHelper);
        getServer().getPluginManager().registerEvents(buyGUI, this);

        getCommand("sell").setExecutor(new SellCommand(databaseHelper));
        getCommand("marketplace").setExecutor(new MarketplaceCommand(this));
        getCommand("blackmarket").setExecutor(new BlackmarketCommand(this));
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

    public BlackmarketGUI getBlackmarketGUI() {
        return blackmarketGUI;
    }

    public BuyGUI getBuyGUI() {
        return buyGUI;
    }

    public static Main getInstance() {
        return instance;
    }
}
