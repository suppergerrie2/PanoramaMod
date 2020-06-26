package com.suppergerrie2.panorama;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderSkyboxCube;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;

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
        RenderSystem.matrixMode(5889);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.multMatrix(Matrix4f.perspective(85.0D,
                                                     (float) mc.getMainWindow().getFramebufferWidth() / (float) mc
                                                             .getMainWindow().getFramebufferHeight(), 0.05F, 10.0F));
        RenderSystem.matrixMode(5888);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        int i = 2;

        for (int pass = 0; pass < 4; ++pass) {
            RenderSystem.pushMatrix();
            float f = ((float) (pass % 2) / 2.0F - 0.5F) / 256.0F;
            float f1 = ((float) (pass / 2) / 2.0F - 0.5F) / 256.0F;
            RenderSystem.translatef(f, f1, 0.0F);
            RenderSystem.rotatef(pitch, 1.0F, 0.0F, 0.0F);
            RenderSystem.rotatef(yaw, 0.0F, 1.0F, 0.0F);

            for (int side = 0; side < 6; ++side) {
                this.textures[side].bindTexture();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
                int l = Math.round(255.0F * alpha) / (pass + 1);
                if (side == 0) {
                    bufferbuilder.pos(-1.0D, -1.0D, 1.0D).color(255, 255, 255, l).tex(0.0F, 0.0F).endVertex();
                    bufferbuilder.pos(-1.0D, 1.0D, 1.0D).color(255, 255, 255, l).tex(0.0F, 1.0F).endVertex();
                    bufferbuilder.pos(1.0D, 1.0D, 1.0D).color(255, 255, 255, l).tex(1.0F, 1.0F).endVertex();
                    bufferbuilder.pos(1.0D, -1.0D, 1.0D).color(255, 255, 255, l).tex(1.0F, 0.0F).endVertex();
                }

                if (side == 1) {
                    bufferbuilder.pos(1.0D, -1.0D, 1.0D).color(255, 255, 255, l).tex(0.0F, 0.0F).endVertex();
                    bufferbuilder.pos(1.0D, 1.0D, 1.0D).color(255, 255, 255, l).tex(0.0F, 1.0F).endVertex();
                    bufferbuilder.pos(1.0D, 1.0D, -1.0D).color(255, 255, 255, l).tex(1.0F, 1.0F).endVertex();
                    bufferbuilder.pos(1.0D, -1.0D, -1.0D).color(255, 255, 255, l).tex(1.0F, 0.0F).endVertex();
                }

                if (side == 2) {
                    bufferbuilder.pos(1.0D, -1.0D, -1.0D).color(255, 255, 255, l).tex(0.0F, 0.0F).endVertex();
                    bufferbuilder.pos(1.0D, 1.0D, -1.0D).color(255, 255, 255, l).tex(0.0F, 1.0F).endVertex();
                    bufferbuilder.pos(-1.0D, 1.0D, -1.0D).color(255, 255, 255, l).tex(1.0F, 1.0F).endVertex();
                    bufferbuilder.pos(-1.0D, -1.0D, -1.0D).color(255, 255, 255, l).tex(1.0F, 0.0F).endVertex();
                }

                if (side == 3) {
                    bufferbuilder.pos(-1.0D, -1.0D, -1.0D).color(255, 255, 255, l).tex(0.0F, 0.0F).endVertex();
                    bufferbuilder.pos(-1.0D, 1.0D, -1.0D).color(255, 255, 255, l).tex(0.0F, 1.0F).endVertex();
                    bufferbuilder.pos(-1.0D, 1.0D, 1.0D).color(255, 255, 255, l).tex(1.0F, 1.0F).endVertex();
                    bufferbuilder.pos(-1.0D, -1.0D, 1.0D).color(255, 255, 255, l).tex(1.0F, 0.0F).endVertex();
                }

                if (side == 4) {
                    bufferbuilder.pos(-1.0D, -1.0D, -1.0D).color(255, 255, 255, l).tex(0.0F, 0.0F).endVertex();
                    bufferbuilder.pos(-1.0D, -1.0D, 1.0D).color(255, 255, 255, l).tex(0.0F, 1.0F).endVertex();
                    bufferbuilder.pos(1.0D, -1.0D, 1.0D).color(255, 255, 255, l).tex(1.0F, 1.0F).endVertex();
                    bufferbuilder.pos(1.0D, -1.0D, -1.0D).color(255, 255, 255, l).tex(1.0F, 0.0F).endVertex();
                }

                if (side == 5) {
                    bufferbuilder.pos(-1.0D, 1.0D, 1.0D).color(255, 255, 255, l).tex(0.0F, 0.0F).endVertex();
                    bufferbuilder.pos(-1.0D, 1.0D, -1.0D).color(255, 255, 255, l).tex(0.0F, 1.0F).endVertex();
                    bufferbuilder.pos(1.0D, 1.0D, -1.0D).color(255, 255, 255, l).tex(1.0F, 1.0F).endVertex();
                    bufferbuilder.pos(1.0D, 1.0D, 1.0D).color(255, 255, 255, l).tex(1.0F, 0.0F).endVertex();
                }

                tessellator.draw();
            }

            RenderSystem.popMatrix();
            RenderSystem.colorMask(true, true, true, false);
        }

        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.matrixMode(5889);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(5888);
        RenderSystem.popMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }
}
