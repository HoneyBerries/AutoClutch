package net.honeyberries.clutch;

import net.honeyberries.AutoClutch;
import net.honeyberries.config.AutoClutchConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
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

        // Calculate eye-level distance to ground
        double distanceToGround = getEyeDistanceToGround(player, client.level);
        logger.debug("Eye distance to ground: {}", distanceToGround);

        // Check if player is falling and will take damage
        boolean willTakeDamage = player.fallDistance > 3.0
                && !player.onGround()
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
            targetDistanceBlocks = sampleTruncatedNormal(
                    AutoClutchConfig.getInstance().meanBlocks,
                    AutoClutchConfig.getInstance().varianceBlocks
            );
        }

        // Already triggered this fall
        if (hasTriggered) {
            return;
        }

        // Check if player is holding a water bucket or powdered snow
        if (!isHoldingClutchMaterial(player)) {
            return;
        }


        // Trigger water bucket placement when we reach target distance
        if (distanceToGround > 0 && distanceToGround <= targetDistanceBlocks) {
            placeItem(client, player);
            hasTriggered = true;
        }
    }

    /**
     * Resets the clutch handler state for a new fall or when clutching is not needed.
     */
    private void reset() {
        isActiveAndFalling = false;
        hasTriggered = false;
        targetDistanceBlocks = -1;
    }

    /**
     * Checks if the player is holding a water bucket in either hand, and water is enabled in config.
     *
     * @param player The player to check.
     * @return True if the player is holding a water bucket and it's enabled.
     */
    private boolean isHoldingClutchMaterial(LocalPlayer player) {
        if (!AutoClutchConfig.getInstance().enableWater) {
            return false;
        }

        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);

        return mainHand.is(Items.WATER_BUCKET) || offHand.is(Items.WATER_BUCKET);
    }

    /**
     * Gets the interaction hand that is holding the water bucket.
     *
     * @param player The player to check.
     * @return The hand holding the water bucket, or null if not found.
     */
    @Nullable
    private InteractionHand getClutchMaterialHand(LocalPlayer player) {
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);

        if (mainHand.is(Items.WATER_BUCKET)) {
            return InteractionHand.MAIN_HAND;
        } else if (offHand.is(Items.WATER_BUCKET)) {
            return InteractionHand.OFF_HAND;
        }

        return null;
    }

    /**
     * Calculates the vertical distance from the player's eyes to the nearest solid ground below.
     *
     * @param player The player whose position to check.
     * @param level  The world level.
     * @return The distance in blocks from the player's eyes to the ground, or -1 if no ground found within 128 blocks.
     */
    private double getEyeDistanceToGround(LocalPlayer player, Level level) {
        Vec3 playerPos = player.position();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // Start from the player's eye position and scan downward
        double startY = player.getEyeY();

        // Check up to 128 blocks down
        for (int i = 0; i < 128; i++) {
            pos.set(playerPos.x, startY - i, playerPos.z);
            BlockState state = level.getBlockState(pos);

            // Found a solid block
            if (!state.isAir() && state.isCollisionShapeFullBlock(level, pos)) {
                double groundY = pos.getY() + 1.0; // Top of the block
                return startY - groundY;
            }
        }

        // No ground found within 128 blocks
        return -1;
    }

    /**
     * Attempts to place a water bucket using the correct hand.
     *
     * @param client The Minecraft client instance.
     * @param player The player performing the action.
     */
    private void placeItem(Minecraft client, LocalPlayer player) {
        if (client.gameMode == null) return;

        // Determine which hand is holding the water bucket
        InteractionHand hand = getClutchMaterialHand(player);

        if (hand == null) {
            logger.error("Failed to determine clutch material hand");
            return;
        }

        logger.info("Placing water bucket for clutch");
        client.gameMode.useItem(player, hand);
    }


    /**
     * Sample from a truncated normal distribution.
     * Uses rejection sampling to stay within bounds.
     *
     * @param mean Mean of the distribution (in blocks)
     * @param stddev Standard deviation (in blocks)
     * @return A sampled value within bounds
     */
    private double sampleTruncatedNormal(double mean, double stddev) {
        double sample;
        int attempts = 0;
        final int maxAttempts = 16; // Safety limit

        do {
            sample = mean + random.nextGaussian() * stddev;
            attempts++;

            // Safety: if we can't find a valid sample, just use the mean
            if (attempts > maxAttempts) {
                sample = Math.clamp(mean, AutoClutchConfig.MIN_BLOCKS, AutoClutchConfig.MAX_BLOCKS);
                break;
            }
        } while (sample < AutoClutchConfig.MIN_BLOCKS || sample > AutoClutchConfig.MAX_BLOCKS);

        logger.info("Sample {} from truncated normal distribution with mean {} and standard deviation {}", sample, mean, stddev);

        return sample;
    }
}
