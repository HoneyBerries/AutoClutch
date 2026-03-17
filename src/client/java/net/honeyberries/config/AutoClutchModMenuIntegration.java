package net.honeyberries.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * ModMenu integration for AutoClutch.
 * <p>
 * Provides a YACL-powered config screen with safe sliders for mean and variance,
 * and a toggle for enabling/disabling the mod. All values are clamped to safe ranges.
 */
public class AutoClutchModMenuIntegration implements ModMenuApi {
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
                        // General group: enable/disable toggle
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
                                .build())
                        // Timing group: mean and variance sliders
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("config.autoclutch.group.timing"))
                                .description(OptionDescription.of(Component.translatable("config.autoclutch.group.timing.description")))
                                // Mean slider
                                .option(Option.<Double>createBuilder()
                                        .name(Component.translatable("config.autoclutch.mean"))
                                        .description(OptionDescription.of(Component.translatable("config.autoclutch.mean.description")))
                                        .binding(
                                                AutoClutchConfig.DEFAULT_MEAN_BLOCKS,
                                                () -> AutoClutchConfig.getInstance().meanBlocks,
                                                value -> AutoClutchConfig.getInstance().meanBlocks = value
                                        )
                                        .controller(option -> DoubleSliderControllerBuilder.create(option)
                                                .range(AutoClutchConfig.MIN_BLOCKS, AutoClutchConfig.MAX_BLOCKS)
                                                .step(0.1)
                                                .formatValue(value -> Component.literal(String.format(Locale.ROOT, "%.1f blocks", value))))
                                        .build())
                                // Variance slider
                                .option(Option.<Double>createBuilder()
                                        .name(Component.translatable("config.autoclutch.variance"))
                                        .description(OptionDescription.of(Component.translatable("config.autoclutch.variance.description")))
                                        .binding(
                                                AutoClutchConfig.DEFAULT_VARIANCE_BLOCKS,
                                                () -> AutoClutchConfig.getInstance().varianceBlocks,
                                                value -> AutoClutchConfig.getInstance().varianceBlocks = value
                                        )
                                        .controller(option -> DoubleSliderControllerBuilder.create(option)
                                                .range(AutoClutchConfig.MIN_VARIANCE_BLOCKS, AutoClutchConfig.MAX_VARIANCE_BLOCKS)
                                                .step(0.1)
                                                .formatValue(value -> Component.literal(String.format(Locale.ROOT, "%.1f blocks", value))))
                                        .build())
                                .build())
                        .build())
                // Save callback: persists config to disk
                .save(AutoClutchConfig::save)
            .build()
        .generateScreen(parent);
    }
}
