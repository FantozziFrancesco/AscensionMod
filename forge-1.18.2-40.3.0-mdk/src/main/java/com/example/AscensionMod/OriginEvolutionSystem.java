package com.example.ascensionmod.origins;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AscensionMod.MODID)
public class OriginEvolutionSystem {

    // Force the starter origin on first join
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            OriginComponent component = OriginComponent.get(player);
            OriginLayer layer = OriginLayers.getLayer(new Identifier("ascensionmod", "origins"));

            if (component.getOrigins(layer).isEmpty()) {
                component.setOrigin(layer, ModOrigins.ORIGIN_STARTER);
            }
        }
    }

    // Handle evolution based on player actions
    public static void evolveOrigin(ServerPlayer player, Identifier newOrigin) {
        OriginComponent component = OriginComponent.get(player);
        OriginLayer layer = OriginLayers.getLayer(new Identifier("ascensionmod", "origins"));

        if (component.getOrigin(layer) != newOrigin) {
            component.setOrigin(layer, newOrigin);
            // Trigger any additional logic (e.g., grant items, effects, etc.)
        }
    }
}