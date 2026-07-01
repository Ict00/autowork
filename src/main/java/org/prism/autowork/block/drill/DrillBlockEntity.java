package org.prism.autowork.block.drill;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.Autowork;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.block.placer.PlacerBlock;
import org.prism.autowork.hudinv.HudInventoryProvider;
import org.prism.autowork.other.ModOther;
import org.prism.autowork.other.ModUtils;
import org.prism.autowork.screens.drill.DrillMenu;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DrillBlockEntity extends BlockEntity implements MenuProvider {
    public ItemStackHandler handler = new ItemStackHandler(9) {

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
            }
        }
    };

    public ItemStackHandler toolHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            tool = toolHandler.getStackInSlot(0);
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
            }
        }

        @Override
        public boolean isItemValid(int i, ItemStack itemStack) {
            return itemStack.is(ModOther.TOOL_TAG);
        }
    };

    private ItemStack tool = ItemStack.EMPTY.copy();
    private int progress = 0;

    public boolean putTool(ItemStack tool) {
        if (this.tool.isEmpty()) {
            toolHandler.insertItem(0, tool.copy(), false);
            setChanged();
            return true;
        }
        return false;
    }

    public boolean hasTool() {
        return !tool.isEmpty();
    }

    public int getToolSignal() {
        if (!hasTool()) {
            return 0;
        }
        else {
            try {
                var x = (int) (((float) tool.get(DataComponents.DAMAGE) / (float) tool.get(DataComponents.MAX_DAMAGE)) * 15f);
                return Mth.clamp(x, 0, 15);
            }
            catch (Exception ex) {
                // Just in case
                return 0;
            }
        }
    }

    public ItemStack extractTool() {
        if (!tool.isEmpty()) {
            var toReturn = this.tool;
            this.tool = ItemStack.EMPTY.copy();
            setChanged();
            return toReturn;
        }
        return ItemStack.EMPTY.copy();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        tool = ItemStack.parseOptional(provider, tag.getCompound("tool"));
        if (!tool.isEmpty()) {
            toolHandler.insertItem(0, tool, false);
        }
        handler.deserializeNBT(provider, tag.getCompound("inventory"));
        progress = tag.getInt("progress");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (!tool.isEmpty()) {
            tag.put("tool", tool.save(provider));
        }
        tag.putInt("progress", progress);
        tag.put("inventory", handler.serializeNBT(provider));
    }

    public void tick(BlockState state, ServerLevel level, BlockPos pos) {
        if (hasTool()) {
            if (!state.getValue(DrillBlock.HAS_TOOL)) {
                level.setBlockAndUpdate(pos, state.setValue(DrillBlock.HAS_TOOL, true));
            }
        }
        else {
            if (state.getValue(DrillBlock.HAS_TOOL)) {
                level.setBlockAndUpdate(pos, state.setValue(DrillBlock.HAS_TOOL, false));
            }
        }


        var face = state.getValue(BlockStateProperties.FACING);
        AtomicBoolean anySignal = new AtomicBoolean(level.hasNeighborSignal(pos));

        if (!anySignal.get()) {
            if (state.getValue(BlockStateProperties.POWERED)) {
                level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, false));
            }
            return;
        }
        if (!state.getValue(BlockStateProperties.POWERED)) {
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, true));
        }
        var front = ModUtils.lookTo(pos, face);
        var blockInFront = level.getBlockState(front);

        if (!tool.isCorrectToolForDrops(blockInFront) && blockInFront.requiresCorrectToolForDrops()) {
            return;
        }

        if (blockInFront.is(BlockTags.AIR)) return;

        var speed = blockInFront.getDestroySpeed(level, front);

        if (speed < 0) return;

        float toolSpeed = tool.getDestroySpeed(blockInFront);

        int effLevel = ModUtils.getEnchantment(tool, Enchantments.EFFICIENCY, level.registryAccess());

        if (effLevel > 0 && tool.isCorrectToolForDrops(blockInFront)) {
            toolSpeed += effLevel * effLevel + 1;
        }

        var maxProgress = Math.max(1, (int)(speed * 20 / toolSpeed));

        int durabilityDamage = 1 + (int)(speed / 3f);
        boolean applyDamage = true;
        try {
            int unbreakingLevel = ModUtils.getEnchantment(tool, Enchantments.UNBREAKING, level.registryAccess());

            if (unbreakingLevel > 0) {
                applyDamage = level.random.nextInt(unbreakingLevel + 1) == 0;
            }
        }
        catch (Exception ignore) {

        }

        if (progress >= maxProgress) {
            LootParams.Builder params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(front))
                    .withParameter(LootContextParams.TOOL, tool)
                    .withParameter(LootContextParams.BLOCK_STATE, blockInFront)
                    .withOptionalParameter(
                            LootContextParams.BLOCK_ENTITY,
                            level.getBlockEntity(front)
                    );

            List<ItemStack> drops =
                    blockInFront.getDrops(params);

            for (var drop : drops) {
                var remaining = ItemHandlerHelper.insertItem(handler, drop, false);

                if (!remaining.isEmpty()) {
                        var itemEntity = new ItemEntity(level, front.getX(), front.getY(), front.getZ(), remaining);
                    level.addFreshEntity(itemEntity);
                }
            }

            level.destroyBlock(front, false);

            if (tool.has(DataComponents.DAMAGE) && tool.has(DataComponents.MAX_DAMAGE)) {
                if (applyDamage) {
                    var dmgCurrent = tool.get(DataComponents.DAMAGE);
                    var dmgMax = tool.get(DataComponents.MAX_DAMAGE);
                    dmgCurrent += durabilityDamage;
                    if (dmgCurrent >= dmgMax) {
                        level.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 1, 1.5f);
                        toolHandler.extractItem(0, 1, false);
                    } else {
                        tool.set(DataComponents.DAMAGE, dmgCurrent);
                    }
                }
            }

            progress = 0;

            level.sendBlockUpdated(pos, state, state, 2);
            this.setChanged();
        }
        else {
            progress++;
            var progress_l = (int)((float)progress / maxProgress * 10);
            if (progress_l % 2 != 0) {
                level.playSound(null, pos, blockInFront.getSoundType(level, pos, null).getBreakSound(), SoundSource.BLOCKS, 1, 1.5f);
            }
            level.destroyBlockProgress(-1, front, progress_l);
            this.setChanged();
        }

        this.setChanged();
    }

    public @Nullable IItemHandler getCapability(@Nullable Direction direction) {
        if (direction == getBlockState().getValue(BlockStateProperties.FACING)) {
            return toolHandler;
        }

        return handler;
    }

    public static void staticTick(Level level, BlockPos pos, BlockState state, DrillBlockEntity blockEntity) {
        if (!level.isClientSide) {
            blockEntity.tick(state, (ServerLevel) level, pos);
            level.updateNeighborsAt(pos, ModBlocks.DRILL.get());
        }
    }

    public DrillBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DRILL_BE.get(), pos, blockState);
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

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new DrillMenu(i, inventory, this);
    }
}
