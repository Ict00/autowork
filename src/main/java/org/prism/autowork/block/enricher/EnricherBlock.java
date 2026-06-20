package org.prism.autowork.block.enricher;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.item.ModItems;

public class EnricherBlock extends BaseEntityBlock implements BlockHelpProvider {
    public static final MapCodec<EnricherBlock> CODEC = simpleCodec(EnricherBlock::new);
    public static final EnumProperty<EnricherState> ENRICHER_STATE = EnumProperty.create("state", EnricherState.class);


    public EnricherBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return  new SimpleMenuProvider((EnricherBlockEntity)level.getBlockEntity(pos), Component.empty());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock()) && !movedByPiston) {
            if (level.getBlockEntity(pos) instanceof EnricherBlockEntity be) {
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
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, ModBlockEntities.ENRICHER_BE.get(), EnricherBlockEntity::staticTick);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ENRICHER_STATE, BlockStateProperties.FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.FACING, context.getNearestLookingDirection().getOpposite()).setValue(ENRICHER_STATE, EnricherState.STILL);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ModBlockEntities.ENRICHER_BE.get().create(blockPos, blockState);
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .details("blockhelp.autowork.enricher.details")
                .front("blockhelp.autowork.enricher.front")
                .storage()
                .build();
    }

    public enum EnricherState implements StringRepresentable {
        STILL("off"),
        POWERED_WORKING("work_on"),
        POWERED_STUCK("work_off");

        private final String name;

        EnricherState(String name) {
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
