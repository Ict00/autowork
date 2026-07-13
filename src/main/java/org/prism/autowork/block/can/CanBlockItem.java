package org.prism.autowork.block.can;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.prism.autowork.block.placer.ISpecialPlaceable;
import org.prism.autowork.blockhelp.HelpfulBlockItem;
import org.prism.autowork.other.ModData;
import org.prism.autowork.other.data.CanComponent;

import java.text.DecimalFormat;
import java.util.List;

public class CanBlockItem extends HelpfulBlockItem implements ISpecialPlaceable {
    private static final DecimalFormat FORMAT = new DecimalFormat("#.##");

    public CanBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        CanComponent containing = context.getItemInHand().get(ModData.CAN);
        var res = super.place(context);

        if (res == InteractionResult.sidedSuccess(context.getLevel().isClientSide) && containing != null) {
            BlockPos pos = context.getClickedPos();

            if (context.getLevel().getBlockEntity(pos) instanceof CanBlockEntity entity) {
                entity.itemComponent = containing;
            }
        }

        return res;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltip) {
        var component = stack.get(ModData.CAN);
        if (component != null) {
            float saturation = 0;
            float nutrition = 0;

            for (var f : component.food()) {
                saturation += f.saturation();
                nutrition += f.nutrition();
            }

            components.add(Component.translatable("block.autowork.can.info.uses", component.food().size()).withColor(0xb3b3b3));
            components.add(Component.translatable("block.autowork.can.info.n", FORMAT.format(nutrition)).withColor(0xf5a97a));
            components.add(Component.translatable("block.autowork.can.info.s", FORMAT.format(saturation)).withColor(0xf5c87a));
        }

        super.appendHoverText(stack, context, components, tooltip);
    }

    @Override
    public void placePostAction(ItemStack stack, Level level, BlockPos pos) {
        var containing = stack.get(ModData.CAN);

        if (containing == null) return;

        if (level.getBlockEntity(pos) instanceof CanBlockEntity entity) {
            entity.itemComponent = containing;
        }
    }
}
