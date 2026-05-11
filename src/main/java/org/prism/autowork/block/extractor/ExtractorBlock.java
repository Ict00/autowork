package org.prism.autowork.block.extractor;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.other.ModUtils;

public class ExtractorBlock extends Block implements BlockHelpProvider {
    public static final DirectionProperty FACING = DirectionProperty.create("facing");
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public ExtractorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos p_60513_, boolean p_60514_) {
        super.neighborChanged(state, level, pos, block, p_60513_, p_60514_);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        level.scheduleTick(pos, asBlock(), 10);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        if (!state.getValue(POWERED) && level.hasNeighborSignal(pos)) {
            var face = state.getValue(FACING);
            var backFace = state.getValue(FACING).getOpposite();

            var front = ModUtils.lookTo(pos, face);
            var back = ModUtils.lookTo(pos, backFace);

            var capB = level.getCapability(Capabilities.ItemHandler.BLOCK, front, backFace);
            var capA = level.getCapability(Capabilities.ItemHandler.BLOCK, back, face);


            if (capA != null && capB != null) {
                for (int i = 0; i < capA.getSlots(); i++) {
                    var stack = capA.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        var simExtracted = capA.extractItem(i, 4, true);
                        if (simExtracted.isEmpty()) continue;

                        var simRemaining = ItemHandlerHelper.insertItemStacked(capB, simExtracted, true);
                        int canMove = simExtracted.getCount() - simRemaining.getCount();
                        if (canMove <= 0) continue;

                        var extracted = capA.extractItem(i, canMove, false);
                        ItemHandlerHelper.insertItemStacked(capB, extracted, false);

                        level.setBlockAndUpdate(pos, state.setValue(POWERED, true));
                        level.playSound(null, pos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS);
                        return;
                    }
                }
            }
            level.scheduleTick(pos, asBlock(), 10);
        }
        else {
            if (state.getValue(POWERED)) {
                level.setBlockAndUpdate(pos, state.setValue(POWERED, false));
            }
            else {
                level.scheduleTick(pos, asBlock(), 10);
            }
        }
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
                .storage_required_front()
                .storage_required_back()
                .details("blockhelp.autowork.extractor.details")
                .no_storage()
                .only_when_powered()
                .build();
    }
}
