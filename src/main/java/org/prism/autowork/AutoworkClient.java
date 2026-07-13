package org.prism.autowork;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.block.can.CanItemDecorator;
import org.prism.autowork.block.filterchute.FilterChuteBlockRenderer;
import org.prism.autowork.block.fluidbarrel.FluidBarrelBlockRenderer;
import org.prism.autowork.block.holder.HolderBlockRenderer;
import org.prism.autowork.block.precise_observer.PreciseObserverRenderer;
import org.prism.autowork.compat.wPonder.AutoworkPonderPlugin;
import org.prism.autowork.entities.ModEntities;
import org.prism.autowork.entities.signal.SignalEntityModel;
import org.prism.autowork.entities.signal.SignalEntityRenderer;
import org.prism.autowork.item.ModItems;
import org.prism.autowork.particles.ModParticles;
import org.prism.autowork.particles.SignalParticles;
import org.prism.autowork.screens.ModMenus;
import org.prism.autowork.screens.breeze_collector.BreezeCollectorScreen;
import org.prism.autowork.screens.canner.CannerScreen;
import org.prism.autowork.screens.cartloader.CartLoaderScreen;
import org.prism.autowork.screens.cartrefiller.CartRefillerScreen;
import org.prism.autowork.screens.cartunloader.CartUnloaderScreen;
import org.prism.autowork.screens.drill.DrillScreen;
import org.prism.autowork.screens.enricher.EnricherScreen;
import org.prism.autowork.screens.fisher.FisherScreen;
import org.prism.autowork.screens.harvester.HarvesterScreen;
import org.prism.autowork.screens.painter.PainterScreen;
import org.prism.autowork.screens.placer.PlacerScreen;
import org.prism.autowork.screens.pump.PumpScreen;
import org.prism.autowork.screens.repair_station.RepairStationScreen;
import org.prism.autowork.screens.saw.SawScreen;
import org.prism.autowork.screens.templater.TemplaterScreen;

@Mod(value = Autowork.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Autowork.MODID, value = Dist.CLIENT)
public class AutoworkClient {
    public AutoworkClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }

    @SubscribeEvent
    public static void registerItemDecorators(RegisterItemDecorationsEvent event) {
        event.register(ModItems.CAN_ITEM.get(), new CanItemDecorator());
    }


    @SubscribeEvent
    public static void clientInit(final FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new AutoworkPonderPlugin());
    }


    @SubscribeEvent
    public static void onClientSetup(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                ModEntities.SIGNAL_ENTITY.get(),
                SignalEntityRenderer::new
        );

        ItemBlockRenderTypes.setRenderLayer(ModBlocks.HOLDER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.PAINTER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.SAW.get(), RenderType.cutout());
    }

    @SubscribeEvent
    static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.FILTER_CHUTE_BE.get(),
                FilterChuteBlockRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.PRECISE_OBSERVER_BE.get(),
                PreciseObserverRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.HOLDER_BE.get(),
                HolderBlockRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_BARREL_BE.get(),
                FluidBarrelBlockRenderer::new);
    }

    @SubscribeEvent
    public static void regScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.DRILL_MENU.get(), DrillScreen::new);
        event.register(ModMenus.PLACER_MENU.get(), PlacerScreen::new);
        event.register(ModMenus.CARTLOADER_MENU.get(), CartLoaderScreen::new);
        event.register(ModMenus.CARTUNLOADER_MENU.get(), CartUnloaderScreen::new);
        event.register(ModMenus.CARTREFILLER_MENU.get(), CartRefillerScreen::new);
        event.register(ModMenus.BREEZE_COLLECTOR_MENU.get(), BreezeCollectorScreen::new);
        event.register(ModMenus.PUMP_MENU.get(), PumpScreen::new);
        event.register(ModMenus.ENRICHER_MENU.get(), EnricherScreen::new);
        event.register(ModMenus.PAINTER_MENU.get(), PainterScreen::new);
        event.register(ModMenus.TEMPLATER_MENU.get(), TemplaterScreen::new);
        event.register(ModMenus.REPAIR_STATION_MENU.get(), RepairStationScreen::new);
        event.register(ModMenus.HARVESTER_MENU.get(), HarvesterScreen::new);
        event.register(ModMenus.SAW_MENU.get(), SawScreen::new);
        event.register(ModMenus.FISHER_MENU.get(), FisherScreen::new);
        event.register(ModMenus.CANNER_MENU.get(), CannerScreen::new);
    }

    @SubscribeEvent
    public static void regParFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.SIGNAL_PARTICLES.get(), SignalParticles.Provider::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SignalEntityModel.LAYER_LOCATION, SignalEntityModel::createBodyLayer);
    }
}
