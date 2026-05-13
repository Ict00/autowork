package org.prism.autowork.block.placer;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.CommonConfig;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.block.drill.DrillBlockEntity;
import org.prism.autowork.block.filterchute.FilterChuteBlock;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.other.ModUtils;

import java.util.List;

public class PlacerBlock extends BaseEntityBlock implements BlockHelpProvider {
    public static final MapCodec<PlacerBlock> CODEC = simpleCodec(PlacerBlock::new);
    public static final DirectionProperty FACING = DirectionProperty.create("facing");
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public PlacerBlock(Properties p_49224_) {
        super(p_49224_);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock()) && !movedByPiston) {
            if (level.getBlockEntity(pos) instanceof PlacerBlockEntity be) {
                for (int i = 0; i < be.handler.getSlots(); i++) {
                    var stack = be.handler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        var newItemEntity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                        level.addFreshEntity(newItemEntity);
                    }
                }
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos other, boolean p_52705_) {
        if (!level.isClientSide && CommonConfig.MACHINES_ON_PULSE.getAsBoolean()) {
            Direction direction = Direction.getNearest(
                    pos.getX() - other.getX(),
                    pos.getY() - other.getY(),
                    pos.getZ() - other.getZ()
            ).getOpposite();

            boolean flag = level.hasSignal(other, direction);
            boolean flag1 = state.getValue(POWERED);

            if (flag && !flag1) {
                level.scheduleTick(pos, this, 15);
                level.setBlock(pos, state.setValue(POWERED, true), 2);
            } else if (!flag && flag1) {
                level.setBlock(pos, state.setValue(POWERED, false), 2);
            }
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if (!CommonConfig.MACHINES_ON_PULSE.get())
            level.scheduleTick(pos, asBlock(), 10);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        if (state.getValue(POWERED) && level.getBlockEntity(pos) instanceof PlacerBlockEntity be) {
            var face = state.getValue(FACING);
            var front = ModUtils.lookTo(pos, face);
            var blockInFront = level.getBlockState(front);

            if (!blockInFront.canBeReplaced()) {
                if (!CommonConfig.MACHINES_ON_PULSE.get())
                    level.scheduleTick(pos, asBlock(), 10);
                return;
            }

            var item = be.getOne();

            if (item.isEmpty() || !(item.getItem() instanceof BlockItem)) {
                if (!CommonConfig.MACHINES_ON_PULSE.get())
                    level.scheduleTick(pos, asBlock(), 10);
                return;
            }

            var blockItem = (BlockItem)item.getItem();

            level.setBlockAndUpdate(front, blockItem.getBlock().defaultBlockState());
            level.setBlockAndUpdate(pos, state.setValue(POWERED, false));

            level.playSound(null, pos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 1, 0.5f);

            level.scheduleTick(pos, asBlock(), 10);

            be.setChanged();
        }
        else {
            if (!state.getValue(POWERED) && CommonConfig.MACHINES_ON_PULSE.get()) {
                level.setBlockAndUpdate(pos, state.setValue(POWERED, true));
            }
            else {
                if(!CommonConfig.MACHINES_ON_PULSE.get())
                    level.scheduleTick(pos, asBlock(), 10);
            }
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ModBlockEntities.PLACER_BE.get().create(blockPos, blockState);
    }

    @Override
    protected RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(POWERED, false).setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .front("blockhelp.autowork.placer.front")
                .details("blockhelp.autowork.placer.details")
                .storage()
                .only_when_powered()
                .build();
    }
}
