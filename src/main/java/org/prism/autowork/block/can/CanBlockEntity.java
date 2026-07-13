package org.prism.autowork.block.can;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.other.data.CanComponent;

import java.util.ArrayList;

public class CanBlockEntity extends BlockEntity {
    public CanComponent itemComponent = new CanComponent(new ArrayList<>(), Items.AIR);

    public CanBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.CAN_BE.get(), pos, blockState);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("food")) {
            Tag encodedTag = tag.get("food");

            CanComponent.CODEC.parse(NbtOps.INSTANCE, encodedTag)
                    .resultOrPartial()
                    .ifPresent(deComponent -> {
                       itemComponent = deComponent;
                    });
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CanComponent.CODEC.encodeStart(NbtOps.INSTANCE, itemComponent)
                .resultOrPartial()
                .ifPresent(enTag -> {
                    tag.put("food", enTag);
                });
    }
}
