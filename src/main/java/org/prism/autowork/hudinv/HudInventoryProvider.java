package org.prism.autowork.hudinv;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
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
        return new HandlerResult(cap, false);
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

    default boolean getItem(Player player, HandlerResult result, Level level, BlockPos pos) {
        if (player.getMainHandItem().isEmpty()) {
            for (int i = result.handler.getSlots()-1; i > 0; i--) {
                var stack = result.handler.getStackInSlot(i);

                if (!stack.isEmpty()) {
                    ItemHandlerHelper.giveItemToPlayer(player, result.handler.extractItem(i, stack.getCount(), false), 98);
                    var state = level.getBlockState(pos);
                    level.sendBlockUpdated(pos, state, state, 2);
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    default ResourceLocation hudTextureLoc() {
        return null;
    }
    default int slotWidthOverride() {
        return 20;
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
