package org.prism.autowork.screens;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.prism.autowork.Autowork;
import org.prism.autowork.block.repair_station.RepairStationBlockEntity;
import org.prism.autowork.screens.breeze_collector.BreezeCollectorMenu;
import org.prism.autowork.screens.canner.CannerMenu;
import org.prism.autowork.screens.cartloader.CartLoaderMenu;
import org.prism.autowork.screens.cartrefiller.CartRefillerMenu;
import org.prism.autowork.screens.cartunloader.CartUnloaderMenu;
import org.prism.autowork.screens.drill.DrillMenu;
import org.prism.autowork.screens.enricher.EnricherMenu;
import org.prism.autowork.screens.fisher.FisherMenu;
import org.prism.autowork.screens.harvester.HarvesterMenu;
import org.prism.autowork.screens.painter.PainterMenu;
import org.prism.autowork.screens.placer.PlacerMenu;
import org.prism.autowork.screens.pump.PumpMenu;
import org.prism.autowork.screens.repair_station.RepairStationMenu;
import org.prism.autowork.screens.saw.SawMenu;
import org.prism.autowork.screens.templater.TemplaterMenu;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Autowork.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<TemplaterMenu>> TEMPLATER_MENU =
            regType("templater_menu", TemplaterMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<DrillMenu>> DRILL_MENU =
            regType("drill_menu", DrillMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<CannerMenu>> CANNER_MENU =
            regType("canner_menu", CannerMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<HarvesterMenu>> HARVESTER_MENU =
            regType("harvester_menu", HarvesterMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<SawMenu>> SAW_MENU =
            regType("saw_menu", SawMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<FisherMenu>> FISHER_MENU =
            regType("fisher_menu", FisherMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<PainterMenu>> PAINTER_MENU =
            regType("painter_menu", PainterMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<PumpMenu>> PUMP_MENU =
            regType("pump_menu", PumpMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<EnricherMenu>> ENRICHER_MENU =
            regType("enricher_menu", EnricherMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<PlacerMenu>> PLACER_MENU =
            regType("placer_menu", PlacerMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<BreezeCollectorMenu>> BREEZE_COLLECTOR_MENU =
            regType("breeze_collector_menu", BreezeCollectorMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<CartRefillerMenu>> CARTREFILLER_MENU =
            regType("cartrefiller_menu", CartRefillerMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<CartLoaderMenu>> CARTLOADER_MENU =
            regType("cartloader_menu", CartLoaderMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<RepairStationMenu>> REPAIR_STATION_MENU =
            regType("repair_station_menu", RepairStationMenu::new);


    public static final DeferredHolder<MenuType<?>, MenuType<CartUnloaderMenu>> CARTUNLOADER_MENU =
            regType("cartunloader_menu", CartUnloaderMenu::new);

    private static <T extends AbstractContainerMenu>DeferredHolder<MenuType<?>, MenuType<T>> regType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
