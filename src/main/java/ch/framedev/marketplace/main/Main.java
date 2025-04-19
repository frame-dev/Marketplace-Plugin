package ch.framedev.marketplace.main;

import ch.framedev.marketplace.commands.BlackmarketCommand;
import ch.framedev.marketplace.commands.MarketplaceCommand;
import ch.framedev.marketplace.commands.SellCommand;
import ch.framedev.marketplace.commands.TransactionCommand;
import ch.framedev.marketplace.database.DatabaseHelper;
import ch.framedev.marketplace.guis.*;
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
    private UpdateGUI updateGUI;
    private UpdateDeeperGUI updateDeeperGUI;

    @Override
    public void onLoad() {
        // Force the inventory to be closed
        Bukkit.getOnlinePlayers().forEach(HumanEntity::closeInventory);
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.vaultManager = new VaultManager();

        DatabaseHelper databaseHelper = new DatabaseHelper();

        this.marketplaceGUI = new MarketplaceGUI(databaseHelper);
        getServer().getPluginManager().registerEvents(marketplaceGUI, this);

        this.blackmarketGUI = new BlackmarketGUI(this, databaseHelper);
        getServer().getPluginManager().registerEvents(blackmarketGUI, this);

        this.confirmationGUI = new ConfirmationGUI(this, databaseHelper);
        getServer().getPluginManager().registerEvents(confirmationGUI, this);

        this.updateGUI = new UpdateGUI(databaseHelper);
        getServer().getPluginManager().registerEvents(updateGUI, this);

        this.updateDeeperGUI = new UpdateDeeperGUI(databaseHelper);
        Bukkit.getServer().getPluginManager().registerEvents(updateDeeperGUI, this);

        getCommand("sell").setExecutor(new SellCommand(databaseHelper));
        getCommand("marketplace").setExecutor(new MarketplaceCommand(this));
        getCommand("blackmarket").setExecutor(new BlackmarketCommand(this));
        getCommand("transactions").setExecutor(new TransactionCommand(databaseHelper));

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

    public UpdateGUI getUpdateGUI() {
        return updateGUI;
    }

    public UpdateDeeperGUI getUpdateDeeperGUI() {
        return updateDeeperGUI;
    }

    public static Main getInstance() {
        return instance;
    }
}
