package org.prism.autowork.block.saw;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.CommonConfig;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.other.ModOther;
import org.prism.autowork.other.ModUtils;
import org.prism.autowork.screens.saw.SawMenu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SawBlockEntity extends BlockEntity implements MenuProvider {
    public ItemStackHandler handler = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            update();
        }
    };

    public ItemStackHandler fuel = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            update();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getBurnTime(null) > 0;
        }
    };

    Block target = null;
    List<BlockPos> blocksToMine = new ArrayList<>();
    int progress = 0;
    int fuelAmount = 0;


    public SawBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.SAW_BE.get(), pos, blockState);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        handler.deserializeNBT(registries, tag.getCompound("inventory"));
        fuel.deserializeNBT(registries, tag.getCompound("fuel"));
        fuelAmount = tag.getInt("fuelInt");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", handler.serializeNBT(registries));
        tag.put("fuel", fuel.serializeNBT(registries));
        tag.putInt("fuelInt", fuelAmount);
    }

    public void tick(ServerLevel level, BlockPos pos, BlockState state) {
        if (level == null) {
            return;
        }

        var st = state.getValue(SawBlock.STATE);

        switch (st) {
            case STILL -> {
                if (level.hasNeighborSignal(pos)) {
                    level.setBlockAndUpdate(pos, state.setValue(SawBlock.STATE, SawBlock.SawState.POWERED));
                }
            }
            case POWERED -> {
                if (!level.hasNeighborSignal(pos)) {
                    level.setBlockAndUpdate(pos, state.setValue(SawBlock.STATE, SawBlock.SawState.STILL));
                    return;
                }

                var fuelItem = fuel.getStackInSlot(0);
                if (!fuelItem.isEmpty()) {
                    fuelAmount += fuelItem.getBurnTime(null);
                    fuelItem.shrink(1);
                }

                if (fuelAmount > 0) {
                    level.setBlockAndUpdate(pos, state.setValue(SawBlock.STATE, SawBlock.SawState.WORKING));
                }
            }
            case WORKING -> {
                progress++;
                fuelAmount--;

                if (progress >= CommonConfig.SAW_BLOCK_SPEED.get()) {
                    progress = 0;
                    var next = popNext();
                    if (next != null) {
                        var nextState = level.getBlockState(next);

                        LootParams.Builder params = new LootParams.Builder(level)
                                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(next))
                                .withParameter(LootContextParams.BLOCK_STATE, state)
                                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                                .withOptionalParameter(
                                        LootContextParams.BLOCK_ENTITY,
                                        level.getBlockEntity(pos)
                                );

                        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, nextState), next.getX()+0.5, next.getY()+0.5, next.getZ()+0.5, 15, 0.5, 0.5, 0.5, 0);
                        level.setBlockAndUpdate(next, Blocks.AIR.defaultBlockState());
                        level.playSound(null, next, nextState.getSoundType(level, pos, null).getBreakSound(), SoundSource.BLOCKS);

                        var drops = nextState.getDrops(params);

                        for (var item : drops) {
                            var remaining = ItemHandlerHelper.insertItemStacked(handler, item, false);
                            if (!remaining.isEmpty()) {
                                var itemEntity = new ItemEntity(level, next.getX()+0.5, next.getY()+0.5, next.getZ()+0.5, remaining, 0, 0, 0);
                                level.addFreshEntity(itemEntity);
                            }
                        }
                    }
                }

                if (fuelAmount <= 0) {
                    level.setBlockAndUpdate(pos, state.setValue(SawBlock.STATE, SawBlock.SawState.POWERED));
                }
                if (!level.hasNeighborSignal(pos)) {
                    level.setBlockAndUpdate(pos, state.setValue(SawBlock.STATE, SawBlock.SawState.STILL));
                }
            }
        }
    }

    public static void staticTick(Level level, BlockPos pos, BlockState state, SawBlockEntity sawBlockEntity) {
        sawBlockEntity.tick((ServerLevel) level, pos, state);
    }

    private void update() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
        }
    }

    public void nullify() {
        blocksToMine.clear();
        target = null;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    private @Nullable BlockPos popNext() {
        invalidateMine();
        try {
            var l = blocksToMine.getLast();

            invalidateMine(ModUtils.safeBlockPos(l, level));

            return blocksToMine.removeLast();
        }
        catch (Exception ignore) {
            return null;
        }
    }

    private void recursiveSeek(AtomicInteger counter, BlockPos pos) {
        if (blocksToMine.contains(pos)) {
            return;
        }

        if (counter.get() > CommonConfig.SAW_BLOCK_LIMIT.get()) {
            return;
        }

        assert level != null;
        var state = level.getBlockState(pos);

        if (!state.is(target)) {
            return;
        }

        blocksToMine.add(pos);
        counter.incrementAndGet();

        ArrayList<BlockPos> posToRec = new ArrayList<>();
        posToRec.add(pos.above());
        posToRec.add(pos.east());
        posToRec.add(pos.west());
        posToRec.add(pos.north());
        posToRec.add(pos.south());

        posToRec.add(pos.south().west());
        posToRec.add(pos.south().east());


        posToRec.add(pos.north().west());
        posToRec.add(pos.north().east());

        for (var p : posToRec) {
            recursiveSeek(counter, p);
        }
    }

    public @Nullable IItemHandler getCapability(@Nullable Direction direction) {
        var facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (direction == facing || direction.getOpposite() == facing) {
            return handler;
        }

        return fuel;
    }

    private void recalculateMine() {
        assert level != null;
        blocksToMine.clear();

        var facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        var inFront = ModUtils.safeBlockPos(ModUtils.lookTo(getBlockPos(), facing), level);
        var stateInFront = level.getBlockState(inFront);

        if (!stateInFront.is(ModOther.SAWABLE)) {
            return;
        }

        target = stateInFront.getBlock();
        var newCounter = new AtomicInteger(0);
        recursiveSeek(newCounter, inFront);

        blocksToMine.sort((a, b) -> {
            return Integer.compare(a.getY(), b.getY());
        });
    }

    private void invalidateMine(BlockPos p) {
        if (level == null) {
            return;
        }
        if (target == null || blocksToMine.isEmpty()) {
            recalculateMine();
        }
        else {
            var stateInFront = level.getBlockState(p);

            if (!stateInFront.is(target)) {
                recalculateMine();
            }
        }
    }

    private void invalidateMine() {
        var facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        var inFront = ModUtils.lookTo(getBlockPos(), facing);

        invalidateMine(ModUtils.safeBlockPos(inFront, level));
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new SawMenu(i, inventory, this);
    }
}
