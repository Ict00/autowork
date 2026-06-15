package org.prism.autowork.block.placer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface ISpecialPlaceable {
    void placePostAction(ItemStack stack, Level level, BlockPos pos);
}
