package org.prism.autowork.particles;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.particle.AshParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.prism.autowork.Autowork;

import java.util.function.Supplier;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>>
            PARTICLE_TYPES =
            DeferredRegister.create(
                    BuiltInRegistries.PARTICLE_TYPE,
                    Autowork.MODID
            );

    public static final Supplier<SimpleParticleType> SIGNAL_PARTICLES = PARTICLE_TYPES.register("signal_particles",
            () -> new SimpleParticleType(true));

    public static void register(IEventBus bus) {
        PARTICLE_TYPES.register(bus);
    }
}
