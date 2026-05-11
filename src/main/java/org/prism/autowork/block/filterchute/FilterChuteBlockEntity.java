package org.prism.autowork.block.filterchute;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;

public class FilterChuteBlockEntity extends BlockEntity {
    private ItemStack filterItem = ItemStack.EMPTY;

    public FilterChuteBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.FILTER_CHUTE_BE.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider prov) {
        super.saveAdditional(tag, prov);
        if (!filterItem.isEmpty()) {
            tag.put("filter", filterItem.save(prov));
        }
        else {
            if (tag.contains("filter")) {
                tag.remove("filter");
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider prov) {
        super.loadAdditional(tag, prov);
        filterItem = ItemStack.parseOptional(prov, tag.getCompound("filter"));
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {

        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public void nullFilter() {
        filterItem = ItemStack.EMPTY;
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        setChanged();
    }

    public void setFilter(ItemStack example) {
        filterItem = example.copy();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        setChanged();
    }

    public ItemStack getFilterItem() {
        return filterItem;
    }
}
