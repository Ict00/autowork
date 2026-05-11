package org.prism.autowork.block.chute;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.block.filterchute.FilterChuteBlock;
import org.prism.autowork.block.filterchute.FilterChuteBlockEntity;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;

import javax.annotation.Nullable;
import java.util.List;

public class ChuteBlock extends Block implements BlockHelpProvider {
    public static final BooleanProperty UPPER = BooleanProperty.create("upper");
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public ChuteBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if (level.getBlockState(pos.above()).is(ModBlocks.CHUTES)) {
            level.setBlockAndUpdate(pos, state.setValue(UPPER, false));
            return;
        }

        level.scheduleTick(pos, asBlock(), 5);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block p_60512_, BlockPos p_60513_, boolean p_60514_) {
        super.neighborChanged(state, level, pos, p_60512_, p_60513_, p_60514_);
        level.scheduleTick(pos, asBlock(), 5);

        if (!level.getBlockState(pos.above()).is(ModBlocks.CHUTES)) {
            level.setBlockAndUpdate(pos, state.setValue(UPPER, true));
        }
        else {
            if (state.getValue(UPPER)) {
                level.setBlockAndUpdate(pos, state.setValue(UPPER, false).setValue(POWERED, level.hasNeighborSignal(pos)));
            }
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        if (!level.getBlockState(pos).getValue(UPPER)) {
            return;
        }

        if (level.hasNeighborSignal(pos)) {
            if (!state.getValue(POWERED)) {
                level.setBlockAndUpdate(pos, state.setValue(POWERED, true));
            }
            level.scheduleTick(pos, asBlock(), 5);
            return;
        }
        else {
            if (state.getValue(POWERED)) {
                level.setBlockAndUpdate(pos, state.setValue(POWERED, false));
            }
        }

        var capTop = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.above(), Direction.DOWN);

        if (capTop == null) {
            level.scheduleTick(pos, asBlock(), 5);
            return;
        }
        var seek = getEnd(level, pos);

        var end = seek.pos;

        if (end != null) {

            var capBottom = seek.cap;

            if (capBottom != null) {
                for (int i = 0; i < capTop.getSlots(); i++) {
                    var stack = capTop.getStackInSlot(i);

                    if (!stack.isEmpty() && seek.test(stack)) {
                        var simulatedExtract = capTop.extractItem(i, 4, true);
                        if (simulatedExtract.isEmpty()) break;

                        var simulatedRemaining = ItemHandlerHelper.insertItemStacked(capBottom, simulatedExtract, true);

                        int canMove = simulatedExtract.getCount() - simulatedRemaining.getCount();
                        if (canMove <= 0) break;

                        var extracted = capTop.extractItem(i, canMove, false);
                        ItemHandlerHelper.insertItemStacked(capBottom, extracted, false);
                        break;
                    }
                }
            }
            else {
                for (int i = 0; i < capTop.getSlots(); i++) {
                    var stack = capTop.getStackInSlot(i);

                    if (!stack.isEmpty() && seek.test(stack)) {
                        var simulatedExtract = capTop.extractItem(i, 4, true);
                        if (simulatedExtract.isEmpty()) break;

                        var extracted = capTop.extractItem(i, simulatedExtract.getCount(), false);
                        var itemEntity = new ItemEntity(
                                level,
                                end.getX() + 0.5,
                                end.getY() + 0.5,
                                end.getZ() + 0.5,
                                extracted, 0, 0, 0);
                        level.addFreshEntity(itemEntity);
                        break;
                    }
                }
            }
        }

        level.scheduleTick(pos, asBlock(), 5);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, UPPER);
    }

    static ChuteSeekResult getEnd(ServerLevel level, BlockPos pos) {
        var below = level.getBlockState(pos.below());
        if (below.is(ModBlocks.CHUTES)) {

            if (level.getBlockEntity(pos.below()) instanceof FilterChuteBlockEntity be) {
                var blackList = below.getValue(FilterChuteBlock.BLACKLIST);
                var filter = be.getFilterItem();

                var seek = getEnd(level, pos.below());
                var filterObj = new ChuteFilter(filter, blackList);
                seek.filter.add(filterObj);

                return seek;
            }

            return getEnd(level, pos.below());
        }

        if (below.is(BlockTags.AIR)) {
            return new ChuteSeekResult(NonNullList.create(), pos.below(), null);
        }

        var cap = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.below(), Direction.UP);

        if (cap != null) {
            return new ChuteSeekResult(NonNullList.create(), pos.below(), cap);
        }

        return new ChuteSeekResult(NonNullList.create(), null, null);
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .storage_required(Direction.UP)
                .direction("blockhelp.autowork.chute.bottom", Direction.DOWN)
                .details("blockhelp.autowork.chute.details")
                .no_storage()
                .multiblock()
                .only_when_unpowered()
                .build();
    }


    record ChuteFilter(ItemStack filter, boolean blacklist) {

    }

    record ChuteSeekResult(List<ChuteFilter> filter, @Nullable BlockPos pos, @Nullable IItemHandler cap) {
        public boolean test(ItemStack other) {
            for (var test : filter) {
                if (test.filter.isEmpty() && test.blacklist) {
                    return false;
                }

                if (other.is(test.filter.getItem())) {
                    if (test.blacklist) {
                        return false;
                    }
                }
                else {
                    if (!test.blacklist) {
                        return false;
                    }
                }
            }

            return true;
        }
    }
}
