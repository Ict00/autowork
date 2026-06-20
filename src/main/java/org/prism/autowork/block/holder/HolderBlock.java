package org.prism.autowork.block.holder;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.block.pump.PumpBlockEntity;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;

public class HolderBlock extends BaseEntityBlock implements BlockHelpProvider {
    public static final MapCodec<HolderBlock> CODEC = simpleCodec(HolderBlock::new);
    private static VoxelShape SHAPE_DOWN = Block.box(4.0F, 0.0F, 4.0F, 12.0F, 9.0F, 12.0F);
    private static VoxelShape SHAPE_UP = Block.box(4.0F, 7.0F, 4.0F, 12.0F, 16.0F, 12.0F);
    private static VoxelShape SHAPE_NORTH = Block.box(4.0F, 4.0F, 0.0F, 12.0F, 12.0F, 9.0F);
    private static VoxelShape SHAPE_SOUTH = Block.box(4.0F, 4.0F, 7.0F, 12.0F, 12.0F, 16.0F);

    private static VoxelShape SHAPE_WEST = Block.box(0.0F, 4.0F, 4.0F, 9.0F, 12.0F, 12.0F);
    private static VoxelShape SHAPE_EAST = Block.box(7.0F, 4.0F, 4.0F, 16.0F, 12.0F, 12.0F);


    public HolderBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);

        if (!level.isClientSide && !state.is(newState.getBlock()) && !movedByPiston) {
            if (level.getBlockEntity(pos) instanceof HolderBlockEntity be) {
                var stack = be.handler.getStackInSlot(0);
                if (!stack.isEmpty()) {
                    var newItemEntity = new ItemEntity(level, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, stack);
                    level.addFreshEntity(newItemEntity);
                }
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof HolderBlockEntity be) {
                var stack = player.getMainHandItem();

                if (stack.isEmpty()) {
                    var st = be.handler.extractItem(0, 1, false);
                    if (!st.isEmpty()) {
                        ItemHandlerHelper.giveItemToPlayer(player, st);
                    }
                }
                else {
                    var swap = be.handler.extractItem(0, 1, false);

                    be.handler.insertItem(0, stack.copyWithCount(1), false);
                    if (!player.isCreative()) {
                        stack.shrink(1);
                        ItemHandlerHelper.giveItemToPlayer(player, swap);
                    }
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.FACING, context.getClickedFace().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(BlockStateProperties.FACING)) {
            case UP -> SHAPE_UP;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_DOWN;
        };
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ModBlockEntities.HOLDER_BE.get().create(blockPos, blockState);
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .details("blockhelp.autowork.holder.details")
                .direction("blockhelp.autowork.holder.bottom", Direction.DOWN)
                .storage()
                .build();
    }
}
