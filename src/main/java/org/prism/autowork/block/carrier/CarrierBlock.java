package org.prism.autowork.block.carrier;

import net.minecraft.core.BlockPos;
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

public class CarrierBlock extends Block implements BlockHelpProvider {
    public CarrierBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.POWERED, BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.POWERED, false).setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block _block, BlockPos other_pos, boolean p_60514_) {
        super.neighborChanged(state, level, pos, _block, other_pos, p_60514_);

        if (level instanceof ServerLevel level1) {
            recalculateState(pos, state, level1);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level tlevel, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, tlevel, pos, oldState, movedByPiston);

        if (oldState.is(ModBlocks.CARRIER) && tlevel instanceof ServerLevel level) {
            var oldFacing = oldState.getValue(BlockStateProperties.HORIZONTAL_FACING);
            var newFacing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

            if (oldFacing != newFacing && state.getValue(BlockStateProperties.POWERED)) {
                var oldFrontPos = ModUtils.lookTo(pos, oldFacing);
                var newFrontPos = ModUtils.lookTo(pos, newFacing);

                if (level.getBlockState(oldFrontPos).is(ModOther.CARRIER_IMMOVABLE)) {
                    return;
                }


                var res = BlocksAbstractLogic.abstractMover(level, oldFrontPos, newFrontPos);

                if (res) {
                    level.playSound(null, pos, SoundEvents.VAULT_EJECT_ITEM, SoundSource.BLOCKS, 1, 1.5f);
                    level.sendParticles(ParticleTypes.POOF, oldFrontPos.getX()+0.5, oldFrontPos.getY()+0.5, oldFrontPos.getZ()+0.5, 10, 0.5, 0.5 , 0.5, 0);
                }
            }
        }
    }

    protected void stick(BlockPos pos, ServerLevel level) {
        level.playSound(null, pos, SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 1, 1.5f);
        level.sendParticles(ParticleTypes.ITEM_SLIME, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, 10, 0.5, 0.5, 0.5, 0);
    }

    protected void recalculateState(BlockPos pos, BlockState state, ServerLevel level) {
        var isPowered = state.getValue(BlockStateProperties.POWERED);
        boolean changedSomething = false;
        if (level.hasNeighborSignal(pos) && !isPowered) {
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, true));
            changedSomething = true;
        }
        else if (!level.hasNeighborSignal(pos) && isPowered) {
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, false));
            changedSomething = true;
        }

        if (changedSomething) {
            var facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            var front = ModUtils.lookTo(pos, facing);

            if (BlocksAbstractLogic.checkMovable(level.getBlockState(front), level, front)) {
                stick(pos, level);
            }
        }
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .front("blockhelp.autowork.carrier.front")
                .details("blockhelp.autowork.carrier.details")
                .only_when_powered()
                .build();
    }
}
