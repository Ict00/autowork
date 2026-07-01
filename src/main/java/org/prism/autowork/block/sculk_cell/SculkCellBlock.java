package org.prism.autowork.block.sculk_cell;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.item.ModItems;
import org.prism.autowork.other.ModData;

import java.util.ArrayList;
import java.util.List;

public class SculkCellBlock extends BaseEntityBlock implements BlockHelpProvider {
    public static final MapCodec<SculkCellBlock> CODEC = simpleCodec(SculkCellBlock::new);
    public static final IntegerProperty STATE = IntegerProperty.create("state", 0, 6);

    public SculkCellBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        var x = new ArrayList<>(super.getDrops(state, params));
        var be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);

        if (be instanceof SculkCellBlockEntity scbe) {
            var xp = scbe.getXp();
            var st = new ItemStack(ModItems.SCULK_CELL_ITEM.get());

            if (xp != 0) {
                st.set(ModData.SCULK_CELL_EXPERIENCE, xp);
            }

            x.add(st);
        }

        return x;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof SculkCellBlockEntity be) {
            if (player.isCrouching()) {
                var x = be.extractXp(100, false);
                if (x != 0) {
                    player.giveExperiencePoints(x);
                    level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1, 1.5f);
                }
            }
            else {
                var get = player.totalExperience;
                if (get > 100) {
                    get = 100;
                }

                var put = be.putXp(get, true);
                if (put != 0) {
                    be.putXp(put, false);

                    player.giveExperiencePoints(put * -1);
                    level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1, 0.5f);
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATE);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(STATE, 0);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ModBlockEntities.SCULK_CELL_BE.get().create(blockPos, blockState);
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .details("blockhelp.autowork.sculk_cell.details")
                .build();
    }
}
