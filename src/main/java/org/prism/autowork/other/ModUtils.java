package org.prism.autowork.other;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.prism.autowork.CommonConfig;

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

    public static Vec3 blockPosVec(BlockPos a) {
        return new Vec3(a.getX(), a.getY(), a.getZ());
    }
}
