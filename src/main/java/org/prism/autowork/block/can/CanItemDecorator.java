package org.prism.autowork.block.can;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.IItemDecorator;
import org.prism.autowork.other.ModData;
import org.prism.autowork.other.data.CanComponent;

public class CanItemDecorator implements IItemDecorator {

    // TODO: Make this shit look better
    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int x, int y) {
        CanComponent component = stack.get(ModData.CAN);
        if (component == null) return false;

        Item item = component.fullItem();
        if (item == Items.AIR) return false;

        PoseStack pose = guiGraphics.pose();

        pose.pushPose();

        float scale = 0.5f;

        pose.translate(0, 0, 300);
        pose.scale(scale, scale, 1.0f);

        guiGraphics.renderItem(new ItemStack(item), (int) ((x - 1) / scale), (int) ((y + 9) / scale));

        pose.popPose();

        return true;
    }

}