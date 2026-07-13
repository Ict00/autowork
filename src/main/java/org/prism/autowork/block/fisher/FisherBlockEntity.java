package org.prism.autowork.block.fisher;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.block.common.BlocksAbstractLogic;
import org.prism.autowork.other.ModOther;
import org.prism.autowork.other.ModUtils;
import org.prism.autowork.screens.fisher.FisherMenu;

import java.util.List;

public class FisherBlockEntity extends BlockEntity implements MenuProvider {
    int progress = 0;
    public ItemStackHandler handler = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
            }
        }
    };

    public ItemStackHandler fishing_rod = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof FishingRodItem;
        }
    };

    public FisherBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.FISHER_BE.get(), pos, blockState);
    }

    public @Nullable IItemHandler getCapability(@Nullable Direction direction) {
        if (direction == getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            return fishing_rod;
        }

        return handler;
    }

    public void tick(ServerLevel level, BlockPos pos, BlockState state) {
        var rod = fishing_rod.getStackInSlot(0);

        if (rod.isEmpty()) return;
        if (!(rod.getItem() instanceof FishingRodItem)) return;
        if (state.getValue(BlockStateProperties.POWERED) && !level.hasNeighborSignal(pos)) {
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, false));
            return;
        }
        else if (!state.getValue(BlockStateProperties.POWERED) && level.hasNeighborSignal(pos)) {
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, true));
        }

        if (!state.getValue(BlockStateProperties.POWERED)) return;

        var facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        var front = ModUtils.lookTo(pos, facing);
        var frontState = level.getBlockState(front);

        if (!frontState.is(Blocks.WATER) && !frontState.is(Blocks.LAVA)) return;
        var biome = level.getBiome(pos);

        if (!biome.is(ModOther.AQUATIC) && !biome.is(ModOther.BEACH)) return;

        progress++;

        if (progress < 250) return;
        progress = 0;

        LootParams lootparams = (new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, rod).create(LootContextParamSets.FISHING));
        LootTable loottable = level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING);
        List<ItemStack> list = loottable.getRandomItems(lootparams);

        for (var item : list) {
            var remaining = ItemHandlerHelper.insertItemStacked(handler, item, false);

            if (!remaining.isEmpty()) {
                var itemEntity = new ItemEntity(level, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, remaining, 0, 0, 0);
                level.addFreshEntity(itemEntity);
            }
        }

        level.playSound(null, pos, SoundEvents.FISHING_BOBBER_SPLASH, SoundSource.BLOCKS, 0.5f, 1.4f);

        BlocksAbstractLogic.itemUser(pos, rod, level);
        setChanged();
    }

    public static void staticTick(Level level, BlockPos pos, BlockState state, FisherBlockEntity fisherBlockEntity) {
        fisherBlockEntity.tick((ServerLevel) level, pos, state);
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new FisherMenu(i, inventory, this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        handler.deserializeNBT(registries, tag.getCompound("inventory"));
        fishing_rod.deserializeNBT(registries, tag.getCompound("fishing_rod"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", handler.serializeNBT(registries));
        tag.put("fishing_rod", fishing_rod.serializeNBT(registries));
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }
}
