package net.honeyberries;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod initializer for AutoClutch.
 * <p>
 * Registers the mod with Fabric and sets up logging.
 */
public class AutoClutch implements ModInitializer {
    /** The mod ID for AutoClutch. */
    public static final String MOD_ID = "autoclutch";

    /**
     * Logger for AutoClutch. Use this for all log output.
     * Using the mod ID as the logger name is best practice.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * Called by Fabric when the mod is initialized.
     * Use this to set up mod-wide logic.
     */
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.info("Hello Fabric world!");
    }
}