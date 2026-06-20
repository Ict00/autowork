package org.prism.autowork.other;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
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
import org.prism.autowork.block.holder.HolderBlockEntity;
import org.prism.autowork.hudinv.HudInventoryProvider;
import org.prism.autowork.other.datamaps.CrushingMap;

import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber(value = Dist.CLIENT, modid = Autowork.MODID)
public class HudRender {
    private static final int BH_X = 0;
    private static final int BH_Y = 0;

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

        if (ClientConfig.HOLDER_DECORATIONS_RENDER.get()) {
            if (!(mc.hitResult instanceof BlockHitResult blockHit)) {
                return;
            }

            BlockPos pos = blockHit.getBlockPos();
            BlockEntity ube = mc.level.getBlockEntity(pos);

            if (ube == null) {
                return;
            }

            if (ube instanceof HolderBlockEntity be) {
                GuiGraphics graphics = event.getGuiGraphics();

                var item = be.handler.getStackInSlot(0);

                if (!item.isEmpty()) {
                    var x = graphics.guiWidth() / 2 - 9;
                    var y = graphics.guiHeight() / 2 - 9;

                    graphics.renderItemDecorations(Minecraft.getInstance().font, item, x, y);
                }
            }
        }

        if (ClientConfig.BUFFER_HUD_RENDER.get()) {
            if (!(mc.hitResult instanceof BlockHitResult blockHit)) {
                return;
            }

            BlockPos pos = blockHit.getBlockPos();
            BlockEntity be = mc.level.getBlockEntity(pos);

            if (be == null) {
                return;
            }

            GuiGraphics graphics = event.getGuiGraphics();

            if (be instanceof HudInventoryProvider prov) {
                int cX = (graphics.guiWidth() / 2) - (int)(prov.hudWidth(blockHit.getDirection())*1.5) + BH_X;
                int cY = (graphics.guiHeight() / 2) - (prov.hudHeight(blockHit.getDirection())/2) + BH_Y;

                int slots = prov.slotCount(blockHit.getDirection());
                var sqrt = Math.sqrt(slots);

                var handler = prov.getLookHandler(blockHit.getDirection(), mc.level, pos);
                boolean inlineRender = Math.pow(sqrt, 2) != slots || slots == 1;

                if (inlineRender) {
                    for (int i = 0; i < slots; i++) {
                        var ov = prov.hasOverrideLocationForSlot(i) ? prov.getSlotOverride(i) : null;

                        int x = ov != null ? ov.getA() + cX : cX + i * 16;
                        int y = ov != null ? ov.getB() + cY : cY;

                        renderItem(graphics, x, y, handler.handler().getStackInSlot(i), mc.font);
                    }
                }
                else {
                    int c = 0;
                    int r = 0;

                    for (int i = 0; i < slots; i++) {
                        var ov = prov.hasOverrideLocationForSlot(i) ? prov.getSlotOverride(i) : null;

                        int x = ov != null ? ov.getA() + cX : 1 + cX + c * (16);
                        int y = ov != null ? ov.getB() + cY : 1 + cY + r * (16);

                        renderItem(graphics, x, y, handler.handler().getStackInSlot(i), mc.font);

                        if ((i+1)%sqrt == 0) {
                            c = 0;
                            r++;
                        }
                        else {
                            c++;
                        }
                    }
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
