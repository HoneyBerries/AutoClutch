package net.honeyberries.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Example mixin for injecting code into MinecraftServer.loadLevel().
 * <p>
 * This demonstrates how to use Mixin to run code at the start of a server method.
 */
@Mixin(MinecraftServer.class)
public class ExampleMixin {
    /**
     * Injects code at the start of MinecraftServer.loadLevel().
     *
     * @param info Callback info provided by Mixin.
     */
    @Inject(at = @At("HEAD"), method = "loadLevel")
    private void init(CallbackInfo info) {
        // This code is injected into the start of MinecraftServer.loadLevel()V
    }
}