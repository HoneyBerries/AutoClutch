package net.honeyberries.clutch;

import net.honeyberries.AutoClutch;
import net.honeyberries.config.AutoClutchConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Random;

/**
 * Handles automatic water bucket clutching for the player.
 * <p>
 * This class detects when the player is falling and, if holding a water bucket, attempts to place water
 * at a configurable distance from the ground to prevent fall damage. The trigger distance is sampled
 * from a truncated normal distribution to simulate human-like reaction times.
 */
public class ClutchHandler {
    /** Random instance for sampling trigger distances. */
    private static final Random random = new Random();

    /** True if the player is currently falling and clutch logic is active. */
    private boolean isActiveAndFalling = false;

    /** True if the clutch action has already been triggered during this fall. */
    private boolean hasTriggered = false;

    /** True once we're in the placement window so we can retry until water is placed. */
    private boolean attemptingPlacement = false;

    /** The target distance (in blocks) from the ground to trigger the clutch. */
    private double targetDistanceBlocks = -1;


    private final Logger logger = AutoClutch.LOGGER;



    /**
     * Called every client tick to update clutch logic.
     *
     * @param client The Minecraft client instance.
     */
    public void tick(Minecraft client) {
        // If the mod is disabled, reset state and do nothing
        if (!AutoClutchConfig.getInstance().enabled) {
            reset();
            return;
        }

        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            reset();
            return;
        }

        // Check if player is falling and will take damage
        boolean falling = !player.onGround() && player.getDeltaMovement().y < -0.08;
        boolean willTakeDamage = falling
                && player.fallDistance > 2.0
                && Objects.requireNonNull(player.gameMode()).isSurvival();

        // Reset if conditions aren't met
        if (!willTakeDamage) {
            reset();
            return;
        }

        // Just started falling - initialize trigger distance
        if (!isActiveAndFalling) {
            isActiveAndFalling = true;
            hasTriggered = false;
            attemptingPlacement = false;
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

        Level level = client.level;

        // Calculate distance to ground
        double distanceToGround = getDistanceToGround(player, level);

        // Use the configured timing but enforce a minimum buffer so we never trigger too late
        double safeTriggerDistance = Math.max(
                targetDistanceBlocks,
                AutoClutchConfig.MIN_BLOCKS + 0.75
        );

        boolean withinTriggerWindow = distanceToGround > 0 && distanceToGround <= safeTriggerDistance;
        boolean inFailsafeWindow = distanceToGround > 0 && distanceToGround <= AutoClutchConfig.MIN_BLOCKS + 0.5;

        if (withinTriggerWindow) {
            attemptingPlacement = true;
        }

        if (attemptingPlacement || inFailsafeWindow) {
            boolean placed = placeWaterBucket(client, player, level);
            if (placed) {
                hasTriggered = true;
                attemptingPlacement = false;
            }
        }
    }

    /**
     * Resets the clutch handler state for a new fall or when clutching is not needed.
     */
    private void reset() {
        isActiveAndFalling = false;
        hasTriggered = false;
        attemptingPlacement = false;
        targetDistanceBlocks = -1;
    }

    /**
     * Checks if the player is holding a water bucket in either hand.
     *
     * @param player The player to check.
     * @return True if the player is holding a water bucket.
     */
    private boolean isHoldingWaterBucket(LocalPlayer player) {
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);

        return mainHand.is(Items.WATER_BUCKET) || offHand.is(Items.WATER_BUCKET);
    }

    /**
     * Calculates the vertical distance from the player to the nearest solid ground below.
     *
     * @param player The player whose position to check.
     * @param level  The world level.
     * @return The distance in blocks to the ground, or -1 if no ground found within 100 blocks.
     */
    private double getDistanceToGround(LocalPlayer player, Level level) {
        Vec3 playerPos = player.position();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // Start from player position and raycast downward
        double startY = playerPos.y;

        // Check up to 128 blocks down
        for (int i = 0; i < 128; i++) {
            pos.set(playerPos.x, startY - i, playerPos.z);
            BlockState state = level.getBlockState(pos);

            // Found a solid block
            if (!state.isAir() && state.isCollisionShapeFullBlock(level, pos)) {
                double groundY = pos.getY() + 1.0; // Top of the block
                return playerPos.y - groundY;
            }
        }

        // No ground found within 100 blocks
        return -1;
    }

    /**
     * Attempts to place a water bucket using the correct hand.
     *
     * @param client The Minecraft client instance.
     * @param player The player performing the action.
     */
    private boolean placeWaterBucket(Minecraft client, LocalPlayer player, Level level) {
        if (client.gameMode == null) return false;

        // Determine which hand is holding the water bucket
        InteractionHand hand = InteractionHand.MAIN_HAND;

        // Check if main hand is NOT a water bucket.
        // If it's not, we check if the off-hand IS a water bucket.
        if (!player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.WATER_BUCKET)) {
            hand = InteractionHand.OFF_HAND;
        }

        // Use the item in the identified hand
        client.gameMode.useItem(player, hand);

        // Success when the bucket is consumed or water already exists at/under the player
        boolean bucketConsumed = !player.getItemInHand(hand).is(Items.WATER_BUCKET);
        boolean waterPlaced = isWaterNearby(player, level);

        return bucketConsumed || waterPlaced;
    }

    private boolean isWaterNearby(LocalPlayer player, Level level) {
        BlockPos feet = player.blockPosition();
        return level.getFluidState(feet).is(FluidTags.WATER)
                || level.getFluidState(feet.below()).is(FluidTags.WATER);
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
        final int maxAttempts = 16; // Safety limit

        do {
            sample = mean + random.nextGaussian() * stddev;
            attempts++;

            // Safety: if we can't find a valid sample, just use the mean
            if (attempts > maxAttempts) {
                sample = Math.max(min, Math.min(max, mean));
                break;
            }
        } while (sample < min || sample > max);

        logger.info("Sample {} from truncated normal distribution with mean {} and standard deviation {}", sample, mean, stddev);

        return sample;
    }
}
