package com.suppergerrie2.panorama;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;

public class Config {

    public static final ClientConfig CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;
    private static final Logger LOGGER = LogManager
            .getLogger(PanoramaMod.MOD_ID + " Mod Event Subscriber");

    public static Path panoramaSaveFolder = new File("./panoramas/").toPath();
    public static boolean useCustomPanorama = true;
    public static boolean disableFlashWarning = false;

    static {
        final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder()
                .configure(ClientConfig::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfig.ModConfigEvent event) {
        final ModConfig config = event.getConfig();
        // Rebake the configs when they change
        if (config.getSpec() == CLIENT_SPEC) {
            bakeClient(config);
            LOGGER.debug("Baked client config");
        }
    }

    private static void bakeClient(ModConfig config) {
        panoramaSaveFolder = new File(CLIENT.savePath.get()).toPath();
        useCustomPanorama = CLIENT.useCustomPanoramas.get();
        disableFlashWarning = CLIENT.disableFlashWarning.get();
    }

    public static void loadConfig(ForgeConfigSpec spec, Path path) {

        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        configData.load();
        spec.setConfig(configData);
    }

    public static class ClientConfig {

        public final ForgeConfigSpec.ConfigValue<String> savePath;
        public final ForgeConfigSpec.BooleanValue useCustomPanoramas;
        public final ForgeConfigSpec.BooleanValue disableFlashWarning;

        ClientConfig(ForgeConfigSpec.Builder builder) {
            builder.push("Save locations");
            savePath = builder
                    .comment("Where to save the panoramas")
                    .define("panoramaSaveFolder", panoramaSaveFolder.toString());
            builder.pop();
            builder.push("Main menu");

            useCustomPanoramas = builder
                    .comment("Whether to use custom panoramas on the main menu")
                    .define("useCustomPanoramas", true);

            disableFlashWarning = builder
                    .comment("Whether to show the flash warning screen on launch. ")
                    .define("disableFlashWarning", false);

            builder.pop();
        }

    }

}
