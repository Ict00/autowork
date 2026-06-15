package org.prism.autowork.block.sculkmover;

import net.minecraft.client.particle.SculkChargeParticle;
import net.minecraft.client.particle.ShriekParticle;
import net.minecraft.client.particle.SonicBoomParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SculkChargeParticleOptions;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.CommonConfig;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.other.ModOther;
import org.prism.autowork.other.ModUtils;

import java.util.ArrayList;

public class SculkMoverBlock extends Block implements BlockHelpProvider {
    public SculkMoverBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING, BlockStateProperties.POWERED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.POWERED, false).setValue(BlockStateProperties.FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if (!level.isClientSide) {
            level.scheduleTick(pos, asBlock(), 20);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        ArrayList<BlockPos> poses = new ArrayList<>();

        if (level.hasNeighborSignal(pos) && !state.getValue(BlockStateProperties.POWERED)) {
            var facing = state.getValue(BlockStateProperties.FACING);

            var cPos = ModUtils.lookTo(pos, facing).mutable();
            poses.add(cPos.immutable());

            if (level.getBlockState(cPos).isAir()) {

                for (int i = 0; i < CommonConfig.SCULK_MOVER_RANGE.get(); i++) {
                    cPos.move(facing);

                    var currentState = level.getBlockState(cPos);

                    if (!currentState.canBeReplaced()) {
                        if (currentState.is(Blocks.SCULK)) {
                            break;
                        }

                        CompoundTag tag = null;
                        var access = level.registryAccess();

                        if (level.getBlockEntity(cPos) instanceof BlockEntity be) {
                            if (currentState.is(ModOther.BLOCK_ENTITY_MOVABLE)) {
                                tag = be.saveCustomOnly(access);
                                be.setRemoved();
                            } else {
                                break;
                            }
                        }
                        else {
                            if (currentState.getPistonPushReaction() == PushReaction.DESTROY ||
                            currentState.getPistonPushReaction() == PushReaction.IGNORE ||
                            currentState.getPistonPushReaction() == PushReaction.BLOCK || currentState.getDestroySpeed(level, cPos) == -1) {
                                if (!currentState.is(ModOther.MOVABLE)) {
                                    break;
                                }
                            }
                        }

                        var previousPos = ModUtils.lookTo(cPos, facing.getOpposite());

                        level.setBlock(cPos, Blocks.AIR.defaultBlockState(), 2);
                        level.setBlock(previousPos, currentState, 2);
                        level.sendBlockUpdated(previousPos, currentState, currentState, 3);

                        if (tag != null) {
                            level.getBlockEntity(previousPos).loadWithComponents(tag, access);
                        }

                        for (var pp : poses) {
                            level.sendParticles(ParticleTypes.SONIC_BOOM, pp.getX() + 0.5, pp.getY() + 0.5, pp.getZ() + 0.5, i, 0, 0, 0, 1);
                        }

                        level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, true));
                        level.playSound(null, pos, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.BLOCKS, 1, 2);

                        break;
                    }
                    if (i % 2 == 0) {
                        poses.add(cPos.immutable());
                    }
                }
            }
        }
        else if (state.getValue(BlockStateProperties.POWERED)) {
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, false));
        }

        poses.clear();


        level.scheduleTick(pos, asBlock(), 20);
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .front("blockhelp.autowork.sculk_mover.front")
                .details("blockhelp.autowork.sculk_mover.details")
                .only_when_powered()
                .build();
    }
}
