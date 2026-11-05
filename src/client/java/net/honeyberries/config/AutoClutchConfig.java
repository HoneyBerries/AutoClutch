package net.honeyberries.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.AutoGen;
import dev.isxander.yacl3.config.v2.api.autogen.TickBox;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.honeyberries.AutoClutch;
import net.minecraft.resources.Identifier;

/**
 * Configuration for AutoClutch, built on YACL's annotation-based {@code @AutoGen} system.
 * <p>
 * Labels live in {@code en_us.json} under {@code yacl3.config.autoclutch:config.*}; the
 * {@code checkConfigLangKeys} Gradle task fails the build if any is missing.
 * Field names must be unique because option lang keys are scoped by field name only.
 * The config is stored at {@code config/autoclutch.json}.
 */
public class AutoClutchConfig {
    public static final ConfigClassHandler<AutoClutchConfig> HANDLER = ConfigClassHandler
            .createBuilder(AutoClutchConfig.class)
            .id(Identifier.fromNamespaceAndPath(AutoClutch.MOD_ID, "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("autoclutch.json"))
                    .build())
            .build();

    /** @return the live config instance. */
    public static AutoClutchConfig config() {
        return HANDLER.instance();
    }

    /** Saves the config off the tick thread. */
    public static void saveAsync() {
        Thread.startVirtualThread(HANDLER::save);
    }

    private static final String GENERAL = "general";

    @SerialEntry
    @AutoGen(category = GENERAL, group = "general")
    @TickBox
    public boolean enabled = true;

    @SerialEntry
    @AutoGen(category = GENERAL, group = "general")
    @TickBox
    public boolean enableWater = true;
}
