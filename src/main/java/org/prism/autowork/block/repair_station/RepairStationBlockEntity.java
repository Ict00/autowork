package org.prism.autowork.block.repair_station;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.block.holder.HolderBlock;
import org.prism.autowork.other.ModUtils;
import org.prism.autowork.other.capability.IHaveExperience;
import org.prism.autowork.screens.repair_station.RepairStationMenu;

public class RepairStationBlockEntity extends BlockEntity implements MenuProvider {
    public ItemStackHandler handler = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == 1) {
                var st = getStackInSlot(0);

                if (st.isEmpty()) {
                    return false;
                }

                return st.getItem().isValidRepairItem(st, stack);
            }
            else {
                return stack.isRepairable();
            }
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

    public RepairStationBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.REPAIR_STATION_BE.get(), pos, blockState);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        handler.deserializeNBT(registries, tag.getCompound("inventory"));
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

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }


    public void doTheTrick() {
        var tool = handler.getStackInSlot(0);

        if (tool.isEmpty()) {
            return;
        }
        if (level == null) {
            return;
        }

        var below = getBlockPos().below();
        IHaveExperience exp;

        if (level.getBlockEntity(below) instanceof IHaveExperience e) {
            exp = e;
        }
        else {
            return;
        }
        var state = getBlockState();
        var pos = getBlockPos();

        var mending = ModUtils.getEnchantment(tool, Enchantments.MENDING, level.registryAccess());
        var damage = tool.getDamageValue();

        if (damage == 0) {
            return;
        }


        if (mending == 1) {
            var requiredXp = damage/2;

            var take = exp.extractXp(requiredXp, true);
            if (take == 0) {
                return;
            }
            exp.extractXp(take, false);
            tool.setDamageValue(damage-take*2);

            handler.extractItem(0, 1, false);

            var facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            var front = ModUtils.lookTo(pos, facing);
            var capFront = level.getCapability(Capabilities.ItemHandler.BLOCK, front, facing.getOpposite());

            if (capFront == null) {
                var entity = new ItemEntity(level, front.getX() + 0.5, front.getY() + 0.5, front.getZ() + 0.5, tool, 0, 0, 0);
                level.addFreshEntity(entity);
                level.playSound(null, pos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 1, 1.5f);
            }
            else {
                var ins = ItemHandlerHelper.insertItem(capFront, tool, false);
                if (!ins.isEmpty()) {
                    var entity = new ItemEntity(level, front.getX() + 0.5, front.getY() + 0.5, front.getZ() + 0.5, tool, 0, 0, 0);
                    level.addFreshEntity(entity);
                }
                level.playSound(null, pos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 1, 1.5f);
            }
        }
        else {
            var repairItems = handler.getStackInSlot(1);
            if (repairItems.isEmpty()) {
                return;
            }

            int maxDamage = tool.getMaxDamage();
            int repairAmount = maxDamage/4;
            var cost = tool.get(DataComponents.REPAIR_COST);

            if (cost == null || cost == 0) {
                cost = 14;
            }

            var extracted = exp.extractXp(cost, true);
            if (extracted != cost) {
                return;
            }
            exp.extractXp(cost, false);

            for (; damage > 0 && !repairItems.isEmpty(); damage -= repairAmount) {
                repairItems.shrink(1);
            }

            tool.setDamageValue(Math.max(damage, 0));
            handler.extractItem(0, 1, false);

            var facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            var front = ModUtils.lookTo(pos, facing);
            var capFront = level.getCapability(Capabilities.ItemHandler.BLOCK, front, facing.getOpposite());

            if (capFront == null) {
                var entity = new ItemEntity(level, front.getX() + 0.5, front.getY() + 0.5, front.getZ() + 0.5, tool, 0, 0, 0);
                level.addFreshEntity(entity);
                level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1, 1.5f);
            }
            else {
                var ins = ItemHandlerHelper.insertItem(capFront, tool, false);
                if (!ins.isEmpty()) {
                    var entity = new ItemEntity(level, front.getX() + 0.5, front.getY() + 0.5, front.getZ() + 0.5, tool, 0, 0, 0);
                    level.addFreshEntity(entity);
                }
                level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1, 1.5f);
            }
        }
    }

    public @Nullable IItemHandler getCapability(@Nullable Direction direction) {
        return handler;
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new RepairStationMenu(i, inventory, this);
    }
}
