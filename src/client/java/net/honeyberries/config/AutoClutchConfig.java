package net.honeyberries.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.honeyberries.AutoClutch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AutoClutchConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("autoclutch.json");
    private static AutoClutchConfig INSTANCE = new AutoClutchConfig();

    public boolean enabled = true;
    public double meanBlocks = 2.5;
    public double varianceBlocks = 1.2;

    // Derived bounds - not serialized
    public static final double MIN_BLOCKS = 1.5;
    public static final double MAX_BLOCKS = 4.5;

    public static AutoClutchConfig getInstance() {
        return INSTANCE;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                INSTANCE = GSON.fromJson(json, AutoClutchConfig.class);
                AutoClutch.LOGGER.info("Config loaded from {}", CONFIG_PATH);
            } catch (IOException e) {
                AutoClutch.LOGGER.error("Failed to load config, using defaults", e);
            }
        } else {
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            String json = GSON.toJson(INSTANCE);
            Files.writeString(CONFIG_PATH, json);
            AutoClutch.LOGGER.info("Config saved to {}", CONFIG_PATH);
        } catch (IOException e) {
            AutoClutch.LOGGER.error("Failed to save config", e);
        }
    }
}
