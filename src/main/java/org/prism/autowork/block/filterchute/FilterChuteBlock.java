package org.prism.autowork.block.filterchute;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.block.drill.DrillBlock;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.other.ModOther;

import static org.prism.autowork.block.chute.ChuteBlock.doChuteStuff;

public class FilterChuteBlock extends BaseEntityBlock implements BlockHelpProvider {
    public static final BooleanProperty BLACKLIST = BooleanProperty.create("blacklist");

    public static final MapCodec<FilterChuteBlock> CODEC = simpleCodec(FilterChuteBlock::new);

    public FilterChuteBlock(Properties p_49224_) {
        super(p_49224_);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.getMainHandItem().isEmpty()) return InteractionResult.FAIL;

        if (player.isCrouching()) {
            level.setBlockAndUpdate(pos, state.setValue(BLACKLIST, !state.getValue(BLACKLIST)));
            level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1, 1.5f);
            return InteractionResult.SUCCESS;
        }

        if (level.getBlockEntity(pos) instanceof FilterChuteBlockEntity be) {
            be.nullFilter();
            level.playSound(null, pos, SoundEvents.PAINTING_PLACE, SoundSource.BLOCKS, 1, 1.5f);
            return InteractionResult.SUCCESS;
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack someStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand p_316595_, BlockHitResult p_316140_) {
        var stack = player.getMainHandItem();

        if (!stack.isEmpty() && level.getBlockEntity(pos) instanceof FilterChuteBlockEntity be) {
            be.setFilter(stack);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1, 1.5f);
            return ItemInteractionResult.SUCCESS;
        }

        return super.useItemOn(someStack, state, level, pos, player, p_316595_, p_316140_);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if (!level.isClientSide) {
            level.scheduleTick(pos, asBlock(), 5);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        if (level.getBlockState(pos.above()).is(ModBlocks.CHUTES)) {
            level.scheduleTick(pos, asBlock(), 5);
            return;
        }

        doChuteStuff(state, level, pos, asBlock(), pos.above());
        level.scheduleTick(pos, asBlock(), 5);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BLACKLIST);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BLACKLIST, false);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ModBlockEntities.FILTER_CHUTE_BE.get().create(blockPos, blockState);
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .direction("blockhelp.autowork.filterchute.top", Direction.UP)
                .direction("blockhelp.autowork.filterchute.bottom", Direction.DOWN)
                .details("blockhelp.autowork.filterchute.details")
                .no_storage()
                .multiblock()
                .only_when_unpowered()
                .other("blockhelp.autowork.filterchute.addition1")
                .other("blockhelp.autowork.filterchute.addition2")
                .build();
    }
}
