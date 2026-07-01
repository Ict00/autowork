package org.prism.autowork.block.sculk_cell;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.prism.autowork.CommonConfig;
import org.prism.autowork.block.fluidbarrel.FluidBarrelBlockEntity;
import org.prism.autowork.blockhelp.HelpfulBlockItem;
import org.prism.autowork.other.ModData;
import org.prism.autowork.other.data.FluidStackComponent;

import java.util.List;

public class SculkCellItem extends HelpfulBlockItem {
    public SculkCellItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.has(ModData.SCULK_CELL_EXPERIENCE);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xa1e158;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return stack.getOrDefault(ModData.SCULK_CELL_EXPERIENCE, 0) == 0 ? 64 : 1;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        var dat = stack.get(ModData.SCULK_CELL_EXPERIENCE);

        if (dat != null) {
            var max = CommonConfig.SCULK_CELL_CAPACITY.get();
            return (int)(((float)dat / (float)max) * 13f);
        }

        return super.getBarWidth(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag tooltip) {
        if (isBarVisible(stack)) {
            var contained = stack.getOrDefault(ModData.SCULK_CELL_EXPERIENCE, 0);
            components.add(Component.literal("❇ ").withColor(0xb8f760).append(Component.literal(String.format("%d / %d", contained, CommonConfig.SCULK_CELL_CAPACITY.get())).withColor(0x95fa48)));
        }

        super.appendHoverText(stack, context, components, tooltip);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        int containing = context.getItemInHand().getOrDefault(ModData.SCULK_CELL_EXPERIENCE, 0);
        var res = super.place(context);

        if (res == InteractionResult.sidedSuccess(context.getLevel().isClientSide)) {
            BlockPos pos = context.getClickedPos();

            if (containing != 0 && context.getLevel().getBlockEntity(pos) instanceof SculkCellBlockEntity entity) {
                entity.putXp(containing, false);
            }
        }

        return res;
    }
}
