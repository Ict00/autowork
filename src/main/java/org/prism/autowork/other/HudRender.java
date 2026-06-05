package org.prism.autowork.other;


import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import org.prism.autowork.Autowork;
import org.prism.autowork.ClientConfig;
import org.prism.autowork.hudinv.HudInventoryProvider;
import org.prism.autowork.other.datamaps.CrushingMap;

import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber(value = Dist.CLIENT, modid = Autowork.MODID)
public class HudRender {
    private static final int BH_X = 0;
    private static final int BH_Y = 0;
    private static final int BH_D = 2;

    private static void renderItem(GuiGraphics gui, int x, int y, ItemStack stack, Font font) {
        gui.renderItem(stack, x, y);
        gui.renderItemDecorations(font, stack, x, y);
    }

    @SubscribeEvent
    public static void onRender(RenderGuiLayerEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null) {
            return;
        }

        if (ClientConfig.BUFFER_HUD_RENDER.get()) {
            if (!(mc.hitResult instanceof BlockHitResult blockHit)) {
                System.out.println("HATE");
                return;
            }

            BlockPos pos = blockHit.getBlockPos();
            BlockEntity be = mc.level.getBlockEntity(pos);

            if (be == null) {
                System.out.println("NOOO");
                return;
            }

            GuiGraphics graphics = event.getGuiGraphics();

            if (be instanceof HudInventoryProvider prov) {
                System.out.println("FUCK");
                var texture = prov.hudTextureLoc();

                if (texture != null) {
                    graphics.blit(texture, BH_X, BH_Y,0,0,16,16,16,16);
                }

                var handler = prov.getLookHandler(blockHit.getDirection(), mc.level, pos);
                int slots = handler.handler().getSlots();
                boolean inlineRender = Math.pow(Math.sqrt(slots), 2) == slots;
                var slotWidth = prov.slotWidthOverride();

                if (inlineRender) {
                    System.out.println("YUP");
                    for (int i = 0; i < slots; i++) {
                        var ov = prov.hasOverrideLocationForSlot(i) ? prov.getSlotOverride(i) : null;

                        int x = ov != null ? ov.getA() + BH_X : BH_X + i * slotWidth + BH_D;
                        int y = ov != null ? ov.getB() + BH_Y : BH_Y;

                        renderItem(graphics, x, y, handler.handler().getStackInSlot(i), mc.font);
                    }
                }
                else {
                    System.out.println("NOPE");
                    // TODO: implement
                }
            }
        }

        if (ClientConfig.CRUSHING_HUD_HELPER.get()) {

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
                graphics.drawString(mc.font, String.format("%d", data.getCount()), graphics.guiWidth() / 2 + 10, graphics.guiHeight() / 2 + 10, 0xFFFFFF);
            }

            graphics.renderItem(data, graphics.guiWidth() / 2 + 5, graphics.guiHeight() / 2 + 5);
        }
    }
}
