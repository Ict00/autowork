package org.prism.autowork.block.repair_station;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.block.drill.DrillBlockEntity;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;

public class RepairStationBlock extends BaseEntityBlock implements BlockHelpProvider {
    public static final MapCodec<RepairStationBlock> CODEC = simpleCodec(RepairStationBlock::new);

    public RepairStationBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return  new SimpleMenuProvider((RepairStationBlockEntity)level.getBlockEntity(pos), Component.empty());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            player.openMenu(
                    (MenuProvider) level.getBlockEntity(pos),
                    buf -> buf.writeBlockPos(pos)
            );
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock()) && !movedByPiston) {
            if (level.getBlockEntity(pos) instanceof RepairStationBlockEntity be) {
                for (int i = 0; i < be.handler.getSlots(); i++) {
                    var stack = be.handler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        var newItemEntity = new ItemEntity(level, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, stack);
                        level.addFreshEntity(newItemEntity);
                    }
                }
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block p_60512_, BlockPos p_60513_, boolean p_60514_) {
        super.neighborChanged(state, level, pos, p_60512_, p_60513_, p_60514_);

        if (!state.getValue(BlockStateProperties.POWERED) && level.hasNeighborSignal(pos)) {
            if (level.getBlockEntity(pos) instanceof RepairStationBlockEntity be && !level.isClientSide) {
                be.doTheTrick();
                level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, true));
            }
        }
        else if (state.getValue(BlockStateProperties.POWERED) && !level.hasNeighborSignal(pos)) {
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, false));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.POWERED, BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite()).setValue(BlockStateProperties.POWERED, false);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ModBlockEntities.REPAIR_STATION_BE.get().create(blockPos, blockState);
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .details("blockhelp.autowork.repair_station.details")
                .direction("blockhelp.autowork.controlled_catalyst.bottom", Direction.DOWN)
                .storage_required_front()
                .only_when_powered()
                .build();
    }
}
