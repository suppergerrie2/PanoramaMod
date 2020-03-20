package com.suppergerrie2.panorama;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import static com.suppergerrie2.panorama.Config.panoramaSaveFolder;

@Mod(PanoramaMod.MOD_ID)
public class PanoramaMod {

    public static final String MOD_ID = "spanorama";

    public PanoramaMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
        Config.loadConfig(Config.CLIENT_SPEC,
                          FMLPaths.CONFIGDIR.get().resolve(String.format("%s-client.toml", MOD_ID)));

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            PanoramaClientEvents events = new PanoramaClientEvents();

            MinecraftForge.EVENT_BUS.addListener(events::renderEvent);
            MinecraftForge.EVENT_BUS.addListener(events::cameraSetupEvent);
            MinecraftForge.EVENT_BUS.addListener(events::fovModifier);
            MinecraftForge.EVENT_BUS.addListener(events::inputEvent);
            MinecraftForge.EVENT_BUS.addListener(events::openMainMenu);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(Config::onModConfigEvent);

            panoramaSaveFolder = Minecraft.getInstance().gameDir.toPath().resolve("panoramas");
        });

    }

}
