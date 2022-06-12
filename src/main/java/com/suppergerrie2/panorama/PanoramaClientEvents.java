package com.suppergerrie2.panorama;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.math.Vector3f;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Random;

import static com.suppergerrie2.panorama.Config.panoramaSaveFolder;

public class PanoramaClientEvents {

    public static final KeyMapping createPanoramaKey = new KeyMapping(
            PanoramaMod.MOD_ID + ".key.createPanorama",
            GLFW.GLFW_KEY_H,
            "key.categories." + PanoramaMod.MOD_ID);
    private static final Logger     LOGGER            = LogManager.getLogger();
    static HashMap<Path, DynamicTexture[]> skyboxTextureCache = new HashMap<>();

    boolean showedWarningMessage = false;

    static {
        ClientRegistry.registerKeyBinding(createPanoramaKey);
    }

    public PanoramaClientEvents() {
        MinecraftForge.EVENT_BUS.addListener(this::renderEvent);
        MinecraftForge.EVENT_BUS.addListener(this::cameraSetupEvent);
        MinecraftForge.EVENT_BUS.addListener(this::fovModifier);
        MinecraftForge.EVENT_BUS.addListener(this::inputEvent);
        MinecraftForge.EVENT_BUS.addListener(this::openMainMenu);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Config::onModConfigEvent);

        panoramaSaveFolder = Minecraft.getInstance().gameDirectory.toPath().resolve("panoramas");
    }

    boolean makePanorama     = false;
    Vec3    panoramaPosition = Vec3.ZERO;
    long    startTime        = System.currentTimeMillis();
    Vector3f[] stages = new Vector3f[]{
            new Vector3f(0, 0, 0),
            new Vector3f(90, 0, 0),
            new Vector3f(180, 0, 0),
            new Vector3f(-90, 0, 0),
            new Vector3f(0, -90, 0),
            new Vector3f(0, 90, 0)
    };
    int stage = 0;


    private static void takeScreenshot(final int stage, final long time, boolean sendMessage) {
        Window window = Minecraft.getInstance().getWindow();
        final NativeImage screenshot = Screenshot
                .takeScreenshot(/*window.getWidth(), window.getHeight(),*/
                        Minecraft.getInstance().getMainRenderTarget());

        Util.ioPool().execute(() -> {
            NativeImage squareScreenshot = null;
            try {
                Path panoramaFolder = panoramaSaveFolder.resolve(
                        String.format("%s", time));

                if (!panoramaFolder.toFile().exists() || !panoramaFolder.toFile().isDirectory()) {
                    if (!panoramaFolder.toFile().mkdirs()) {
                        throw new IOException(
                                String.format("Failed to create folder %s",
                                        panoramaFolder.toAbsolutePath()));
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
                squareScreenshot.writeToFile(path);

                if (sendMessage) {
                    File target = path.getParent().toFile();

                    Component textComponent = (Component.literal(
                            target.getName()))
                            .withStyle(ChatFormatting.UNDERLINE)
                            .withStyle((p_238335_1_) ->
                                    p_238335_1_.withClickEvent(
                                            new ClickEvent(ClickEvent.Action.OPEN_FILE,
                                                           target.getAbsolutePath())));

                    Minecraft.getInstance().execute(() ->
                            Minecraft.getInstance()
                                    .gui
                                    .getChat()
                                    .addMessage(
                                            Component.translatable(
                                                    "spanorama.panorama.success",
                                                    textComponent
                                            )));
                }

            } catch (Exception e) {
                LOGGER.error("Failed to save screenshot!");
                e.printStackTrace();

                Minecraft.getInstance().execute(() ->
                        Minecraft.getInstance()
                                .gui
                                .getChat()
                                .addMessage(
                                        Component.translatable(
                                                "spanorama.panorama.failed"
                                        )));
            } finally {
                screenshot.close();
                if (squareScreenshot != null) {
                    squareScreenshot.close();
                }
            }
        });
    }

    /**
     * Get a random panorama from the {@link Config#panoramaSaveFolder}. Panoramas are saved in the
     * following format: {@link Config#panoramaSaveFolder}/{unix timestamp}/panorama_%d.png Where %d
     * is a number between 0 and 5 (inclusive)
     * <p>
     * If no panorama is found null is returned
     *
     * @return A {@link DynamicTexture} array with size 6, or null if no panorama is found
     */
    @Nullable
    static DynamicTexture[] getRandomPanorama() {
        Random random = new Random();

        try {
            //Make sure the panorama save folder exists and create it if it doesnt
            if (!panoramaSaveFolder.toFile().exists()) {
                if (!panoramaSaveFolder.toFile().mkdirs()) {
                    LOGGER.error("Failed to create panorama save folder: {}",
                            panoramaSaveFolder.toAbsolutePath());
                    return null;
                }
            }

            //Filter out any folders that dont have the needed images
            Path[] paths = Files.list(panoramaSaveFolder).filter(path -> {
                for (int i = 0; i < 6; i++) {
                    if (!path.resolve(String.format("panorama_%d.png", i)).toFile().exists()) {
                        return false;
                    }
                }
                return true;
            }).toArray(Path[]::new);

            //If no paths are remaining return null
            if (paths.length == 0) {
                return null;
            } else {
                //If there are paths choose a random one
                Path theChosenOne = paths[random.nextInt(paths.length)];

                //Check if the images are loaded already, and if not load them
                return skyboxTextureCache.computeIfAbsent(theChosenOne, (path) -> {

                    try {
                        DynamicTexture[] textures = new DynamicTexture[6];

                        for (int i = 0; i < textures.length; i++) {
                            InputStream stream = Files
                                    .newInputStream(
                                            path.resolve(String.format("panorama_%d.png", i)));
                            NativeImage image = NativeImage.read(stream);
                            textures[i] = new DynamicTexture(image);
                            image.close();
                            stream.close();
                        }

                        return textures;
                    } catch (Exception e) {
                        return null;
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Set a random panorama on the given {@link TitleScreen}.
     *
     * @param screen The screen to set the random panorama to, if null only the resources will be
     *               set and not the renderer itself
     */
    private void setRandomPanorama(@Nullable TitleScreen screen) {

        //If custom panoramas are disabled make sure the vanilla resources are set
        DynamicTexture[] textures = Config.useCustomPanorama ? getRandomPanorama() : null;
        TitleScreen.CUBE_MAP = textures != null ? new RenderDynamicSkyboxCube(
                textures) : new CubeMap(
                new ResourceLocation("textures/gui/title/background/panorama"));
        if (screen != null) {
            screen.panorama = new PanoramaRenderer(TitleScreen.CUBE_MAP);
        }
    }

    public void openMainMenu(ScreenOpenEvent event) {
        if (event.getScreen() instanceof TitleScreen titleScreen) {
            if (!showedWarningMessage && !Config.disableFlashWarning) {
                event.setScreen(new ScreenFlashWarningScreen(event.getScreen()));
                showedWarningMessage = true;
            } else {
                setRandomPanorama(titleScreen);
            }
        }
    }

    @SubscribeEvent
    void renderEvent(RenderLevelLastEvent event) {
        if (Minecraft.getInstance().level != null && makePanorama) {
            takeScreenshot(stage, startTime, stage == (stages.length - 2));

            stage++;

            makePanorama = stage < stages.length;

            Minecraft.getInstance().options.hideGui = makePanorama;
        }
    }

    @SubscribeEvent
    void cameraSetupEvent(EntityViewRenderEvent.CameraSetup cameraSetup) {
        if (makePanorama) {
            Vector3f rotation = stages[stage];
            cameraSetup.setYaw(rotation.x());
            cameraSetup.setPitch(rotation.y());
            cameraSetup.setRoll(rotation.z());

            cameraSetup.getCamera().setPosition(panoramaPosition);
        }
    }

    @SubscribeEvent
    void fovModifier(EntityViewRenderEvent.FieldOfView fovEvent) {
        if (makePanorama) {
            fovEvent.setFOV(90);
        }
    }

    @SubscribeEvent
    void inputEvent(InputEvent.KeyInputEvent event) {
        if (createPanoramaKey.consumeClick() && !makePanorama) {
            Minecraft.getInstance().options.hideGui = true;

            makePanorama = true;
            stage = 0;
            startTime = System.currentTimeMillis();

            if (Minecraft.getInstance().getCameraEntity() != null) {
                panoramaPosition = Minecraft.getInstance().getCameraEntity().getEyePosition(0);
            } else {
                panoramaPosition =
                        Minecraft.getInstance().player != null ?
                                Minecraft.getInstance().player.getEyePosition(0) :
                                Vec3.ZERO;
            }
            LOGGER.info("Pressed create panorama key");
        }
    }
}
