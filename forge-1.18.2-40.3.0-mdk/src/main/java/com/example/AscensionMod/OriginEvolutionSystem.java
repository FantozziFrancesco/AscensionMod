package com.example.ascensionmod;

import io.github.edwinmindcraft.origins.api.capabilities.IOriginContainer;
import io.github.edwinmindcraft.origins.api.origin.Origin;
import io.github.edwinmindcraft.origins.api.origin.OriginLayer;
import io.github.edwinmindcraft.origins.api.origin.OriginLayers;
import io.github.edwinmindcraft.origins.api.origin.OriginRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class OriginEvolutionSystem {
    public static void evolveOrigin(ServerPlayer player, ResourceLocation newOrigin) {
        IOriginContainer component = player.getCapability(OriginsAPI.ORIGIN_CONTAINER).orElse(null);
        OriginLayer layer = OriginLayers.getLayer(new ResourceLocation("ascensionmod", "origins"));

        if (component != null && layer != null) {
            Origin origin = OriginRegistry.get(newOrigin);
            if (origin != null) {
                component.setOrigin(layer, origin);
            }
        }
    }
}