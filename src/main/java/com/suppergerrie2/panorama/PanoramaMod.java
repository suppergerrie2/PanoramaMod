package com.suppergerrie2.panorama;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(PanoramaMod.MOD_ID)
public class PanoramaMod {

    public static final String MOD_ID = "spanorama";

    public PanoramaMod(ModContainer container, IEventBus eventBus, Dist dist) {
        container.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        if(dist == Dist.CLIENT) {
            new PanoramaClientEvents(eventBus);
        }
    }

}
