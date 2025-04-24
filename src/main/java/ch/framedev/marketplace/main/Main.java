package ch.framedev.marketplace.main;

import ch.framedev.marketplace.commands.*;
import ch.framedev.marketplace.database.DatabaseHelper;
import ch.framedev.marketplace.guis.*;
import ch.framedev.marketplace.utils.ConfigUtils;
import ch.framedev.marketplace.utils.ConfigVariables;
import ch.framedev.marketplace.utils.ReplacementUtils;
import ch.framedev.marketplace.vault.VaultManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Main extends JavaPlugin {

    // Singleton
    private static Main instance;

    private DatabaseHelper databaseHelper;
    private VaultManager vaultManager;

    // GUI's
    private MarketplaceGUI marketplaceGUI;
    private BlackmarketGUI blackmarketGUI;
    private ConfirmationGUI confirmationGUI;
    private UpdateGUI updateGUI;
    private UpdateDeeperGUI updateDeeperGUI;
    private AdminGUI adminGUI;
    private AdminDeeperGUI adminDeeperGUI;
    private TransactionGUI transactionGUI;

    // Utils
    private ReplacementUtils replacementUtils;

    @Override
    public void onLoad() {
        // Force the inventory to be closed
        Bukkit.getOnlinePlayers().forEach(HumanEntity::closeInventory);
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void onEnable() {
        // Initialize the singleton instance
        instance = this;
        saveDefaultConfig();

        // Setup vault
        this.vaultManager = new VaultManager();

        // Setup Database Helper
        this.databaseHelper = new DatabaseHelper();

        // Set up the Inventories
        this.marketplaceGUI = new MarketplaceGUI(this, databaseHelper);
        getServer().getPluginManager().registerEvents(marketplaceGUI, this);

        this.blackmarketGUI = new BlackmarketGUI(this, databaseHelper);
        getServer().getPluginManager().registerEvents(blackmarketGUI, this);

        this.confirmationGUI = new ConfirmationGUI(this, databaseHelper);
        getServer().getPluginManager().registerEvents(confirmationGUI, this);

        this.updateGUI = new UpdateGUI(this, databaseHelper);
        getServer().getPluginManager().registerEvents(updateGUI, this);

        this.updateDeeperGUI = new UpdateDeeperGUI(this, databaseHelper);
        Bukkit.getServer().getPluginManager().registerEvents(updateDeeperGUI, this);

        this.adminGUI = new AdminGUI(this, databaseHelper);
        getServer().getPluginManager().registerEvents(adminGUI, this);

        this.adminDeeperGUI = new AdminDeeperGUI(databaseHelper);
        getServer().getPluginManager().registerEvents(adminDeeperGUI, this);

        this.transactionGUI = new TransactionGUI(this, databaseHelper);
        getServer().getPluginManager().registerEvents(transactionGUI, this);

        // Set up the commands
        getCommand("sell").setExecutor(new SellCommand(databaseHelper));
        getCommand("marketplace").setExecutor(new MarketplaceCommand(this));
        getCommand("blackmarket").setExecutor(new BlackmarketCommand(this));
        getCommand("transactions").setExecutor(new TransactionCommand(this, databaseHelper));
        getCommand("marketplace-admin").setExecutor(new AdminCommand(this));

        // Creates the Default Config
        ConfigUtils configUtils = new ConfigUtils(this);
        configUtils.createDefaultConfig();

        // Creates the default transactionTexts.yml file
        File file = new File(getDataFolder(), "transactionTexts.yml");
        if (!file.exists()) {
            saveResource("transactionTexts.yml", true);
        }

        this.replacementUtils = new ReplacementUtils(file);
    }

    @Override
    public void onDisable() {
        // Force the inventory to be closed
        if (ConfigVariables.SETTINGS_CLOSE_INVENTORY_RELOAD)
            Bukkit.getOnlinePlayers().forEach(HumanEntity::closeInventory);
    }

    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
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

    public AdminGUI getAdminGUI() {
        return adminGUI;
    }

    public AdminDeeperGUI getAdminDeeperGUI() {
        return adminDeeperGUI;
    }

    public TransactionGUI getTransactionGUI() {
        return transactionGUI;
    }

    public ReplacementUtils getReplacementUtils() {
        return replacementUtils;
    }

    public static Main getInstance() {
        return instance;
    }
}
