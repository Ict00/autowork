package org.prism.autowork.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import org.prism.autowork.other.ModOther;

import java.util.List;

public class WrenchItem extends Item {
    public WrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide) {
            var pos = context.getClickedPos();
            var state = context.getLevel().getBlockState(pos);

            if (state.is(ModOther.FIXABLE_TAG)) {
                var block = state.getBlock();
                context.getLevel().scheduleTick(pos, block, 1);
                context.getLevel().playSound(null, pos, SoundEvents.VAULT_ACTIVATE, SoundSource.PLAYERS, 1, 1.5f);

                return InteractionResult.SUCCESS;
            }
        }

        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> components, TooltipFlag flag) {
        super.appendHoverText(stack, ctx, components, flag);

        if (!flag.hasControlDown()) {
            components.add(Component.translatable("itemhelp.show_more").withColor(0xF3FF47));
        }
        else {
            components.add(Component.translatable("itemhelp.autowork.wrench").withColor(0xb6f030));
        }
    }
}
