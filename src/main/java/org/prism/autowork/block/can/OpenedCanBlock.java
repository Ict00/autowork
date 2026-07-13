package org.prism.autowork.block.can;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.item.ModItems;
import org.prism.autowork.other.ModData;
import org.prism.autowork.other.data.CanComponent;

import java.util.ArrayList;
import java.util.List;

public class OpenedCanBlock extends BaseEntityBlock {
    public static final MapCodec<OpenedCanBlock> CODEC = simpleCodec(OpenedCanBlock::new);

    public OpenedCanBlock(Properties properties) {
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
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof CanBlockEntity be) {
            var food = be.itemComponent;
            var component = food.peek();

            var mutableArray = new ArrayList<>(food.food());
            mutableArray.removeLast();

            be.itemComponent = new CanComponent(mutableArray, food.fullItem());
            be.setChanged();

            if (component != null) {
                player.getFoodData().eat(component);
                level.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.BLOCKS, 1, 1.5f);
            }
            System.out.println(component);
            System.out.println(food);
            System.out.println(food.food());
            if (component == null || be.itemComponent.food().isEmpty()) {
                level.setBlockAndUpdate(pos, ModBlocks.EMPTY_CAN.get().defaultBlockState());
                level.playSound(null, pos, SoundEvents.VAULT_CLOSE_SHUTTER, SoundSource.BLOCKS, 1, 1.5f);
            }

            return InteractionResult.SUCCESS;
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        var s = super.getDrops(state, params);
        System.out.println("!");
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof CanBlockEntity canBe) {
            var addStack = new ItemStack(ModItems.OPENED_CAN_ITEM.get());
            System.out.println("!!");
            addStack.set(ModData.CAN, canBe.itemComponent);
            var l = new ArrayList<ItemStack>();
            l.add(addStack);
            System.out.println("!!!");
            return l;
        }

        return s;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ModBlockEntities.CAN_BE.get().create(blockPos, blockState);
    }
}
