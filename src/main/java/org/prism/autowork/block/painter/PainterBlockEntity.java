package org.prism.autowork.block.painter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.other.ModUtils;
import org.prism.autowork.recipe.ModRecipes;
import org.prism.autowork.recipe.PaintRecipe.PaintRecipeInput;
import org.prism.autowork.recipe.SpillingRecipe.SpillingRecipeInput;
import org.prism.autowork.screens.painter.PainterMenu;

public class PainterBlockEntity extends BlockEntity implements MenuProvider {
    public ItemStackHandler handler = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof DyeItem;
        }

        @Override
        protected void onContentsChanged(int slot) {
            update();
        }
    };


    public PainterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.PAINTER_BE.get(), pos, blockState);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        handler.deserializeNBT(registries, tag.getCompound("dye"));
    }



    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("dye", handler.serializeNBT(registries));
    }

    public void paint() {
        var state = getBlockState();

        if (!state.getValue(BlockStateProperties.POWERED)) {
            var stack = handler.getStackInSlot(0);
            var level = getLevel();

            if (!stack.isEmpty() && level != null) {
                var facing = getBlockState().getValue(BlockStateProperties.FACING);
                var front = ModUtils.lookTo(getBlockPos(), facing);

                var block = level.getBlockState(front).getBlock();

                var s = new ItemStack(block.asItem());
                var input = new PaintRecipeInput(stack, s);


                var optionalRecipe = level.getRecipeManager().getRecipeFor(ModRecipes.PAINTING_RECIPE_TYPE.get(), input, level);

                if (optionalRecipe.isPresent()) {
                    var actualRecipe = optionalRecipe.get().value();
                    var painted = actualRecipe.result();

                    if (painted.getItem() instanceof BlockItem blockItem && !s.is(blockItem)) {
                        level.setBlockAndUpdate(front, blockItem.getBlock().defaultBlockState());
                        level.setBlockAndUpdate(getBlockPos(), state.setValue(BlockStateProperties.POWERED, true));
                        level.playSound(null, getBlockPos(), SoundEvents.PAINTING_PLACE, SoundSource.BLOCKS, 1, 1.5f);
                        level.scheduleTick(getBlockPos(), ModBlocks.PAINTER.get(), 15);

                        stack.shrink(1);
                    }
                }
            }
        }
    }

    private void update() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL_IMMEDIATE);
            requestModelDataUpdate();
        }
    }

    @Override
    public void onDataPacket(Connection net,
                             ClientboundBlockEntityDataPacket pkt,
                             HolderLookup.Provider registries) {
        super.onDataPacket(net, pkt, registries);

        if (level != null && level.isClientSide) {
            Minecraft.getInstance().levelRenderer.blockChanged(
                    level,
                    worldPosition,
                    getBlockState(),
                    getBlockState(),
                    8
            );
        }
    }

    public int getColor() {
        ItemStack stack = handler.getStackInSlot(0);
        if (!stack.isEmpty()) {
            return (stack.getItem() instanceof DyeItem dye)
                    ? dye.getDyeColor().getTextureDiffuseColor()
                    : 0xfff2d585;
        }

        return 0xfff2d585;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public @Nullable IItemHandler getCapability(@Nullable Direction direction) {
        return handler;
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
        return new PainterMenu(i, inventory, this);
    }
}
