package org.prism.autowork.block.can;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.item.ModItems;
import org.prism.autowork.item.custom.IWrenchable;
import org.prism.autowork.other.ModData;

import java.util.List;

public class CanBlock extends BaseEntityBlock implements IWrenchable {
    public static final MapCodec<CanBlock> CODEC = simpleCodec(CanBlock::new);

    public CanBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.OPEN);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.OPEN, false);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        var s = super.getDrops(state, params);
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof CanBlockEntity canBe) {
            var addStack = new ItemStack(ModItems.CAN_ITEM.get());
            addStack.set(ModData.CAN, canBe.itemComponent);
            s.add(addStack);
        }

        return s;
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
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ModBlockEntities.CAN_BE.get().create(blockPos, blockState);
    }

    @Override
    public void wrench(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof CanBlockEntity oldBe) {
            var oldComponent = oldBe.itemComponent;
            level.setBlockAndUpdate(pos, ModBlocks.OPENED_CAN.get().defaultBlockState());
            if (level.getBlockEntity(pos) instanceof CanBlockEntity newBe) {
                newBe.itemComponent = oldComponent;
                newBe.setChanged();
            }
        }


    }
}
