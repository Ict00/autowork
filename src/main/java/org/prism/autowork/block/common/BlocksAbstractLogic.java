package org.prism.autowork.block.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.prism.autowork.block.cart_manipulators.CartHelper;
import org.prism.autowork.other.ModUtils;

import java.util.List;
import java.util.function.Supplier;

public final class BlocksAbstractLogic {
    private BlocksAbstractLogic() { }

    public static void cartUnloaderTick(Level level, BlockPos pos, BlockState state, Supplier<IItemHandler> minecartCapGet,
                                        Supplier<IItemHandler> storageCapGet) {
        if (!state.getValue(BlockStateProperties.POWERED) && level.hasNeighborSignal(pos)) {
            var facing = state.getValue(BlockStateProperties.FACING);

            var front = ModUtils.lookTo(pos, facing);

            var minecartCap = minecartCapGet.get();
            var storageCap = storageCapGet.get();

            if (minecartCap != null && storageCap != null) {
                try {
                    var aabb = new AABB(front).inflate(0.2);
                    List<MinecartChest> entities = level.getEntitiesOfClass(
                            MinecartChest.class,
                            aabb,
                            (et) -> et instanceof MinecartChest
                    );

                    if (entities.isEmpty()) {
                        return;
                    }

                    var filter = CartHelper.getCartName(level, pos, facing);
                    boolean caughtAny = false;

                    for (MinecartChest entity : entities) {
                        if (filter != null) {
                            if (entity.getCustomName() == null) {
                                continue;
                            }
                            if (!entity.getCustomName().getString().equals(filter)) {
                                continue;
                            }
                        }

                        var items = entity.getItemStacks();

                        for (ItemStack item : items) {
                            var remains = ItemHandlerHelper.insertItem(storageCap, item, false);
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
                        level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, true));
                        level.playSound(null, pos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS);
                    }
                }
                catch (Exception ignored) {

                }
            }
        }
        else {
            if (state.getValue(BlockStateProperties.POWERED)) {
                level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, false));
            }
        }
    }


    public static void cartLoaderTick(Level level, BlockPos pos, BlockState state, Supplier<IItemHandler> minecartCapGet,
                                      Supplier<IItemHandler> storageCapGet) {
        if (!state.getValue(BlockStateProperties.POWERED) && level.hasNeighborSignal(pos)) {
            var facing = state.getValue(BlockStateProperties.FACING);

            var front = ModUtils.lookTo(pos, facing);

            var minecartCap = minecartCapGet.get();
            var storageCap = storageCapGet.get();

            if (minecartCap != null && storageCap != null) {
                for (int i = 0; i < minecartCap.getSlots(); i++) {
                    var stack = minecartCap.getStackInSlot(i);

                    if (stack.is(Items.CHEST_MINECART)) {
                        minecartCap.extractItem(i, 1, false);
                        var name = CartHelper.getCartName(level, pos, facing);
                        var cart = new MinecartChest(level, front.getX()+0.5f, front.getY(), front.getZ()+0.5f);

                        if (name != null) {
                            cart.setCustomName(Component.literal(name));
                        }

                        for (int x = 0; x < 27; x++) {
                            if (x >= storageCap.getSlots()) {
                                break;
                            }

                            var extracted = storageCap.extractItem(x, 64, false);

                            if (!extracted.isEmpty()) {
                                cart.setItem(x, extracted);
                            }
                        }

                        level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, true));
                        level.playSound(null, pos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS);
                        level.addFreshEntity(cart);

                        return;
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

    public static void breezeCollectorAnimateTick(Level level, BlockPos pos, BlockState state, int aabbSide, RandomSource random) {
        if (!state.getValue(BlockStateProperties.POWERED)) {
            return;
        }

        Direction facing = state.getValue(BlockStateProperties.FACING);

        Vec3 front = Vec3.atCenterOf(pos)
                .add(
                        facing.getStepX(),
                        facing.getStepY(),
                        facing.getStepZ()
                );

        for (int i = 0; i < 9; i++) {

            double spread = aabbSide*2;

            double x =
                    front.x + (random.nextDouble() - 0.5) * spread;

            double y =
                    front.y + (random.nextDouble() - 0.5) * spread;

            double z =
                    front.z + (random.nextDouble() - 0.5) * spread;

            Vec3 particlePos = new Vec3(x, y, z);

            Vec3 velocity =
                    front.subtract(particlePos)
                            .normalize()
                            .scale(0.1);

            level.addParticle(
                    ParticleTypes.CLOUD,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    velocity.x,
                    velocity.y,
                    velocity.z
            );
        }
    }

    public static void breezeCollectorTick(Level level, BlockPos pos, BlockState state, int aabbSide,
                                           Supplier<IItemHandler> containerGet) {
        if (level.hasNeighborSignal(pos)) {
            if (!state.getValue(BlockStateProperties.POWERED)) {
                level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, true));
            }
        }
        else {
            if (state.getValue(BlockStateProperties.POWERED)) {
                level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWERED, false));
            }
            return;
        }

        var facing = state.getValue(BlockStateProperties.FACING);

        var container = containerGet.get();

        if (container == null) {
            return;
        }

        var face = ModUtils.direction2vec(facing);
        var aabbAdd = ModUtils.vecMultiply(face, aabbSide/2.0);

        var aabb = new AABB(pos);
        aabb = aabb.inflate(aabbSide).move(aabbAdd.add(aabbAdd));

        List<ItemEntity> entities = level.getEntitiesOfClass(
                ItemEntity.class,
                aabb,
                (et) -> et instanceof ItemEntity
        );

        var start = ModUtils.blockPosVec(ModUtils.lookTo(pos, facing));

        for (int i = 0; i < entities.size(); i++) {
            var entity = entities.get(i);
            var position = entity.position();

            if (Math.sqrt(entity.distanceToSqr(start)) <= 1.5f) {
                var remains = ItemHandlerHelper.insertItem(container, entity.getItem(), false);

                if (!remains.isEmpty()) {
                    entity.setItem(remains);
                }
                else {
                    level.playSound(null, pos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS);
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }

                continue;
            }

            var delta = ModUtils.vecDivide(position.subtract(start), 2.5);
            delta = ModUtils.vecDivide(delta, 10);

            entity.addDeltaMovement(delta.multiply(-1,-1,-1));
            entity.hurtMarked = true;
        }
    }
}
