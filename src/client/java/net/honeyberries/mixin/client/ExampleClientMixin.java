package net.honeyberries.mixin.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Example client mixin for injecting code into Minecraft.run().
 * <p>
 * This demonstrates how to use Mixin to run code at the start of a client method.
 */
@Mixin(Minecraft.class)
public class ExampleClientMixin {
    /**
     * Injects code at the start of Minecraft.run().
     *
     * @param info Callback info provided by Mixin.
     */
    @Inject(at = @At("HEAD"), method = "run")
    private void init(CallbackInfo info) {
        // This code is injected into the start of Minecraft.run()V
    }
}