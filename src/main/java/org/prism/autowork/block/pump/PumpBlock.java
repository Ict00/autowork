package org.prism.autowork.block.pump;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.block.drill.DrillBlockEntity;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.item.ModItems;
import org.prism.autowork.item.custom.IWrenchable;

public class PumpBlock extends BaseEntityBlock implements IWrenchable, BlockHelpProvider {
    public static final MapCodec<PumpBlock> CODEC = simpleCodec(PumpBlock::new);
    public static final EnumProperty<PumpBlock.PumpState> PUMP_STATE = EnumProperty.create("state", PumpBlock.PumpState.class);

    public PumpBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return  new SimpleMenuProvider((PumpBlockEntity)level.getBlockEntity(pos), Component.empty());
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult result) {
        if (!level.isClientSide) {
            if (!player.getMainHandItem().is(ModItems.WRENCH)) {
                player.openMenu(
                        (MenuProvider) level.getBlockEntity(pos),
                        buf -> buf.writeBlockPos(pos)
                );
                return InteractionResult.SUCCESS;
            }
            else {
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.FAIL;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return state.getValue(PUMP_STATE) == PumpState.DONE;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(PUMP_STATE) == PumpState.DONE ? 15 : 0;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock()) && !movedByPiston) {
            if (level.getBlockEntity(pos) instanceof PumpBlockEntity be) {
                var stack = be.handler.getStackInSlot(0);
                if (!stack.isEmpty()) {
                    var newItemEntity = new ItemEntity(level, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, stack);
                    level.addFreshEntity(newItemEntity);
                }
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ModBlockEntities.PUMP_BE.get().create(blockPos, blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PUMP_STATE);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, ModBlockEntities.PUMP_BE.get(), PumpBlockEntity::staticTick);
    }


    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(PUMP_STATE, PumpState.STILL);
    }

    @Override
    public void wrench(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(PUMP_STATE) == PumpState.DONE) {
            level.setBlockAndUpdate(pos, state.setValue(PUMP_STATE, PumpState.STILL));
        }
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .details("blockhelp.autowork.pump.details")
                .direction("blockhelp.autowork.pump.top", Direction.UP)
                .direction("blockhelp.autowork.pump.bottom", Direction.DOWN)
                .storage()
                .no_fluid_storage()
                .build();
    }

    public enum PumpState implements StringRepresentable {
        STILL("still"),
        WORKING("working"),
        DONE("done"),
        STUCK("stuck");

        private final String name;

        PumpState(String name) {
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
