package com.suppergerrie2.panorama;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import oshi.util.tuples.Pair;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Random;

import static com.suppergerrie2.panorama.Config.panoramaSaveFolder;

public class PanoramaClientEvents {

    public static final  KeyMapping                      createPanoramaKey  = new KeyMapping(
            PanoramaMod.MOD_ID + ".key.createPanorama", GLFW.GLFW_KEY_H, "key.categories." + PanoramaMod.MOD_ID);
    private static final Logger                          LOGGER             = LogManager.getLogger();
    static final         HashMap<Path, DynamicTexture[]> skyboxTextureCache = new HashMap<>();

    public PanoramaClientEvents() {
        MinecraftForge.EVENT_BUS.addListener(this::inputEvent);
        MinecraftForge.EVENT_BUS.addListener(this::openMainMenu);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Config::onModConfigEvent);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerKeyMappingsEvent);

        panoramaSaveFolder = Minecraft.getInstance().gameDirectory.toPath()
                                                                  .resolve("panoramas");
    }

    public void registerKeyMappingsEvent(RegisterKeyMappingsEvent event) {
        event.register(createPanoramaKey);
    }

    private static void takeScreenshot(RenderTarget rendertarget, final int stage, final long time,
                                       boolean sendMessage) {
        final NativeImage screenshot = Screenshot.takeScreenshot(rendertarget);

        Util.ioPool()
            .execute(() -> {
                NativeImage squareScreenshot = null;
                try {
                    Path panoramaFolder = panoramaSaveFolder.resolve(String.format("%s", time));

                    if (!panoramaFolder.toFile()
                                       .exists() || !panoramaFolder.toFile()
                                                                   .isDirectory()) {
                        if (!panoramaFolder.toFile()
                                           .mkdirs()) {
                            throw new IOException(
                                    String.format("Failed to create folder %s", panoramaFolder.toAbsolutePath()));
                        }
                    }

                    int width  = screenshot.getWidth();
                    int height = screenshot.getHeight();
                    int x      = 0;
                    int y      = 0;

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
                        File target = path.getParent()
                                          .toFile();

                        Component textComponent = (Component.literal(target.getName())).withStyle(
                                                                                               ChatFormatting.UNDERLINE)
                                                                                       .withStyle(
                                                                                               (p_238335_1_) -> p_238335_1_.withClickEvent(
                                                                                                       new ClickEvent(
                                                                                                               ClickEvent.Action.OPEN_FILE,
                                                                                                               target.getAbsolutePath())));

                        Minecraft.getInstance()
                                 .execute(() -> Minecraft.getInstance().gui.getChat()
                                                                           .addMessage(Component.translatable(
                                                                                   "spanorama.panorama.success",
                                                                                   textComponent)));
                    }

                } catch (Exception e) {
                    LOGGER.error("Failed to save screenshot!");
                    e.printStackTrace();

                    Minecraft.getInstance()
                             .execute(() -> Minecraft.getInstance().gui.getChat()
                                                                       .addMessage(Component.translatable(
                                                                               "spanorama.panorama.failed")));
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
     * @return A {@link Pair} with a {@link DynamicTexture[]} of size 6 and the name of the panorama, or null if no panorama is found
     */
    @Nullable
    static Pair<DynamicTexture[], String> getRandomPanorama() {
        Random random = new Random();

        try {
            //Make sure the panorama save folder exists and create it if it doesnt
            if (!panoramaSaveFolder.toFile()
                                   .exists()) {
                if (!panoramaSaveFolder.toFile()
                                       .mkdirs()) {
                    LOGGER.error("Failed to create panorama save folder: {}", panoramaSaveFolder.toAbsolutePath());
                    return null;
                }
            }

            //Filter out any folders that dont have the needed images
            Path[] paths = Files.list(panoramaSaveFolder)
                                .filter(path -> {
                                    for (int i = 0; i < 6; i++) {
                                        if (!path.resolve(String.format("panorama_%d.png", i))
                                                 .toFile()
                                                 .exists()) {
                                            return false;
                                        }
                                    }
                                    return true;
                                })
                                .toArray(Path[]::new);

            //If no paths are remaining return null
            if (paths.length == 0) {
                return null;
            } else {
                //If there are paths choose a random one
                Path theChosenOne = paths[random.nextInt(paths.length)];

                //Check if the images are loaded already, and if not load them
                return new Pair<>(skyboxTextureCache.computeIfAbsent(theChosenOne, (path) -> {

                    try {
                        DynamicTexture[] textures = new DynamicTexture[6];

                        for (int i = 0; i < textures.length; i++) {
                            InputStream stream = Files.newInputStream(
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
                }), theChosenOne.getFileName()
                                .toString());
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
        Pair<DynamicTexture[], String> textures = Config.useCustomPanorama ? getRandomPanorama() : null;

        ResourceLocation base;
        if (textures == null) {
            base = new ResourceLocation("minecraft", "textures/gui/title/background/panorama");
        } else {
            base = new ResourceLocation(PanoramaMod.MOD_ID,
                                        "textures/gui/title/background/panorama/" + textures.getB());

            for (int i = 0; i < 6; i++) {
                Minecraft.getInstance()
                         .getTextureManager()
                         .register(new ResourceLocation(base.getNamespace(), base.getPath() + "_" + i + ".png"),
                                   textures.getA()[i]);
            }
        }

        TitleScreen.CUBE_MAP = new CubeMap(base);
        if (screen != null) {
            screen.panorama = new PanoramaRenderer(TitleScreen.CUBE_MAP);
        }
    }

    public void openMainMenu(ScreenEvent.Opening event) {
        if (event.getScreen() instanceof TitleScreen titleScreen) {
            setRandomPanorama(titleScreen);
        }
    }

    void createPanorama() {
        final long currentTime = System.currentTimeMillis();

        Minecraft     instance      = Minecraft.getInstance();
        Window        window        = instance.getWindow();
        Player        player        = instance.player;
        GameRenderer  gameRenderer  = instance.gameRenderer;
        LevelRenderer levelRenderer = instance.levelRenderer;
        int           oldWidth      = window.getWidth();
        int           oldHeight     = window.getHeight();
        RenderTarget  rendertarget  = new TextureTarget(Config.renderResolution, Config.renderResolution, true, Minecraft.ON_OSX);
        float         oldXRot       = player.getXRot();
        float         oldYRot       = player.getYRot();
        float         oldXRot0      = player.xRotO;
        float         oldYRot0      = player.yRotO;
        gameRenderer.setRenderBlockOutline(false);

        try {
            gameRenderer.setPanoramicMode(true);
            levelRenderer.graphicsChanged();
            window.setWidth(Config.renderResolution);
            window.setHeight(Config.renderResolution);

            for (int stage = 0; stage < 6; ++stage) {
                applyRotationForStage(player, oldYRot, stage);

                player.yRotO = player.getYRot();
                player.xRotO = player.getXRot();
                rendertarget.bindWrite(true);
                gameRenderer.renderLevel(1.0F, 0L, new PoseStack());

                File file = panoramaSaveFolder.toFile();
                takeScreenshot(rendertarget, stage, currentTime, stage == 5);
                Screenshot.grab(file, "panorama_" + stage + ".png", rendertarget, (p_231415_) -> {
                });
            }
        } finally {
            player.setXRot(oldXRot);
            player.setYRot(oldYRot);
            player.xRotO = oldXRot0;
            player.yRotO = oldYRot0;
            gameRenderer.setRenderBlockOutline(true);
            window.setWidth(oldWidth);
            window.setHeight(oldHeight);
            rendertarget.destroyBuffers();
            gameRenderer.setPanoramicMode(false);
            levelRenderer.graphicsChanged();
            instance.getMainRenderTarget()
                    .bindWrite(true);
        }
    }

    /**
     * Apply the correct rotation for the given stage to the player.
     * @param player The player to apply the rotation to
     * @param baseYRot The base y rotation
     * @param stage Stage to apply rotation for
     */
    private void applyRotationForStage(Player player, float baseYRot, int stage) {
        if(stage < 0 || stage > 5) return;

        switch (stage) {
            case 0 -> {
                player.setYRot(baseYRot);
                player.setXRot(0.0F);
            }
            case 1 -> {
                player.setYRot((baseYRot + 90.0F) % 360.0F);
                player.setXRot(0.0F);
            }
            case 2 -> {
                player.setYRot((baseYRot + 180.0F) % 360.0F);
                player.setXRot(0.0F);
            }
            case 3 -> {
                player.setYRot((baseYRot - 90.0F) % 360.0F);
                player.setXRot(0.0F);
            }
            case 4 -> {
                player.setYRot(baseYRot);
                player.setXRot(-90.0F);
            }
            case 5 -> {
                player.setYRot(baseYRot);
                player.setXRot(90.0F);
            }
        }
    }

    @SubscribeEvent
    void inputEvent(InputEvent.Key event) {
        if (createPanoramaKey.consumeClick()) {
            createPanorama();
        }
    }
}
