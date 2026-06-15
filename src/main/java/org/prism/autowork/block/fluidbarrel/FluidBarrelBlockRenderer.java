package org.prism.autowork.block.fluidbarrel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidBarrelBlockRenderer implements BlockEntityRenderer<FluidBarrelBlockEntity> {

    public FluidBarrelBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FluidBarrelBlockEntity barrel, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!barrel.getBlockState().getValue(BlockStateProperties.OPEN)) {
            return;
        }

        FluidStack fluid = barrel.tank.getFluid();

        if (fluid.isEmpty()) {
            return;
        }

        float fill = fluid.getAmount() / (float) barrel.tank.getCapacity();

        renderFluidSurface(fluid, fill, poseStack, buffer, packedLight);
    }

    private void renderFluidSurface(FluidStack stack, float fill, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(stack.getFluid());
        ResourceLocation stillTexture = ext.getStillTexture(stack);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
        int color = ext.getTintColor(stack);

        // :sob:

        float a = ((color >> 24) & 255) / 255f;
        float r = ((color >> 16) & 255) / 255f;
        float g = ((color >> 8) & 255) / 255f;
        float b = (color & 255) / 255f;

        if (a <= 0f) {
            a = 1f;
        }

        float minX = 2f / 16f;
        float maxX = 14f / 16f;

        float minZ = 2f / 16f;
        float maxZ = 14f / 16f;

        float minY = 2f / 16f;
        float maxY = 14f / 16f;

        float y = minY + (maxY - minY) * fill;

        VertexConsumer vc =
                buffer.getBuffer(RenderType.translucent());

        PoseStack.Pose pose = poseStack.last();

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();

        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        vc.addVertex(pose, minX, y, minZ)
                .setColor(r, g, b, a)
                .setUv(u0, v0)
                .setLight(packedLight)
                .setNormal(0.0F, 1.0F, 0.0F);

        vc.addVertex(pose, minX, y, maxZ)
                .setColor(r, g, b, a)
                .setUv(u0, v1)
                .setLight(packedLight)
                .setNormal(0.0F, 1.0F, 0.0F);;

        vc.addVertex(pose, maxX, y, maxZ)
                .setColor(r, g, b, a)
                .setUv(u1, v1)
                .setLight(packedLight)
                .setNormal(0.0F, 1.0F, 0.0F);

        vc.addVertex(pose, maxX, y, minZ)
                .setColor(r, g, b, a)
                .setUv(u1, v0)
                .setLight(packedLight)
                .setNormal(0.0F, 1.0F, 0.0F);
    }
}