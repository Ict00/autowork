package org.prism.autowork.other;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.prism.autowork.Autowork;
import org.prism.autowork.other.data.FluidStackComponent;

import java.util.function.Supplier;

public class ModData {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, Autowork.MODID);
    public static final Supplier<DataComponentType<FluidStackComponent>> BARREL_FLUID = DATA_COMPONENTS.register("barrel_fluid",
            () -> DataComponentType.<FluidStackComponent>builder().persistent(FluidStackComponent.CODEC).build());
    public static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
    }
}
