package org.prism.autowork.block.sculk_cell;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.CommonConfig;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.other.capability.IHaveExperience;

public class SculkCellBlockEntity extends BlockEntity implements IHaveExperience {
    private int storedXp = 0;

    public SculkCellBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.SCULK_CELL_BE.get(), pos, blockState);
    }


    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("xp", storedXp);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        storedXp = tag.getInt("xp");
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public void update() {
        setChanged();

        if (level != null && !level.isClientSide) {
            int stateCalculated = (int)(((float)storedXp / (float)CommonConfig.SCULK_CELL_CAPACITY.get())*6);
            var stateOld = getBlockState().getValue(SculkCellBlock.STATE);

            if (stateOld != stateCalculated) {
                level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(SculkCellBlock.STATE, stateCalculated));
            }
            else {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    public int getXp() {
        return storedXp;
    }

    public int extractXp(int max, boolean simulate) {
        int extracted = storedXp - max;

        if (extracted >= 0) {
            if (!simulate) {
                storedXp -= max;
                update();
            }

            return max;
        }

        var n = storedXp;

        if (!simulate) {
            storedXp = 0;
            update();
        }

        return n;
    }

    public int putXp(int amount, boolean simulate) {
        int left = CommonConfig.SCULK_CELL_CAPACITY.getAsInt() - (storedXp + amount);

        if (left >= 0) {
            if (!simulate) {
                storedXp += amount;
                update();
            }
            return amount;
        }

        int toPut = left + amount;

        if (!simulate) {
            storedXp += toPut;
            update();
        }

        return toPut;
    }
}
