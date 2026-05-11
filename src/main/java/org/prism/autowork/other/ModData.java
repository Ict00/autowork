package org.prism.autowork.other;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.prism.autowork.Autowork;

import java.util.function.Supplier;

public class ModData {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, Autowork.MODID);

    public static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
    }
}
