package com.suppergerrie2.panorama;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderSkyboxCube;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class RenderDynamicSkyboxCube extends RenderSkyboxCube {

    DynamicTexture[] textures;

    public RenderDynamicSkyboxCube(DynamicTexture[] textures) {
        super(new ResourceLocation("dont_load_texture"));

        if (textures.length != 6) throw new IllegalArgumentException("Need 6 textures to render a skybox!");

        this.textures = textures;
    }

    @Override
    public void render(Minecraft mc, float pitch, float yaw, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.matrixMode(5889);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.multMatrix(
                Matrix4f.perspective(85.0D, (float) mc.mainWindow.getFramebufferWidth() / (float) mc.mainWindow
                        .getFramebufferHeight(), 0.05F, 10.0F));
        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.enableBlend();
        GlStateManager.disableAlphaTest();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
        GlStateManager
                .blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                   GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        for (int pass = 0; pass < 4; ++pass) {
            GlStateManager.pushMatrix();

            float f = ((float) (pass % 2) / 2.0F - 0.5F) / 256.0F;
            float f1 = ((float) (pass / 2) / 2.0F - 0.5F) / 256.0F;

            GlStateManager.translatef(f, f1, 0.0F);
            GlStateManager.rotatef(pitch, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotatef(yaw, 0.0F, 1.0F, 0.0F);

            for (int texture = 0; texture < 6; ++texture) {
//                mc.getTextureManager().bindTexture(this.locations[texture]);
                textures[texture].bindTexture();

                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

                int l = Math.round(255.0F * alpha) / (pass + 1);

                if (texture == 0) {
                    bufferbuilder.pos(-1.0D, -1.0D, 1.0D).tex(0.0D, 0.0D).color(255, 255, 255, l).endVertex();
                    bufferbuilder.pos(-1.0D, 1.0D, 1.0D).tex(0.0D, 1.0D).color(255, 255, 255, l).endVertex();
                    bufferbuilder.pos(1.0D, 1.0D, 1.0D).tex(1.0D, 1.0D).color(255, 255, 255, l).endVertex();
                    bufferbuilder.pos(1.0D, -1.0D, 1.0D).tex(1.0D, 0.0D).color(255, 255, 255, l).endVertex();
                }

                if (texture == 1) {
                    bufferbuilder.pos(1.0D, -1.0D, 1.0D).tex(0.0D, 0.0D).color(255, 255, 255, l).endVertex();
                    bufferbuilder.pos(1.0D, 1.0D, 1.0D).tex(0.0D, 1.0D).color(255, 255, 255, l).endVertex();
                    bufferbuilder.pos(1.0D, 1.0D, -1.0D).tex(1.0D, 1.0D).color(255, 255, 255, l).endVertex();
                    bufferbuilder.pos(1.0D, -1.0D, -1.0D).tex(1.0D, 0.0D).color(255, 255, 255, l).endVertex();
                }

                if (texture == 2) {
                    bufferbuilder.pos(1.0D, -1.0D, -1.0D).tex(0.0D, 0.0D).color(255, 255, 255, l).endVertex();
                    bufferbuilder.pos(1.0D, 1.0D, -1.0D).tex(0.0D, 1.0D).color(255, 255, 255, l).endVertex();
                    bufferbuilder.pos(-1.0D, 1.0D, -1.0D).tex(1.0D, 1.0D).color(255, 255, 255, l).endVertex();
                    bufferbuilder.pos(-1.0D, -1.0D, -1.0D).tex(1.0D, 0.0D).color(255, 255, 255, l).endVertex();
                }

                if (texture == 3) {
                    bufferbuilder.pos(-1.0D, -1.0D, -1.0D).tex(0.0D, 0.0D).color(255, 255, 255, l).endVertex();
                    bufferbuilder.pos(-1.0D, 1.0D, -1.0D).tex(0.0D, 1.0D).color(255, 255, 255, l).endVertex();
                    bufferbuilder.pos(-1.0D, 1.0D, 1.0D).tex(1.0D, 1.0D).color(255, 255, 255, l).endVertex();
                    bufferbuilder.pos(-1.0D, -1.0D, 1.0D).tex(1.0D, 0.0D).color(255, 255, 255, l).endVertex();
                }

                if (texture == 4) {
                    bufferbuilder.pos(-1.0D, -1.0D, -1.0D).tex(0.0D, 0.0D).color(255, 255, 255, l).endVertex();
                    bufferbuilder.pos(-1.0D, -1.0D, 1.0D).tex(0.0D, 1.0D).color(255, 255, 255, l).endVertex();
                    bufferbuilder.pos(1.0D, -1.0D, 1.0D).tex(1.0D, 1.0D).color(255, 255, 255, l).endVertex();
                    bufferbuilder.pos(1.0D, -1.0D, -1.0D).tex(1.0D, 0.0D).color(255, 255, 255, l).endVertex();
                }

                if (texture == 5) {
                    bufferbuilder.pos(-1.0D, 1.0D, 1.0D).tex(0.0D, 0.0D).color(255, 255, 255, l).endVertex();
                    bufferbuilder.pos(-1.0D, 1.0D, -1.0D).tex(0.0D, 1.0D).color(255, 255, 255, l).endVertex();
                    bufferbuilder.pos(1.0D, 1.0D, -1.0D).tex(1.0D, 1.0D).color(255, 255, 255, l).endVertex();
                    bufferbuilder.pos(1.0D, 1.0D, 1.0D).tex(1.0D, 0.0D).color(255, 255, 255, l).endVertex();
                }

                tessellator.draw();
            }

            GlStateManager.popMatrix();
            GlStateManager.colorMask(true, true, true, false);
        }

        bufferbuilder.setTranslation(0.0D, 0.0D, 0.0D);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.matrixMode(5889);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableDepthTest();
    }
}
