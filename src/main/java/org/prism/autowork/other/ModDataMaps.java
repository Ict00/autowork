package org.prism.autowork.other;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.prism.autowork.Autowork;
import org.prism.autowork.other.datamaps.CrushingMap;

public class ModDataMaps {
    public static final DataMapType<Item, CrushingMap> CRUSHING_MAP =
            DataMapType.builder(
                    ResourceLocation.fromNamespaceAndPath(Autowork.MODID, "crushing"),
                    Registries.ITEM,
                    CrushingMap.CODEC
            ).synced(CrushingMap.CODEC, true).build();
}
