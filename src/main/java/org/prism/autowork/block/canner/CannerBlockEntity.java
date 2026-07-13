package org.prism.autowork.block.canner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.block.can.CanBlockEntity;
import org.prism.autowork.other.data.CanComponent;
import org.prism.autowork.screens.canner.CannerMenu;

import java.util.ArrayList;

public class CannerBlockEntity extends BlockEntity implements MenuProvider {
    public ItemStackHandler handler = new ItemStackHandler(9) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getFoodProperties(null) != null;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
            }
        }
    };

    public CannerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.CANNER_BE.get(), pos, blockState);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        handler.deserializeNBT(registries, tag.getCompound("inventory"));
    }

    public void doFunnyStuff() {
        if (level == null) return;
        var pos = getBlockPos();
        var belowPos = pos.below();
        var belowState = level.getBlockState(belowPos);
        if (!belowState.is(ModBlocks.EMPTY_CAN)) return;

        ArrayList<FoodProperties> foodProps = new ArrayList<>();
        Item full = null;

        for (int i = 0; i < handler.getSlots(); i++) {
            var stack = handler.getStackInSlot(i);

            if (!stack.isEmpty()) {
                var food = stack.getFoodProperties(null);
                if (food == null) continue;
                foodProps.add(food);

                if (full == null) {
                    full = stack.getItem();
                }
                else {
                    if (!full.equals(stack.getItem())) {
                        full = Items.AIR;
                    }
                }

                handler.extractItem(i, 1, false);
            }
        }

        var can = new CanComponent(foodProps, full);

        level.setBlockAndUpdate(belowPos, ModBlocks.CAN.get().defaultBlockState());
        if (level.getBlockEntity(belowPos) instanceof CanBlockEntity be) {
            be.itemComponent = can;
            be.setChanged();

            level.playSound(null, pos, SoundEvents.VAULT_EJECT_ITEM, SoundSource.BLOCKS, 1, 1.5f);
        }
    }



    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.put("inventory", handler.serializeNBT(registries));
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

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new CannerMenu(i, inventory, this);
    }
}
