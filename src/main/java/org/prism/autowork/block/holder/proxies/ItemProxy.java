package org.prism.autowork.block.holder.proxies;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

public class ItemProxy implements IItemHandler {
    private final IItemHandler handler;

    public ItemProxy(ItemStack stack) {
        handler = stack.getCapability(Capabilities.ItemHandler.ITEM);
    }

    @Override
    public int getSlots() {
        return handler.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        return handler.getStackInSlot(i);
    }

    @Override
    public ItemStack insertItem(int i, ItemStack itemStack, boolean b) {
        var x = handler.insertItem(i, itemStack, b);

        if (!b) {
            onContentsChanged();
        }

        return x;
    }

    @Override
    public ItemStack extractItem(int i, int i1, boolean b) {
        var x = handler.extractItem(i, i1, b);

        if (!b) {
            onContentsChanged();
        }

        return x;
    }

    @Override
    public int getSlotLimit(int i) {
        return handler.getSlotLimit(i);
    }

    @Override
    public boolean isItemValid(int i, ItemStack itemStack) {
        return handler.isItemValid(i, itemStack);
    }

    public void onContentsChanged() {

    }
}
