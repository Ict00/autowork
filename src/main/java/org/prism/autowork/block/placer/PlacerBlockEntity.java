package org.prism.autowork.block.placer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.Autowork;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.hudinv.HudInventoryProvider;
import org.prism.autowork.screens.drill.DrillMenu;
import org.prism.autowork.screens.placer.PlacerMenu;

import java.util.Objects;

public class PlacerBlockEntity extends BlockEntity implements MenuProvider {
    public ItemStackHandler handler = new ItemStackHandler(1) {
        // FIX THIS PLEASE! SHULKERS SHOULD BE PLACEABLE
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof BlockItem &&
                    !(Objects.requireNonNullElse(stack.get(DataComponents.CONTAINER), ItemContainerContents.EMPTY).stream().findAny().isPresent() || stack.has(DataComponents.BLOCK_ENTITY_DATA));
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
            }
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


    public ItemStack getOne() {
        for (int i = 0; i < handler.getSlots(); i++) {
            var stack = handler.getStackInSlot(i);

            if (!stack.isEmpty()) {
                return handler.extractItem(i, 1, false);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new PlacerMenu(i, inventory, this);
    }
}
