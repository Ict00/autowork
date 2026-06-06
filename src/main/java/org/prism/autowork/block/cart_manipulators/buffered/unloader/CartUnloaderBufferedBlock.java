package org.prism.autowork.block.cart_manipulators.buffered.unloader;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.block.common.BlocksAbstractLogic;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;

import java.util.function.Supplier;

public class CartUnloaderBufferedBlock extends BaseEntityBlock implements BlockHelpProvider {
    public static final MapCodec<CartUnloaderBufferedBlock> CODEC = simpleCodec(CartUnloaderBufferedBlock::new);

    public CartUnloaderBufferedBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        if (level.getBlockEntity(pos) instanceof CartUnloaderBufferedBlockEntity be) {
            Supplier<IItemHandler> minecartCapGetter = () -> be.minecarts;
            Supplier<IItemHandler> storageCapGetter = () -> be.handler;

            BlocksAbstractLogic.cartUnloaderTick(level, pos, state, minecartCapGetter, storageCapGetter);
        }

        level.scheduleTick(pos, this, 2);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult result) {
        if (!level.isClientSide) {
            player.openMenu(
                    (MenuProvider) level.getBlockEntity(pos),
                    buf -> buf.writeBlockPos(pos)
            );
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return  new SimpleMenuProvider((CartUnloaderBufferedBlockEntity)level.getBlockEntity(pos), Component.empty());
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        level.scheduleTick(pos, asBlock(), 20);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock()) && !movedByPiston) {
            if (level.getBlockEntity(pos) instanceof CartUnloaderBufferedBlockEntity be) {
                for (int i = 0; i < be.handler.getSlots(); i++) {
                    var stack = be.handler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        var newItemEntity = new ItemEntity(level, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, stack);
                        level.addFreshEntity(newItemEntity);
                    }
                }

                for (int i = 0; i < be.minecarts.getSlots(); i++) {
                    var stack = be.minecarts.getStackInSlot(i);
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
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ModBlockEntities.CARTUNLOADER_BUFFERED_BE.get().create(blockPos, blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING, BlockStateProperties.POWERED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.POWERED, false).setValue(BlockStateProperties.FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .direction("blockhelp.autowork.buffered_cartloader.top", Direction.UP)
                .side("blockhelp.autowork.buffered_cartloader.side")
                .side("blockhelp.autowork.cart.side")
                .details("blockhelp.autowork.buffered_cartunloader.details")
                .storage()
                .only_when_powered()
                .configurable_by_sign()
                .build();
    }
}
