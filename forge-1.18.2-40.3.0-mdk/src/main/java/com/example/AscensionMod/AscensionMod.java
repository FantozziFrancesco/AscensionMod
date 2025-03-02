package com.example.ascensionmod;

import java.util.ArrayList;
import java.util.List;
import com.example.ascensionmod.world.dimension.AscensionITeleporter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.common.MinecraftForge;

// Add these imports for logging
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

// Add this import for PlayerChangedDimensionEvent
import net.minecraftforge.event.entity.player.PlayerEvent;

@Mod(AscensionMod.MODID)
public class AscensionMod {
    public static final String MODID = "ascensionmod";
    private static final Logger LOGGER = LogManager.getLogger();

    // Deferred Register for Potions
    private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, MODID);

    // Register the Potion of Ascension
    public static final RegistryObject<Potion> POTION_OF_ASCENSION = POTIONS.register("potion_of_ascension",
            () -> new Potion("potion_of_ascension", new MobEffectInstance(MobEffects.LUCK, 600, 0), new MobEffectInstance(MobEffects.BLINDNESS, 200, 0)));

    // Register the Homing Potion
    public static final RegistryObject<Potion> HOMING_POTION = POTIONS.register("homing_potion",
            () -> new Potion("homing_potion", new MobEffectInstance(MobEffects.LUCK, 0, 0))); // No visible effect

    // Register the Potion of Descent
    public static final RegistryObject<Potion> POTION_OF_DESCENT = POTIONS.register("potion_of_descent",
            () -> new Potion("potion_of_descent", new MobEffectInstance(MobEffects.DIG_SPEED, 0, 0))); // No visible effect

    // Flag to track if the player has consumed the Potion of Ascension
    private static final String HAS_CONSUMED_ASCENSION_POTION = "has_consumed_ascension_potion";

    public AscensionMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the deferred registers
        POTIONS.register(modEventBus);

        // Register event listeners
        modEventBus.addListener(this::setup);

        // Register Forge event bus
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // No brewing recipe needed here, as Mystic Alchemy handles it via JSON
    }

    // Event handler to check for potions with Luck and Blindness
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getPlayer();
        ItemStack stack = event.getItemStack();

        if (stack.getItem() == Items.POTION || stack.getItem() == Items.SPLASH_POTION || stack.getItem() == Items.LINGERING_POTION) {
            List<MobEffectInstance> effects = PotionUtils.getMobEffects(stack);

            boolean hasLuck = false;
            boolean hasBlindness = false;

            // Check if the potion has both Luck and Blindness effects
            for (MobEffectInstance effect : effects) {
                if (effect.getEffect() == MobEffects.LUCK) {
                    hasLuck = true;
                }
                if (effect.getEffect() == MobEffects.BLINDNESS) {
                    hasBlindness = true;
                }
            }

            // If both effects are present, replace the potion with the Potion of Ascension
            if (hasLuck && hasBlindness) {
                ItemStack ascensionPotion = new ItemStack(stack.getItem()); // Preserve the potion type (regular, splash, or lingering)
                PotionUtils.setPotion(ascensionPotion, POTION_OF_ASCENSION.get());
                player.setItemInHand(event.getHand(), ascensionPotion);
            }

            // Check if the potion has Blindness, Slowness, and Mining Fatigue
            boolean hasSlowness = false;
            boolean hasWeakness = false;

            for (MobEffectInstance effect : effects) {
                if (effect.getEffect() == MobEffects.MOVEMENT_SLOWDOWN) {
                    hasSlowness = true;
                }
                if (effect.getEffect() == MobEffects.WEAKNESS) {
                    hasWeakness = true;
                }
            }

            // If all three effects are present, replace the potion with the Potion of Descent
            if (hasBlindness && hasSlowness && hasWeakness) {
                ItemStack descentPotion = new ItemStack(stack.getItem()); // Preserve the potion type (regular, splash, or lingering)
                PotionUtils.setPotion(descentPotion, POTION_OF_DESCENT.get());
                player.setItemInHand(event.getHand(), descentPotion);
            }
        }
    }

    // Event handler for potion consumption
    @SubscribeEvent
    public void onPotionConsumed(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            ItemStack stack = event.getItem();
            Potion potion = PotionUtils.getPotion(stack);

            // Check if the consumed potion is the Homing Potion
            if (potion == HOMING_POTION.get()) {
                LOGGER.info("Homing Potion consumed!");
                AscensionITeleporter.teleportToSpawnOnPotionConsumption(player, LOGGER);
            }

            // Check if the consumed potion is the Potion of Ascension
            if (potion == POTION_OF_ASCENSION.get()) {
                LOGGER.info("Potion of Ascension consumed!");
                AscensionITeleporter.teleportToNetherOnPotionConsumption(player, LOGGER);
            }

            // Check if the consumed potion is the Potion of Descent
            if (potion == POTION_OF_DESCENT.get()) {
                LOGGER.info("Potion of Descent consumed!");
                AscensionITeleporter.teleportToCaveDimensionOnPotionConsumption(player, LOGGER);
            }
        }
    }

    // Event handler to check player Y-coordinate and teleport if necessary
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            Level world = player.level;
            int buildLimit = world.getMaxBuildHeight();

            // Check if the player has consumed the Potion of Ascension
            boolean hasConsumedPotion = player.getPersistentData().getBoolean(HAS_CONSUMED_ASCENSION_POTION);

            // Check if the player is in the placeholder dimension (Nether)
            if (world.dimension() == Level.NETHER) {
                // If the player is above the build limit, teleport them to the overworld
                if (player.getY() > buildLimit) {
                    LOGGER.info("Player exceeded build limit in placeholder dimension. Teleporting to overworld...");
                    ArrayList<Object> result = AscensionITeleporter.fallToOverworld(player, LOGGER);
                    ServerPlayer teleportedPlayer = (ServerPlayer) result.get(0);
                    BlockPos destinationPos = (BlockPos) result.get(1);
                    AscensionITeleporter.teleportPlayerWithMount(teleportedPlayer, teleportedPlayer.getServer().getLevel(Level.OVERWORLD), destinationPos, LOGGER);
                }
            }
            // Check if the player is in the overworld
            else if (world.dimension() == Level.OVERWORLD) {
                // If the player is below the build limit, teleport them back to the placeholder dimension
                if (player.getY() < world.getMinBuildHeight() && hasConsumedPotion) {
                    LOGGER.info("Player fell below build limit in overworld. Teleporting to placeholder dimension...");
                    ArrayList<Object> result = AscensionITeleporter.ascendFromOverworld(player, LOGGER);
                    ServerPlayer teleportedPlayer = (ServerPlayer) result.get(0);
                    BlockPos destinationPos = (BlockPos) result.get(1);
                    AscensionITeleporter.teleportPlayerWithMount(teleportedPlayer, teleportedPlayer.getServer().getLevel(Level.NETHER), destinationPos, LOGGER);

                    // Give the player Slow Falling for 1 minute
                    player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 1200, 0));
                }
            }
        }
    }

    // Event handler to record spawnpoints in the Cave Dimension
    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) event.getEntity();

            // Check if the player entered the Cave Dimension (The Depths)
            if (event.getTo() == AscensionITeleporter.THE_DEPTHS) {
                // Record the player's spawnpoint in the Cave Dimension
                AscensionITeleporter.recordCaveDimensionSpawnpoint(player);
            }
        }
    }
}