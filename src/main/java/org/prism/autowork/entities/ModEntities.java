package org.prism.autowork.entities;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.prism.autowork.Autowork;
import org.prism.autowork.entities.signal.SignalEntity;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Autowork.MODID);

    public static final Supplier<EntityType<SignalEntity>> SIGNAL_ENTITY = ENTITY_TYPES.register(
            "signal", () -> EntityType.Builder.<SignalEntity>of(SignalEntity::new, MobCategory.MISC)
                    .sized(0.4f, 0.4f).build("signal"));

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}
