package com.example.AscensionMod;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class ModOrigins {

    public static final Identifier ORIGIN_CRAWLER = new Identifier("ascensionmod", "crawler");
    public static final Identifier ORIGIN_CHASM_HOPPER = new Identifier("ascensionmod", "chasm_hopper");
    public static final Identifier ORIGIN_SCALED_ONE = new Identifier("ascensionmod", "scaled_one");
    public static final Identifier COMBAT_BOOST_KEY = new Identifier("ascensionmod", "combat_boost");

    public static void registerOrigins() {
        OriginRegistry.register(ORIGIN_CRAWLER, createCrawlerOrigin());
        OriginRegistry.register(ORIGIN_CHASM_HOPPER, createChasmHopperOrigin());
        OriginRegistry.register(ORIGIN_SCALED_ONE, createScaledOneOrigin());
    }

    private static Origin createCrawlerOrigin() {
        return new Origin(
                ORIGIN_CRAWLER,
                "Crawler",
                "You are a crawler.",
                () -> {
                    // Add effects or powers here
                    return new PowerFactory<>(
                            new Identifier("ascensionmod", "crawler_power"),
                            new SerializableData(),
                            data -> (type, player) -> new Power(type, player) {
                                @Override
                                public void onGained() {
                                    // Add power effects here
                                }
                            };
                }
        );
    }

    private static Origin createChasmHopperOrigin() {
        return new Origin(
                ORIGIN_CHASM_HOPPER,
                "Chasm Hopper",
                "You are a chasm hopper.",
                () -> {
                    // Add effects or powers here
                    return new PowerFactory<>(
                            new Identifier("ascensionmod", "chasm_hopper_power"),
                            new SerializableData(),
                            data -> (type, player) -> new Power(type, player) {
                                @Override
                                public void onGained() {
                                    // Add power effects here
                                }
                            };
                }
        );
    }

    private static Origin createScaledOneOrigin() {
        return new Origin(
                ORIGIN_SCALED_ONE,
                "Scaled One",
                "You are a scaled one.",
                () -> {
                    // Add effects or powers here
                    return new PowerFactory<>(
                            new Identifier("ascensionmod", "scaled_one_power"),
                            new SerializableData(),
                            data -> (type, player) -> new Power(type, player) {
                                @Override
                                public void onGained() {
                                    // Add power effects here
                                }
                            };
                }
        );
    }
}