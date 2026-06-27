package org.prism.autowork.block.bulbs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.other.ModUtils;

public abstract class AbstractBulb extends Block {
    public AbstractBulb(Properties properties) {
        super(properties);
    }

    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (oldState.getBlock() != state.getBlock() && level instanceof ServerLevel serverlevel) {
            this.flip(state, serverlevel, pos);
        }
    }

    protected void neighborChanged(BlockState state, Level p_308955_, BlockPos pos, Block p_308949_, BlockPos lPos, boolean p_309085_) {
        if (p_308955_ instanceof ServerLevel serverlevel && !ModUtils.lookTo(pos, state.getValue(BlockStateProperties.FACING).getOpposite()).equals(lPos)) {
            this.flip(state, serverlevel, pos);
        }
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (state.getValue(BlockStateProperties.LIT) && direction == state.getValue(BlockStateProperties.FACING)) {
            return 15;
        }

        return super.getDirectSignal(state, level, pos, direction);
    }

    protected int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockState.getValue(BlockStateProperties.LIT) && blockState.getValue(BlockStateProperties.FACING) == side ? 15 : 0;
    }

    public abstract void flip(BlockState state, ServerLevel level, BlockPos pos);

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.LIT, BlockStateProperties.POWERED, BlockStateProperties.FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.LIT, false).setValue(BlockStateProperties.POWERED, false).setValue(BlockStateProperties.FACING, context.getClickedFace());
    }
}
