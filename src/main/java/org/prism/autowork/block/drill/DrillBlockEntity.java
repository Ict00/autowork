package org.prism.autowork.block.drill;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.other.ModOther;
import org.prism.autowork.other.ModUtils;

import java.util.List;

public class DrillBlockEntity extends BlockEntity {
    public ItemStackHandler handler = new ItemStackHandler(9);
    public IItemHandler toolHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int i) {
            return tool;
        }

        @Override
        public ItemStack insertItem(int i, ItemStack itemStack, boolean b) {
            if (tool.isEmpty() && b) {
                tool = itemStack;
                setChanged();
                return ItemStack.EMPTY;
            }

            return itemStack;
        }

        @Override
        public ItemStack extractItem(int i, int i1, boolean b) {
            if (!tool.isEmpty()) {
                var ret = tool;

                if (!b) {
                    tool = ItemStack.EMPTY;
                }

                return ret;
            }

            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int i) {
            return 1;
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
            this.tool = tool.copy();
            setChanged();
            return true;
        }
        return false;
    }

    public boolean hasTool() {
        return !tool.isEmpty();
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
        if (!level.hasNeighborSignal(pos)) {
            if (state.getValue(DrillBlock.POWERED)) {
                level.setBlockAndUpdate(pos, state.setValue(DrillBlock.POWERED, false));
            }
            return;
        }
        if (!state.getValue(DrillBlock.POWERED)) {
            level.setBlockAndUpdate(pos, state.setValue(DrillBlock.POWERED, true));
        }


        var front = ModUtils.lookTo(pos, state.getValue(DrillBlock.FACING));
        var blockInFront = level.getBlockState(front);

        if (!tool.isCorrectToolForDrops(blockInFront) && blockInFront.requiresCorrectToolForDrops()) {
            return;
        }

        if (blockInFront.is(BlockTags.AIR)) return;

        var speed = blockInFront.getDestroySpeed(level, front);

        if (speed < 0) return;

        float toolSpeed = tool.getDestroySpeed(blockInFront);

        int effLevel = tool.getEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY));

        if (effLevel > 0 && tool.isCorrectToolForDrops(blockInFront)) {
            toolSpeed += effLevel * effLevel + 1;
        }

        var maxProgress = Math.max(1, (int)(speed * 20 / toolSpeed));

        int durabilityDamage = 1 + (int)(speed / 3f);

        int unbreakingLevel = tool.getEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING));

        boolean applyDamage = true;
        if (unbreakingLevel > 0) {
            applyDamage = level.random.nextInt(unbreakingLevel + 1) == 0;
        }

        if (progress >= maxProgress) {
            LootParams.Builder params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(front))
                    .withParameter(LootContextParams.TOOL, tool)
                    .withParameter(LootContextParams.BLOCK_STATE, blockInFront)
                    .withOptionalParameter(
                            LootContextParams.BLOCK_ENTITY,
                            level.getBlockEntity(front)
                    );;

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
                        tool = ItemStack.EMPTY;
                    } else {
                        tool.set(DataComponents.DAMAGE, dmgCurrent);
                    }
                }
            }

            progress = 0;

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
        return handler;
    }

    public static void staticTick(Level level, BlockPos pos, BlockState state, DrillBlockEntity blockEntity) {
        if (!level.isClientSide) {
            blockEntity.tick(state, (ServerLevel) level, pos);
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
}
