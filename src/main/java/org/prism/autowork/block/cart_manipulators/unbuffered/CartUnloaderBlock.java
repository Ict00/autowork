package org.prism.autowork.block.cart_manipulators.unbuffered;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.block.common.BlocksAbstractLogic;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.other.ModUtils;

import java.util.function.Supplier;

public class CartUnloaderBlock extends Block implements BlockHelpProvider {
    public CartUnloaderBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        var facing = state.getValue(BlockStateProperties.FACING);
        var oppositeFace = facing.getOpposite();

        var back = ModUtils.lookTo(pos, oppositeFace);

        Supplier<IItemHandler> minecartCap = () -> level.getCapability(Capabilities.ItemHandler.BLOCK, pos.above(), Direction.DOWN);
        Supplier<IItemHandler> storageCap = () -> level.getCapability(Capabilities.ItemHandler.BLOCK, back, facing);

        BlocksAbstractLogic.cartUnloaderTick(level, pos, state, minecartCap, storageCap);

        level.scheduleTick(pos, this, 2);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        level.scheduleTick(pos, asBlock(), 10);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING, BlockStateProperties.POWERED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.POWERED, false).setValue(BlockStateProperties.FACING, context.getHorizontalDirection().getOpposite());
    }


    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .storage_required(Direction.UP)
                .storage_required_back()
                .side("blockhelp.autowork.cart.side")
                .details("blockhelp.autowork.cartunloader.details")
                .no_storage()
                .only_when_powered()
                .configurable_by_sign()
                .build();
    }
}
