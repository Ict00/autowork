package org.prism.autowork.block.harvester;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.prism.autowork.block.common.BlocksAbstractLogic;
import org.prism.autowork.other.ModOther;
import org.prism.autowork.other.ModUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class HarvestDispatch {
    public static boolean dispatch(ServerLevel level, BlockPos pos, ItemStack hoe, IItemHandler handler) {
        var state = level.getBlockState(pos);

        if (!state.is(ModOther.HARVESTABLE)) {
            return false;
        }

        LootParams.Builder params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, hoe)
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withOptionalParameter(
                        LootContextParams.BLOCK_ENTITY,
                        level.getBlockEntity(pos)
                );

        boolean result;

        if (state.is(ModOther.HARVESTABLE_MULTIBLOCK)) {
            result = multiblock(level, pos, handler, params);
        }
        else if (state.is(ModOther.HARVESTABLE_FRUIT)) {
            result = fruit(level, pos, handler, params);
        }
        else {
            result = crop(level, pos, handler, params);
        }

        if (result) {
            BlocksAbstractLogic.itemUser(pos, hoe, level);
        }

        return result;
    }

    private static @Nullable IntegerProperty getAgeProperty(BlockState state) {
        if (state.hasProperty(BlockStateProperties.AGE_7)) {
            return BlockStateProperties.AGE_7;
        }
        if (state.hasProperty(BlockStateProperties.AGE_2)) {
            return BlockStateProperties.AGE_2;
        }
        if (state.hasProperty(BlockStateProperties.AGE_3)) {
            return BlockStateProperties.AGE_3;
        }
        if (state.hasProperty(BlockStateProperties.AGE_5)) {
            return BlockStateProperties.AGE_5;
        }
        if (state.hasProperty(BlockStateProperties.AGE_4)) {
            return BlockStateProperties.AGE_4;
        }
        if (state.hasProperty(BlockStateProperties.AGE_25)) {
            return BlockStateProperties.AGE_25;
        }
        return null;
    }

    private static int maxPropVal (IntegerProperty property) {
        var possible = property.getPossibleValues();

        var max = possible.stream().toList().stream().max(Integer::compare);
        return max.orElse(0);
    }

    private static boolean fruit(ServerLevel level, BlockPos pos, IItemHandler handler, LootParams.Builder params) {
        var state = level.getBlockState(pos);

        var drops = state.getDrops(params);
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, 15, 0.5, 0.5, 0.5, 0);

        level.playSound(null, pos, state.getSoundType(level, pos, null).getBreakSound(), SoundSource.BLOCKS);

        for (var item : drops) {
            var remaining = ItemHandlerHelper.insertItemStacked(handler, item, false);
            if (!remaining.isEmpty()) {
                var itemEntity = new ItemEntity(level, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, remaining, 0, 0, 0);
                level.addFreshEntity(itemEntity);
            }
        }

        return true;
    }

    private static boolean crop(ServerLevel level, BlockPos pos, IItemHandler handler, LootParams.Builder params) {
        var state = level.getBlockState(pos);

        if (state.is(ModOther.HARVESTABLE_NO_BREAK)) {
            return false;
        }

        var prop = getAgeProperty(state);
        if (prop == null) {
            return false;
        }
        var max = maxPropVal(prop);
        if (max == 0) {
            return false;
        }

        var age = state.getValue(prop);
        if (age != max) {
            return false;
        }

        var drops = state.getDrops(params);

        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, 15, 0.5, 0.5, 0.5, 0);
        level.setBlockAndUpdate(pos, state.setValue(prop, 0));
        level.playSound(null, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS);


        for (var item : drops) {
            var remaining = ItemHandlerHelper.insertItemStacked(handler, item, false);
            if (!remaining.isEmpty()) {
                var itemEntity = new ItemEntity(level, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, remaining, 0, 0, 0);
                level.addFreshEntity(itemEntity);
            }
        }

        return true;
    }

    private static boolean multiblock(ServerLevel level, BlockPos pos, IItemHandler handler, LootParams.Builder params) {
        var state = level.getBlockState(pos);
        var block = state.getBlock();

        ArrayList<ItemStack> totalDrops = new ArrayList<>();

        var cPos = pos.mutable().move(Direction.UP);

        AtomicBoolean any = new AtomicBoolean(false);

        for (; level.getBlockState(cPos).is(block); cPos.move(Direction.UP)) {
            totalDrops.addAll(state.getDrops(params));
            level.setBlock(cPos, Blocks.AIR.defaultBlockState(), HarvesterBlock.UPDATE_CLIENTS);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, 15, 0.5, 0.5, 0.5, 0);
            level.playSound(null, cPos, state.getSoundType(level, pos, null).getBreakSound(), SoundSource.BLOCKS);
            any.set(true);
        }

        for (var item : totalDrops) {
            var remaining = ItemHandlerHelper.insertItemStacked(handler, item, false);
            if (!remaining.isEmpty()) {
                var itemEntity = new ItemEntity(level, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, remaining, 0, 0, 0);
                level.addFreshEntity(itemEntity);
            }
        }

        return any.get();
    }
}
