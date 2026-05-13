package org.prism.autowork;

import ca.weblite.objc.Client;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.prism.autowork.block.ModBlockEntities;
import org.prism.autowork.block.filterchute.FilterChuteBlockRenderer;

@Mod(value = Autowork.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Autowork.MODID, value = Dist.CLIENT)
public class AutoworkClient {
    public AutoworkClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {

    }

    @SubscribeEvent
    static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.FILTER_CHUTE_BE.get(),
                FilterChuteBlockRenderer::new);
    }
}
