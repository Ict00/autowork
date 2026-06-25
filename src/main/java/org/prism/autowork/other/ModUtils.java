package org.prism.autowork.other;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.prism.autowork.CommonConfig;
import org.prism.autowork.block.rotator.RotatorBlock;

import java.util.Arrays;
import java.util.function.Function;

public class ModUtils {
    public static Vec3 direction2vec(Direction direction) {
        return switch (direction) {
            case EAST -> new Vec3(1, 0, 0);
            case WEST -> new Vec3(-1, 0, 0);
            case NORTH -> new Vec3(0, 0, -1);
            case SOUTH -> new Vec3(0, 0, 1);
            case UP -> new Vec3(0, 1, 0);
            case DOWN -> new Vec3(0, -1, 0);
        };
    }

    public static AABB safeAABBfromTwoVec(Vec3 aP, Vec3 bP, Level level) {
        BlockPos a = vec2BlockPos(aP);
        BlockPos b = vec2BlockPos(bP);
        SubLevelAccess aaAccess = SableCompanion.INSTANCE.getContaining(level, a);

        if (aaAccess != null) {
            Pose3dc pose = aaAccess.logicalPose();

            aP = pose.transformPosition(aP);
        }

        SubLevelAccess bbAccess = SableCompanion.INSTANCE.getContaining(level, b);

        if (bbAccess != null) {
            Pose3dc pose = bbAccess.logicalPose();

            bP = pose.transformPosition(bP);
        }

        return new AABB(aP, bP);
    }

    public static boolean hasNeighborSignal(Level level, BlockPos pos) {
        return level.hasNeighborSignal(pos);
    }

    public static boolean hasNeighborSignalExcluding(Level level, BlockPos pos, Direction ... exclude) {
        var lst = Arrays.stream(exclude).toList();
        for (var dir : Direction.values()) {
            if (!lst.contains(dir) && hasSignal(level, pos, dir)) {
                return true;
            }
        }
        return false;
    }

    public static AABB safeAABBfromTwoPos(BlockPos a, BlockPos b, Level level) {
        Vec3 aP = blockPos2Vec(a);
        Vec3 bP = blockPos2Vec(b);
        SubLevelAccess aaAccess = SableCompanion.INSTANCE.getContaining(level, a);

        if (aaAccess != null) {
            Pose3dc pose = aaAccess.logicalPose();

            aP = pose.transformPosition(aP);
        }

        SubLevelAccess bbAccess = SableCompanion.INSTANCE.getContaining(level, b);

        if (bbAccess != null) {
            Pose3dc pose = bbAccess.logicalPose();

            bP = pose.transformPosition(bP);
        }

        return new AABB(aP, bP);
    }

    public static BlockPos safeBlockPos(BlockPos pos, Level level) {
        Vec3 position = blockPos2Vec(pos);
        SubLevelAccess subLevelAccess = SableCompanion.INSTANCE.getContaining(level, pos);

        if (subLevelAccess != null) {
            Pose3dc pose = subLevelAccess.logicalPose();

            var x = pose.transformPosition(position);
            return new BlockPos((int) x.x, (int) x.y, (int) x.z);
        }
        return pos;
    }

    public static BlockPos safeBlockPos(Vec3 position, Level level) {
        BlockPos pos = vec2BlockPos(position);
        SubLevelAccess subLevelAccess = SableCompanion.INSTANCE.getContaining(level, pos);

        if (subLevelAccess != null) {
            Pose3dc pose = subLevelAccess.logicalPose();

            var x = pose.transformPosition(position);
            return new BlockPos((int) x.x, (int) x.y, (int) x.z);
        }
        return pos;
    }

    public static Vec3 getLookVector(Level level, BlockPos pos, Direction localDirection) {
        Vec3 local = new Vec3(
                localDirection.getStepX(),
                localDirection.getStepY(),
                localDirection.getStepZ()
        );

        SubLevelAccess subLevelAccess = SableCompanion.INSTANCE.getContaining(level, pos);

        if (subLevelAccess == null) {
            return local;
        }

        return subLevelAccess.logicalPose().transformNormal(local);
    }

    public static Vec3 safeVecPos(Vec3 position, Level level) {
        BlockPos pos = vec2BlockPos(position);
        SubLevelAccess subLevelAccess = SableCompanion.INSTANCE.getContaining(level, pos);

        if (subLevelAccess != null) {
            Pose3dc pose = subLevelAccess.logicalPose();

            var x = pose.transformPosition(position);
            return x;
        }
        return position;
    }

    public static AABB safeAABBfromPos(BlockPos pos, Level level) {
        Vec3 position = blockPos2Vec(pos);
        SubLevelAccess subLevelAccess = SableCompanion.INSTANCE.getContaining(level, pos);

        if (subLevelAccess != null) {
            Pose3dc pose = subLevelAccess.logicalPose();
            var x = pose.transformPosition(position);
            return new AABB(vec2BlockPos(x));
        }

        return new AABB(vec2BlockPos(position));
    }

    public static Direction getLook(Level level, BlockPos pos, Direction defaultValue) {
        SubLevelAccess subLevelAccess = SableCompanion.INSTANCE.getContaining(level, pos);

        if (subLevelAccess != null) {
            Pose3dc pose = subLevelAccess.logicalPose();

            return quaternionToDirection(pose.orientation());
        }

        return defaultValue;
    }

    public static Vec3i direction2vec2(Direction direction) {
        var a = direction2vec(direction);
        return new Vec3i((int) a.x, (int) a.y, (int) a.z);
    }

    public static Direction quaternionToDirection(Quaterniondc q) {
        Vector3d forward = new Vector3d(0, 0, -1);
        q.transform(forward);

        return Direction.getNearest(
                (float) forward.x,
                (float) forward.y,
                (float) forward.z
        );
    }

    public static Direction tweakedRotate(Function<Direction, Direction> _90, Function<Direction, Direction> _180, Function<Direction, Direction> _270, Direction direction, RotatorBlock.AngleState angleState) {
        if (direction == Direction.UP || direction == Direction.DOWN) {
            return direction;
        }

        return switch (angleState) {
            case _90 -> _90.apply(direction);
            case _180 -> _180.apply(direction);
            case _270 -> _270.apply(direction);
        };
    }

    public static boolean hasSignal(Level level, BlockPos pos, Direction face) {
        var opp = face.getOpposite();
        if (CommonConfig.NEW_REDSTONE_DETECTION.get()) {
            return level.hasSignal(ModUtils.lookTo(pos, opp), face) || level.hasSignal(ModUtils.lookTo(pos, opp), opp);
        }
        else {
            return level.hasSignal(ModUtils.lookTo(pos, face), opp);
        }
    }

    public static BlockPos lookTo(BlockPos source, Direction dir) {
        return switch (dir) {
            case EAST -> source.east();
            case WEST -> source.west();
            case NORTH -> source.north();
            case SOUTH -> source.south();
            case UP -> source.above();
            case DOWN -> source.below();
        };
    }

    public static Vec3 vecPlusBlock(Vec3 a, BlockPos b) {
        return a.add(b.getX(), b.getY(), b.getZ());
    }

    public static Vec3 vecMultiply(Vec3 a, double b) {
        return new Vec3(a.x * b, a.y * b, a.z * b);
    }
    public static Vec3 vecDivide(Vec3 a, double b) {
        return new Vec3(a.x / b, a.y / b, a.z / b);
    }

    public static Vec3 blockPos2Vec(BlockPos a) {
        return new Vec3(a.getX(), a.getY(), a.getZ());
    }

    public static BlockPos vec2BlockPos(Vec3 a) {
        return new BlockPos((int) a.x, (int) a.y, (int) a.z);
    }
}
