package org.prism.autowork.block.toggler;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.other.ModUtils;

public class TogglerBlock extends Block implements BlockHelpProvider {
    public static final MapCodec<TogglerBlock> CODEC = simpleCodec(TogglerBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final DirectionProperty FACING = DirectionProperty.create("facing");
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    protected @NotNull MapCodec<? extends TogglerBlock> codec() {
        return CODEC;
    }

    public TogglerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (oldState.getBlock() != state.getBlock() && level instanceof ServerLevel serverlevel) {
            this.checkAndFlip(state, serverlevel, pos);
        }
    }

    protected void neighborChanged(BlockState state, Level p_308955_, BlockPos pos, Block p_308949_, BlockPos lPos, boolean p_309085_) {
        if (p_308955_ instanceof ServerLevel serverlevel && !ModUtils.lookTo(pos, state.getValue(FACING).getOpposite()).equals(lPos)) {
            this.checkAndFlip(state, serverlevel, pos);
        }
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (state.getValue(LIT) && direction == state.getValue(FACING)) {
            return 15;
        }

        return super.getDirectSignal(state, level, pos, direction);
    }

    protected int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockState.getValue(LIT) && blockState.getValue(FACING) == side ? 15 : 0;
    }

    public void checkAndFlip(BlockState state, ServerLevel level, BlockPos pos) {
        boolean flag = false;
        var facing = state.getValue(FACING);

        for (var d : Direction.values()) {
            if (d != facing) {
                if (ModUtils.hasSignal(level, pos, d)) {
                    flag = true;
                    break;
                }
            }
        }

        if (flag != state.getValue(POWERED)) {
            BlockState blockstate = state;
            if (!state.getValue(POWERED)) {
                blockstate = state.cycle(LIT);
                level.playSound(null, pos, blockstate.getValue(LIT) ? SoundEvents.COPPER_BULB_TURN_ON : SoundEvents.COPPER_BULB_TURN_OFF, SoundSource.BLOCKS);
            }

            level.setBlockAndUpdate(pos, blockstate.setValue(POWERED, flag));
        }
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, POWERED, FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(LIT, false).setValue(POWERED, false).setValue(FACING, context.getClickedFace());
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .back("blockhelp.autowork.toggler.back")
                .details("blockhelp.autowork.toggler.details")
                .configurable_by_sign()
                .build();
    }
}
