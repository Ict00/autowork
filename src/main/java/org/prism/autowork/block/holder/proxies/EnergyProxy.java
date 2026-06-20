package org.prism.autowork.block.holder.proxies;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class EnergyProxy implements IEnergyStorage {
    private final IEnergyStorage handler;

    public EnergyProxy(ItemStack stack) {
        handler = stack.getCapability(Capabilities.EnergyStorage.ITEM);
    }

    @Override
    public int receiveEnergy(int i, boolean b) {
        return handler.receiveEnergy(i, b);
    }

    @Override
    public int extractEnergy(int i, boolean b) {
        return handler.extractEnergy(i, b);
    }

    @Override
    public int getEnergyStored() {
        return handler.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return handler.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return handler.canExtract();
    }

    @Override
    public boolean canReceive() {
        return handler.canReceive();
    }
}
