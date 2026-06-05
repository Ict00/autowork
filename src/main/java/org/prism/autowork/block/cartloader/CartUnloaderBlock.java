package org.prism.autowork.block.cartloader;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.blockhelp.BlockHelpInfo;
import org.prism.autowork.blockhelp.BlockHelpProvider;
import org.prism.autowork.other.ModUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CartUnloaderBlock extends Block implements BlockHelpProvider {
    public static final DirectionProperty FACING = DirectionProperty.create("facing");
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public CartUnloaderBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        if (!state.getValue(POWERED) && level.hasNeighborSignal(pos)) {
            var facing = state.getValue(FACING);
            var oppositeFace = facing.getOpposite();

            var front = ModUtils.lookTo(pos, facing);
            var back = ModUtils.lookTo(pos, oppositeFace);

            var minecartCap = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.above(), Direction.DOWN);
            var storageCap = level.getCapability(Capabilities.ItemHandler.BLOCK, back, facing);

            if (minecartCap != null && storageCap != null) {
                try {
                    var aabb = new AABB(front).inflate(0.2);
                    List<MinecartChest> entities = level.getEntitiesOfClass(
                            MinecartChest.class,
                            aabb,
                            (et) -> et instanceof MinecartChest
                    );

                    if (entities.isEmpty()) {
                        level.scheduleTick(pos, asBlock(), 10);
                        return;
                    }

                    var filter = CartHelper.getCartName(level, pos, facing);
                    boolean caughtAny = false;

                    for (int i = 0; i < entities.size(); i++) {
                        var entity = entities.get(i);

                        if (filter != null) {
                            if (entity.getCustomName() == null) {
                                continue;
                            }
                            if (!entity.getCustomName().getString().equals(filter)) {
                                continue;
                            }
                        }

                        var items = entity.getItemStacks();

                        for (int x = 0; x < items.size(); x++) {
                            var remains = ItemHandlerHelper.insertItem(storageCap, items.get(x), false);
                            System.out.println(remains);

                            if (!remains.isEmpty()) {
                                var itemEntity = new ItemEntity(level, front.getX(), front.getY(), front.getZ(), remains);
                                level.addFreshEntity(itemEntity);
                            }
                        }

                        var remains = ItemHandlerHelper.insertItem(minecartCap, new ItemStack(Items.CHEST_MINECART), false);
                        if (!remains.isEmpty()) {
                            var itemEntity = new ItemEntity(level, front.getX(), front.getY(), front.getZ(), remains);
                            level.addFreshEntity(itemEntity);
                        }

                        entity.remove(Entity.RemovalReason.CHANGED_DIMENSION);
                        caughtAny = true;
                    }
                    if (caughtAny) {
                        level.setBlockAndUpdate(pos, state.setValue(POWERED, true));
                        level.playSound(null, pos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS);
                    }
                    else {
                        level.scheduleTick(pos, asBlock(), 10);
                    }
                    return;
                }
                catch (Exception ex) {

                }
            }
            level.scheduleTick(pos, asBlock(), 10);
        }
        else {
            if (state.getValue(POWERED)) {
                level.setBlockAndUpdate(pos, state.setValue(POWERED, false));
            }
            else {
                level.scheduleTick(pos, asBlock(), 2);
            }
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        level.scheduleTick(pos, asBlock(), 10);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(POWERED, false).setValue(FACING, context.getHorizontalDirection().getOpposite());
    }


    @Override
    public BlockHelpInfo getHelp() {
        return BlockHelpInfo.builder()
                .storage_required(Direction.UP)
                .storage_required_back()
                .details("blockhelp.autowork.cartunloader.details")
                .no_storage()
                .only_when_powered()
                .configurable_by_sign()
                .build();
    }
}
