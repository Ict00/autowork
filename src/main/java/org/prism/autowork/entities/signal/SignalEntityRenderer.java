package org.prism.autowork.entities.signal;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.prism.autowork.Autowork;

@OnlyIn(Dist.CLIENT)
public class SignalEntityRenderer extends EntityRenderer<SignalEntity> {
    private static final ResourceLocation TEXTURE_LOCATION =
            ResourceLocation.fromNamespaceAndPath(Autowork.MODID, "textures/entity/projectiles/signal_entity.png");

    private final SignalEntityModel<SignalEntity> model;

    public SignalEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SignalEntityModel<>(context.bakeLayer(SignalEntityModel.LAYER_LOCATION));
    }

    @Override
    public void render(SignalEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, yaw, partialTick, poseStack, bufferSource, packedLight);

        if (entity.tickCount >= 2) {
            this.model.setupAnim(entity, 0, 0, entity.tickCount + partialTick, 0, 0);
            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucentCull(TEXTURE_LOCATION));
            this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        }
    }

    protected float xOffset(float tickCount) {
        return tickCount * 0.03F;
    }

    @Override
    public ResourceLocation getTextureLocation(SignalEntity entity) {
        return TEXTURE_LOCATION;
    }
}

