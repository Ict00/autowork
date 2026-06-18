package org.prism.autowork.block.redstone_hub;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.blockhelp.WarnLevel;
import org.prism.autowork.blockhelp.WarningProvider;
import org.prism.autowork.other.ModUtils;

import java.util.concurrent.atomic.AtomicBoolean;

public class RedstoneHubBlock extends Block implements BlockHelpProvider, WarningProvider {

    public static final EnumProperty<HubState> HUB_STATE = EnumProperty.create("state", HubState.class);
    public static final EnumProperty<RedstoneState> REDSTONE_STATE = EnumProperty.create("redstone", RedstoneState.class);

    public RedstoneHubBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block b, BlockPos cB, boolean p_60514_) {
        super.neighborChanged(state, level, pos, b, cB, p_60514_);

        if (level.hasNeighborSignal(pos) != state.getValue(BlockStateProperties.POWERED) && !level.getBlockState(cB).is(ModBlocks.REDSTONE_HUB_BLOCK) && state.getValue(REDSTONE_STATE).isInput()) {
            recalculateOthersState(level, pos, state);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) {
            recalculateState(level, pos, state);
            var ns = level.getBlockState(pos);
            if (ns.getValue(BlockStateProperties.POWERED) != level.hasNeighborSignal(pos) && state.getValue(REDSTONE_STATE).isInput()) {
                recalculateOthersState(level, pos, ns);
            }
            level.scheduleTick(pos, asBlock(), 20);
        }
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return !state.getValue(REDSTONE_STATE).isInput();
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        var facing = state.getValue(BlockStateProperties.FACING);
        var opp = facing.getOpposite();
        return !state.getValue(REDSTONE_STATE).isOutput() ? 0 : state.getValue(BlockStateProperties.POWERED) && (state.getValue(HUB_STATE) != HubState.MIDDLE || direction != facing && direction != opp) ? 15 : 0;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        recalculateState(level, pos, state);

        level.scheduleTick(pos, asBlock(), 1);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player p_316132_, InteractionHand p_316595_, BlockHitResult hit) {
        if (!level.isClientSide) {
            var a = state.getValue(REDSTONE_STATE);
            BlockState st = switch (a) {
                case NONE -> state.setValue(REDSTONE_STATE, RedstoneState.OUTPUT);
                case INPUT -> state.setValue(REDSTONE_STATE, RedstoneState.NONE);
                case OUTPUT -> state.setValue(REDSTONE_STATE, RedstoneState.INPUT);
            };

            level.setBlockAndUpdate(pos, st);
            recalculateOthersState(level, pos, st);
        }

        return ItemInteractionResult.SUCCESS;
    }

    protected void recalculateOthersState(Level level, BlockPos pos, BlockState state) {
        AtomicBoolean powered = new AtomicBoolean(level.hasNeighborSignal(pos));

        if (state.getValue(HUB_STATE) == HubState.SINGLE) {
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, powered.get()));
            return;
        }

        var mutPos = pos.mutable();
        var facing = state.getValue(BlockStateProperties.FACING);
        var opposite = facing.getOpposite();

        if (state.getValue(HUB_STATE) != HubState.START) {
            for (; ; ) {
                mutPos = mutPos.move(opposite);
                var st = level.getBlockState(mutPos);


                if (!st.is(ModBlocks.REDSTONE_HUB_BLOCK)) {
                    return;
                }

                if (st.getValue(HUB_STATE) == HubState.START) {
                    break;
                }
            }
        }

        var mutCopy = mutPos.immutable().mutable();

        for (;;) {
            var st = level.getBlockState(mutCopy);
            if (st.getValue(REDSTONE_STATE).isInput()) {
                powered.set(powered.get() || level.hasNeighborSignal(mutCopy));
            }
            if (st.getValue(HUB_STATE) == HubState.END) {
                break;
            }
            mutCopy = mutCopy.move(facing);
        }

        for (;;) {
            var st = level.getBlockState(mutPos);
            var nt = st.setValue(BlockStateProperties.POWERED, powered.get());
            if (!st.equals(nt)) {
                level.setBlockAndUpdate(mutPos, nt);
            }

            if (st.getValue(HUB_STATE) == HubState.END) {
                break;
            }
            mutPos = mutPos.move(facing);
        }
    }
    protected boolean isConnected(Level level, BlockPos pos, Direction self) {
        var s = level.getBlockState(pos);
        return s.is(ModBlocks.REDSTONE_HUB_BLOCK) && s.getValue(BlockStateProperties.FACING) == self;
    }

    protected void recalculateState(Level level, BlockPos pos, BlockState state) {
        BlockState newState;
        var facing = state.getValue(BlockStateProperties.FACING);
        var opposite = facing.getOpposite();

        var back = ModUtils.lookTo(pos, opposite);
        var front = ModUtils.lookTo(pos, facing);

        if (isConnected(level, back, facing) &&
                isConnected(level, front, facing)) {
            newState = state.setValue(HUB_STATE, HubState.MIDDLE);
        }
        else if (isConnected(level, back, facing)) {
            newState = state.setValue(HUB_STATE, HubState.END);
        }
        else if (isConnected(level, front, facing)) {
            newState = state.setValue(HUB_STATE, HubState.START);
        }
        else {
            newState = state.setValue(HUB_STATE, HubState.SINGLE);
        }
        if (!state.equals(newState)) {
            level.setBlockAndUpdate(pos, newState);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HUB_STATE, REDSTONE_STATE, BlockStateProperties.FACING, BlockStateProperties.POWERED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        var level = context.getLevel();
        var clicked = context.getClickedPos();
        var dir = context.getClickedFace();

        Direction endDir = context.getNearestLookingDirection().getOpposite();

        var behind = level.getBlockState(ModUtils.lookTo(clicked, dir.getOpposite()));
        if (behind.is(ModBlocks.REDSTONE_HUB_BLOCK)) {
            endDir = behind.getValue(BlockStateProperties.FACING);
        }

        return this.defaultBlockState().setValue(BlockStateProperties.POWERED, false)
                .setValue(REDSTONE_STATE, RedstoneState.OUTPUT)
                .setValue(HUB_STATE, HubState.SINGLE)
                .setValue(BlockStateProperties.FACING, endDir);

    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .side("blockhelp.autowork.redstone_hub.side")
                .front("blockhelp.autowork.redstone_hub.front")
                .details("blockhelp.autowork.redstone_hub.details")
                .multiblock()
                .other("blockhelp.autowork.redstone_hub.none_mode")
                .other("blockhelp.autowork.redstone_hub.input_mode")
                .other("blockhelp.autowork.redstone_hub.output_mode")
                .build();
    }

    @Override
    public WarnLevel warnLevel() {
        return WarnLevel.CRITICAL;
    }

    @Override
    public String getWarning() {
        return "blockhelp.warn.critical";
    }


    public enum RedstoneState implements StringRepresentable {
        OUTPUT("output"),
        INPUT("input"),
        NONE("none");

        private final String name;

        RedstoneState(String name) {
            this.name = name;
        }

        public boolean isInput() {
            return this == INPUT;
        }

        public boolean isOutput() {
            return this == OUTPUT;
        }

        public boolean isNone() {
            return this == NONE;
        }

        public String toString() {
            return this.name;
        }
        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public enum HubState implements StringRepresentable {
        SINGLE("single"),
        MIDDLE("middle"),
        START("start"),
        END("end");

        private final String name;

        HubState(String name) {
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
