package com.example.ascensionmod;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.edwinmindcraft.origins.api.origin.Origin;
import io.github.edwinmindcraft.origins.api.origin.OriginRegistry;
import io.github.edwinmindcraft.origins.api.power.Power;
import io.github.edwinmindcraft.origins.api.power.PowerType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.function.Function;

public class ModOrigins {
    public static final ResourceLocation ORIGIN_CRAWLER = new ResourceLocation("ascensionmod", "crawler");
    public static final ResourceLocation ORIGIN_CHASM_HOPPER = new ResourceLocation("ascensionmod", "chasm_hopper");
    public static final ResourceLocation ORIGIN_SCALED_ONE = new ResourceLocation("ascensionmod", "scaled_one");

    public static void registerOrigins() {
        OriginRegistry.register(ORIGIN_CRAWLER, createCrawlerOrigin());
        OriginRegistry.register(ORIGIN_CHASM_HOPPER, createChasmHopperOrigin());
        OriginRegistry.register(ORIGIN_SCALED_ONE, createScaledOneOrigin());
    }

    private static JsonObject loadOriginData(ResourceLocation originId) {
        try {
            InputStream stream = Minecraft.getInstance().getResourceManager().getResource(
                    new ResourceLocation(originId.getNamespace(), "origins/" + originId.getPath() + ".json")
            ).getInputStream();
            return JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
        } catch (Exception e) {
            AscensionMod.LOGGER.error("Failed to load origin data for " + originId, e);
            return null;
        }
    }

    private static Origin createCrawlerOrigin() {
        JsonObject data = loadOriginData(ORIGIN_CRAWLER);
        if (data == null) return null;

        return Origin.builder(ORIGIN_CRAWLER)
                .addPower(new PowerFactory<>(ORIGIN_CRAWLER, data -> (type, player) -> new Power(type, player) {
                    private int climbsRemaining;
                    private boolean isClimbing = false;

                    @Override
                    public void onAdded() {
                        // Apply Weakness effect
                        player.addEffect(new MobEffectInstance(
                                MobEffects.WEAKNESS, // Weakness effect
                                Integer.MAX_VALUE,   // Infinite duration
                                0,                  // Amplifier (level 1)
                                false,              // No particles
                                false               // No icon
                        ));

                        // Adjust size
                        float size = data.get("size").getAsFloat();
                        player.setScale(size);

                        // Adjust max health
                        int maxHealth = data.get("max_health").getAsInt();
                        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealth);

                        // Apply perpetual effects
                        data.getAsJsonArray("perpetual_effects").forEach(effect -> {
                            String effectName = effect.getAsString();
                            MobEffects effectInstance = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectName));
                            if (effectInstance != null) {
                                player.addEffect(new MobEffectInstance(effectInstance, Integer.MAX_VALUE, 0, false, false));
                            } else {
                                AscensionMod.LOGGER.warn("Unknown effect: " + effectName);
                            }
                        });

                        // Initialize climbing
                        climbsRemaining = data.getAsJsonObject("climbing").get("limit").getAsInt();
                    }

                    @Override
                    public void onRemoved() {
                        // Remove Weakness effect
                        player.removeEffect(MobEffects.WEAKNESS);

                        // Reset size
                        player.setScale(1.0f);

                        // Reset max health
                        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0);

                        // Remove perpetual effects
                        data.getAsJsonArray("perpetual_effects").forEach(effect -> {
                            String effectName = effect.getAsString();
                            MobEffects effectInstance = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectName));
                            if (effectInstance != null) {
                                player.removeEffect(effectInstance);
                            }
                        });
                    }

                    @Override
                    public void tick() {
                        // Climbing logic
                        if (player.horizontalCollision && climbsRemaining > 0) {
                            if (!isClimbing) {
                                isClimbing = true;
                                player.setDeltaMovement(player.getDeltaMovement().multiply(1, 0, 1)); // Stop vertical movement
                            }

                            if (player.input.jumping) {
                                player.setDeltaMovement(player.getDeltaMovement().add(0, 0.2, 0)); // Climb up
                                climbsRemaining--;
                            } else if (player.input.shiftKeyDown) {
                                player.setDeltaMovement(player.getDeltaMovement().add(0, -0.2, 0)); // Descend
                            }
                        } else {
                            isClimbing = false;
                        }
                    }
                }))
                .build();
    }

    private static Origin createChasmHopperOrigin() {
        JsonObject data = loadOriginData(ORIGIN_CHASM_HOPPER);
        if (data == null) return null;

        return Origin.builder(ORIGIN_CHASM_HOPPER)
                .addPower(new PowerFactory<>(ORIGIN_CHASM_HOPPER, data -> (type, player) -> new Power(type, player) {
                    private boolean hasDoubleJumped = false;
                    private boolean isHoldingWall = false;
                    private int wallJumpsRemaining;

                    @Override
                    public void onAdded() {
                        // Apply Slow Falling effect
                        player.addEffect(new MobEffectInstance(
                                MobEffects.SLOW_FALLING, // Slow Falling effect
                                Integer.MAX_VALUE,       // Infinite duration
                                0,                      // Amplifier (level 1)
                                false,                  // No particles
                                false                   // No icon
                        ));

                        // Adjust size
                        float size = data.get("size").getAsFloat();
                        player.setScale(size);

                        // Adjust max health
                        int maxHealth = data.get("max_health").getAsInt();
                        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealth);

                        // Apply perpetual effects
                        data.getAsJsonArray("perpetual_effects").forEach(effect -> {
                            String effectName = effect.getAsString();
                            MobEffects effectInstance = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectName));
                            if (effectInstance != null) {
                                player.addEffect(new MobEffectInstance(effectInstance, Integer.MAX_VALUE, 0, false, false));
                            } else {
                                AscensionMod.LOGGER.warn("Unknown effect: " + effectName);
                            }
                        });

                        // Initialize wall jumping
                        wallJumpsRemaining = data.getAsJsonObject("wall_jump").get("limit").getAsInt();
                    }

                    @Override
                    public void onRemoved() {
                        // Remove Slow Falling effect
                        player.removeEffect(MobEffects.SLOW_FALLING);

                        // Reset size
                        player.setScale(1.0f);

                        // Reset max health
                        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0);

                        // Remove perpetual effects
                        data.getAsJsonArray("perpetual_effects").forEach(effect -> {
                            String effectName = effect.getAsString();
                            MobEffects effectInstance = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectName));
                            if (effectInstance != null) {
                                player.removeEffect(effectInstance);
                            }
                        });
                    }

                    @Override
                    public void tick() {
                        // Wall jumping logic
                        if (player.horizontalCollision && !player.isOnGround()) {
                            isHoldingWall = true;
                            player.setDeltaMovement(player.getDeltaMovement().multiply(0.5, 1, 0.5)); // Slow down movement

                            if (player.input.jumping && wallJumpsRemaining > 0) {
                                Vec3 jumpDirection = player.getLookAngle().scale(0.5).add(0, 0.5, 0);
                                player.setDeltaMovement(jumpDirection); // Jump off the wall
                                wallJumpsRemaining--;
                                hasDoubleJumped = true;
                            }
                        } else {
                            isHoldingWall = false;
                        }

                        // Double jump logic
                        if (player.isOnGround() || player.isInWater()) {
                            hasDoubleJumped = false; // Reset double jump when on ground or in water
                        }

                        if (player.jumping && !player.isOnGround() && !hasDoubleJumped && !isHoldingWall) {
                            player.setDeltaMovement(player.getDeltaMovement().add(0, 0.5, 0)); // Apply double jump
                            hasDoubleJumped = true;
                        }
                    }
                }))
                .build();
    }

    private static Origin createScaledOneOrigin() {
        JsonObject data = loadOriginData(ORIGIN_SCALED_ONE);
        if (data == null) return null;

        return Origin.builder(ORIGIN_SCALED_ONE)
                .addPower(new PowerFactory<>(ORIGIN_SCALED_ONE, data -> (type, player) -> new Power(type, player) {
                    private static final UUID COMBAT_BOOST_ID = UUID.randomUUID();

                    @Override
                    public void onAdded() {
                        // Grant Combat Boost (increased attack damage)
                        player.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(
                                new AttributeModifier(
                                        COMBAT_BOOST_ID,
                                        "Scaled One Combat Boost",
                                        2.0, // Increase attack damage by 2
                                        AttributeModifier.Operation.ADDITION
                                )
                        );

                        // Adjust size
                        float size = data.get("size").getAsFloat();
                        player.setScale(size);

                        // Adjust max health
                        int maxHealth = data.get("max_health").getAsInt();
                        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealth);

                        // Apply perpetual effects
                        data.getAsJsonArray("perpetual_effects").forEach(effect -> {
                            String effectName = effect.getAsString();
                            MobEffects effectInstance = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectName));
                            if (effectInstance != null) {
                                player.addEffect(new MobEffectInstance(effectInstance, Integer.MAX_VALUE, 0, false, false));
                            } else {
                                AscensionMod.LOGGER.warn("Unknown effect: " + effectName);
                            }
                        });
                    }

                    @Override
                    public void onRemoved() {
                        // Remove Combat Boost
                        player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(COMBAT_BOOST_ID);

                        // Reset size
                        player.setScale(1.0f);

                        // Reset max health
                        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0);

                        // Remove perpetual effects
                        data.getAsJsonArray("perpetual_effects").forEach(effect -> {
                            String effectName = effect.getAsString();
                            MobEffects effectInstance = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectName));
                            if (effectInstance != null) {
                                player.removeEffect(effectInstance);
                            }
                        });
                    }
                }))
                .build();
    }
}