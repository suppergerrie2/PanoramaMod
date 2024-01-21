package com.suppergerrie2.panorama;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;

public class Config {

    public static final ClientConfig CLIENT;
    public static final ModConfigSpec CLIENT_SPEC;
    private static final Logger LOGGER = LogManager
            .getLogger(PanoramaMod.MOD_ID + " Mod Event Subscriber");

    public static Path panoramaSaveFolder = new File("./panoramas/").toPath();
    public static boolean useCustomPanorama = true;
    public static int renderResolution = 1920;

    static {
        final Pair<ClientConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder()
                .configure(ClientConfig::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfigEvent event) {
        final ModConfig config = event.getConfig();
        // Rebake the configs when they change
        if (config.getSpec() == CLIENT_SPEC) {
            bakeClient();
            LOGGER.debug("Baked client config");
        }
    }

    private static void bakeClient() {
        panoramaSaveFolder = new File(CLIENT.savePath.get()).toPath();
        useCustomPanorama = CLIENT.useCustomPanoramas.get();
        renderResolution = CLIENT.renderResolution.get();
    }

    public static void loadConfig(ModConfigSpec spec, Path path) {
        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        configData.load();
        spec.setConfig(configData);
    }

    public static class ClientConfig {

        public final ModConfigSpec.ConfigValue<String> savePath;
        public final ModConfigSpec.BooleanValue useCustomPanoramas;
        public final ModConfigSpec.IntValue     renderResolution;

        ClientConfig(ModConfigSpec.Builder builder) {
            builder.push("Save Settings");
            savePath = builder
                    .comment("Where to save the panoramas")
                    .define("panoramaSaveFolder", panoramaSaveFolder.toString());
            renderResolution = builder
                    .comment("The resolution of one image in the panorama. Increasing the resolution will lead to higher quality images but at the cost of performance and storage space")
                    .defineInRange("renderResolution", 1920, 240, 8192);
            builder.pop();
            builder.push("Main menu");

            useCustomPanoramas = builder
                    .comment("Whether to use custom panoramas on the main menu")
                    .define("useCustomPanoramas", true);

            builder.pop();
        }

    }

}
