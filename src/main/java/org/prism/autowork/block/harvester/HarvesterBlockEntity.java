package org.prism.autowork.block.harvester;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.screens.harvester.HarvesterMenu;

import java.util.concurrent.atomic.AtomicBoolean;

public class HarvesterBlockEntity extends BlockEntity implements MenuProvider {
    public ItemStackHandler handler = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
            }
        }
    };

    public ItemStackHandler hoe = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof HoeItem;
        }
    };

    public HarvesterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.HARVESTER_BE.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", handler.serializeNBT(registries));
        tag.put("hoe", hoe.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        hoe.deserializeNBT(registries, tag.getCompound("hoe"));
        handler.deserializeNBT(registries, tag.getCompound("inventory"));
    }

    public void tick(ServerLevel level, BlockPos pos, BlockState state) {
        var facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        var powered = state.getValue(BlockStateProperties.POWERED);

        if (powered && !level.hasNeighborSignal(pos)) {
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, false));
            return;
        }
        else if (!powered && level.hasNeighborSignal(pos)) {
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, true));
            return;
        }

        if (!powered) {
            return;
        }
        var cPos = pos.mutable();
        AtomicBoolean res = new AtomicBoolean(false);
        for (int i = 0; i < 3; i++) {
            cPos.move(facing);

            var t = HarvestDispatch.dispatch(level, cPos, hoe.getStackInSlot(0), handler);
            if (t) {
                res.set(true);
            }
        }

        if (res.get()) {
            setChanged();
        }
    }

    public @Nullable IItemHandler getCapability(@Nullable Direction direction) {
        if (direction == getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            return hoe;
        }

        return handler;
    }

    public static void staticTick(Level level, BlockPos pos, BlockState state, HarvesterBlockEntity harvesterBlockEntity) {
        harvesterBlockEntity.tick((ServerLevel) level, pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new HarvesterMenu(i, inventory, this);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
