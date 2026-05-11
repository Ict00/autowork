package org.prism.autowork.block.ticker;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CalibratedSculkSensorBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CalibratedSculkSensorBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.block.drill.DrillBlockEntity;
import org.prism.autowork.other.ModUtils;

public class TickerBlockEntity extends BlockEntity {
    public TickerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.TICKER_BE.get(), pos, blockState);
    }

    private int setTicks = 0;
    private int curTick = 0;

    public void tick(ServerLevel level, BlockPos pos, BlockState state) {
        var face = state.getValue(TickerBlock.FACING);
        var oppositeFace = face.getOpposite();



        if (level.hasSignal(ModUtils.lookTo(pos, oppositeFace), face)) {
            var calculatedStage = Mth.clamp((int) (((float) curTick / setTicks) * 4f), 0, 4);

            if (state.getValue(TickerBlock.STAGE) != calculatedStage) {
                level.setBlockAndUpdate(pos, state.setValue(TickerBlock.STAGE, calculatedStage));
            }

            if (curTick > setTicks + 4) {
                curTick = 0;
                setChanged();
                return;
            }

            curTick++;
            setChanged();
            updateNeighborsInFront(level, pos, state);
        }
    }

    public void setTicks(int val) {
        curTick = 0;
        setTicks = val;
        setChanged();
    }

    public static void staticTick(Level level, BlockPos pos, BlockState state, TickerBlockEntity blockEntity) {
        if (!level.isClientSide) {
            blockEntity.tick((ServerLevel) level, pos, state);
        }
    }

    protected void updateNeighborsInFront(Level level, BlockPos pos, BlockState state) {
        Direction direction = (Direction)state.getValue(TickerBlock.FACING);
        BlockPos blockpos = pos.relative(direction.getOpposite());
        level.neighborChanged(blockpos, ModBlocks.TICKER.get(), pos);
        level.updateNeighborsAtExceptFromFacing(blockpos, ModBlocks.TICKER.get(), direction);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider prov) {
        super.saveAdditional(tag, prov);
        tag.putInt("tick", curTick);
        tag.putInt("setTick", setTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider prov) {
        super.loadAdditional(tag, prov);
        curTick = tag.getInt("tick");
        setTicks = tag.getInt("setTick");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}

