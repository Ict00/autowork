package org.prism.autowork;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CommonConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue DISTRIBUTOR_RANGE = BUILDER
            .defineInRange("distributorRange", 15, 2, 64);
    static final ModConfigSpec SPEC = BUILDER.build();
}
