package org.prism.autowork.other;


import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import org.prism.autowork.Autowork;
import org.prism.autowork.ClientConfig;
import org.prism.autowork.other.datamaps.CrushingMap;

import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber(value = Dist.CLIENT, modid = Autowork.MODID)
public class HudRender {
    @SubscribeEvent
    public static void onRender(RenderGuiLayerEvent.Post event) {
        if (!ClientConfig.CRUSHING_HUD_HELPER.get()) return;

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null) {
            return;
        }

        AtomicBoolean hasCrushing = new AtomicBoolean(false);

        EnchantmentHelper.runIterationOnItem(mc.player.getMainHandItem(), (x, y) -> {
            if (x.is(ModOther.CRUSHING_ENCHANTMENT)) {
                hasCrushing.set(true);
            }
        });

        if (!hasCrushing.get()) {
            return;
        }

        if (!(mc.hitResult instanceof BlockHitResult blockHit)) {
            return;
        }

        BlockPos pos = blockHit.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);
        var item = state.getBlock().asItem();
        var data = CrushingMap.getConversion(item);

        if (data.isEmpty()) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        if (data.getCount() > 1) {
            graphics.drawString(mc.font, String.format("%d", data.getCount()), graphics.guiWidth()/2+10, graphics.guiHeight()/2+10, 0xFFFFFF);
        }

        graphics.renderItem(data, graphics.guiWidth()/2+5, graphics.guiHeight()/2+5);
    }
}
