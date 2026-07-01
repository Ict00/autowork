package org.prism.autowork.other;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.prism.autowork.Autowork;
import org.prism.autowork.other.data.FluidStackComponent;
import org.prism.autowork.other.data.template_card.TemplateCardComponent;

import java.util.function.Supplier;

public class ModData {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, Autowork.MODID);
    public static final Supplier<DataComponentType<FluidStackComponent>> BARREL_FLUID = DATA_COMPONENTS.register("barrel_fluid",
            () -> DataComponentType.<FluidStackComponent>builder().persistent(FluidStackComponent.CODEC).build());

    public static final Supplier<DataComponentType<Integer>> SCULK_CELL_EXPERIENCE = DATA_COMPONENTS.register("sculk_cell_experience",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());

    public static final Supplier<DataComponentType<TemplateCardComponent>> TEMPLATE_CARD = DATA_COMPONENTS.register("template_card",
            () -> DataComponentType.<TemplateCardComponent>builder().persistent(TemplateCardComponent.CODEC).build());

    public static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
    }
}
