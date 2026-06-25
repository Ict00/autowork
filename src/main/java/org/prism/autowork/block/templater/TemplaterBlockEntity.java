package org.prism.autowork.block.templater;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.item.ModItems;
import org.prism.autowork.other.ModData;
import org.prism.autowork.other.ModOther;
import org.prism.autowork.other.ModUtils;
import org.prism.autowork.other.data.template_card.ItemCountPair;
import org.prism.autowork.other.data.template_card.SlotItemPair;
import org.prism.autowork.screens.pump.PumpMenu;
import org.prism.autowork.screens.templater.TemplaterMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class TemplaterBlockEntity extends BlockEntity implements MenuProvider {
    public TemplaterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.TEMPLATER_BE.get(), pos, blockState);
    }

    public ItemStackHandler handler = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
            }
        }
    };

    public ItemStackHandler cardHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
            }
        }

        @Override
        public boolean isItemValid(int i, ItemStack itemStack) {
            return itemStack.is(ModItems.TEMPLATE_CARD) && itemStack.has(ModData.TEMPLATE_CARD);
        }
    };

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        handler.deserializeNBT(registries, tag.getCompound("inventory"));
        cardHandler.deserializeNBT(registries, tag.getCompound("card"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", handler.serializeNBT(registries));
        tag.put("card", cardHandler.serializeNBT(registries));
    }

    public @Nullable IItemHandler getCapability(@Nullable Direction direction) {
        return handler;
    }

    private boolean canTry(List<ItemCountPair> pairList) {
        HashMap<Item, Integer> handlersItems = new HashMap<>();

        for (int i = 0; i < handler.getSlots(); i++) {
            var item = handler.getStackInSlot(i);

            if (handlersItems.containsKey(item.getItem())) {
                handlersItems.put(item.getItem(), handlersItems.get(item.getItem())+item.getCount());
            }
            else {
                handlersItems.put(item.getItem(), item.getCount());
            }
        }

        ArrayList<ItemCountPair> countPairs = new ArrayList<>();

        for (var item : handlersItems.entrySet()) {
            countPairs.add(new ItemCountPair(item.getValue(), item.getKey()));
        }


        for (var pair : pairList) {
            boolean found = false;

            for (var other : countPairs) {
                if (other.count() >= pair.count() && pair.item().equals(other.item())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false;
            }
        }

        return true;
    }

    public void doTheTrick() {
        var template = cardHandler.getStackInSlot(0);
        if (template.isEmpty()) return;
        var data = template.get(ModData.TEMPLATE_CARD);
        if (data == null) return;
        var level = getLevel();
        if (level == null) return;

        var state = getBlockState();
        var pos = getBlockPos();

        var block = data.apply();
        var facing = state.getValue(BlockStateProperties.FACING);
        var front = ModUtils.lookTo(pos, facing);

        var inFront = level.getBlockState(front);

        if (!inFront.is(block)) return;

        if (!canTry(data.items())) return;

        HashMap<Item, Integer> lookupMap = new HashMap<>();

        Consumer<SlotItemPair> validator = (SlotItemPair pair) -> {
            if (lookupMap.containsKey(pair.item())) {
                return;
            }

            for (int i = 0; i < handler.getSlots(); i++) {
                var stack = handler.getStackInSlot(i);

                if (stack.is(pair.item())) {
                    lookupMap.put(pair.item(), i);
                }
            }
        };

        AtomicBoolean insertedAnything = new AtomicBoolean(false);

        for (var caps : data.config().entrySet()) {
            var otherCap = level.getCapability(Capabilities.ItemHandler.BLOCK, front, caps.getKey());

            if (otherCap != null) {
                for (var pair : caps.getValue()) {
                    if (pair.slot() >= otherCap.getSlots()) {
                        continue;
                    }

                    validator.accept(pair);
                    if (lookupMap.containsKey(pair.item())) {
                        var stack = handler.getStackInSlot(lookupMap.get(pair.item()));
                        var copy = stack.copyWithCount(pair.count());

                        var tryPut = otherCap.insertItem(pair.slot(), copy, true);

                        var inserted = copy.getCount() - tryPut.getCount();
                        if (inserted <= 0) continue;
                        insertedAnything.set(true);

                        otherCap.insertItem(pair.slot(), stack.copyWithCount(inserted), false);
                        stack.shrink(inserted);

                        if (stack.isEmpty()) {
                            lookupMap.remove(pair.item());
                        }
                    }
                }
            }
        }

        if (insertedAnything.get()) {
            level.playSound(null, pos, SoundEvents.VAULT_INSERT_ITEM, SoundSource.BLOCKS, 1, 1.5f);
        }
        else {
            level.playSound(null, pos, SoundEvents.VAULT_INSERT_ITEM_FAIL, SoundSource.BLOCKS, 0.5f, 0.9f);
        }
    }


    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new TemplaterMenu(i, inventory, this);
    }
}
