package org.prism.autowork.block.spiller;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
import org.prism.autowork.recipe.ModRecipes;
import org.prism.autowork.recipe.SpillingRecipe.SpillingRecipeInput;

public class SpillerBlock extends Block implements BlockHelpProvider {
    public SpillerBlock(Properties properties) {
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

        level.scheduleTick(pos, asBlock(), 10);
    }

    protected void doTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(BlockStateProperties.POWERED) && level.hasNeighborSignal(pos)) {
            var facing = state.getValue(BlockStateProperties.FACING);
            var opp = facing.getOpposite();

            var front = ModUtils.lookTo(pos, facing);
            var back = ModUtils.lookTo(pos, opp);
            var capBack = level.getCapability(Capabilities.FluidHandler.BLOCK, back, facing);

            if (capBack != null) {
                if (level.getBlockState(front).canBeReplaced() && !level.getBlockState(front).liquid()) {
                    var sim = capBack.drain(1000, IFluidHandler.FluidAction.SIMULATE);

                    if (sim.getAmount() == 1000) {
                        capBack.drain(1000, IFluidHandler.FluidAction.EXECUTE);

                        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1, 1.5f);
                        level.setBlockAndUpdate(front, sim.getFluid().defaultFluidState().createLegacyBlock());
                        level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, true));
                    }
                } else {
                    var inputStack = new ItemStack(level.getBlockState(front).getBlock());
                    var inputFluid = capBack.getFluidInTank(0);

                    var input = new SpillingRecipeInput(inputFluid, inputStack);

                    var optionalRecipe = level.getRecipeManager().getRecipeFor(ModRecipes.SPILLING_RECIPE_TYPE.get(),
                            input, level);

                    if (optionalRecipe.isPresent()) {
                        var actualRecipe = optionalRecipe.get().value();
                        FluidStack drained = capBack.drain(actualRecipe.inputFluid().getAmount(), IFluidHandler.FluidAction.SIMULATE);

                        if (drained.getAmount() == actualRecipe.inputFluid().getAmount()) {
                            var res = actualRecipe.assemble(input, null);

                            if (res.getItem() instanceof BlockItem blockItem) {
                                capBack.drain(drained.getAmount(), IFluidHandler.FluidAction.EXECUTE);
                                var placed = blockItem.getBlock().defaultBlockState();

                                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1, 1.5f);
                                level.setBlockAndUpdate(front, placed);
                                level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, true));
                            }
                        }
                    }
                }
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
                .front("blockhelp.autowork.spiller.front")
                .details("blockhelp.autowork.spiller.details")
                .fluid_storage_required_back()
                .no_fluid_storage()
                .only_when_powered()
                .build();
    }
}
