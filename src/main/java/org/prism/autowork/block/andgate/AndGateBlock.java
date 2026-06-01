package org.prism.autowork.block.andgate;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.other.ModUtils;

public class AndGateBlock extends DiodeBlock implements BlockHelpProvider {
    public static final MapCodec<AndGateBlock> CODEC = simpleCodec(AndGateBlock::new);
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 3);

    public AndGateBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false).setValue(STAGE, 0));
    }

    @Override
    protected boolean sideInputDiodesOnly() {
        return true;
    }

    @Override
    protected MapCodec<? extends DiodeBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean shouldTurnOn(Level level, BlockPos pos, BlockState state) {
        var face = state.getValue(FACING);
        var left = face.getCounterClockWise();
        var right = face.getClockWise();

        var isLeftActive = ModUtils.hasSignal(level, pos, left);
        var isRightActive = ModUtils.hasSignal(level, pos, right);

        BlockState newState = state.setValue(STAGE, 0);

        if (isLeftActive) {
            newState = state.setValue(STAGE, 1);
        }
        if (isRightActive) {
            newState = state.setValue(STAGE, 2);
        }
        if (isLeftActive && isRightActive) {
            newState = state.setValue(STAGE, 3);
        }

        if (!newState.equals(state)) {
            level.setBlockAndUpdate(pos, newState);
        }

        if (isLeftActive && isRightActive) {
            return true;
        }

        return false;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STAGE, POWERED);
    }

    @Override
    protected int getDelay(BlockState blockState) {
        return 2;
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .left("blockhelp.autowork.andgate.side")
                .right("blockhelp.autowork.andgate.side")
                .front("blockhelp.autowork.andgate.front")
                .details("blockhelp.autowork.andgate.details")
                .only_when_powered()
                .build();
    }
}
