package com.suppergerrie2.panorama;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;

@Mod(PanoramaMod.MOD_ID)
public class PanoramaMod {

    public static final String MOD_ID = "spanorama";

    public PanoramaMod(IEventBus eventBus, Dist dist) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
        Config.loadConfig(Config.CLIENT_SPEC,
                          FMLPaths.CONFIGDIR.get().resolve(String.format("%s-client.toml", MOD_ID)));

        if(dist == Dist.CLIENT) {
            new PanoramaClientEvents(eventBus);
        }
    }

}
