package com.example.ascensionmod.events;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = "ascensionmod")
public class PlayerSpawnHandler {

    public static final ResourceKey<Level> MODDED_DIMENSION = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("ecod", "the_depths"));
    private static BlockPos randomSpawnPoint = null;
    private static final Random random = new Random();

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel moddedLevel = event.getServer().getLevel(MODDED_DIMENSION);
        if (moddedLevel != null) {
            randomSpawnPoint = generateRandomSpawnPoint(moddedLevel);
            System.out.println("Random spawn point set to: " + randomSpawnPoint);
        } else {
            System.err.println("Modded dimension not found!");
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!player.getPersistentData().contains("has_spawned_in_modded_dimension")) {
                player.getPersistentData().putBoolean("has_spawned_in_modded_dimension", true);

                ServerLevel targetLevel = player.getServer().getLevel(MODDED_DIMENSION);
                if (targetLevel != null && randomSpawnPoint != null) {
                    ensureAirBlocksAroundSpawn(targetLevel, randomSpawnPoint);
                    player.setRespawnPosition(MODDED_DIMENSION, randomSpawnPoint, 0.0F, true, false);
                    player.teleportTo(targetLevel, randomSpawnPoint.getX(), randomSpawnPoint.getY(), randomSpawnPoint.getZ(), 0.0F, 0.0F);
                } else {
                    System.err.println("Failed to load modded dimension or spawn point is null!");
                }
            }
        }
    }

    private static BlockPos generateRandomSpawnPoint(ServerLevel level) {
        if (level == null) {
            return null;
        }

        int minX = -1000;
        int maxX = 1000;
        int minZ = -1000;
        int maxZ = 1000;

        // Generate random X and Z coordinates
        int x = random.nextInt(maxX - minX) + minX;
        int z = random.nextInt(maxZ - minZ) + minZ;

        // Find th at the generated X/Z coordinatese roof height (highest non-air block)
        int roofHeight = findRoofHeight(level, x, z);

        // Find a suitable Y coordinate below the roof
        int y = findSuitableY(level, x, z, roofHeight);

        return new BlockPos(x, y, z);
    }

    private static int findRoofHeight(ServerLevel level, int x, int z) {
        // Start from the top of the world and move downward to find the highest non-air block
        for (int y = level.getMaxBuildHeight() - 1; y >= level.getMinBuildHeight(); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.getBlockState(pos).isAir()) {
                return y; // Return the Y-coordinate of the highest non-air block
            }
        }
        // If no non-air block is found, return a default value (e.g., just below the max build height)
        return level.getMaxBuildHeight() - 1;
    }

    private static int findSuitableY(ServerLevel level, int x, int z, int roofHeight) {
        // Start just below the roof and move downward to find a valid spawn point
        for (int y = roofHeight - 1; y >= level.getMinBuildHeight(); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockPos belowPos = pos.below();

            // Check if the block at the current position is air and the block below is solid
            if (level.getBlockState(pos).isAir() && !level.getBlockState(belowPos).isAir()) {
                return y; // Return the first valid Y coordinate
            }
        }

        // If no valid Y coordinate is found, return a default value (e.g., just below the roof)
        return roofHeight - 1;
    }

    private static void ensureAirBlocksAroundSpawn(ServerLevel level, BlockPos spawnPos) {
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 2; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos pos = spawnPos.offset(x, y, z);
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }
}