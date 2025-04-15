package ch.framedev.marketplace.main;

import ch.framedev.marketplace.commands.SellCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;

        getCommand("sell").setExecutor(new SellCommand());
    }

    @Override
    public void onDisable() {

    }

    public static Main getInstance() {
        return instance;
    }
}
