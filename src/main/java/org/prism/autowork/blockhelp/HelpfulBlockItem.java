package org.prism.autowork.blockhelp;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class HelpfulBlockItem extends BlockItem {
    public HelpfulBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltip) {
        super.appendHoverText(stack, context, components, tooltip);
        if (stack.getItem() instanceof BlockItem bi) {
            if (bi.getBlock() instanceof BlockHelpProvider prov) {
                var help = prov.getHelp();
                var addedAny = false;

                if (tooltip.hasControlDown()) {
                    components.addAll(help.technical());
                    addedAny = true;
                }

                if (tooltip.hasShiftDown()) {
                    if (addedAny) {
                        components.add(Component.literal("------------------").withColor(0xadadad));
                    }
                    components.addAll(help.details());
                    addedAny = true;
                }

                if (!addedAny) {
                    components.add(Component.translatable("blockhelp.show_more").withColor(0xF3FF47));
                }
            }
        }
    }
}
