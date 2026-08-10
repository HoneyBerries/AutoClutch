package net.honeyberries.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * ModMenu integration for AutoClutch.
 * <p>
 * Provides a YACL-powered config screen with toggles for enabling/disabling the mod
 * and its water bucket clutch behavior.
 */
public class AutoClutchModMenu implements ModMenuApi {
    /**
     * Returns a factory for the AutoClutch config screen, as required by ModMenu.
     * The screen is built using YACL and reflects/saves values to AutoClutchConfig.
     */
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::createConfigScreen;
    }

    /**
     * Builds the YACL config screen for AutoClutch.
     *
     * @param parent The parent screen (ModMenu passes this in)
     * @return The config screen instance
     */
    private Screen createConfigScreen(Screen parent) {
        // Build the config UI using YACL's builder API
        return YetAnotherConfigLib.createBuilder()
                // Set the screen title
                .title(Component.translatable("config.autoclutch.title"))
                // Add the main config category
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("config.autoclutch.category.general"))
                        // General group: enable/disable toggle and material selection
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("config.autoclutch.group.general"))
                                .description(OptionDescription.of(Component.translatable("config.autoclutch.group.general.description")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.autoclutch.enabled"))
                                        .description(OptionDescription.of(Component.translatable("config.autoclutch.enabled.description")))
                                        // Bind to config value
                                        .binding(true, () -> AutoClutchConfig.getInstance().enabled, value -> AutoClutchConfig.getInstance().enabled = value)
                                        .controller(BooleanControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.autoclutch.enable_water"))
                                        .description(OptionDescription.of(Component.translatable("config.autoclutch.enable_water.description")))
                                        .binding(true, () -> AutoClutchConfig.getInstance().enableWater, value -> AutoClutchConfig.getInstance().enableWater = value)
                                        .controller(BooleanControllerBuilder::create)
                                        .build())
                                .build())
                        .build())
                // Save callback: persists config to disk
                .save(AutoClutchConfig::save)
            .build()
        .generateScreen(parent);
    }
}
