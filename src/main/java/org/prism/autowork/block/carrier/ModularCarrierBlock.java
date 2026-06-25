package org.prism.autowork.block.carrier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.block.common.BlocksAbstractLogic;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.other.ModOther;
import org.prism.autowork.other.ModUtils;

public class ModularCarrierBlock extends Block implements BlockHelpProvider {
    public ModularCarrierBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void onPlace(BlockState state, Level tlevel, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, tlevel, pos, oldState, movedByPiston);

        if (oldState.is(ModBlocks.MODULAR_CARRIER) && tlevel instanceof ServerLevel level) {
            var oldFacing = oldState.getValue(BlockStateProperties.HORIZONTAL_FACING);
            var newFacing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

            if (oldFacing != newFacing) {
                var oldFrontPos = ModUtils.lookTo(pos, oldFacing);
                var newFrontPos = ModUtils.lookTo(pos, newFacing);


                if (!level.getBlockState(oldFrontPos).is(ModOther.CARRIER_IMMOVABLE)) {
                    var res = BlocksAbstractLogic.abstractMover(level, oldFrontPos, newFrontPos);

                    if (res) {
                        level.playSound(null, pos, SoundEvents.VAULT_EJECT_ITEM, SoundSource.BLOCKS, 1, 1.5f);
                        level.sendParticles(ParticleTypes.POOF, oldFrontPos.getX()+0.5, oldFrontPos.getY()+0.5, oldFrontPos.getZ()+0.5, 10, 0.5, 0.5 , 0.5, 0);
                    }
                }



                var top = pos.above();
                var topState = level.getBlockState(top);

                if (topState.is(ModBlocks.MODULAR_CARRIER)) {
                    var v = topState.getValue(BlockStateProperties.HORIZONTAL_FACING);
                    if (v != newFacing) {
                        tlevel.setBlockAndUpdate(top, topState.setValue(BlockStateProperties.HORIZONTAL_FACING, newFacing));
                    }
                }

                var bottom = pos.below();
                var bottomState = level.getBlockState(bottom);

                if (bottomState.is(ModBlocks.MODULAR_CARRIER)) {
                    var v = bottomState.getValue(BlockStateProperties.HORIZONTAL_FACING);
                    if (v != newFacing) {
                        tlevel.setBlockAndUpdate(bottom, bottomState.setValue(BlockStateProperties.HORIZONTAL_FACING, newFacing));
                    }
                }
            }
        }
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .front("blockhelp.autowork.modular_carrier.front")
                .details("blockhelp.autowork.modular_carrier.details")
                .direction("blockhelp.autowork.modular_carrier.top", Direction.UP)
                .direction("blockhelp.autowork.modular_carrier.bottom", Direction.DOWN)
                .multiblock()
                .build();
    }
}
