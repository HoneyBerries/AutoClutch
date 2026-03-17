package net.honeyberries.clutch;

import net.honeyberries.config.AutoClutchConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class ClutchHandler {
    private static final Random random = new Random();
    private boolean isFalling = false;
    private boolean hasTriggered = false;
    private double targetDistanceBlocks = -1;

    public void tick(Minecraft client) {
        if (!AutoClutchConfig.getInstance().enabled) {
            reset();
            return;
        }

        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            reset();
            return;
        }

        // Check if player is falling
        boolean currentlyFalling = player.getDeltaMovement().y < -0.5 && !player.onGround();

        // Reset if no longer falling or on ground
        if (!currentlyFalling || player.onGround()) {
            reset();
            return;
        }

        // Just started falling - initialize trigger distance
        if (!isFalling) {
            isFalling = true;
            hasTriggered = false;
            targetDistanceBlocks = sampleTruncatedNormal(
                    AutoClutchConfig.getInstance().meanBlocks,
                    AutoClutchConfig.getInstance().varianceBlocks,
                    AutoClutchConfig.MIN_BLOCKS,
                    AutoClutchConfig.MAX_BLOCKS
            );
        }

        // Already triggered this fall
        if (hasTriggered) {
            return;
        }

        // Check if player is holding a water bucket
        if (!isHoldingWaterBucket(player)) {
            return;
        }

        // Calculate distance to ground
        double distanceToGround = getDistanceToGround(player, client.level);

        // Trigger water bucket placement when we reach target distance
        if (distanceToGround > 0 && distanceToGround <= targetDistanceBlocks) {
            placeWaterBucket(client, player);
            hasTriggered = true;
        }
    }

    private void reset() {
        isFalling = false;
        hasTriggered = false;
        targetDistanceBlocks = -1;
    }

    private boolean isHoldingWaterBucket(LocalPlayer player) {
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);

        return mainHand.is(Items.WATER_BUCKET) || offHand.is(Items.WATER_BUCKET);
    }

    private double getDistanceToGround(LocalPlayer player, Level level) {
        Vec3 playerPos = player.position();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // Start from player position and raycast downward
        double startY = playerPos.y;

        // Check up to 100 blocks down
        for (int i = 0; i < 100; i++) {
            pos.set(playerPos.x, startY - i, playerPos.z);
            BlockState state = level.getBlockState(pos);

            // Found a solid block
            if (!state.isAir() && state.isSolid()) {
                double groundY = pos.getY() + 1.0; // Top of the block
                return playerPos.y - groundY;
            }
        }

        // No ground found within 100 blocks
        return -1;
    }

    private void placeWaterBucket(Minecraft client, LocalPlayer player) {
        // Determine which hand has the water bucket
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (!player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.WATER_BUCKET)) {
            hand = InteractionHand.OFF_HAND;
        }

        // Use the vanilla interaction system - this creates the exact same packet as a real click
        if (client.gameMode != null) {
            client.gameMode.useItem(player, hand);
        }
    }

    /**
     * Sample from a truncated normal distribution.
     * Uses rejection sampling to stay within bounds.
     *
     * @param mean Mean of the distribution (in blocks)
     * @param stddev Standard deviation (in blocks)
     * @param min Minimum value (1.5 blocks)
     * @param max Maximum value (4.5 blocks)
     * @return A sampled value within bounds
     */
    private double sampleTruncatedNormal(double mean, double stddev, double min, double max) {
        double sample;
        int attempts = 0;
        final int maxAttempts = 1000; // Safety limit

        do {
            sample = mean + random.nextGaussian() * stddev;
            attempts++;

            // Safety: if we can't find a valid sample, just use the mean
            if (attempts > maxAttempts) {
                sample = Math.max(min, Math.min(max, mean));
                break;
            }
        } while (sample < min || sample > max);

        return sample;
    }
}
