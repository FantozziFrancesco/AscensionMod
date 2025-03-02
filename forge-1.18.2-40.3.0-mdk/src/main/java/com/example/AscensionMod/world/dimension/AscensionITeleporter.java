package com.example.ascensionmod.world.dimension;

import net.minecraft.core.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.ITeleporter;

// Add this import for Entity
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;

public class AscensionITeleporter implements ITeleporter {
    public AscensionITeleporter() {
    }

    // Constants for tracking spawnpoints in the Cave Dimension
    private static final String CAVE_DIMENSION_SPAWN_X = "cave_dimension_spawn_x";
    private static final String CAVE_DIMENSION_SPAWN_Y = "cave_dimension_spawn_y";
    private static final String CAVE_DIMENSION_SPAWN_Z = "cave_dimension_spawn_z";

    // Use "ecod:the_depths" as the Cave Dimension
    public static final ResourceKey<Level> THE_DEPTHS = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("ecod", "the_depths"));

    /**
     * Records the player's current position as their spawnpoint in the Cave Dimension.
     */
    public static void recordCaveDimensionSpawnpoint(ServerPlayer player) {
        player.getPersistentData().putInt(CAVE_DIMENSION_SPAWN_X, (int) player.getX());
        player.getPersistentData().putInt(CAVE_DIMENSION_SPAWN_Y, (int) player.getY());
        player.getPersistentData().putInt(CAVE_DIMENSION_SPAWN_Z, (int) player.getZ());
    }

    /**
     * Retrieves the player's recorded spawnpoint in the Cave Dimension.
     */
    public static BlockPos getCaveDimensionSpawnpoint(ServerPlayer player) {
        int x = player.getPersistentData().getInt(CAVE_DIMENSION_SPAWN_X);
        int y = player.getPersistentData().getInt(CAVE_DIMENSION_SPAWN_Y);
        int z = player.getPersistentData().getInt(CAVE_DIMENSION_SPAWN_Z);
        return new BlockPos(x, y, z);
    }

    /**
     * Teleports the player to their last recorded spawnpoint in the Cave Dimension when consuming the Potion of Descent.
     */
    public static void teleportToCaveDimensionOnPotionConsumption(ServerPlayer player, org.apache.logging.log4j.Logger logger) {
        ServerLevel caveLevel = player.getServer().getLevel(THE_DEPTHS);

        // If the Cave Dimension is not loaded, log an error and return
        if (caveLevel == null) {
            logger.error("Cave Dimension (The Depths) not loaded!");
            return;
        }

        // Check if the player is already in the Cave Dimension
        if (player.level.dimension() == THE_DEPTHS) {
            logger.info("Player is already in the Cave Dimension (The Depths)!");
            return;
        }

        // Get the player's last recorded spawnpoint in the Cave Dimension
        BlockPos spawnPos = getCaveDimensionSpawnpoint(player);

        // Teleport the player and their mount
        teleportPlayerWithMount(player, caveLevel, spawnPos, logger);
    }

    /**
     * Teleports the player from the placeholder dimension (Nether) to the overworld at the same X and Z coordinates.
     * If the player is mounted, the mount is teleported with them.
     */
    public static ArrayList<Object> fallToOverworld(ServerPlayer player, org.apache.logging.log4j.Logger logger) {
        ServerLevel newLevel = player.getServer().getLevel(Level.OVERWORLD); // Target dimension is overworld
        double pX = player.getX(); // Keep the same X coordinate
        double pY = 0.0; // Set Y-coordinate to 0
        double pZ = player.getZ(); // Keep the same Z coordinate
        BlockPos destinationPos = new BlockPos(pX, pY, pZ);

        if (ForgeHooks.onTravelToDimension(player, newLevel.dimension())) {
            int tries = 0;

            // Find a safe Y-coordinate in the overworld
            BlockPos tempPos = destinationPos;
            while (newLevel.getBlockState(tempPos).getMaterial() != Material.AIR &&
                    !newLevel.getBlockState(tempPos).getFluidState().is(Fluids.WATER) &&
                    newLevel.getBlockState(tempPos.above()).getMaterial() != Material.AIR &&
                    !newLevel.getBlockState(tempPos.above()).getFluidState().is(Fluids.WATER) &&
                    tries < 128) {
                tempPos = tempPos.above(); // Move upward to find a safe spot
                tries++;
            }

            if (tempPos.getY() >= 0) { // Ensure the Y-coordinate is valid
                pY = tempPos.getY();
                destinationPos = new BlockPos(pX, pY, pZ);
            }
        }

        // Teleport the player and their mount
        teleportPlayerWithMount(player, newLevel, destinationPos, logger);

        ArrayList<Object> array = new ArrayList<>();
        array.add(player);
        array.add(destinationPos);
        return array;
    }

    /**
     * Teleports the player from the overworld to the placeholder dimension (Nether) at the same X and Z coordinates.
     * If the player is mounted, the mount is teleported with them.
     */
    public static ArrayList<Object> ascendFromOverworld(ServerPlayer player, org.apache.logging.log4j.Logger logger) {
        ServerLevel newLevel = player.getServer().getLevel(Level.NETHER); // Target dimension is Nether
        double pX = player.getX(); // Keep the same X coordinate
        double pY = newLevel.getMaxBuildHeight() - 1; // Set Y-coordinate to build limit height
        double pZ = player.getZ(); // Keep the same Z coordinate
        BlockPos destinationPos = new BlockPos(pX, pY, pZ);

        if (ForgeHooks.onTravelToDimension(player, newLevel.dimension())) {
            int tries = 0;

            // Find a safe Y-coordinate in the Nether
            BlockPos tempPos = destinationPos;
            while (newLevel.getBlockState(tempPos).getMaterial() != Material.AIR &&
                    !newLevel.getBlockState(tempPos).getFluidState().is(Fluids.WATER) &&
                    newLevel.getBlockState(tempPos.below()).getMaterial() != Material.AIR &&
                    !newLevel.getBlockState(tempPos.below()).getFluidState().is(Fluids.WATER) &&
                    tries < 128) {
                tempPos = tempPos.below(); // Move downward to find a safe spot
                tries++;
            }

            if (tempPos.getY() < newLevel.getMaxBuildHeight()) { // Ensure the Y-coordinate is valid
                pY = tempPos.getY();
                destinationPos = new BlockPos(pX, pY, pZ);
            }
        }

        // Teleport the player and their mount
        teleportPlayerWithMount(player, newLevel, destinationPos, logger);

        ArrayList<Object> array = new ArrayList<>();
        array.add(player);
        array.add(destinationPos);
        return array;
    }

    /**
     * Finds a safe Y-coordinate in the Nether at the given X and Z coordinates.
     */
    private static int findSafeY(ServerLevel level, int x, int z) {
        // Start from the top of the world and move downward to find a safe Y-coordinate
        for (int y = level.getMaxBuildHeight() - 1; y >= level.getMinBuildHeight(); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockPos belowPos = pos.below();

            // Check if the block at the current position is air and the block below is solid
            if (level.getBlockState(pos).isAir() && !level.getBlockState(belowPos).isAir()) {
                return y; // Return the first valid Y-coordinate
            }
        }

        // If no valid Y-coordinate is found, return a default value (e.g., just below the roof)
        return level.getMaxBuildHeight() - 1;
    }

    /**
     * Teleports a player and their mount to the specified dimension and position.
     * Ensures the player is not dismounted during teleportation.
     */
    public static void teleportPlayerWithMount(ServerPlayer player, ServerLevel targetLevel, BlockPos targetPos, org.apache.logging.log4j.Logger logger) {
        Entity mount = player.getVehicle(); // Get the player's mount (if any)

        // Teleport the player
        player.teleportTo(targetLevel, targetPos.getX(), targetPos.getY(), targetPos.getZ(), player.getYRot(), player.getXRot());

        // Teleport the mount if the player has one
        if (mount != null) {
            // Use the correct teleportTo method signature
            mount.teleportTo(targetPos.getX(), targetPos.getY(), targetPos.getZ());

            // Re-mount the player on their mount
            player.startRiding(mount, true); // The 'true' parameter forces the player to stay mounted
        }
    }

    /**
     * Teleports the player to their spawnpoint when consuming the Homing Potion.
     */
    public static void teleportToSpawnOnPotionConsumption(ServerPlayer player, org.apache.logging.log4j.Logger logger) {
        ServerLevel spawnLevel = player.getServer().getLevel(player.getRespawnDimension());
        BlockPos spawnPos = player.getRespawnPosition();

        // If the spawn position or level is null, use the world spawn
        if (spawnPos == null || spawnLevel == null) {
            spawnLevel = player.getServer().getLevel(Level.OVERWORLD);
            spawnPos = spawnLevel.getSharedSpawnPos();
        }

        // Teleport the player and their mount
        teleportPlayerWithMount(player, spawnLevel, spawnPos, logger);
    }

    /**
     * Teleports the player to the Nether when consuming the Potion of Ascension.
     */
    public static void teleportToNetherOnPotionConsumption(ServerPlayer player, org.apache.logging.log4j.Logger logger) {
        ServerLevel netherLevel = player.getServer().getLevel(Level.NETHER);

        // If the Nether dimension is not loaded, log an error and return
        if (netherLevel == null) {
            logger.error("Nether dimension not loaded!");
            return;
        }

        // Calculate the Nether coordinates (same X and Z as Overworld)
        double netherX = player.getX();
        double netherZ = player.getZ();

        // Find a safe Y-coordinate in the Nether
        int netherY = findSafeY(netherLevel, (int) netherX, (int) netherZ);

        // Create a BlockPos for the spawn point
        BlockPos netherPos = new BlockPos(netherX, netherY, netherZ);

        // Teleport the player and their mount
        teleportPlayerWithMount(player, netherLevel, netherPos, logger);
    }
}