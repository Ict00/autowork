package org.prism.autowork.block.fluidbarrel;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.prism.autowork.item.ModItems;
import org.prism.autowork.other.ModData;
import org.prism.autowork.other.data.FluidStackComponent;

import java.util.List;
import java.util.Objects;

public class FluidBarrelItemWrapper implements IFluidHandlerItem {
    ItemStack given;

    public FluidBarrelItemWrapper(ItemStack given) {
        this.given = given;
    }

    private FluidStack fluid() {
        FluidStack component = given.getOrDefault(ModData.BARREL_FLUID, FluidStackComponent.empty()).toFluidStack();
        return component;
    }

    @Override
    public ItemStack getContainer() {
        return given.copy();
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int i) {
        return fluid();
    }

    @Override
    public int getTankCapacity(int i) {
        return 5000;
    }

    @Override
    public boolean isFluidValid(int i, FluidStack fluidStack) {
        return fluidStack.getFluid().isSame(Objects.requireNonNull(given.getOrDefault(ModData.BARREL_FLUID, FluidStackComponent.empty()).toFluidStack()).getFluid());
    }

    private void setFluid(FluidStack stack) {
        if (stack.getAmount() > 5000) {
            stack.setAmount(5000);
        }


        if (stack.isEmpty()) {
            given = new ItemStack(ModItems.FLUID_BARREL_ITEM.get());
            return;
        }

        given.set(ModData.BARREL_FLUID, new FluidStackComponent(stack.getFluid(), stack.getAmount()));
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        var fluid = fluid();

        if (resource.isEmpty()) {
            return 0;
        }
        if (action.simulate()) {
            if (fluid.isEmpty()) {
                return Math.min(5000, resource.getAmount());
            }
            if (!FluidStack.isSameFluidSameComponents(fluid, resource)) {
                return 0;
            }
            return Math.min(5000 - fluid.getAmount(), resource.getAmount());
        }
        if (fluid.isEmpty()) {
            fluid = resource.copyWithAmount(Math.min(5000, resource.getAmount()));
            setFluid(fluid);
            return fluid.getAmount();
        }
        if (!FluidStack.isSameFluidSameComponents(fluid, resource)) {
            return 0;
        }
        int filled = 5000 - fluid.getAmount();

        if (resource.getAmount() < filled) {
            fluid.grow(resource.getAmount());
            filled = resource.getAmount();
        } else {
            fluid.setAmount(5000);
        }
        setFluid(fluid);
        return filled;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !FluidStack.isSameFluidSameComponents(resource, fluid())) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        var fluid = fluid();
        int drained = maxDrain;
        if (fluid.getAmount() < drained) {
            drained = fluid.getAmount();
        }
        FluidStack stack = fluid.copyWithAmount(drained);
        if (action.execute() && drained > 0) {
            fluid.shrink(drained);
            setFluid(fluid);
        }
        return stack;
    }
}
