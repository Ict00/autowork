package org.prism.autowork.block.holder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.block.holder.proxies.EnergyProxy;
import org.prism.autowork.block.holder.proxies.FluidProxy;
import org.prism.autowork.block.holder.proxies.ItemProxy;

public class HolderBlockEntity extends BlockEntity {
    public ItemStackHandler handler = new ItemStackHandler(1) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();

            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), HolderBlock.UPDATE_ALL);
                invalidateCapabilities();
            }
        }
    };

    public HolderBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.HOLDER_BE.get(), pos, blockState);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        handler.deserializeNBT(registries, tag.getCompound("item"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("item", handler.serializeNBT(registries));
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public IItemHandler getPutCap(Direction side) {
        return handler;
    }

    public IItemHandler getProxyItem(Direction side) {
        if (side != getBlockState().getValue(BlockStateProperties.FACING)) {
            return null;
        }

        var stack = handler.getStackInSlot(0);

        if (stack.isEmpty()) {
            return null;
        }

        var itemHandler = stack.getCapability(Capabilities.ItemHandler.ITEM);

        if (itemHandler == null) {
            return null;
        }

        return new ItemProxy(stack) {
            @Override
            public void onContentsChanged() {
                setChanged();

                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), HolderBlock.UPDATE_ALL);
                }
            }
        };
    }

    public IFluidHandler getProxyFluid(Direction side) {
        if (side != getBlockState().getValue(BlockStateProperties.FACING)) {
            return null;
        }

        var stack = handler.getStackInSlot(0);

        if (stack.isEmpty()) {
            return null;
        }

        var fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);

        if (fluidHandler == null) {
            return null;
        }

        return new FluidProxy(stack, (x) -> {
            var t = handler.getStackInSlot(0);
            if (!t.isEmpty()) {
                handler.extractItem(0, 1, false);
                handler.insertItem(0, x.copy(), false);
            }
        }) {
            @Override
            public void onContentsChanged() {
                setChanged();

                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), HolderBlock.UPDATE_ALL);
                }
            }
        };
    }


    public IEnergyStorage getProxyEnergy(Direction side) {
        if (side != getBlockState().getValue(BlockStateProperties.FACING)) {
            return null;
        }

        var stack = handler.getStackInSlot(0);

        if (stack.isEmpty()) {
            return null;
        }

        var energyHandler = stack.getCapability(Capabilities.EnergyStorage.ITEM);

        if (energyHandler == null) {
            return null;
        }

        return new EnergyProxy(stack) {
            @Override
            public void onContentsChanged() {
                setChanged();

                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), HolderBlock.UPDATE_ALL);
                }
            }
        };
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
