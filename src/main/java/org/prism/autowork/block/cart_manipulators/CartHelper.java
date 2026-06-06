package org.prism.autowork.block.cart_manipulators;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.prism.autowork.other.ModUtils;

import javax.annotation.Nullable;

public class CartHelper {

    public static @Nullable String getCartName(Level level, BlockPos pos, Direction facing) {
        Direction[] possibleDirs = { facing.getClockWise(), facing.getCounterClockWise(), facing.getOpposite()};

        for (Direction dir : possibleDirs) {
            var bPos = ModUtils.lookTo(pos, dir);
            var state = level.getBlockState(bPos);

            if (state.is(BlockTags.WALL_SIGNS)) {
                if (state.getValue(WallSignBlock.FACING) == dir && level.getBlockEntity(bPos) instanceof SignBlockEntity be) {
                    var frontText = be.getFrontText();
                    for (int i = 0; i < 4; i++) {
                        var msg = frontText.getMessage(i, false);
                        var strEd = msg.getString();
                        if (!strEd.isEmpty()) {
                            return strEd;
                        }
                    }
                }
            }
        }

        return null;
    }
}
