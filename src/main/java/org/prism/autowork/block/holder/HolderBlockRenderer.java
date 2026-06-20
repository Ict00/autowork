package org.prism.autowork.block.holder;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class HolderBlockRenderer implements BlockEntityRenderer<HolderBlockEntity> {

    public HolderBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(HolderBlockEntity holderBlockEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {
        var item = holderBlockEntity.handler.getStackInSlot(0);

        if (item.isEmpty()) return;

        var itemRenderer = Minecraft.getInstance().getItemRenderer();

        Vec3 offset = switch (holderBlockEntity.getBlockState().getValue(BlockStateProperties.FACING)) {
            case SOUTH -> new Vec3(0.5, 0.5, 0.7);
            case NORTH -> new Vec3(0.5, 0.5, 0.3);
            case EAST -> new Vec3(0.7, 0.5, 0.5);
            case WEST -> new Vec3(0.3, 0.5, 0.5);
            case DOWN -> new Vec3(0.5, 0.3, 0.5);
            case UP -> new Vec3(0.5, 0.7, 0.5);
            default -> new Vec3(0.5, 0.5, 0.5);
        };

        renderItem(item, poseStack, itemRenderer, holderBlockEntity, multiBufferSource, (float) offset.x, (float) offset.y, (float) offset.z);
    }

    private void renderItem(ItemStack stack, PoseStack poseStack, ItemRenderer itemRenderer, HolderBlockEntity be, MultiBufferSource bufferSource, float x, float y, float z) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(0.5f, 0.5f, 0.5f);

        BlockPos blockPos = be.getBlockPos();
        Vec3 worldItemPos = new Vec3(blockPos.getX() + x, blockPos.getY() + y, blockPos.getZ() + z);

        Vec3 cameraPosition = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vec3 diff = worldItemPos.subtract(cameraPosition);
        
        float yRot = (float) (Mth.atan2(diff.x, diff.z) + Math.PI);
        poseStack.mulPose(Axis.YP.rotation(yRot));

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, 200, OverlayTexture.NO_OVERLAY,
                poseStack, bufferSource, be.getLevel(), 1);
        poseStack.popPose();
    }
}
