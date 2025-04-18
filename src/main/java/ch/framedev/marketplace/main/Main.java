package ch.framedev.marketplace.main;

import ch.framedev.marketplace.commands.BlackmarketCommand;
import ch.framedev.marketplace.commands.MarketplaceCommand;
import ch.framedev.marketplace.commands.SellCommand;
import ch.framedev.marketplace.database.DatabaseHelper;
import ch.framedev.marketplace.guis.BlackmarketGUI;
import ch.framedev.marketplace.guis.ConfirmationGUI;
import ch.framedev.marketplace.guis.MarketplaceGUI;
import ch.framedev.marketplace.utils.ConfigUtils;
import ch.framedev.marketplace.vault.VaultManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main instance;

    private VaultManager vaultManager;
    private MarketplaceGUI marketplaceGUI;
    private BlackmarketGUI blackmarketGUI;
    private ConfirmationGUI confirmationGUI;

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

        this.confirmationGUI = new ConfirmationGUI(databaseHelper);
        getServer().getPluginManager().registerEvents(confirmationGUI, this);

        getCommand("sell").setExecutor(new SellCommand(databaseHelper));
        getCommand("marketplace").setExecutor(new MarketplaceCommand(this));
        getCommand("blackmarket").setExecutor(new BlackmarketCommand(this));

        new ConfigUtils(this);
    }

    @Override
    public void onDisable() {
        // Force the inventory to be closed
        Bukkit.getOnlinePlayers().forEach(HumanEntity::closeInventory);
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

    public ConfirmationGUI getBuyGUI() {
        return confirmationGUI;
    }

    public static Main getInstance() {
        return instance;
    }
}
