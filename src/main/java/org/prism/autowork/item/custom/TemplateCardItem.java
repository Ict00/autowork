package org.prism.autowork.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.prism.autowork.other.ModData;
import org.prism.autowork.other.data.template_card.TemplateCardComponent;

import java.util.List;

public class TemplateCardItem extends Item {
    public TemplateCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        var cardComponent = stack.get(ModData.TEMPLATE_CARD);

        if (cardComponent != null) {
            return Component.empty().append(super.getName(stack)).append(": ").append(cardComponent.apply().getName());
        }

        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> components, TooltipFlag flag) {
        super.appendHoverText(stack, ctx, components, flag);

        boolean any = false;

        if (!flag.hasControlDown() && !flag.hasShiftDown()) {
            components.add(Component.translatable("blockhelp.show_more").withColor(0xF3FF47));
        }
        if (flag.hasControlDown()) {
            any = true;
            components.add(Component.translatable("itemhelp.autowork.template_card").withColor(0xb6f030));
        }
        if (flag.hasShiftDown()) {

            var data = stack.get(ModData.TEMPLATE_CARD);

            if (data != null) {
                if (any) {
                    components.add(Component.literal("------------------").withColor(0xadadad));
                }

                components.add(Component.translatable("item.autowork.template_card.sides").withColor(0xf0b026));

                for (var dir : data.config().keySet()) {
                    components.add(Component.literal("- ").withColor(0xf7932f).append(Component.translatable(String.format("blockhelp.direction.%s", dir.getName())).withColor(0xf04e26)));
                }

                components.add(Component.translatable("item.autowork.template_card.items").withColor(0xf0b026));


                for (var i : data.items()) {
                    components.add(Component.literal("- ").append(Integer.toString(i.count())).append(" ").withColor(0xf7932f).append(Component.empty().append(i.item().getName(new ItemStack(i.item(), i.count()))).withColor(0xf04e26)));
                }
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var face = context.getClickedFace();
        var stack = context.getItemInHand();

        if (!level.isClientSide) {
            var data = stack.get(ModData.TEMPLATE_CARD);

            if (data != null) {
                if (level.getBlockState(pos).is(data.apply())) {
                    var tryCap = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, face);

                    if (tryCap != null) {
                        data = data.addItems(tryCap, face);

                        if (data.config().isEmpty()) {
                            stack.remove(ModData.TEMPLATE_CARD);
                        }
                        else {
                            stack.set(ModData.TEMPLATE_CARD, data);
                        }

                        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1, 1.5f);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            else {
                var tryCap = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, face);

                if (tryCap != null) {
                    data = TemplateCardComponent.EMPTY.setBlock(level.getBlockState(pos).getBlock()).addItems(tryCap, face);
                    stack.set(ModData.TEMPLATE_CARD, data);
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1, 1.5f);
                    return InteractionResult.SUCCESS;
                }
            }
        }


        return super.useOn(context);
    }
}
