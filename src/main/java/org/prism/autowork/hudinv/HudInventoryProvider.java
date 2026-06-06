package org.prism.autowork.hudinv;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import oshi.util.tuples.Pair;

import javax.annotation.Nullable;

public interface HudInventoryProvider {
    default HandlerResult getLookHandler(Direction direction, Level level, BlockPos pos) {
        var cap = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, direction);
        return new HandlerResult(cap, takeOnlyForSide(direction));
    }

    default boolean takeOnlyForSide(Direction direction) {
        return false;
    }

    default boolean putItem(ItemStack stack, HandlerResult result, Level level, BlockPos pos) {
        if (!result.takeOnly) {
            var remains = ItemHandlerHelper.insertItemStacked(result.handler, stack.copy(), false);

            if (!remains.isEmpty()) {
                stack.setCount(remains.getCount());
            }
            else {
                stack.setCount(0);
            }
            var state = level.getBlockState(pos);
            level.sendBlockUpdated(pos, state, state, 2);

            return true;
        }

        return false;
    }

    default ItemInteractionResult useOn(ItemStack stack, Direction face, Level level, BlockPos pos, @Nullable Player player) {
        boolean x;

        if (stack.isEmpty()) {
            if (player != null) {
                x = getItem(player, getLookHandler(face, level, pos), level, pos);
            }
            else {
                x = false;
            }
        }
        else {
            x = putItem(stack, getLookHandler(face, level, pos), level, pos);
        }

        if (x) {
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1, 1.5f);
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.FAIL;
    }

    default boolean getItem(Player player, HandlerResult result, Level level, BlockPos pos) {
        if (player.getMainHandItem().isEmpty()) {
            for (int i = result.handler.getSlots()-1; i >= 0; i--) {
                var stack = result.handler.getStackInSlot(i);

                if (!stack.isEmpty()) {
                    player.getInventory().add(result.handler.extractItem(i, stack.getCount(), false));
                    var state = level.getBlockState(pos);
                    level.sendBlockUpdated(pos, state, state, 2);
                    return true;
                }
            }
        }
        return false;
    }

    default int hudWidth(Direction hitFace) {
        return slotCountSquared(hitFace)*slotWidthOverride()+xDist();
    }

    default int hudHeight(Direction hitFace) {
        return slotCountSquared(hitFace) * slotWidthOverride()+yDist();
    }

    default int slotCountSquared(Direction face) {
        return (int)(Math.sqrt(slotCount(face)));
    }

    default int xDist() {
        return 2;
    }

    default int yDist() {
        return 2;
    }

    int slotCount(Direction hitFace);

    default int slotWidthOverride() {
        return 18;
    }
    default boolean hasOverrideLocationForSlot(int slot) {
        return false;
    }
    @Nullable
    default Pair<Integer, Integer> getSlotOverride(int slot) {
        return null;
    }

    record HandlerResult(IItemHandler handler, boolean takeOnly) {

    }
}
