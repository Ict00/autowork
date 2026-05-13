package org.prism.autowork;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue CRUSHING_HUD_HELPER = BUILDER
            .define("crushingHudHelper", true);
    static final ModConfigSpec SPEC = BUILDER.build();
}
