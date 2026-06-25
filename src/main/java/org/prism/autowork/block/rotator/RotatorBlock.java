package org.prism.autowork.block.rotator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.redstone_hub.RedstoneHubBlock;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.other.ModUtils;

public class RotatorBlock extends Block implements BlockHelpProvider {
    public static final EnumProperty<AngleState> ANGLE_STATE = EnumProperty.create("angle", AngleState.class);
    public static final DirectionProperty FACING = DirectionProperty.create("facing", (x) -> x == Direction.UP || x == Direction.DOWN);

    public RotatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ANGLE_STATE, FACING, BlockStateProperties.POWERED);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if (!level.isClientSide) {
            level.scheduleTick(pos, asBlock(), 15);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        var angle = state.getValue(ANGLE_STATE);

        var newAngle = switch (angle) {
            case _90 -> AngleState._180;
            case _180 -> AngleState._270;
            case _270 -> AngleState._90;
        };

        level.setBlockAndUpdate(pos, state.setValue(ANGLE_STATE, newAngle));
        if (!level.isClientSide) {
            level.playSound(null, pos, SoundEvents.VAULT_CLOSE_SHUTTER, SoundSource.BLOCKS, 1, 1.5f);
        }
        player.displayClientMessage(Component.translatable("block.autowork.rotator.angle").withColor(0x6ffc76).append(Component.literal(newAngle.toString()).withColor(0xe6c845)), true);

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        doTick(state, level, pos);
        level.scheduleTick(pos, asBlock(), 15);
    }

    protected void doTick(BlockState state, ServerLevel level, BlockPos pos) {
        if (!state.getValue(BlockStateProperties.POWERED)) {
            if (!level.hasNeighborSignal(pos)) return;

            var facing = state.getValue(FACING);
            var angle = state.getValue(ANGLE_STATE);
            var front = ModUtils.lookTo(pos, facing);
            var otherState = level.getBlockState(front);

            DirectionProperty prop;

            if (otherState.hasProperty(BlockStateProperties.FACING)) {
                prop = BlockStateProperties.FACING;
            }
            else if (otherState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                prop = BlockStateProperties.HORIZONTAL_FACING;
            }
            else {
                return;
            }

            var dir = otherState.getValue(prop);

            if (facing == Direction.DOWN) {
                dir = ModUtils.tweakedRotate(Direction::getClockWise, Direction::getOpposite, Direction::getCounterClockWise, dir, angle);
            }
            else {
                dir = ModUtils.tweakedRotate(Direction::getCounterClockWise, Direction::getOpposite, Direction::getClockWise, dir, angle);
            }

            level.setBlockAndUpdate(front, otherState.setValue(prop, dir));
            level.playSound(null, pos, SoundEvents.VAULT_ACTIVATE, SoundSource.BLOCKS, 1, 1.5f);
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, true));
            level.sendParticles(ParticleTypes.POOF, pos.getX()+0.5, pos.getY()+1, pos.getZ()+0.5, 10, 0.5, 0.5 , 0.5, 0);
        }
        else {
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, false));
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        var dir = Direction.UP;

        var get = context.getNearestLookingDirection().getOpposite();
        if (get == Direction.DOWN) {
            dir = get;
        }

        return this.defaultBlockState().setValue(ANGLE_STATE, AngleState._90)
                .setValue(BlockStateProperties.POWERED, false)
                .setValue(FACING, dir);
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .front("blockhelp.autowork.rotator.front")
                .details("blockhelp.autowork.rotator.details")
                .only_when_powered()
                .build();
    }

    public enum AngleState implements StringRepresentable {
        _90("90"),
        _180("180"),
        _270("270");

        private final String name;

        AngleState(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
