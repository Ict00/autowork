package org.prism.autowork;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.block.drill.DrillBlockEntity;
import org.prism.autowork.block.placer.PlacerBlockEntity;
import org.prism.autowork.item.ModItems;
import org.prism.autowork.other.ModData;
import org.prism.autowork.other.ModDataMaps;
import org.prism.autowork.other.ModOther;
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
        ModOther.register(modEventBus);

        modEventBus.addListener(this::registerCapabilityProvider);
        modEventBus.addListener(this::registerDataMapTypes);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private void registerDataMapTypes(RegisterDataMapTypesEvent event) {
        event.register(ModDataMaps.CRUSHING_MAP);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    public void registerCapabilityProvider(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.DRILL_BE.get(),
                DrillBlockEntity::getCapability
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.PLACER_BE.get(),
                PlacerBlockEntity::getCapability
        );
    }
}
