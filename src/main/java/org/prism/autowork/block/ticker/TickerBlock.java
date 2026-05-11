package org.prism.autowork.block.ticker;

import com.mojang.serialization.MapCodec;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.block.drill.DrillBlockEntity;
import org.prism.autowork.block.placer.PlacerBlock;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;

public class TickerBlock extends BaseEntityBlock implements BlockHelpProvider {
    public static final MapCodec<TickerBlock> CODEC = simpleCodec(TickerBlock::new);
    public static final DirectionProperty FACING = DirectionProperty.create("facing");
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 4);

    public TickerBlock(Properties p_49224_) {
        super(p_49224_);
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
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, ModBlockEntities.TICKER_BE.get(), TickerBlockEntity::staticTick);
    }

    protected int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockState.getValue(STAGE) == 4 && blockState.getValue(FACING).getOpposite() == side ? 15 : 0;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos blockPos, Player player, InteractionHand p_316595_, BlockHitResult p_316140_) {

        if (!level.isClientSide) {
            if (stack.is(Items.CLOCK) && level.getBlockEntity(blockPos) instanceof TickerBlockEntity be) {
                var customName = stack.get(DataComponents.CUSTOM_NAME);
                if (customName != null) {
                    var nameString = customName.getString();


                    StringBuilder builder = new StringBuilder();
                    int counter = 0;

                    for (int i = 0; i < nameString.length(); i++) {
                        var c = nameString.charAt(i);

                        switch (c) {
                            case 't' -> {
                                counter += Integer.parseInt(builder.toString());
                                builder = new StringBuilder();
                            }
                            case 's' -> {
                                counter += Integer.parseInt(builder.toString()) * 20;
                                builder = new StringBuilder();
                            }
                            case 'm' -> {
                                counter += Integer.parseInt(builder.toString()) * 20 * 60;
                                builder = new StringBuilder();
                            }
                            case 'h' -> {
                                counter += Integer.parseInt(builder.toString()) * 20 * 60 * 60;
                                builder = new StringBuilder();
                            }
                            default -> {
                                if (Character.isDefined(c)) {
                                    builder.append(c);
                                }
                            }
                        }
                    }

                    level.playSound(null, blockPos, SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 1, 1.5f);
                    player.displayClientMessage(Component.translatable("block.autowork.ticker.set", counter).withColor(16111404), true);
                    be.setTicks(counter);
                    return ItemInteractionResult.SUCCESS;
                }
            }
        }
        else {
            return ItemInteractionResult.SUCCESS;
        }

        return super.useItemOn(stack, state, level, blockPos, player, p_316595_, p_316140_);
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (state.getValue(STAGE) == 4 && direction == state.getValue(FACING).getOpposite()) {
            return 15;
        }

        return super.getDirectSignal(state, level, pos, direction);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STAGE);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(STAGE, 0).setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ModBlockEntities.TICKER_BE.get().create(blockPos, blockState);
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .back("blockhelp.autowork.ticker.back")
                .front("blockhelp.autowork.ticker.front")
                .details("blockhelp.autowork.ticker.details")
                .only_when_powered()
                .build();
    }
}
