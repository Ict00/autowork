package org.prism.autowork.block.bulbs.quartz;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.prism.autowork.block.bulbs.AbstractBulb;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.other.ModUtils;

public class QuartzBulbBlock extends AbstractBulb implements BlockHelpProvider {
    public QuartzBulbBlock(Properties properties) {
        super(properties);
    }


    @Override
    public void flip(BlockState state, ServerLevel level, BlockPos pos) {
        boolean hasSignal = false;
        var facing = state.getValue(BlockStateProperties.FACING);

        for (var d : Direction.values()) {
            if (d != facing) {
                if (ModUtils.hasSignal(level, pos, d)) {
                    hasSignal = true;
                    break;
                }
            }
        }
        if (hasSignal && !state.getValue(BlockStateProperties.POWERED)) {
            var newState = state.setValue(BlockStateProperties.LIT, true).setValue(BlockStateProperties.POWERED, true);
            level.playSound(null, pos, SoundEvents.COPPER_BULB_TURN_ON, SoundSource.BLOCKS);
            level.setBlockAndUpdate(pos, newState);
        }
        else if (!hasSignal && state.getValue(BlockStateProperties.POWERED)) {
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, false));
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (state.getValue(BlockStateProperties.LIT)) {
            level.scheduleTick(pos, asBlock(), 20);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        if (state.getValue(BlockStateProperties.LIT)) {
            level.playSound(null, pos, SoundEvents.COPPER_BULB_TURN_OFF, SoundSource.BLOCKS);
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, false));
        }
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .back("blockhelp.autowork.quartz_bulb.back")
                .details("blockhelp.autowork.quartz_bulb.details")
                .configurable_by_sign()
                .build();
    }
}
