package com.suppergerrie2.panorama;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.renderer.RenderSkybox;
import net.minecraft.client.renderer.RenderSkyboxCube;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.resources.SimpleResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ScreenShotHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static com.suppergerrie2.panorama.Config.panoramaSaveFolder;

@Mod(PanoramaMod.MOD_ID)
public class PanoramaMod {
    public static final String MOD_ID = "spanorama";
    public static final KeyBinding createPanoramaKey = new KeyBinding(MOD_ID + ".key.createPanorama", GLFW.GLFW_KEY_H,
                                                                      "key.categories." + MOD_ID);
    private static final Logger LOGGER = LogManager.getLogger();

    static {
        ClientRegistry.registerKeyBinding(createPanoramaKey);
    }

    boolean makePanorama = false;

    long startTime = System.currentTimeMillis();
    Vector3f[] stages = new Vector3f[]{
            new Vector3f(0, 0, 0),
            new Vector3f(90, 0, 0),
            new Vector3f(180, 0, 0),
            new Vector3f(-90, 0, 0),
            new Vector3f(0, -90, 0),
            new Vector3f(0, 90, 0)
    };
    int stage = 0;

    public PanoramaMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
        Config.loadConfig(Config.CLIENT_SPEC,
                          FMLPaths.CONFIGDIR.get().resolve(String.format("%s-client.toml", MOD_ID)));

        MinecraftForge.EVENT_BUS.addListener(this::renderEvent);
        MinecraftForge.EVENT_BUS.addListener(this::cameraSetupEvent);
        MinecraftForge.EVENT_BUS.addListener(this::fovModifier);
        MinecraftForge.EVENT_BUS.addListener(this::inputEvent);
        MinecraftForge.EVENT_BUS.addListener(this::openMainMenu);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Config::onModConfigEvent);

        panoramaSaveFolder = Minecraft.getInstance().gameDir.toPath().resolve("panoramas");
    }

    private static void takeScreenshot(final int stage, final long time) {
        MainWindow window = Minecraft.getInstance().mainWindow;
        final NativeImage screenshot = ScreenShotHelper
                .createScreenshot(window.getFramebufferWidth(), window.getFramebufferHeight(),
                                  Minecraft.getInstance().getFramebuffer());

        SimpleResource.RESOURCE_IO_EXECUTOR.execute(() -> {
            NativeImage squareScreenshot = null;
            try {
                Path panoramaFolder = panoramaSaveFolder.resolve(
                        String.format("%s", time));

                if (!panoramaFolder.toFile().exists() || !panoramaFolder.toFile().isDirectory()) {
                    if (!panoramaFolder.toFile().mkdirs()) {
                        throw new IOException(
                                String.format("Failed to create folder %s", panoramaFolder.toAbsolutePath()));
                    }
                }

                int width = screenshot.getWidth();
                int height = screenshot.getHeight();
                int x = 0;
                int y = 0;

                //Make it square!
                int size = Math.min(width, height);

                if (width > height) {
                    x = (width - height) / 2;
                } else {
                    y = (height - width) / 2;
                }

                squareScreenshot = new NativeImage(size, size, false);
                screenshot.resizeSubRectTo(x, y, size, size, squareScreenshot);

                Path path = panoramaFolder.resolve(String.format("panorama_%d.png", stage));

                LOGGER.info("Writing to {}", path.toAbsolutePath());
                squareScreenshot.write(path);

            } catch (Exception e) {
                LOGGER.error("Failed to save screenshot!");
                e.printStackTrace();
            } finally {
                screenshot.close();
                if (squareScreenshot != null) squareScreenshot.close();
            }
        });
    }

    static DynamicTexture[] getRandomPanorama() {
        Random random = new Random();
        try {
            if (!panoramaSaveFolder.toFile().exists()) {
                if (!panoramaSaveFolder.toFile().mkdirs()) {
                    LOGGER.error("Failed to create panorama save folder: {}", panoramaSaveFolder.toAbsolutePath());
                    return null;
                }
            }

            Path[] paths = Files.list(panoramaSaveFolder).filter(path -> {
                for (int i = 0; i < 6; i++) {
                    if (!path.resolve(String.format("panorama_%d.png", i)).toFile().exists()) {
                        return false;
                    }
                }
                return true;
            }).toArray(Path[]::new);

            if (paths.length == 0) {
                return null;
            } else {
                Path path = paths[random.nextInt(paths.length)];

                DynamicTexture[] textures = new DynamicTexture[6];

                for (int i = 0; i < textures.length; i++) {
                    InputStream stream = Files.newInputStream(path.resolve(String.format("panorama_%d.png", i)));
                    NativeImage image = NativeImage.read(stream);
                    textures[i] = new DynamicTexture(image);
                    image.close();
                    stream.close();
                }

                return textures;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void setRandomPanorama(MainMenuScreen screen) {
        if (!Config.useCustomPanorama) {
            MainMenuScreen.PANORAMA_RESOURCES = new RenderSkyboxCube(
                    new ResourceLocation("textures/gui/title/background/panorama"));
            return;
        }

        DynamicTexture[] textures = getRandomPanorama();
        if (textures != null) {
            MainMenuScreen.PANORAMA_RESOURCES = new RenderDynamicSkyboxCube(getRandomPanorama());
            if (screen != null) screen.panorama = new RenderSkybox(MainMenuScreen.PANORAMA_RESOURCES);
        }
    }

    public void openMainMenu(GuiOpenEvent event) {
        if (event.getGui() instanceof MainMenuScreen) {
            setRandomPanorama((MainMenuScreen) event.getGui());
        }
    }

    @SubscribeEvent
    void renderEvent(RenderWorldLastEvent event) {
        if (Minecraft.getInstance().world != null && makePanorama) {
            takeScreenshot(stage, startTime);

            stage++;

            makePanorama = stage < stages.length;
            Minecraft.getInstance().gameSettings.hideGUI = makePanorama;
        }
    }

    @SubscribeEvent
    void cameraSetupEvent(EntityViewRenderEvent.CameraSetup cameraSetup) {
        if (makePanorama) {
            Vector3f rotation = stages[stage];
            cameraSetup.setYaw(rotation.getX());
            cameraSetup.setPitch(rotation.getY());
            cameraSetup.setRoll(rotation.getZ());
        }
    }

    @SubscribeEvent
    void fovModifier(EntityViewRenderEvent.FOVModifier fovModifier) {
        if (makePanorama) {
            fovModifier.setFOV(90);
        }
    }

    @SubscribeEvent
    void inputEvent(InputEvent.KeyInputEvent event) {
        if (createPanoramaKey.isPressed() && !makePanorama) {
            Minecraft.getInstance().gameSettings.hideGUI = true;

            makePanorama = true;
            stage = 0;
            startTime = System.currentTimeMillis();
            LOGGER.info("Pressed create panorama key");
        }
    }

}
