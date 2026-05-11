package org.prism.autowork.block;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.prism.autowork.Autowork;
import org.prism.autowork.block.drill.DrillBlockEntity;
import org.prism.autowork.block.filterchute.FilterChuteBlockEntity;
import org.prism.autowork.block.placer.PlacerBlockEntity;
import org.prism.autowork.block.ticker.TickerBlockEntity;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Autowork.MODID);

    public static final Supplier<BlockEntityType<DrillBlockEntity>> DRILL_BE =
            BLOCK_ENTITIES.register("drill_be", () -> BlockEntityType.Builder.of(
                    DrillBlockEntity::new, ModBlocks.DRILL.get()
            ).build(null));

    public static final Supplier<BlockEntityType<TickerBlockEntity>> TICKER_BE =
            BLOCK_ENTITIES.register("ticker_be", () -> BlockEntityType.Builder.of(
                    TickerBlockEntity::new, ModBlocks.TICKER.get()
            ).build(null));

    public static final Supplier<BlockEntityType<PlacerBlockEntity>> PLACER_BE =
            BLOCK_ENTITIES.register("placer_be", () -> BlockEntityType.Builder.of(
                    PlacerBlockEntity::new, ModBlocks.PLACER.get()
            ).build(null));

    public static final Supplier<BlockEntityType<FilterChuteBlockEntity>> FILTER_CHUTE_BE =
            BLOCK_ENTITIES.register("filter_chute_be", () -> BlockEntityType.Builder.of(
                    FilterChuteBlockEntity::new, ModBlocks.FILTER_CHUTE.get()
            ).build(null));

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
