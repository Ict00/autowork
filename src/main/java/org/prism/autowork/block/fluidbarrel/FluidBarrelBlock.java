package org.prism.autowork.block.fluidbarrel;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.item.ModItems;
import org.prism.autowork.other.ModData;
import org.prism.autowork.other.data.FluidStackComponent;

import java.util.ArrayList;
import java.util.List;

public class FluidBarrelBlock extends BaseEntityBlock implements BlockHelpProvider {
    public static final MapCodec<FluidBarrelBlock> CODEC = simpleCodec(FluidBarrelBlock::new);
    private static final VoxelShape INSIDE = box(2.0, 4.0, 2.0, 14.0, 16.0, 14.0);
    protected static final VoxelShape SHAPE = Shapes.join(
            Shapes.block(),
            Shapes.or(box(0.0, 0.0, 4.0, 16.0, 0.0, 12.0), box(4.0, 0.0, 0.0, 12.0, 0.0, 16.0), box(2.0, 0.0, 2.0, 14.0, 0.0, 14.0), INSIDE),
            BooleanOp.ONLY_FIRST
    );


    public FluidBarrelBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        var x = new ArrayList<>(super.getDrops(state, params));
        var be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);

        if (be instanceof FluidBarrelBlockEntity fbbe) {
            var fluidCopy = fbbe.getCapability(null).getFluidInTank(0).copy();
            var st = new ItemStack(ModItems.FLUID_BARREL_ITEM.get());
            if (!fluidCopy.isEmpty()) {
                st.set(ModData.BARREL_FLUID, new FluidStackComponent(fluidCopy.getFluid(), fluidCopy.getAmount()));
            }
            x.add(st);
        }

        return x;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(BlockStateProperties.OPEN)) {
            return SHAPE;
        }

        return super.getShape(state, level, pos, context);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ModBlockEntities.FLUID_BARREL_BE.get().create(blockPos, blockState);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.OPEN);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.OPEN, false);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof FluidBarrelBlockEntity be) {
            var cap = be.getCapability(null);
            var total = cap.getTankCapacity(0);
            var f = cap.getFluidInTank(0);
            return Math.clamp((int)(15 * ((float)f.getAmount()/(float)total)), 0, 15);
        }

        return super.getAnalogOutputSignal(state, level, pos);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof FluidBarrelBlockEntity be && hitResult.getDirection() != Direction.DOWN && state.getValue(BlockStateProperties.OPEN)) {
            var cap = be.getCapability(null);

            var copy = stack.copyWithCount(1);

            IFluidHandlerItem fluidHandler = copy.getCapability(Capabilities.FluidHandler.ITEM);

            if (fluidHandler != null && fluidHandler.getTanks() > 0) {
                FluidStack fStack = fluidHandler.getFluidInTank(0);

                if (fStack.isEmpty()) {
                    var res = FluidUtil.tryFillContainer(copy, cap, fluidHandler.getTankCapacity(0), player, true);

                    if (res.success) {
                        Fluid fluidToPlay = fluidHandler.getFluidInTank(0).getFluid();

                        if (!player.isCreative()) {
                            stack.shrink(1);
                            ItemHandlerHelper.giveItemToPlayer(player, res.result);
                        }

                        level.playSound(null, pos, fluidToPlay.getPickupSound().isPresent() ? fluidToPlay.getPickupSound().get() : SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS);
                        return ItemInteractionResult.SUCCESS;
                    }
                }
                else {
                    var res = FluidUtil.tryEmptyContainer(copy, cap, fStack.getAmount(), player, true);
                    if (res.success) {
                        Fluid fluidToPlay = cap.getFluidInTank(0).getFluid();

                        level.playSound(null, pos, fluidToPlay.getPickupSound().isPresent() ? fluidToPlay.getPickupSound().get() : SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS);

                        if (!player.isCreative()) {
                            stack.shrink(1);
                            ItemHandlerHelper.giveItemToPlayer(player, res.result);
                        }
                        return ItemInteractionResult.SUCCESS;
                    }
                }
            }
        }

        var before = state.getValue(BlockStateProperties.OPEN);

        if (!before) {
            if (hitResult.getDirection() != Direction.UP) {
                return ItemInteractionResult.FAIL;
            }
        }
        if (hitResult.getDirection() == Direction.DOWN) {
            return ItemInteractionResult.FAIL;
        }

        level.playSound(null, pos, before ? SoundEvents.BARREL_CLOSE : SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 1, 1.5f);
        level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.OPEN, !before));
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .other("blockhelp.autowork.fluid_barrel.capacity")
                .fluid_storage()
                .details("blockhelp.autowork.fluid_barrel.details")
                .build();
    }
}
