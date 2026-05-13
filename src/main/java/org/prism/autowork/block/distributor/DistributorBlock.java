package org.prism.autowork.block.distributor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.prism.autowork.CommonConfig;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.other.ModUtils;

import java.util.List;

public class DistributorBlock extends Block implements BlockHelpProvider {
    public static final BooleanProperty MASTER = BooleanProperty.create("master");
    public static final DirectionProperty FACING = DirectionProperty.create("facing");


    public DistributorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        if (state.getValue(MASTER)) {
            var slaveSeekResult = getSlaves(pos, level, state.getValue(FACING));

            if (slaveSeekResult.metMaster) {
                for (var slave : slaveSeekResult.slaves) {
                    if (slave.isMaster) {
                        level.setBlockAndUpdate(slave.pos, slave.state.setValue(MASTER, false));
                    }
                }
            }

            slaveSeekResult.slaves.removeIf((x) -> x.handler == null);

            if (slaveSeekResult.slaves.isEmpty()) {
                level.scheduleTick(pos, asBlock(), 10);
                return;
            }

            var cap = level.getCapability(Capabilities.ItemHandler.BLOCK, ModUtils.lookTo(pos, Direction.UP), Direction.DOWN);

            if (cap == null) {
                level.scheduleTick(pos, asBlock(), 10);
                return;
            }

            ItemStack stack = null;
            int stackSlot = -1;

            for (int i = 0; i < cap.getSlots(); i++) {
                var st = cap.getStackInSlot(i);
                if (!st.isEmpty()) {
                    var simExtract = cap.extractItem(i, st.getCount(), true);
                    if (!simExtract.isEmpty()) {
                        stack = simExtract;
                        stackSlot = i;
                    }
                    break;
                }
            }

            if (stack == null || stackSlot == -1) {
                level.scheduleTick(pos, asBlock(), 10);
                return;
            }

            var countPerSlave = stack.getCount() / slaveSeekResult.slaves.size();
            var leftover = stack.getCount() - (countPerSlave * slaveSeekResult.slaves.size());

            if (countPerSlave == 0) {
                var sl = slaveSeekResult.slaves.getFirst();
                var fSCap = sl.handler;

                var sim = ItemHandlerHelper.insertItemStacked(fSCap, stack, true);
                int canInsert = stack.getCount() - sim.getCount();
                if (canInsert <= 0) {
                    level.scheduleTick(pos, asBlock(), 10);
                    return;
                }

                var toInsert = stack.copy();
                toInsert.setCount(canInsert);
                cap.extractItem(stackSlot, canInsert, false);
                ItemHandlerHelper.insertItemStacked(fSCap, toInsert, false);

                level.scheduleTick(pos, asBlock(), 10);
                return;
            }

            var nStack = stack.copy();
            nStack.setCount(countPerSlave);

            int totalToExtract = 0;
            for (var slave : slaveSeekResult.slaves()) {
                var slaveCap = slave.handler;

                if (leftover > 0) {
                    var bonusCopy = stack.copy();
                    bonusCopy.setCount(leftover);
                    var sim = ItemHandlerHelper.insertItemStacked(slaveCap, bonusCopy, true);
                    totalToExtract += leftover - sim.getCount();
                }

                var sim = ItemHandlerHelper.insertItemStacked(slaveCap, nStack, true);
                totalToExtract += countPerSlave - sim.getCount();
            }

            if (totalToExtract <= 0) {
                level.scheduleTick(pos, asBlock(), 10);
                return;
            }

            cap.extractItem(stackSlot, totalToExtract, false);

            for (var slave : slaveSeekResult.slaves()) {
                var slaveCap = slave.handler;

                if (leftover > 0) {
                    var copy = stack.copy();
                    copy.setCount(leftover);
                    var remains = ItemHandlerHelper.insertItemStacked(slaveCap, copy, false);
                    leftover = remains.getCount();
                }

                ItemHandlerHelper.insertItemStacked(slaveCap, nStack.copy(), false);
            }
            level.scheduleTick(pos, asBlock(), 10);
        }

    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if (!level.isClientSide) {
            level.scheduleTick(pos, asBlock(), 5);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(MASTER, true).setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MASTER, FACING);
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .front("blockhelp.autowork.distributor.master.front")
                .front("blockhelp.autowork.distributor.node.front")
                .storage_required(Direction.UP)
                .details("blockhelp.autowork.distributor.details")
                .no_storage()
                .multiblock()
                .other("blockhelp.autowork.distributor.addition")
                .build();
    }


    private static SlaveSeekResult getSlaves(BlockPos master, Level level, Direction direction) {
        BlockPos cur = ModUtils.lookTo(master, direction);
        NonNullList<DistributorSlave> results = NonNullList.create();
        boolean metMaster = false;
        Direction oldFacing = null;

        for (int i = 0; i < CommonConfig.DISTRIBUTOR_RANGE.getAsInt(); i++) {

            var curState = level.getBlockState(cur);
            if (!curState.is(ModBlocks.DISTRIBUTOR)) {
                break;
            }
            var isMaster = curState.getValue(MASTER).booleanValue();
            var facing = curState.getValue(FACING);

            if (isMaster) {
                metMaster = true;
            }

            var newPos = ModUtils.lookTo(cur, direction);

            if (i != 0) {
                if (oldFacing != facing) {
                    break;
                }
            }

            var tryCap = level.getCapability(Capabilities.ItemHandler.BLOCK, ModUtils.lookTo(cur, facing), facing.getOpposite());

            if (tryCap != null) {
                results.addLast(new DistributorSlave(curState, cur, facing, tryCap, isMaster));
            }
            else {
                results.addLast(new DistributorSlave(curState, cur, facing, null, isMaster));
            }

            oldFacing = facing;
            cur = newPos;
        }

        return new SlaveSeekResult(metMaster, results);
    }

    record SlaveSeekResult(boolean metMaster, List<DistributorSlave> slaves) { }
    record DistributorSlave(BlockState state, BlockPos pos, Direction facing, IItemHandler handler, boolean isMaster) { }
}
