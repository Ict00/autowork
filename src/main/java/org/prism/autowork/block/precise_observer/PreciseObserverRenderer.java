package org.prism.autowork.block.precise_observer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.prism.autowork.block.filterchute.FilterChuteBlockEntity;

public class PreciseObserverRenderer implements BlockEntityRenderer<PreciseObserverBlockEntity> {
    public PreciseObserverRenderer(BlockEntityRendererProvider.Context ctx) {

    }

    @Override
    public void render(PreciseObserverBlockEntity be, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        var state = be.getBlockState();
        var stack = be.getFilterItem();

        switch (state.getValue(BlockStateProperties.FACING)) {
            case UP:
                renderItem(stack, poseStack, itemRenderer, be, multiBufferSource, 0.5f, 0.5f, 1f, 0);
                break;
            case DOWN:
                renderItem(stack, poseStack, itemRenderer, be, multiBufferSource, 0.5f, 0.5f, 0f, 0);
                break;
            default:
                renderItem(stack, poseStack, itemRenderer, be, multiBufferSource, 0.5f, 1f, 0.5f, 90);
                break;
        }
    }

    private void renderItem(ItemStack stack, PoseStack poseStack, ItemRenderer itemRenderer, PreciseObserverBlockEntity be,
                            MultiBufferSource bufferSource, float x, float y, float z, float rot) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(0.5f, 0.5f, 0.5f);

        if (rot != 0) {
            poseStack.mulPose(Axis.XP.rotationDegrees(rot));
        }

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, 200, OverlayTexture.NO_OVERLAY,
                poseStack, bufferSource, be.getLevel(), 1);

        poseStack.popPose();
    }
}
