package org.prism.autowork.block.fluidextractor;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.other.ModUtils;

public class FluidExtractorBlock extends Block implements BlockHelpProvider {
    public FluidExtractorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if (!level.isClientSide) {
            level.scheduleTick(pos, asBlock(), level.getRandom().fork().nextInt(9, 13));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING, BlockStateProperties.POWERED);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        doTick(state, level, pos, random);

        level.scheduleTick(pos, asBlock(), random.nextInt(9, 13));
    }

    protected void doTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(BlockStateProperties.POWERED) && level.hasNeighborSignal(pos)) {
            var facing = state.getValue(BlockStateProperties.FACING);
            var opp = facing.getOpposite();

            var front = ModUtils.lookTo(pos, facing);
            var back = ModUtils.lookTo(pos, opp);

            var capFront = level.getCapability(Capabilities.FluidHandler.BLOCK, front, opp);


            if (capFront != null) {
                var capBack = level.getCapability(Capabilities.FluidHandler.BLOCK, back, facing);
                if (capBack != null) {
                    var st = capBack.drain(1000, IFluidHandler.FluidAction.SIMULATE);
                    var put = capFront.fill(st, IFluidHandler.FluidAction.SIMULATE);

                    if (put != 0) {
                        capBack.drain(put, IFluidHandler.FluidAction.EXECUTE);
                        capFront.fill(st.copyWithAmount(put), IFluidHandler.FluidAction.EXECUTE);
                        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1, 1.5f);
                        level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, true));
                    }
                }
                else if (level.getBlockState(back).getFluidState().isSource() &&
                level.getBlockState(back).getBlock() instanceof LiquidBlock lb) {
                    var st = new FluidStack(lb.fluid.builtInRegistryHolder(), 1000);
                    if (capFront.fill(st, IFluidHandler.FluidAction.SIMULATE) == 1000) {
                        capFront.fill(st, IFluidHandler.FluidAction.EXECUTE);

                        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1, 1.5f);
                        level.setBlockAndUpdate(back, Blocks.AIR.defaultBlockState());
                        level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, true));
                    }
                }

            } else if (level.getBlockState(back).getFluidState().isSource() &&
                    level.getBlockState(front).canBeReplaced() && !level.getBlockState(front).liquid()) {
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1, 1.5f);
                level.setBlockAndUpdate(front, level.getBlockState(back));
                level.setBlockAndUpdate(back, Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, true));
            }
        }
        else {
            if (state.getValue(BlockStateProperties.POWERED)) {
                level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, false));
            }
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.POWERED, false).setValue(BlockStateProperties.FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .front("blockhelp.autowork.fluid_extractor.front")
                .back("blockhelp.autowork.fluid_extractor.back")
                .details("blockhelp.autowork.fluid_extractor.details")
                .no_fluid_storage()
                .only_when_powered()
                .build();
    }
}
