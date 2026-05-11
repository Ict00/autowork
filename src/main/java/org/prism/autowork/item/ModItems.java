package org.prism.autowork.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.prism.autowork.Autowork;
import org.prism.autowork.other.ModData;
import org.prism.autowork.other.ModOther;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Autowork.MODID);

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
