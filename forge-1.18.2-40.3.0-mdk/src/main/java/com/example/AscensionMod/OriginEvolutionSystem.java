package com.example.AscensionMod;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class OriginEvolutionSystem {

    public static void evolveOrigin(ServerPlayerEntity player, Identifier newOrigin) {
        OriginComponent component = ModComponents.ORIGIN.get(player);
        OriginLayer layer = OriginLayers.getLayer(new Identifier("ascensionmod", "origins"));
        if (layer != null) {
            Origin origin = OriginRegistry.get(newOrigin);
            if (origin != null) {
                component.setOrigin(layer, origin);
            }
        }
    }
}