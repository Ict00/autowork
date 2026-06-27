package org.prism.autowork.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.prism.autowork.Autowork;
import org.prism.autowork.block.andgate.AndGateBlock;
import org.prism.autowork.block.breezecollector.BreezeCollectorBlock;
import org.prism.autowork.block.breezecollector.buffered.BufferedBreezeCollectorBlock;
import org.prism.autowork.block.buffer.BufferBlock;
import org.prism.autowork.block.bulbs.amethyst.AmethystBulbBlock;
import org.prism.autowork.block.bulbs.quartz.QuartzBulbBlock;
import org.prism.autowork.block.carrier.CarrierBlock;
import org.prism.autowork.block.carrier.ModularCarrierBlock;
import org.prism.autowork.block.cart_manipulators.buffered.loader.CartLoaderBufferedBlock;
import org.prism.autowork.block.cart_manipulators.buffered.refiller.CartRefillerBufferedBlock;
import org.prism.autowork.block.cart_manipulators.buffered.unloader.CartUnloaderBufferedBlock;
import org.prism.autowork.block.cart_manipulators.unbuffered.CartLoaderBlock;
import org.prism.autowork.block.cart_manipulators.unbuffered.CartRefillerBlock;
import org.prism.autowork.block.cart_manipulators.unbuffered.CartUnloaderBlock;
import org.prism.autowork.block.chute.ChuteBlock;
import org.prism.autowork.block.distributor.DistributorBlock;
import org.prism.autowork.block.drill.DrillBlock;
import org.prism.autowork.block.enricher.EnricherBlock;
import org.prism.autowork.block.extractor.ExtractorBlock;
import org.prism.autowork.block.fan.FanBlock;
import org.prism.autowork.block.filterchute.FilterChuteBlock;
import org.prism.autowork.block.fluidbarrel.FluidBarrelBlock;
import org.prism.autowork.block.fluidextractor.FluidExtractorBlock;
import org.prism.autowork.block.holder.HolderBlock;
import org.prism.autowork.block.painter.PainterBlock;
import org.prism.autowork.block.placer.PlacerBlock;
import org.prism.autowork.block.precise_observer.PreciseObserverBlock;
import org.prism.autowork.block.pump.PumpBlock;
import org.prism.autowork.block.railwayobserver.RailwayObserverBlock;
import org.prism.autowork.block.redstone_coil.RedstoneCoilBlock;
import org.prism.autowork.block.redstone_hub.RedstoneHubBlock;
import org.prism.autowork.block.rotator.RotatorBlock;
import org.prism.autowork.block.sculkmover.SculkMoverBlock;
import org.prism.autowork.block.smelter.SmelterBlock;
import org.prism.autowork.block.spiller.SpillerBlock;
import org.prism.autowork.block.templater.TemplaterBlock;
import org.prism.autowork.block.ticker.TickerBlock;
import org.prism.autowork.block.bulbs.toggler.TogglerBlock;
import org.prism.autowork.block.transmitter.TransmitterBlock;
import org.prism.autowork.blockhelp.HelpfulBlockItem;
import org.prism.autowork.item.ModItems;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Autowork.MODID);

    public static final TagKey<Block> CHUTES = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Autowork.MODID, "chutes"));

    public static final DeferredBlock<Block> BUFFERED_BREEZE_COLLECTOR = registerBlock("buffered_breeze_collector",
            () -> new BufferedBreezeCollectorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> REDSTONE_HUB_BLOCK = registerBlock("redstone_hub",
            () -> new RedstoneHubBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> REDSTONE_COIL = registerBlock("redstone_coil",
            () -> new RedstoneCoilBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                    .isRedstoneConductor(ModBlocks::neverConductor).lightLevel((x) -> x.getValue(BlockStateProperties.POWER))));

    public static final DeferredBlock<Block> BREEZE_COLLECTOR = registerBlock("breeze_collector",
            () -> new BreezeCollectorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> PUMP = registerBlock("pump",
            () -> new PumpBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> HOLDER = registerBlock("holder",
            () -> new HolderBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> ENRICHER = registerBlock("enricher",
            () -> new EnricherBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> PAINTER = registerBlock("painter",
            () -> new PainterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LOOM).isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> SCULK_MOVER = registerBlock("sculk_mover",
            () -> new SculkMoverBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> ENRICHED_REDSTONE_ORE = registerBlock("enriched_redstone_ore",
            () -> new RedStoneOreBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LAPIS_ORE)));

    public static final DeferredBlock<Block> DEEPSLATE_ENRICHED_REDSTONE_ORE = registerBlock("deepslate_enriched_redstone_ore",
            () -> new RedStoneOreBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_LAPIS_ORE)));

    public static final DeferredBlock<Block> ENRICHED_LAPIS_ORE = registerBlock("enriched_lapis_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.LAPIS_ORE)));

    public static final DeferredBlock<Block> DEEPSLATE_ENRICHED_LAPIS_ORE = registerBlock("deepslate_enriched_lapis_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_LAPIS_ORE)));

    public static final DeferredBlock<Block> ENRICHED_COPPER_ORE = registerBlock("enriched_copper_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_ORE)));

    public static final DeferredBlock<Block> DEEPSLATE_ENRICHED_COPPER_ORE = registerBlock("deepslate_enriched_copper_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_COPPER_ORE)));

    public static final DeferredBlock<Block> ENRICHED_DIAMOND_ORE = registerBlock("enriched_diamond_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_ORE)));

    public static final DeferredBlock<Block> DEEPSLATE_ENRICHED_DIAMOND_ORE = registerBlock("deepslate_enriched_diamond_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_DIAMOND_ORE)));

    public static final DeferredBlock<Block> ENRICHED_COAL_ORE = registerBlock("enriched_coal_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_ORE)));

    public static final DeferredBlock<Block> DEEPSLATE_ENRICHED_COAL_ORE = registerBlock("deepslate_enriched_coal_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_COAL_ORE)));

    public static final DeferredBlock<Block> ENRICHED_EMERALD_ORE = registerBlock("enriched_emerald_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_ORE)));

    public static final DeferredBlock<Block> DEEPSLATE_ENRICHED_EMERALD_ORE = registerBlock("deepslate_enriched_emerald_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_EMERALD_ORE)));

    public static final DeferredBlock<Block> ENRICHED_IRON_ORE = registerBlock("enriched_iron_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)));

    public static final DeferredBlock<Block> DEEPSLATE_ENRICHED_IRON_ORE = registerBlock("deepslate_enriched_iron_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)));

    public static final DeferredBlock<Block> ENRICHED_NETHER_GOLD_ORE = registerBlock("enriched_nether_gold_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_GOLD_ORE)));

    public static final DeferredBlock<Block> ENRICHED_NETHER_QUARTZ_ORE = registerBlock("enriched_nether_quartz_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_QUARTZ_ORE)));

    public static final DeferredBlock<Block> ENRICHED_GOLD_ORE = registerBlock("enriched_gold_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_ORE)));

    public static final DeferredBlock<Block> DEEPSLATE_ENRICHED_GOLD_ORE = registerBlock("deepslate_enriched_gold_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_GOLD_ORE)));

    public static final DeferredBlock<Block> BUFFER = registerBlock("buffer",
            () -> new BufferBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> AND_GATE = registerBlock("andgate",
            () -> new AndGateBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COMPARATOR)));

    public static final DeferredBlock<Block> TICKER = registerBlock("ticker",
            () -> new TickerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> RAILWAY_OBSERVER = registerBlock("railway_observer",
            () -> new RailwayObserverBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> PRECISE_OBSERVER = registerBlock("precise_observer",
            () -> new PreciseObserverBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> FLUID_BARREL = registerBlock("fluid_barrel",
            () -> new FluidBarrelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD).sound(SoundType.WOOD).isRedstoneConductor(ModBlocks::neverConductor)), false);

    public static final DeferredBlock<Block> TOGGLER = registerBlock("toggler",
            () -> new TogglerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor).lightLevel((x) -> x.getValue(BlockStateProperties.LIT) ? 15 : 0)));

    public static final DeferredBlock<Block> AMETHYST_BULB = registerBlock("amethyst_bulb",
            () -> new AmethystBulbBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor).lightLevel((x) -> x.getValue(BlockStateProperties.LIT) ? 15 : 0)));

    public static final DeferredBlock<Block> QUARTZ_BULB = registerBlock("quartz_bulb",
            () -> new QuartzBulbBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor).lightLevel((x) -> x.getValue(BlockStateProperties.LIT) ? 15 : 0)));

    public static final DeferredBlock<Block> TRANSMITTER = registerBlock("transmitter",
            () -> new TransmitterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> BUFFERED_CARTLOADER = registerBlock("buffered_cartloader",
            () -> new CartLoaderBufferedBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> CARTLOADER = registerBlock("cartloader",
            () -> new CartLoaderBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> CARTREFILLER = registerBlock("cartrefiller",
            () -> new CartRefillerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> BUFFERED_CARTREFILLER = registerBlock("buffered_cartrefiller",
            () -> new CartRefillerBufferedBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> CARTUNLOADER = registerBlock("cartunloader",
            () -> new CartUnloaderBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> BUFFERED_CARTUNLOADER = registerBlock("buffered_cartunloader",
            () -> new CartUnloaderBufferedBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> DISTRIBUTOR = registerBlock("distributor",
            () -> new DistributorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> CHUTE = registerBlock("chute",
            () -> new ChuteBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> TEMPLATER = registerBlock("templater",
            () -> new TemplaterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> ROTATOR = registerBlock("rotator",
            () -> new RotatorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS).isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> CARRIER = registerBlock("carrier",
            () -> new CarrierBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS).isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> MODULAR_CARRIER = registerBlock("modular_carrier",
            () -> new ModularCarrierBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS).isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> EXTRACTOR = registerBlock("extractor",
            () -> new ExtractorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> FLUID_EXTRACTOR = registerBlock("fluid_extractor",
            () -> new FluidExtractorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> SPILLER = registerBlock("spiller",
            () -> new SpillerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> SMELTER = registerBlock("smelter",
            () -> new SmelterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> FAN = registerBlock("fan",
            () -> new FanBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> DRILL = registerBlock("drill",
            () -> new DrillBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> FILTER_CHUTE = registerBlock("filter_chute",
            () -> new FilterChuteBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));

    public static final DeferredBlock<Block> PLACER = registerBlock("placer",
            () -> new PlacerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).sound(SoundType.METAL).requiresCorrectToolForDrops().isRedstoneConductor(ModBlocks::neverConductor)));


    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        return registerBlock(name, block, true);
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block, boolean registerItem) {
        DeferredBlock<T> returned = BLOCKS.register(name, block);

        if (registerItem)
            registerBlockItem(name, returned);

        return returned;
    }

    private static boolean neverConductor(BlockState p1, BlockGetter p2, BlockPos p3) {
        return false;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new HelpfulBlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
