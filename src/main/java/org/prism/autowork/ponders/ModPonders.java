package org.prism.autowork.ponders;

import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.ponders.scenes.CannerScenes;
import org.prism.autowork.ponders.scenes.SawScenes;

public class ModPonders {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents(ModBlocks.CANNER.getKey().location())
                .addStoryBoard(
                        "canner",
                        CannerScenes::scene
                );

        helper.forComponents(ModBlocks.SAW.getKey().location())
                .addStoryBoard(
                        "saw",
                        SawScenes::scene
                );
    }
}
