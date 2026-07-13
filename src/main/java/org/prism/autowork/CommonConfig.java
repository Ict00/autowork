package org.prism.autowork;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CommonConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue DISTRIBUTOR_RANGE = BUILDER
            .defineInRange("distributorRange", 15, 2, 64);

    public static final ModConfigSpec.BooleanValue NEW_REDSTONE_DETECTION = BUILDER
            .define("newRedstoneDetection", true);

    public static final ModConfigSpec.IntValue SAW_BLOCK_LIMIT = BUILDER
            .defineInRange("sawBlockLimit", 250, 1, 1000);

    public static final ModConfigSpec.IntValue SAW_BLOCK_SPEED = BUILDER
            .defineInRange("sawBlockSpeed", 20, 1, 100);

    public static final ModConfigSpec.IntValue SCULK_MOVER_RANGE = BUILDER
            .defineInRange("sculkMoverRange", 32, 2, 64);

    public static final ModConfigSpec.IntValue PUMP_RANGE = BUILDER
            .defineInRange("pumpRange", 16, 2, 64);

    public static final ModConfigSpec.IntValue TRANSMITTER_SIGNAL_LIFETIME = BUILDER
            .defineInRange("transmitterSignalLifetime", 80, 20, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue SCULK_CELL_CAPACITY = BUILDER
            .defineInRange("sculkCellCapacity", 1000, 100, 20000);

    // for FUTURE (TM) purposes
    /*    public static final ModConfigSpec.BooleanValue MINECART_WITH_CHEST_LOAD_CHUNK = BUILDER
            .define("chestMinecartLoadChunks", true);*/


    static final ModConfigSpec SPEC = BUILDER.build();
}
