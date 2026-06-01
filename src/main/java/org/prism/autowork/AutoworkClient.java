package org.prism.autowork;

import ca.weblite.objc.Client;
import net.minecraft.client.particle.CampfireSmokeParticle;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.block.filterchute.FilterChuteBlockRenderer;
import org.prism.autowork.entities.ModEntities;
import org.prism.autowork.entities.signal.SignalEntityModel;
import org.prism.autowork.entities.signal.SignalEntityRenderer;
import org.prism.autowork.particles.ModParticles;
import org.prism.autowork.particles.SignalParticles;

@Mod(value = Autowork.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Autowork.MODID, value = Dist.CLIENT)
public class AutoworkClient {
    public AutoworkClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }

    @SubscribeEvent
    public static void onClientSetup(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                ModEntities.SIGNAL_ENTITY.get(),
                SignalEntityRenderer::new
        );
    }

    @SubscribeEvent
    static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.FILTER_CHUTE_BE.get(),
                FilterChuteBlockRenderer::new);
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
