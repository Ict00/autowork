package org.prism.autowork.block.fan;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.other.ModUtils;

import java.util.List;

public class FanBlock extends Block implements BlockHelpProvider {
    // 0 - off
    // 1 - outward
    // 2 - inward
    public static final IntegerProperty WIND_STATE = IntegerProperty.create("wind_state", 0, 2);
    public static final DirectionProperty FACING = DirectionProperty.create("facing");


    public FanBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        level.scheduleTick(pos, asBlock(), 1);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        level.setBlockAndUpdate(pos, state.setValue(WIND_STATE, state.getValue(WIND_STATE) == 1 ? 2 : 1));

        level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1f, 1.4f);

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        var signal = level.getBestNeighborSignal(pos);

        if (signal == 0) {
            if (state.getValue(WIND_STATE) != 0) {
                level.setBlockAndUpdate(pos, state.setValue(WIND_STATE, 0));
            }
            level.scheduleTick(pos, asBlock(), 5);
            return;
        }
        else {
            if (state.getValue(WIND_STATE) == 0) {
                level.setBlockAndUpdate(pos, state.setValue(WIND_STATE, 1));
            }
        }

        var dir = ModUtils.direction2vec(state.getValue(FACING));
        var power = ModUtils.vecMultiply(dir, signal);

        var aBlock = ModUtils.blockPosVec(pos).add(0.5, 0.5, 0.5);
        var bBlock = power.add(aBlock);

        var t = aBlock.add(dir);

        var hit = level.clip(
                new ClipContext(
                        t,
                        bBlock,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        CollisionContext.empty()
                )
        );

        if (hit.getType() == HitResult.Type.BLOCK) {
            bBlock = hit.getLocation();
        }

        var aabb = new AABB(aBlock, bBlock).inflate(0.5);

        List<Entity> entities = level.getEntities(
                null,
                aabb
        );


        power = ModUtils.vecMultiply(power, 0.01);

        if (state.getValue(WIND_STATE) == 2) {
            power = ModUtils.vecMultiply(power, -1);
        }

        for (var entity : entities) {
            if (entity instanceof Player player && player.isCrouching()) {
                continue;
            }
            entity.addDeltaMovement(power);
            entity.hurtMarked = true;
        }

        level.scheduleTick(pos, asBlock(), 1);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WIND_STATE);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite()).setValue(WIND_STATE, 0);
    }


    @Override
    public void animateTick(
            BlockState state,
            Level level,
            BlockPos pos,
            RandomSource random
    ) {

        int windState = state.getValue(WIND_STATE);

        if (windState == 0) {
            return;
        }

        int signal = level.getBestNeighborSignal(pos);

        if (signal <= 0) {
            return;
        }

        Direction facing = state.getValue(FACING);

        Vec3 dir = new Vec3(
                facing.getStepX(),
                facing.getStepY(),
                facing.getStepZ()
        );

        Vec3 center = Vec3.atCenterOf(pos);

        Vec3 start = center.add(dir);

        Vec3 end = start.add(dir.scale(signal));

        BlockHitResult hit = level.clip(
                new ClipContext(
                        start,
                        end,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        CollisionContext.empty()
                )
        );

        if (hit.getType() == HitResult.Type.BLOCK) {
            end = hit.getLocation();
        }

        double length = start.distanceTo(end);

        int particleCount = Math.max(2, signal);

        for (int i = 0; i < particleCount; i++) {

            double progress =
                    random.nextDouble() * length;

            Vec3 particlePos =
                    start.add(dir.scale(progress));

            double spread = 0.3;

            particlePos = particlePos.add(
                    (random.nextDouble() - 0.5) * spread,
                    (random.nextDouble() - 0.5) * spread,
                    (random.nextDouble() - 0.5) * spread
            );

            Vec3 velocity;

            if (windState == 1) {
                // outward
                velocity = dir.scale(0.1);
            }
            else {
                // inward
                velocity = dir.scale(-0.1);
            }

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
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .front("blockhelp.autowork.fan.front")
                .details("blockhelp.autowork.fan.details")
                .only_when_powered()
                .other("blockhelp.autowork.fan.addition")
                .power_depends_on_signal()
                .build();
    }
}
