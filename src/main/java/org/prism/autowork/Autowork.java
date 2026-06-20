package org.prism.autowork;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ItemInteractionResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.block.breezecollector.buffered.BufferedBreezeCollectorBlockEntity;
import org.prism.autowork.block.buffer.BufferBlockEntity;
import org.prism.autowork.block.cart_manipulators.buffered.loader.CartLoaderBufferedBlockEntity;
import org.prism.autowork.block.cart_manipulators.buffered.refiller.CartRefillerBufferedBlockEntity;
import org.prism.autowork.block.cart_manipulators.buffered.unloader.CartUnloaderBufferedBlockEntity;
import org.prism.autowork.block.drill.DrillBlockEntity;
import org.prism.autowork.block.enricher.EnricherBlockEntity;
import org.prism.autowork.block.fluidbarrel.FluidBarrelBlockEntity;
import org.prism.autowork.block.fluidbarrel.FluidBarrelItemWrapper;
import org.prism.autowork.block.holder.HolderBlockEntity;
import org.prism.autowork.block.placer.PlacerBlockEntity;
import org.prism.autowork.block.pump.PumpBlockEntity;
import org.prism.autowork.entities.ModEntities;
import org.prism.autowork.item.ModItems;
import org.prism.autowork.other.ModData;
import org.prism.autowork.other.ModDataMaps;
import org.prism.autowork.other.ModOther;
import org.prism.autowork.particles.ModParticles;
import org.prism.autowork.recipe.ModRecipes;
import org.prism.autowork.screens.ModMenus;
import org.slf4j.Logger;

@Mod(Autowork.MODID)
public class Autowork {
    public static final String MODID = "autowork";
    private static final Logger LOGGER = LogUtils.getLogger();
    public Autowork(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        ModBlocks.register(modEventBus);
        ModData.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenus.register(modEventBus);
        ModEntities.register(modEventBus);
        ModOther.register(modEventBus);
        ModParticles.register(modEventBus);
        ModRecipes.register(modEventBus);

        modEventBus.addListener(this::registerCapabilityProvider);
        modEventBus.addListener(this::registerDataMapTypes);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private void registerDataMapTypes(RegisterDataMapTypesEvent event) {
        event.register(ModDataMaps.CRUSHING_MAP);
        event.register(ModDataMaps.FLUID_COLOR_OVERRIDES);
        event.register(ModDataMaps.ENRICHING_MAP);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    public static ResourceLocation loc(String id) {
        return ResourceLocation.fromNamespaceAndPath(Autowork.MODID, id);
    }

    public void registerCapabilityProvider(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.DRILL_BE.get(),
                DrillBlockEntity::getCapability
        );

        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.HOLDER_BE.get(),
                HolderBlockEntity::getProxyFluid
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.HOLDER_BE.get(),
                HolderBlockEntity::getProxyItem
        );

        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.FLUID_BARREL_BE.get(),
                FluidBarrelBlockEntity::getCapability
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ENRICHER_BE.get(),
                EnricherBlockEntity::getCapability
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.PLACER_BE.get(),
                PlacerBlockEntity::getCapability
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.BUFFER_BE.get(),
                BufferBlockEntity::getCapability
        );


        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.PUMP_BE.get(),
                PumpBlockEntity::getCapability
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.CARTLOADER_BUFFERED_BE.get(),
                CartLoaderBufferedBlockEntity::getCapability
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.CARTUNLOADER_BUFFERED_BE.get(),
                CartUnloaderBufferedBlockEntity::getCapability
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.CARTREFILLER_BUFFERED_BE.get(),
                CartRefillerBufferedBlockEntity::getCapability
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.BUFFERED_BREEZE_COLLECTOR_BE.get(),
                BufferedBreezeCollectorBlockEntity::getCapability
        );

        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (itemStack, context) -> new FluidBarrelItemWrapper(itemStack),
                ModItems.FLUID_BARREL_ITEM.get());
    }
}
