package org.prism.autowork.block.holder.proxies;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.function.Consumer;

public class FluidProxy implements IFluidHandler {
    private final IFluidHandlerItem handler;
    private final Consumer<ItemStack> containerChangedCallback;
    public FluidProxy(ItemStack stack, Consumer<ItemStack> containerChangedCallback) {
        handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        this.containerChangedCallback = containerChangedCallback;
    }

    @Override
    public int getTanks() {
        return handler.getTanks();
    }

    @Override
    public FluidStack getFluidInTank(int i) {
        return handler.getFluidInTank(i);
    }

    @Override
    public int getTankCapacity(int i) {
        return handler.getTankCapacity(i);
    }

    @Override
    public boolean isFluidValid(int i, FluidStack fluidStack) {
        return handler.isFluidValid(i, fluidStack);
    }

    @Override
    public int fill(FluidStack fluidStack, FluidAction fluidAction) {
        var filled = handler.fill(fluidStack, fluidAction);
        if (fluidAction.execute()) {
            var st = handler.getContainer();
            containerChangedCallback.accept(st);
            onContentsChanged();
        }
        return filled;
    }

    @Override
    public FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) {
        var drained = handler.drain(fluidStack, fluidAction);
        if (fluidAction.execute()) {
            var st = handler.getContainer();
            containerChangedCallback.accept(st);
            onContentsChanged();
        }
        return drained;
    }

    @Override
    public FluidStack drain(int i, FluidAction fluidAction) {
        var drained = handler.drain(i, fluidAction);
        if (fluidAction.execute()) {
            var st = handler.getContainer();
            containerChangedCallback.accept(st);
            onContentsChanged();
        }
        return drained;
    }

    public void onContentsChanged() {

    }
}
