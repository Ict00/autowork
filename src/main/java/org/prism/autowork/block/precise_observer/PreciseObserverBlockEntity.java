package org.prism.autowork.block.precise_observer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;

public class PreciseObserverBlockEntity extends BlockEntity {
    private ItemStack filter = ItemStack.EMPTY;

    public PreciseObserverBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.PRECISE_OBSERVER_BE.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!filter.isEmpty()) {
            tag.put("filter", filter.save(registries));
        }
        else {
            if (tag.contains("filter")) {
                tag.remove("filter");
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        filter = ItemStack.parseOptional(registries, tag.getCompound("filter"));
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public boolean hasFilter() {
        return !filter.isEmpty();
    }

    public void nullFilter() {
        filter = ItemStack.EMPTY;
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        setChanged();
    }

    public boolean setFilter(ItemStack example) {
        if (!(example.getItem() instanceof BlockItem)) {
            return false;
        }

        filter = example.copy();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        setChanged();

        return true;
    }

    public ItemStack getFilterItem() {
        return filter;
    }
}
