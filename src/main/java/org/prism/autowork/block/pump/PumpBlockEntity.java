package org.prism.autowork.block.pump;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.CommonConfig;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.item.ModItems;
import org.prism.autowork.screens.placer.PlacerMenu;
import org.prism.autowork.screens.pump.PumpMenu;

public class PumpBlockEntity extends BlockEntity implements MenuProvider {
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

    public PumpBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.PUMP_BE.get(), pos, blockState);
    }

    public void tick(BlockState state, Level level, BlockPos pos) {
        switch (state.getValue(PumpBlock.PUMP_STATE)) {
            case DONE -> {

            }
            case STILL -> {
                if (burnTime > 0) {
                    level.setBlockAndUpdate(pos, state.setValue(PumpBlock.PUMP_STATE, PumpBlock.PumpState.WORKING));
                    return;
                }

                var stack = handler.getStackInSlot(0);
                if (!stack.isEmpty() && stack.getBurnTime(null) > 0) {
                    burnTime = stack.getBurnTime(null);
                    stack.shrink(1);
                    update();
                    level.setBlockAndUpdate(pos, state.setValue(PumpBlock.PUMP_STATE, PumpBlock.PumpState.WORKING));
                }
            }
            case STUCK -> {
                var upperCap = level.getCapability(Capabilities.FluidHandler.BLOCK, getBlockPos().above(), Direction.DOWN);

                if (upperCap == null) {
                    return;
                }

                if (upperCap.getTankCapacity(0) - upperCap.getFluidInTank(0).getAmount() >= 1000) {
                    if (burnTime > 0) {
                        level.setBlockAndUpdate(pos, state.setValue(PumpBlock.PUMP_STATE, PumpBlock.PumpState.WORKING));
                    }
                    else {
                        level.setBlockAndUpdate(pos, state.setValue(PumpBlock.PUMP_STATE, PumpBlock.PumpState.STILL));
                    }
                }
            }
            case WORKING -> {
                var upperCap = level.getCapability(Capabilities.FluidHandler.BLOCK, getBlockPos().above(), Direction.DOWN);

                if (upperCap == null) {
                    return;
                }

                if (burnTime <= 0) {
                    var st = handler.getStackInSlot(0);
                    if (!st.isEmpty()) {
                        burnTime = st.getBurnTime(null);
                        st.shrink(1);
                        update();
                    }
                    else {
                        level.setBlockAndUpdate(pos, state.setValue(PumpBlock.PUMP_STATE, PumpBlock.PumpState.STILL));
                    }
                    return;
                }

                if (burnTime % 20 == 0) {
                    var cPos = pos.below().mutable();

                    boolean doDone = true;

                    for (int i = 0; i < CommonConfig.PUMP_RANGE.get(); i++) {
                        var currentState = level.getBlockState(cPos);
                        if (currentState.canBeReplaced()) {
                            if (currentState.getBlock() instanceof LiquidBlock lb) {
                                var fluidState = currentState.getFluidState();
                                if (!fluidState.isSource()) {
                                    cPos.move(Direction.DOWN);
                                    continue;
                                }

                                if (i == 0) {
                                    var constructed = new FluidStack(lb.fluid.builtInRegistryHolder(), 1000);
                                    if (upperCap.fill(constructed, IFluidHandler.FluidAction.SIMULATE) == 1000) {
                                        upperCap.fill(constructed, IFluidHandler.FluidAction.EXECUTE);

                                        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1, 1.5f);
                                        level.setBlockAndUpdate(cPos, Blocks.AIR.defaultBlockState());
                                    }
                                    else {
                                        level.setBlockAndUpdate(pos, state.setValue(PumpBlock.PUMP_STATE, PumpBlock.PumpState.STUCK));
                                    }
                                }
                                else {

                                    level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1, 1.5f);
                                    level.setBlockAndUpdate(cPos, Blocks.AIR.defaultBlockState());
                                    level.setBlockAndUpdate(cPos.above(), currentState);
                                }
                                doDone = false;
                                break;
                            }
                            else if (!currentState.isAir()) {
                                break;
                            }
                            cPos.move(Direction.DOWN);
                        }
                        else {
                            break;
                        }
                    }

                    if (doDone) {
                        level.setBlockAndUpdate(pos, state.setValue(PumpBlock.PUMP_STATE, PumpBlock.PumpState.DONE));
                    }
                }

                burnTime--;
                update();
            }
        }
    }

    public static void staticTick(Level level, BlockPos pos, BlockState state, PumpBlockEntity blockEntity) {
        if (!level.isClientSide) {
            blockEntity.tick(state, level, pos);
            level.updateNeighborsAt(pos, ModBlocks.PUMP.get());
        }
    }

    public @Nullable IItemHandler getCapability(@Nullable Direction direction) {
        return handler;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        handler.deserializeNBT(registries, tag.getCompound("inventory"));
        burnTime = tag.getInt("work");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", handler.serializeNBT(registries));
        tag.putInt("work", burnTime);
    }

    private void update() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
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

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new PumpMenu(i, inventory, this);
    }
}
