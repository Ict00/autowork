package org.prism.autowork.block.controlled_catalyst;

import net.minecraft.client.particle.SculkChargeParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SculkChargeParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.other.capability.IHaveExperience;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ControlledCatalystBlock extends Block implements BlockHelpProvider {
    public ControlledCatalystBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if (!level.isClientSide) {
            level.scheduleTick(pos, asBlock(), 5);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.POWERED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.POWERED, false);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        if (level.hasNeighborSignal(pos) && !state.getValue(BlockStateProperties.POWERED)) {
            var below = pos.below();

            if (level.getBlockEntity(below) instanceof IHaveExperience exp) {

                var aabb = new AABB(pos).inflate(4);
                List<ExperienceOrb> entities = level.getEntitiesOfClass(
                        ExperienceOrb.class,
                        aabb,
                        (et) -> et instanceof ExperienceOrb
                );

                AtomicBoolean anyOrb = new AtomicBoolean(false);

                for (var entity : entities) {
                    var val = entity.getValue();
                    var t = exp.putXp(val, true);

                    if (t != 0) {
                        entity.remove(Entity.RemovalReason.CHANGED_DIMENSION);
                        anyOrb.set(true);
                        exp.putXp(t, false);
                        level.sendParticles(new SculkChargeParticleOptions(0), entity.getX(), entity.getY(), entity.getZ(), 2, 0.5, 0.5, 0.5, 0);
                    } else {
                        break;
                    }
                }

                if (anyOrb.get()) {
                    level.playSound(null, pos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 1, 1.5f);
                    level.sendParticles(new SculkChargeParticleOptions(0), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0);
                    level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, true));
                }
            }
        }
        else if (state.getValue(BlockStateProperties.POWERED)) {
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, false));
        }

        level.scheduleTick(pos, asBlock(), 5);
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .details("blockhelp.autowork.controlled_catalyst.details")
                .direction("blockhelp.autowork.controlled_catalyst.bottom", Direction.DOWN)
                .only_when_powered()
                .build();
    }
}
