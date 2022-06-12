package com.suppergerrie2.panorama;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class ScreenFlashWarningScreen extends Screen {

    private final        Screen         nextScreen;
    private static final Component      MESSAGE_HEADER = Component.translatable(
            PanoramaMod.MOD_ID + ".screenflash.header").withStyle(ChatFormatting.BOLD);
    private static final Component MESSAGE        = Component.translatable(
            PanoramaMod.MOD_ID + ".screenflash.message");
    private static final Component CHECK = Component.translatable(
            PanoramaMod.MOD_ID + ".screenflash.check");
    private              Checkbox  showAgainCheckbox;

    private MultiLineLabel bidiRenderer = MultiLineLabel.EMPTY;

    protected ScreenFlashWarningScreen(Screen nextScreen) {
        super(NarratorChatListener.NO_TITLE);
        this.nextScreen = nextScreen;
    }

    @Override
    protected void init() {
        super.init();

        this.bidiRenderer = MultiLineLabel
                .create(this.font, MESSAGE, this.width - 50);

        int i = (this.bidiRenderer.getLineCount() + 1) * 9;

        this.addRenderableWidget(
                new Button(this.width / 2 - 155, 100 + i, 150, 20, CommonComponents.GUI_PROCEED,
                           (p_230165_1_) -> {
                            if (this.showAgainCheckbox.selected()) {
                                Config.CLIENT.disableFlashWarning.set(true);
                                Config.CLIENT_SPEC.save();
                            }

                            if (this.minecraft != null) {
                                this.minecraft.setScreen(this.nextScreen);
                            }
                        }));

        this.addRenderableWidget(new Button(this.width / 2 - 155 + 160, 100 + i, 150, 20,
                Component.translatable("menu.quit"), (p_230164_1_) -> {
            if (this.minecraft != null) {
                this.minecraft.stop();
            }
        }));

        this.showAgainCheckbox = new Checkbox(this.width / 2 - 155 + 80, 76 + i, 150, 20,
                CHECK, false);
        this.addRenderableWidget(this.showAgainCheckbox);
    }

    @Override
    public void render(@Nonnull PoseStack matrixStack, int mouseX, int mouseY,
                       float partialTicks) {
        this.renderDirtBackground(0);

        drawCenteredString(matrixStack, this.font, MESSAGE_HEADER, this.width / 2, 30, 0xffffff);
        this.bidiRenderer.renderCentered(matrixStack, this.width / 2, 70);

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}
