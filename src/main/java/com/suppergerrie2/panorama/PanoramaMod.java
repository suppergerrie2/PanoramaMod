package com.suppergerrie2.panorama;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(PanoramaMod.MOD_ID)
public class PanoramaMod {

    public static final String MOD_ID = "spanorama";

    public PanoramaMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
        Config.loadConfig(Config.CLIENT_SPEC,
                          FMLPaths.CONFIGDIR.get().resolve(String.format("%s-client.toml", MOD_ID)));

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> PanoramaClientEvents::new);

    }

}
