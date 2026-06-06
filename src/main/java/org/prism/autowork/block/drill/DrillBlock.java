package org.prism.autowork.block.drill;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.hudinv.HudInventoryProvider;
import org.prism.autowork.other.ModOther;

public class DrillBlock extends BaseEntityBlock implements BlockHelpProvider {
    public static final MapCodec<DrillBlock> CODEC = simpleCodec(DrillBlock::new);
    public static final DirectionProperty FACING = DirectionProperty.create("facing");
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public static final BooleanProperty HAS_TOOL = BooleanProperty.create("has_tool");

    public DrillBlock(Properties p_49224_) {
        super(p_49224_);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, ModBlockEntities.DRILL_BE.get(), DrillBlockEntity::staticTick);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof DrillBlockEntity be) {
            return be.getToolSignal();
        }

        return 0;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof DrillBlockEntity be && direction == state.getValue(FACING)) {
            return be.getToolSignal();
        }

        return super.getSignal(state, level, pos, direction);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock()) && !movedByPiston) {
            if (level.getBlockEntity(pos) instanceof DrillBlockEntity be) {
                for (int i = 0; i < be.handler.getSlots(); i++) {
                    var stack = be.handler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        var newItemEntity = new ItemEntity(level, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, stack);
                        level.addFreshEntity(newItemEntity);
                    }
                }

                if (be.hasTool()) {
                    var toolEntity = new ItemEntity(level, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, be.extractTool());

                    level.addFreshEntity(toolEntity);
                }
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return  new SimpleMenuProvider((DrillBlockEntity)level.getBlockEntity(pos), Component.empty());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof DrillBlockEntity be) {
            if (stack.is(ModOther.TOOL_TAG) && !be.hasTool() && hit.getDirection() == state.getValue(FACING)) {
                if(be.putTool(stack))
                    stack.setCount(0);
                return ItemInteractionResult.SUCCESS;
            }
            else if (be.hasTool() && player.getMainHandItem().isEmpty()) {
                player.getInventory().add(be.extractTool());
                return ItemInteractionResult.SUCCESS;
            }
            else {
                if (!level.isClientSide) {
                    player.openMenu(
                            (MenuProvider) level.getBlockEntity(pos),
                            buf -> buf.writeBlockPos(pos)
                    );
                    return ItemInteractionResult.SUCCESS;
                }
            }
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, HAS_TOOL);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(POWERED, false).setValue(HAS_TOOL, false).setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ModBlockEntities.DRILL_BE.get().create(blockPos, blockState);
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .front("blockhelp.autowork.drill.front")
                .details("blockhelp.autowork.drill.details")
                .storage()
                .only_when_powered()
                .other("blockhelp.autowork.drill.addition")
                .build();
    }
}
