package com.example.ascensionmod.origins;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;

public class ModOrigins {

    // Origin IDs
    public static final Identifier ORIGIN_CRAWLER = new Identifier("ascensionmod", "crawler");
    public static final Identifier ORIGIN_CHASM_HOPPER = new Identifier("ascensionmod", "chasm_hopper");
    public static final Identifier ORIGIN_SCALED_ONE = new Identifier("ascensionmod", "scaled_one");

    // Keybind for Scaled One's combat boost
    public static final Identifier COMBAT_BOOST_KEY = new Identifier("ascensionmod", "combat_boost");

    // Register all origins
    public static void register() {
        // Register origins using Forge Origins' system
        OriginRegistry.register(ORIGIN_CRAWLER, createCrawlerOrigin());
        OriginRegistry.register(ORIGIN_CHASM_HOPPER, createChasmHopperOrigin());
        OriginRegistry.register(ORIGIN_SCALED_ONE, createScaledOneOrigin());
    }

    // Define the Crawler origin
    private static Origin createCrawlerOrigin() {
        return new Origin(
                ORIGIN_CRAWLER,
                "Crawler",
                "You are a small, nimble creature, adapted to tight spaces and vertical movement. Your size makes you fragile, but your climbing abilities give you an edge in navigating the world.",
                "ascensionmod:textures/gui/background/crawler.png",
                List.of(
                        createUnifiedHealthPower(),
                        createUnifiedSizePower(),
                        createUnifiedClimbingPower(),
                        createUnifiedPerpetualEffectsPower()
                )
        );
    }

    // Define the Chasm Hopper origin
    private static Origin createChasmHopperOrigin() {
        return new Origin(
                ORIGIN_CHASM_HOPPER,
                "Chasm Hopper",
                "You are an agile explorer, capable of leaping across chasms and scaling great heights. Your enhanced mobility makes you a master of vertical terrain.",
                "ascensionmod:textures/gui/background/chasm_hopper.png",
                List.of(
                        createUnifiedHealthPower(),
                        createUnifiedSizePower(),
                        createUnifiedClimbingPower(),
                        createDoubleJumpPower(),
                        createUnifiedPerpetualEffectsPower()
                )
        );
    }

    // Define the Scaled One origin
    private static Origin createScaledOneOrigin() {
        return new Origin(
                ORIGIN_SCALED_ONE,
                "Scaled One",
                "You are a resilient warrior, protected by thick scales. Your durability and strength make you a formidable opponent in battle.",
                "ascensionmod:textures/gui/background/scaled_one.png",
                List.of(
                        createUnifiedHealthPower(),
                        createUnifiedSizePower(),
                        createUnifiedClimbingPower(),
                        createUnifiedPerpetualEffectsPower(),
                        createCombatBoostPower()
                )
        );
    }

    // Power: Unified Health
    private static Power createUnifiedHealthPower() {
        return new Power(
                new Identifier("ascensionmod", "unified_health"),
                player -> {
                    // Health is handled by the JSON file
                }
        );
    }

    // Power: Unified Size
    private static Power createUnifiedSizePower() {
        return new Power(
                new Identifier("ascensionmod", "unified_size"),
                player -> {
                    // Size is handled by the JSON file
                }
        );
    }

    // Power: Unified Climbing
    private static Power createUnifiedClimbingPower() {
        return new Power(
                new Identifier("ascensionmod", "unified_climbing"),
                player -> {
                    // Climbing is handled by the JSON file
                }
        );
    }

    // Power: Unified Perpetual Effects
    private static Power createUnifiedPerpetualEffectsPower() {
        return new Power(
                new Identifier("ascensionmod", "unified_perpetual_effects"),
                player -> {
                    // Perpetual effects are handled by the JSON file
                }
        );
    }

    // Power: Double Jump
    private static Power createDoubleJumpPower() {
        return new Power(
                new Identifier("ascensionmod", "double_jump"),
                player -> {
                    // Double jump logic here
                }
        );
    }

    // Power: Combat Boost
    private static Power createCombatBoostPower() {
        return new Power(
                new Identifier("ascensionmod", "combat_boost"),
                player -> {
                    // Combat boost logic here
                }
        );
    }
}