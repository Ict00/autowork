package org.prism.autowork.block.filterchute;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class FilterChuteBlockRenderer implements BlockEntityRenderer<FilterChuteBlockEntity> {
    public FilterChuteBlockRenderer(BlockEntityRendererProvider.Context ctx) {

    }

    @Override
    public void render(FilterChuteBlockEntity be, float v, PoseStack poseStack, MultiBufferSource bufferSource, int i, int i1) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        ItemStack stack = be.getFilterItem();

        if(!stack.isEmpty()) {
            renderItem(stack, poseStack, itemRenderer, be, bufferSource, 0.5f, 0.5f, 0, 0);
            renderItem(stack, poseStack, itemRenderer, be, bufferSource, 0.5f, 0.5f, 1, 180);
            renderItem(stack, poseStack, itemRenderer, be, bufferSource, 0f, 0.5f, 0.5f, 90);
            renderItem(stack, poseStack, itemRenderer, be, bufferSource, 1f, 0.5f, 0.5f, 270);
        }
    }

    private void renderItem(ItemStack stack, PoseStack poseStack, ItemRenderer itemRenderer, FilterChuteBlockEntity be,
                            MultiBufferSource bufferSource, float x, float y, float z, float rot) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(0.5f, 0.5f, 0.5f);

        if (rot != 0) {
            poseStack.mulPose(Axis.YP.rotationDegrees(rot));
        }

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, 200, OverlayTexture.NO_OVERLAY,
                poseStack, bufferSource, be.getLevel(), 1);

        poseStack.popPose();
    }
}
