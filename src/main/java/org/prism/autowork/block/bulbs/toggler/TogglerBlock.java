package org.prism.autowork.block.bulbs.toggler;

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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.bulbs.AbstractBulb;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.other.ModUtils;

public class TogglerBlock extends AbstractBulb implements BlockHelpProvider {
    public TogglerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }


    @Override
    public void flip(BlockState state, ServerLevel level, BlockPos pos) {
        boolean flag = false;
        var facing = state.getValue(BlockStateProperties.FACING);

        for (var d : Direction.values()) {
            if (d != facing) {
                if (ModUtils.hasSignal(level, pos, d)) {
                    flag = true;
                    break;
                }
            }
        }

        if (flag != state.getValue(BlockStateProperties.POWERED)) {
            BlockState blockstate = state;
            if (!state.getValue(BlockStateProperties.POWERED)) {
                blockstate = state.cycle(BlockStateProperties.LIT);
                level.playSound(null, pos, blockstate.getValue(BlockStateProperties.LIT) ? SoundEvents.COPPER_BULB_TURN_ON : SoundEvents.COPPER_BULB_TURN_OFF, SoundSource.BLOCKS);
            }

            level.setBlockAndUpdate(pos, blockstate.setValue(BlockStateProperties.POWERED, flag));
        }
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
