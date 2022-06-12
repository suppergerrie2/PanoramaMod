package com.suppergerrie2.panorama;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.IBidiRenderer;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class ScreenFlashWarningScreen extends Screen {

    private final Screen nextScreen;
    private static final ITextComponent MESSAGE_HEADER = new TranslationTextComponent(
            PanoramaMod.MOD_ID + ".screenflash.header").mergeStyle(TextFormatting.BOLD);
    private static final ITextComponent MESSAGE = new TranslationTextComponent(
            PanoramaMod.MOD_ID + ".screenflash.message");
    private static final ITextComponent CHECK = new TranslationTextComponent(
            PanoramaMod.MOD_ID + ".screenflash.check");
    private CheckboxButton showAgainCheckbox;

    private IBidiRenderer bidiRenderer = IBidiRenderer.field_243257_a;

    protected ScreenFlashWarningScreen(Screen nextScreen) {
        super(NarratorChatListener.EMPTY);
        this.nextScreen = nextScreen;
    }

    @Override
    protected void init() {
        super.init();

        this.bidiRenderer = IBidiRenderer
                .func_243258_a(this.font, MESSAGE, this.width - 50);

        int i = (this.bidiRenderer.func_241862_a() + 1) * 9;

        this.addButton(
                new Button(this.width / 2 - 155, 100 + i, 150, 20, DialogTexts.GUI_PROCEED,
                        (p_230165_1_) -> {
                            if (this.showAgainCheckbox.isChecked()) {
                                Config.CLIENT.disableFlashWarning.set(true);
                                Config.CLIENT_SPEC.save();
                            }

                            if (this.minecraft != null) {
                                this.minecraft.displayGuiScreen(this.nextScreen);
                            }
                        }));

        this.addButton(new Button(this.width / 2 - 155 + 160, 100 + i, 150, 20,
                new TranslationTextComponent("menu.quit"), (p_230164_1_) -> {
            if (this.minecraft != null) {
                this.minecraft.shutdown();
            }
        }));

        this.showAgainCheckbox = new CheckboxButton(this.width / 2 - 155 + 80, 76 + i, 150, 20,
                CHECK, false);
        this.addButton(this.showAgainCheckbox);
    }

    @Override
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY,
            float partialTicks) {
        this.renderDirtBackground(0);

        drawCenteredString(matrixStack, this.font, MESSAGE_HEADER, this.width / 2, 30, 0xffffff);
        this.bidiRenderer.func_241863_a(matrixStack, this.width / 2, 70);

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}
