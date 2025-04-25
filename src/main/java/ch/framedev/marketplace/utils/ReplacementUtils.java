package ch.framedev.marketplace.utils;



/*
 * ch.framedev.marketplace.utils
 * =============================================
 * This File was Created by FrameDev.
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 22.04.2025 18:21
 */

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class ReplacementUtils {

    private final FileConfiguration configuration;

    public ReplacementUtils(File file) {
        this.configuration = YamlConfiguration.loadConfiguration(file);
    }

    public List<String> getItemsForSaleList() {
        return configuration.getStringList("itemForSaleList");
    }

    public List<String> getItemsSoldList() {
        return configuration.getStringList("itemSoldList");
    }

    public List<String> getTransactionHistoryList() {
        return configuration.getStringList("transactionHistoryText");
    }

    public List<String> getItemsBoughtList() {
        return configuration.getStringList("itemBoughtList");
    }
}
