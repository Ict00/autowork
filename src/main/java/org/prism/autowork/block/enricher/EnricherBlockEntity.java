package org.prism.autowork.block.enricher;

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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.other.ModUtils;
import org.prism.autowork.other.datamaps.EnrichingMap;
import org.prism.autowork.screens.enricher.EnricherMenu;

public class EnricherBlockEntity extends BlockEntity implements MenuProvider {
    public ItemStackHandler handler = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getBurnTime(null) > 0 && !stack.is(Items.LAVA_BUCKET);
        }

        @Override
        protected void onContentsChanged(int slot) {
            update();
        }
    };
    private int burnTime = 0;
    private int progress = 0;

    public EnricherBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ENRICHER_BE.get(), pos, blockState);
    }

    public @Nullable IItemHandler getCapability(@Nullable Direction direction) {
        return handler;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        handler.deserializeNBT(registries, tag.getCompound("inventory"));
        burnTime = tag.getInt("work");
        progress = tag.getInt("progress");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", handler.serializeNBT(registries));
        tag.putInt("work", burnTime);
        tag.putInt("progress", progress);
    }

    public void tick(BlockState state, ServerLevel level, BlockPos pos) {
        switch (state.getValue(EnricherBlock.ENRICHER_STATE)) {
            case STILL -> {
                if (burnTime > 0) {
                    level.setBlockAndUpdate(pos, state.setValue(EnricherBlock.ENRICHER_STATE, EnricherBlock.EnricherState.POWERED_STUCK));
                    return;
                }

                var stack = handler.getStackInSlot(0);
                if (!stack.isEmpty() && stack.getBurnTime(null) > 0) {
                    burnTime = stack.getBurnTime(null);
                    stack.shrink(1);
                    update();
                    level.setBlockAndUpdate(pos, state.setValue(EnricherBlock.ENRICHER_STATE, EnricherBlock.EnricherState.POWERED_STUCK));
                }
            }
            case POWERED_STUCK -> {
                burnTime--;

                var facing = state.getValue(BlockStateProperties.FACING);
                var front = level.getBlockState(ModUtils.lookTo(pos, facing));
                var blockItem = front.getBlock().asItem();

                var conv = EnrichingMap.getConversion(blockItem);
                if (!conv.isEmpty()) {
                    level.setBlockAndUpdate(pos, state.setValue(EnricherBlock.ENRICHER_STATE, EnricherBlock.EnricherState.POWERED_WORKING));
                }

                if (burnTime <= 0) {
                    level.setBlockAndUpdate(pos, state.setValue(EnricherBlock.ENRICHER_STATE, EnricherBlock.EnricherState.STILL));
                }
                update();
            }
            case POWERED_WORKING -> {
                burnTime--;
                progress++;

                var facing = state.getValue(BlockStateProperties.FACING);
                var face = ModUtils.lookTo(pos, facing);
                var front = level.getBlockState(face);
                var blockItem = front.getBlock().asItem();

                var conv = EnrichingMap.getConversion(blockItem);

                if (conv.isEmpty()) {
                    level.setBlockAndUpdate(pos, state.setValue(EnricherBlock.ENRICHER_STATE, EnricherBlock.EnricherState.POWERED_STUCK));
                }

                if (progress >= 100) {
                    if (conv.getItem() instanceof BlockItem bi) {
                        progress = 0;
                        level.destroyBlockProgress(-1, face, -1);
                        level.setBlockAndUpdate(face, bi.getBlock().defaultBlockState());
                        level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1, 1.5f);
                    }
                }
                else if (progress % 10 == 0) {
                    level.destroyBlockProgress(-1, face, progress/10-1);
                    level.playSound(null, pos, front.getSoundType(level, pos, null).getBreakSound(), SoundSource.BLOCKS, 1, 1.5f);
                }

                if (burnTime <= 0) {
                    level.setBlockAndUpdate(pos, state.setValue(EnricherBlock.ENRICHER_STATE, EnricherBlock.EnricherState.STILL));
                }
                update();
            }
        }
    }

    public static void staticTick(Level level, BlockPos pos, BlockState state, EnricherBlockEntity blockEntity) {
        if (!level.isClientSide) {
            blockEntity.tick(state, (ServerLevel) level, pos);
            level.updateNeighborsAt(pos, ModBlocks.DRILL.get());
        }
    }


    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    private void update() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new EnricherMenu(i, inventory, this);
    }
}
