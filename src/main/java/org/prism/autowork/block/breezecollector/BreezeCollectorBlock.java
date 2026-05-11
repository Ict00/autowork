package org.prism.autowork.block.breezecollector;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.other.ModUtils;

import java.util.List;

public class BreezeCollectorBlock extends Block implements BlockHelpProvider {
    private static final int AABB_SIDE = 4;

    public static final DirectionProperty FACING = DirectionProperty.create("facing");
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public BreezeCollectorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        boolean schedule = true;

        if (level.hasNeighborSignal(pos)) {
            if (!state.getValue(POWERED)) {
                level.setBlockAndUpdate(pos, state.setValue(POWERED, true));
                schedule = false;
            }
        }
        else {
            if (state.getValue(POWERED)) {
                level.setBlockAndUpdate(pos, state.setValue(POWERED, false));
            }
            level.scheduleTick(pos, asBlock(), 2);
            return;
        }

        var facing = state.getValue(FACING);

        var back = ModUtils.lookTo(pos, facing.getOpposite());
        var container = level.getCapability(Capabilities.ItemHandler.BLOCK, back, facing);

        if (container == null) {
            if (schedule) {
                level.scheduleTick(pos, asBlock(), 2);
            }
            return;
        }

        var face = ModUtils.direction2vec(facing);
        var aabbAdd = ModUtils.vecMultiply(face, AABB_SIDE/2.0);

        var aabb = new AABB(pos);
        aabb = aabb.inflate(AABB_SIDE).move(aabbAdd.add(aabbAdd));

        List<ItemEntity> entities = level.getEntitiesOfClass(
                ItemEntity.class,
                aabb,
                (et) -> {
                    return et instanceof ItemEntity;
                }
        );

        var start = ModUtils.blockPosVec(ModUtils.lookTo(pos, facing));

        for (int i = 0; i < entities.size(); i++) {
            var entity = entities.get(i);
            var position = entity.position();

            if (Math.sqrt(entity.distanceToSqr(start)) <= 1.5f) {
                var remains = ItemHandlerHelper.insertItem(container, entity.getItem(), false);

                if (!remains.isEmpty()) {
                    entity.setItem(remains);
                }
                else {
                    level.playSound(null, pos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS);
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }

                continue;
            }

            var delta = ModUtils.vecDivide(position.subtract(start), 2.5);
            delta = ModUtils.vecDivide(delta, 10);

            entity.addDeltaMovement(delta.multiply(-1,-1,-1));
            entity.hurtMarked = true;
        }

        if (schedule) {
            level.scheduleTick(pos, asBlock(), 2);
        }
    }

    @Override
    public void animateTick(
            BlockState state,
            Level level,
            BlockPos pos,
            RandomSource random
    ) {

        if (!state.getValue(POWERED)) {
            return;
        }

        Direction facing = state.getValue(FACING);

        Vec3 front = Vec3.atCenterOf(pos)
                .add(
                        facing.getStepX(),
                        facing.getStepY(),
                        facing.getStepZ()
                );

        for (int i = 0; i < 9; i++) {

            double spread = AABB_SIDE*2;

            double x =
                    front.x + (random.nextDouble() - 0.5) * spread;

            double y =
                    front.y + (random.nextDouble() - 0.5) * spread;

            double z =
                    front.z + (random.nextDouble() - 0.5) * spread;

            Vec3 particlePos = new Vec3(x, y, z);

            Vec3 velocity =
                    front.subtract(particlePos)
                            .normalize()
                            .scale(0.1);

            level.addParticle(
                    ParticleTypes.CLOUD,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    velocity.x,
                    velocity.y,
                    velocity.z
            );
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        level.scheduleTick(pos, asBlock(), 2);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(POWERED, false).setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .front("blockhelp.autowork.breeze_collector.front")
                .storage_required_back()
                .details("blockhelp.autowork.breeze_collector.details")
                .no_storage()
                .only_when_powered()
                .build();
    }
}
