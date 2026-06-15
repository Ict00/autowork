package org.prism.autowork.other.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

public record FluidStackComponent(Fluid fluid, int amount) {
    public static final Codec<FluidStackComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(FluidStackComponent::fluid),
                    Codec.INT.fieldOf("amount").forGetter(FluidStackComponent::amount)
            ).apply(instance, FluidStackComponent::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStackComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.FLUID), FluidStackComponent::fluid,
            ByteBufCodecs.VAR_INT, FluidStackComponent::amount,
            FluidStackComponent::new
    );



    public FluidStackComponent(FluidStack stack) {
        this(stack.getFluid(), stack.getAmount());
    }

    public FluidStack toFluidStack() {
        return new FluidStack(fluid, amount);
    }

    // Static helper for empty stacks
    public static FluidStackComponent empty() {
        return new FluidStackComponent(Fluids.EMPTY, 0);
    }

    public boolean isEmpty() {
        return amount <= 0 || fluid == Fluids.EMPTY;
    }
}