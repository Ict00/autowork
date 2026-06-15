package org.prism.autowork.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;


public interface IWrenchable {
    public void wrench(Level level, BlockPos pos, BlockState state);
}
