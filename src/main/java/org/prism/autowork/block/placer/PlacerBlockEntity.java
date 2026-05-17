package org.prism.autowork.block.placer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;

public class PlacerBlockEntity extends BlockEntity {
    public ItemStackHandler handler = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof BlockItem;
        }
    };

    public PlacerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.PLACER_BE.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider prov) {
        super.saveAdditional(tag, prov);
        tag.put("inventory", handler.serializeNBT(prov));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider prov) {
        super.loadAdditional(tag, prov);
        handler.deserializeNBT(prov, tag.getCompound("inventory"));
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public @Nullable IItemHandler getCapability(@Nullable Direction direction) {
        return handler;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public void pushOne(ItemStack stack) {
        ItemHandlerHelper.insertItemStacked(handler, stack, false);
    }

    public ItemStack getOne() {
        for (int i = 0; i < handler.getSlots(); i++) {
            var stack = handler.getStackInSlot(i);

            if (!stack.isEmpty()) {
                return handler.extractItem(i, 1, false);
            }
        }
        return ItemStack.EMPTY;
    }
}
