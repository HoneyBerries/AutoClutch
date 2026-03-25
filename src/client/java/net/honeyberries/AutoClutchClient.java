package net.honeyberries;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.honeyberries.clutch.ClutchHandler;
import net.honeyberries.config.AutoClutchConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;

/**
 * Client-side initializer for the AutoClutch mod.
 * <p>
 * Handles keybinding registration, tick event registration, and toggling the mod on/off.
 */
public class AutoClutchClient implements ClientModInitializer {
    /** Keybinding for toggling AutoClutch on/off. */
    private static KeyMapping toggleKey;
    /** Handles the clutch logic each tick. */
    private static final ClutchHandler clutchHandler = new ClutchHandler();

    /**
     * Called by Fabric when the client is initializing.
     * Registers keybindings, tick events, and loads config.
     */
    @Override
    public void onInitializeClient() {
        // Load configuration from disk
        AutoClutchConfig.load();

        KeyMapping.Category AUTOCLUTCH_CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(AutoClutch.MOD_ID, "keybinds")
        );

        // Register the keybinding for toggling the mod
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.autoclutch.togglekeybind",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_DONT_CARE,
                AUTOCLUTCH_CATEGORY)
        );

        // Register a tick event to run clutch logic and handle keybinds
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            clutchHandler.tick(client);

            // Handle toggle keybind presses
            while (toggleKey.consumeClick()) {
                boolean newState = !AutoClutchConfig.getInstance().enabled;
                AutoClutchConfig.getInstance().enabled = newState;
                AutoClutchConfig.save();

                // Show a message to the player when toggling
                if (client.player != null) {
                    String key = newState ? "text.autoclutch.enabled" : "text.autoclutch.disabled";
                    // Use a colored translatable component for the action bar message
                    Component message = Component.translatable(key).withStyle(style ->
                        style.withColor(newState ? ChatFormatting.GREEN: ChatFormatting.RED) // Green when enabled, red when disabled
                    );
                    client.player.displayClientMessage(message, true);
                }
            }
        });

        // Log initialization
        AutoClutch.LOGGER.info("AutoClutch initialized!");
    }
}