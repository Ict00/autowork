package org.prism.autowork.block.railwayobserver;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.cartloader.CartHelper;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.other.ModUtils;

import java.util.List;

public class RailwayObserverBlock extends Block implements BlockHelpProvider {
    public static final DirectionProperty FACING = DirectionProperty.create("facing");
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public static final EnumProperty<EasterState> EASTER_STATE = EnumProperty.<EasterState>create("easter", EasterState.class);
    public RailwayObserverBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        level.scheduleTick(pos, asBlock(), 2);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        var facing = state.getValue(FACING);
        var front = ModUtils.lookTo(pos, facing);

        if (!level.getBlockState(front).is(BlockTags.RAILS)) {
            level.scheduleTick(pos, asBlock(), 10);
            return;
        }

        var isPowered = state.getValue(POWERED);

        var filter = CartHelper.getCartName(level, pos, facing);

        if (filter != null) {
            switch (filter) {
                case "34ray_":
                    if (state.getValue(EASTER_STATE) != EasterState.RAY) {
                        level.setBlockAndUpdate(pos, state.setValue(EASTER_STATE, EasterState.RAY));
                        return;
                    }
                    break;
                case "Sumber":
                    if (state.getValue(EASTER_STATE) != EasterState.SUMBER) {
                        level.setBlockAndUpdate(pos, state.setValue(EASTER_STATE, EasterState.SUMBER));
                        return;
                    }
                    break;
                case "Mr_Yuzi":
                    if (state.getValue(EASTER_STATE) != EasterState.YUZI) {
                        level.setBlockAndUpdate(pos, state.setValue(EASTER_STATE, EasterState.YUZI));
                        return;
                    }
                    break;
                case "_ict__":
                    if (state.getValue(EASTER_STATE) != EasterState.ICT) {
                        level.setBlockAndUpdate(pos, state.setValue(EASTER_STATE, EasterState.ICT));
                        return;
                    }
                    break;
                default:
                    if (state.getValue(EASTER_STATE) != EasterState.NONE) {
                        level.setBlockAndUpdate(pos, state.setValue(EASTER_STATE, EasterState.NONE));
                        return;
                    }
                    break;
            }
        }
        else {
                if (state.getValue(EASTER_STATE) != EasterState.NONE) {
                level.setBlockAndUpdate(pos, state.setValue(EASTER_STATE, EasterState.NONE));
                return;
            }
        }

        var aabb = new AABB(front).inflate(0.2);
        List<AbstractMinecart> entities = level.getEntitiesOfClass(
                AbstractMinecart.class,
                aabb,
                (et) -> et instanceof AbstractMinecart
        );

        if (!entities.isEmpty() && filter == null) {
            if (!isPowered) {
                level.setBlockAndUpdate(pos, state.setValue(POWERED, true));
            }
            else {
                level.scheduleTick(pos, asBlock(), 2);
            }
            return;
        }

        if (entities.isEmpty()) {
            if (isPowered) {
                level.setBlockAndUpdate(pos, state.setValue(POWERED, false));
                return;
            }
            level.scheduleTick(pos, asBlock(), 2);
            return;
        }


        for (int i = 0; i < entities.size(); i++) {
            var entity = entities.get(i);

            if (entity.getCustomName() == null) {
                continue;
            }
            if (!entity.getCustomName().getString().equals(filter)) {
                continue;
            }
            if (!isPowered) {
                level.setBlockAndUpdate(pos, state.setValue(POWERED, true));
                return;
            }
            else {
                level.scheduleTick(pos, asBlock(), 2);
                return;
            }
        }
        if (isPowered) {
            level.setBlockAndUpdate(pos, state.setValue(POWERED, false));
            return;
        }

        level.scheduleTick(pos, asBlock(), 2);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    protected int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockState.getValue(POWERED) && side == Direction.DOWN ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (state.getValue(POWERED) && direction == Direction.DOWN) {
            return 15;
        }

        return 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, EASTER_STATE);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(POWERED, false).setValue(EASTER_STATE, EasterState.NONE).setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    public enum EasterState implements StringRepresentable {
        ICT("ict"),
        YUZI("yuzi"),
        SUMBER("sumber"),
        RAY("ray"),
        NONE("none");

        private final String name;

        private EasterState(String name) {
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

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .direction("blockhelp.autowork.railway.top", Direction.UP)
                .details("blockhelp.autowork.railway.details")
                .configurable_by_sign()
                .build();
    }
}
