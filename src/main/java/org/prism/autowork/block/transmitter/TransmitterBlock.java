package org.prism.autowork.block.transmitter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.entities.signal.SignalEntity;
import org.prism.autowork.other.ModUtils;

public class TransmitterBlock extends Block implements BlockHelpProvider {
    // 0 - off
    // 1 - receiving
    // 2 - emitting
    // 3 - idle
    public static final IntegerProperty POWER_STATE = IntegerProperty.create("power_state", 0, 3);

    public TransmitterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block p_60512_, BlockPos q, boolean p_60514_) {
        super.neighborChanged(state, level, pos, p_60512_, q, p_60514_);

        if (!level.isClientSide) {
            var face = state.getValue(BlockStateProperties.FACING);

            if (state.getValue(POWER_STATE) == 0) {
                if (ModUtils.hasSignal(level, pos, face)) {
                    level.setBlockAndUpdate(pos, state.setValue(POWER_STATE, 2));
                    var front = ModUtils.lookTo(pos, face);

                    var newEntity = new SignalEntity
                            (front.getX()+0.5, front.getY()+0.5, front.getZ()+0.5, ModUtils.direction2vec(face), level);

                    level.playSound(null, pos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 1, 1.5f);
                    level.addFreshEntity(newEntity);
                }
            }
        }
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (state.getValue(POWER_STATE) != 0) {
            level.scheduleTick(pos, asBlock(), 10);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        if (state.getValue(POWER_STATE) != 0 && state.getValue(POWER_STATE) != 3) {
            level.setBlockAndUpdate(pos, state.setValue(POWER_STATE, 3));
        }
        else if (state.getValue(POWER_STATE) == 3) {
            level.setBlockAndUpdate(pos, state.setValue(POWER_STATE, 0));
        }
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWER_STATE) == 1 && state.getValue(BlockStateProperties.FACING) == direction ? 15 : 0;
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING, POWER_STATE);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(POWER_STATE, 0).setValue(BlockStateProperties.FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .back("blockhelp.autowork.transmitter.back")
                .back("blockhelp.autowork.transmitter.back2")
                .front("blockhelp.autowork.transmitter.front")
                .details("blockhelp.autowork.transmitter.details")
                .build();
    }
}
