package org.prism.autowork.block.precise_observer;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.block.filterchute.FilterChuteBlockEntity;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.other.ModUtils;

public class PreciseObserverBlock extends BaseEntityBlock implements BlockHelpProvider {
    public static final MapCodec<PreciseObserverBlock> CODEC = simpleCodec(PreciseObserverBlock::new);

    public PreciseObserverBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING, BlockStateProperties.POWERED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.POWERED, false).setValue(BlockStateProperties.FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        level.scheduleTick(pos, asBlock(), 2);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.getMainHandItem().isEmpty()) return InteractionResult.FAIL;
        var facing = state.getValue(BlockStateProperties.FACING);

        if (level.getBlockEntity(pos) instanceof PreciseObserverBlockEntity be) {
            be.nullFilter();
            level.playSound(null, pos, SoundEvents.PAINTING_PLACE, SoundSource.BLOCKS, 1, 1.5f);
            return InteractionResult.SUCCESS;
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack someStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand p_316595_, BlockHitResult hitResult) {
        var stack = player.getMainHandItem();

        if (!stack.isEmpty() && level.getBlockEntity(pos) instanceof PreciseObserverBlockEntity be) {
            if (be.setFilter(stack)) {
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1, 1.5f);
                return ItemInteractionResult.SUCCESS;
            }
        }

        return super.useItemOn(someStack, state, level, pos, player, p_316595_, hitResult);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        doTick(state, level, pos, random);
        level.scheduleTick(pos, asBlock(), 2);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    protected int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockState.getValue(BlockStateProperties.POWERED) && side == blockState.getValue(BlockStateProperties.FACING)? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (state.getValue(BlockStateProperties.POWERED) && direction == state.getValue(BlockStateProperties.FACING)) {
            return 15;
        }

        return 0;
    }

    protected void doTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof PreciseObserverBlockEntity be) {
            if (be.hasFilter()) {
                if (be.getFilterItem().getItem() instanceof BlockItem item) {
                    var targetBlock = item.getBlock();

                    var facing = state.getValue(BlockStateProperties.FACING);
                    var front = ModUtils.lookTo(pos, facing);

                    var sourceState = level.getBlockState(front);

                    if (sourceState.is(targetBlock)) {
                        if (!state.getValue(BlockStateProperties.POWERED)) {
                            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, true));
                        }
                    }
                    else if (state.getValue(BlockStateProperties.POWERED)) {
                        level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, false));
                    }
                }
            }
            else {
                if (state.getValue(BlockStateProperties.POWERED)) {
                    level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, false));
                }
            }
        }
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ModBlockEntities.PRECISE_OBSERVER_BE.get().create(blockPos, blockState);
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .back("blockhelp.autowork.precise_observer.back")
                .front("blockhelp.autowork.precise_observer.front")
                .details("blockhelp.autowork.precise_observer.details")
                .build();
    }
}
