package net.honeyberries.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
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
    public boolean enableWater = true;

    public static AutoClutchConfig getInstance() {
        return INSTANCE;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                AutoClutchConfig loaded = GSON.fromJson(json, AutoClutchConfig.class);
                INSTANCE = loaded != null ? loaded : new AutoClutchConfig();
                AutoClutch.LOGGER.info("Config loaded from {}", CONFIG_PATH);
            } catch (IOException | JsonParseException e) {
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
