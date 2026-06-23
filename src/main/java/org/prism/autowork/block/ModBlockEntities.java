package org.prism.autowork.block;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.prism.autowork.Autowork;
import org.prism.autowork.block.breezecollector.buffered.BufferedBreezeCollectorBlockEntity;
import org.prism.autowork.block.buffer.BufferBlockEntity;
import org.prism.autowork.block.cart_manipulators.buffered.loader.CartLoaderBufferedBlockEntity;
import org.prism.autowork.block.cart_manipulators.buffered.refiller.CartRefillerBufferedBlockEntity;
import org.prism.autowork.block.cart_manipulators.buffered.unloader.CartUnloaderBufferedBlockEntity;
import org.prism.autowork.block.drill.DrillBlockEntity;
import org.prism.autowork.block.enricher.EnricherBlockEntity;
import org.prism.autowork.block.filterchute.FilterChuteBlockEntity;
import org.prism.autowork.block.fluidbarrel.FluidBarrelBlockEntity;
import org.prism.autowork.block.holder.HolderBlockEntity;
import org.prism.autowork.block.painter.PainterBlockEntity;
import org.prism.autowork.block.placer.PlacerBlockEntity;
import org.prism.autowork.block.precise_observer.PreciseObserverBlock;
import org.prism.autowork.block.precise_observer.PreciseObserverBlockEntity;
import org.prism.autowork.block.pump.PumpBlockEntity;
import org.prism.autowork.block.ticker.TickerBlockEntity;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Autowork.MODID);

    public static final Supplier<BlockEntityType<PainterBlockEntity>> PAINTER_BE =
            BLOCK_ENTITIES.register("painter_be", () -> BlockEntityType.Builder.of(
                    PainterBlockEntity::new, ModBlocks.PAINTER.get()
            ).build(null));

    public static final Supplier<BlockEntityType<EnricherBlockEntity>> ENRICHER_BE =
            BLOCK_ENTITIES.register("enricher_be", () -> BlockEntityType.Builder.of(
                    EnricherBlockEntity::new, ModBlocks.ENRICHER.get()
            ).build(null));

    public static final Supplier<BlockEntityType<HolderBlockEntity>> HOLDER_BE =
            BLOCK_ENTITIES.register("holder_be", () -> BlockEntityType.Builder.of(
                    HolderBlockEntity::new, ModBlocks.HOLDER.get()
            ).build(null));

    public static final Supplier<BlockEntityType<BufferBlockEntity>> BUFFER_BE =
            BLOCK_ENTITIES.register("buffer_be", () -> BlockEntityType.Builder.of(
                    BufferBlockEntity::new, ModBlocks.BUFFER.get()
            ).build(null));

    public static final Supplier<BlockEntityType<PumpBlockEntity>> PUMP_BE =
            BLOCK_ENTITIES.register("pump_be", () -> BlockEntityType.Builder.of(
                    PumpBlockEntity::new, ModBlocks.PUMP.get()
            ).build(null));

    public static final Supplier<BlockEntityType<FluidBarrelBlockEntity>> FLUID_BARREL_BE =
            BLOCK_ENTITIES.register("fluid_barrel_be", () -> BlockEntityType.Builder.of(
                    FluidBarrelBlockEntity::new, ModBlocks.FLUID_BARREL.get()
            ).build(null));

    public static final Supplier<BlockEntityType<BufferedBreezeCollectorBlockEntity>> BUFFERED_BREEZE_COLLECTOR_BE =
            BLOCK_ENTITIES.register("buffered_breeze_collector_be", () -> BlockEntityType.Builder.of(
                    BufferedBreezeCollectorBlockEntity::new, ModBlocks.BUFFERED_BREEZE_COLLECTOR.get()
            ).build(null));

    public static final Supplier<BlockEntityType<DrillBlockEntity>> DRILL_BE =
            BLOCK_ENTITIES.register("drill_be", () -> BlockEntityType.Builder.of(
                    DrillBlockEntity::new, ModBlocks.DRILL.get()
            ).build(null));

    public static final Supplier<BlockEntityType<PreciseObserverBlockEntity>> PRECISE_OBSERVER_BE =
            BLOCK_ENTITIES.register("precise_observer_be", () -> BlockEntityType.Builder.of(
                    PreciseObserverBlockEntity::new, ModBlocks.PRECISE_OBSERVER.get()
            ).build(null));

    public static final Supplier<BlockEntityType<CartLoaderBufferedBlockEntity>> CARTLOADER_BUFFERED_BE =
            BLOCK_ENTITIES.register("cartloader_be", () -> BlockEntityType.Builder.of(
                    CartLoaderBufferedBlockEntity::new, ModBlocks.BUFFERED_CARTLOADER.get()
            ).build(null));

    public static final Supplier<BlockEntityType<CartRefillerBufferedBlockEntity>> CARTREFILLER_BUFFERED_BE =
            BLOCK_ENTITIES.register("cartrefiller_be", () -> BlockEntityType.Builder.of(
                    CartRefillerBufferedBlockEntity::new, ModBlocks.BUFFERED_CARTREFILLER.get()
            ).build(null));

    public static final Supplier<BlockEntityType<CartUnloaderBufferedBlockEntity>> CARTUNLOADER_BUFFERED_BE =
            BLOCK_ENTITIES.register("cartunloader_be", () -> BlockEntityType.Builder.of(
                    CartUnloaderBufferedBlockEntity::new, ModBlocks.BUFFERED_CARTUNLOADER.get()
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
