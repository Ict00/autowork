package org.prism.autowork.ponders.scenes;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.foundation.PonderSceneBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.checkerframework.checker.units.qual.C;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.block.saw.SawBlock;

public class SawScenes {
    public static void scene(SceneBuilder builder, SceneBuildingUtil util) {
        var SAW_DEFAULT = ModBlocks.SAW.get().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST)
                .setValue(SawBlock.STATE, SawBlock.SawState.STILL);
        var SAW_WORK = SAW_DEFAULT.setValue(SawBlock.STATE, SawBlock.SawState.WORKING);

        var WOOD = Blocks.OAK_LOG.defaultBlockState();
        var LEVER = Blocks.LEVER.defaultBlockState()
                .setValue(LeverBlock.FACING, Direction.WEST)
                .setValue(LeverBlock.FACE, AttachFace.FLOOR)
                .setValue(LeverBlock.POWERED, false);
        var CENTER = new BlockPos(2, 1, 2);
        var CENTER_FRONT = CENTER.west();

        PonderSceneBuilder scene = new PonderSceneBuilder(builder.getScene());

        scene.title("saw", "How to use saw");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        scene.idle(10);
        scene.world().setBlock(CENTER, SAW_DEFAULT, true);
        scene.world().showSection(util.select().layer(1), Direction.UP);
        scene.overlay().showText(30)
                .text("This is a saw")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 1, 2));
        scene.idle(30);

        scene.world().setBlock(CENTER_FRONT, WOOD, false);
        scene.world().setBlock(CENTER_FRONT.above(), WOOD, false);
        scene.world().setBlock(CENTER_FRONT.above().above(), WOOD, false);
        scene.world().setBlock(CENTER_FRONT.above().above().above(), WOOD, false);

        scene.world().showSection(util.select().layer(2), Direction.UP);
        scene.idle(3);
        scene.world().showSection(util.select().layer(3), Direction.UP);
        scene.idle(3);
        scene.world().showSection(util.select().layer(4), Direction.UP);
        scene.idle(3);
        scene.world().showSection(util.select().layer(5), Direction.UP);

        scene.idle(30);
        scene.overlay().showText(80)
                .text("Place a saw in front of tree, put some fuel in it")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 1, 2));
        scene.idle(80);

        scene.world().setBlock(CENTER.north(), LEVER, false);
        scene.idle(30);
        scene.overlay().showText(60)
                .text("Then, power it by constant redstone signal")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(CENTER.north().getX(), CENTER.north().getY(), CENTER.north().getZ()));
        scene.idle(60);
        scene.world().setBlock(CENTER, SAW_WORK, false);
        scene.world().setBlock(CENTER.north(), LEVER.setValue(LeverBlock.POWERED, true), false);
        scene.overlay().showText(60)
                .text("It'll begin to destroy the tree")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 1, 2));

        scene.idle(20);
        scene.world().destroyBlock(CENTER_FRONT.above().above().above());
        scene.idle(20);
        scene.world().destroyBlock(CENTER_FRONT.above().above());
        scene.idle(20);
        scene.world().destroyBlock(CENTER_FRONT.above());
        scene.idle(20);
        scene.world().destroyBlock(CENTER_FRONT);

        scene.markAsFinished();
    }
}
