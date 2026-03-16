package net.honeyberries;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.honeyberries.clutch.ClutchHandler;
import net.honeyberries.config.AutoClutchConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;

public class AutoClutchClient implements ClientModInitializer {
	private static KeyMapping toggleKey;
	private static final ClutchHandler clutchHandler = new ClutchHandler();

	@Override
	public void onInitializeClient() {
		// Load config
		AutoClutchConfig.load();

		// Register keybinding
		toggleKey = new KeyMapping(
				"key.autoclutch.toggle",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_B,
				Category.MISC
		);
		KeyBindingHelper.registerKeyBinding(toggleKey);

		// Register tick event for clutch handler
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			clutchHandler.tick(client);

			// Handle toggle keybind
			while (toggleKey.consumeClick()) {
				boolean newState = !AutoClutchConfig.getInstance().enabled;
				AutoClutchConfig.getInstance().enabled = newState;
				AutoClutchConfig.save();

				if (client.player != null) {
					String key = newState ? "text.autoclutch.enabled" : "text.autoclutch.disabled";
					client.player.displayClientMessage(Component.translatable(key), true);
				}
			}
		});

		AutoClutch.LOGGER.info("AutoClutch initialized!");
	}
}