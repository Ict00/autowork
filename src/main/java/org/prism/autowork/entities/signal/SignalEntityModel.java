package org.prism.autowork.entities.signal;// Made with Blockbench 5.0.0
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import org.prism.autowork.Autowork;

public class SignalEntityModel<T extends SignalEntity> extends HierarchicalModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Autowork.MODID, "signal_entity"), "main");

	private final ModelPart rot;
	private final ModelPart root;

	public SignalEntityModel(ModelPart root) {
		this.root = root;
		this.rot = root.getChild("rot");
	}

	@Override
	public ModelPart root() {
		return root;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition rot = partdefinition.addOrReplaceChild("rot",
				CubeListBuilder.create().texOffs(0, 0)
						.addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));
		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	public void setupAnim(SignalEntity p_326187_, float p_311922_, float p_312586_, float p_312400_, float p_312547_, float p_311844_) {
		this.rot.yRot = -p_312400_ * 16.0F * ((float)Math.PI / 180F);
		this.rot.xRot = -p_312400_ * 16.0F * ((float)Math.PI / 180F);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		rot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}