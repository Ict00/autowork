package org.prism.autowork.block.smelter;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.other.ModUtils;
import org.prism.autowork.recipe.BulkSmeltRecipe.BulkSmeltRecipeInput;
import org.prism.autowork.recipe.ModRecipes;

public class SmelterBlock extends Block implements BlockHelpProvider {
    public static final BooleanProperty HAS_LAVA = BooleanProperty.create("has_lava");

    public SmelterBlock(Properties properties) {
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
        builder.add(BlockStateProperties.FACING, BlockStateProperties.POWERED, HAS_LAVA);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        doTick(state, level, pos, random);

        level.scheduleTick(pos, asBlock(), 10);
    }

    protected void doTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(BlockStateProperties.POWERED)) {
            var facing = state.getValue(BlockStateProperties.FACING);
            var opp = facing.getOpposite();

            var front = ModUtils.lookTo(pos, facing);
            var back = ModUtils.lookTo(pos, opp);
            var capBack = level.getCapability(Capabilities.FluidHandler.BLOCK, back, facing);

            if (capBack != null) {
                var inputStack = new ItemStack(level.getBlockState(front).getBlock());
                var inputFluid = capBack.getFluidInTank(0);

                if (!inputFluid.is(Fluids.LAVA)) {
                    if (state.getValue(HAS_LAVA)) {
                        level.setBlockAndUpdate(pos, state.setValue(HAS_LAVA, false));
                    }
                    return;
                }
                else {
                    if (!state.getValue(HAS_LAVA)) {
                        level.setBlockAndUpdate(pos, state.setValue(HAS_LAVA, true));
                    }
                }

                var input = new BulkSmeltRecipeInput(inputFluid.getAmount(), inputStack);

                var optionalRecipe = level.getRecipeManager().getRecipeFor(ModRecipes.BULK_SMELTING_RECIPE_TYPE.get(),
                        input, level);

                if (optionalRecipe.isPresent() && level.hasNeighborSignal(pos)) {
                    var actualRecipe = optionalRecipe.get().value();
                    capBack.drain(inputFluid.copyWithAmount(actualRecipe.amount()), IFluidHandler.FluidAction.EXECUTE);

                    var res = actualRecipe.assemble(input, null);

                    if (res.getItem() instanceof BlockItem blockItem) {
                        var placed = blockItem.getBlock().defaultBlockState();

                        level.playSound(null, pos, SoundEvents.WET_SPONGE_DRIES, SoundSource.BLOCKS, 1, 1.5f);
                        level.setBlockAndUpdate(front, placed);
                        level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, true));
                    }
                }
            }
            else if (state.getValue(HAS_LAVA)) {
                level.setBlockAndUpdate(pos, state.setValue(HAS_LAVA, false));
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
        return this.defaultBlockState().setValue(HAS_LAVA, false).setValue(BlockStateProperties.POWERED, false).setValue(BlockStateProperties.FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .front("blockhelp.autowork.smelter.front")
                .details("blockhelp.autowork.smelter.details")
                .fluid_storage_required_back()
                .no_fluid_storage()
                .only_when_powered()
                .build();
    }
}
