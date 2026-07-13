package org.prism.autowork.ponders.scenes;

import net.createmod.ponder.api.element.PonderElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.foundation.PonderSceneBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.block.canner.CannerBlock;

public class CannerScenes {
    public static void scene(SceneBuilder builder, SceneBuildingUtil util) {
        var CANNER_DEFAULT = ModBlocks.CANNER.get().defaultBlockState().setValue(BlockStateProperties.POWERED, false);
        var EMPTY_CAN = ModBlocks.EMPTY_CAN.get().defaultBlockState();
        var CAN = ModBlocks.CAN.get().defaultBlockState();
        var BUTTON = Blocks.OAK_BUTTON.defaultBlockState()
                .setValue(ButtonBlock.FACING, Direction.WEST)
                .setValue(ButtonBlock.POWERED, false);
        var CENTER = new BlockPos(2, 1, 2);

        PonderSceneBuilder scene = new PonderSceneBuilder(builder.getScene());

        scene.title("canner", "How to can a can");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        scene.idle(10);
        scene.world().setBlock(CENTER, CANNER_DEFAULT, true);
        scene.world().showSection(util.select().layer(1), Direction.UP);
        scene.idle(20);
        scene.overlay().showText(30)
                        .text("This is a canner")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 1, 2));
        scene.idle(30);
        scene.world().hideSection(util.select().layer(1), Direction.UP);
        scene.idle(10);
        scene.world().setBlock(CENTER, EMPTY_CAN, false);
        scene.world().setBlock(CENTER.above(), CANNER_DEFAULT, false);
        scene.world().showSection(util.select().layer(1), Direction.UP);
        scene.world().showSection(util.select().layer(2), Direction.UP);
        scene.overlay().showText(80)
                .text("Place an empty can under it, then put some food in it as well")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(2, 1, 2));
        scene.idle(80);

        scene.world().setBlock(CENTER.above().west(), BUTTON, false);
        scene.idle(30);
        scene.overlay().showText(60)
                .text("Then, power it by redstone signal")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(CENTER.above().west().getX(), CENTER.above().west().getY(), CENTER.above().west().getZ()));
        scene.idle(60);
        scene.world().setBlock(CENTER.above().west(), BUTTON.setValue(ButtonBlock.POWERED, true), false);
        scene.world().setBlock(CENTER.above(), CANNER_DEFAULT.setValue(BlockStateProperties.POWERED, true), false);
        scene.world().setBlock(CENTER, CAN, true);
        scene.idle(20);
        scene.world().setBlock(CENTER.above(), CANNER_DEFAULT, false);
        scene.world().setBlock(CENTER.above().west(), BUTTON.setValue(ButtonBlock.POWERED, false), false);

        scene.world().hideSection(util.select().layer(2), Direction.UP);

        scene.idle(20);

        scene.overlay().showText(60)
                .text("Congratulations! You canned your first can");

        scene.idle(60);

        scene.markAsFinished();
    }
}
