package ch.framedev.marketplace.utils;



/*
 * ch.framedev.marketplace.utils
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 18.04.2025 15:50
 */

import ch.framedev.marketplace.main.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UpdateConfig {

    private final File file;
    private final FileConfiguration config;

    public UpdateConfig() {
        this.file = new File(Main.getInstance().getDataFolder(), "update-gui.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        setupDefaultConfig();
    }

    private void setupDefaultConfig() {
        containsOrAdd("title", "&6Update Item");
        containsOrAdd("rowSize", 3);

        Map<String, Object> navigation = new HashMap<>();

        Map<String, Object> next = new HashMap<>();
        next.put("name", "&7Next");
        next.put("item", "ARROW");
        next.put("slot", 8);
        navigation.put("next", next);

        Map<String, Object> previous = new HashMap<>();
        previous.put("name", "&7Previous");
        previous.put("item", "ARROW");
        previous.put("slot", 0);
        navigation.put("previous", previous);

        Map<String, Object> back = new HashMap<>();
        back.put("name", "&7Back");
        back.put("item", "ARROW");
        back.put("slot", 4);
        navigation.put("back", back);

        containsOrAdd("gui.update.navigation", navigation);
    }

    private void containsOrAdd(String key, Object value) {
        if(!config.contains(key))
            config.set(key, value);
        try {
            config.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
