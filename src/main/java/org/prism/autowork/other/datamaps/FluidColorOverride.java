package org.prism.autowork.other.datamaps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.prism.autowork.other.ModData;
import org.prism.autowork.other.ModDataMaps;

public record FluidColorOverride(int color) {
    public static final Codec<FluidColorOverride> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.INT.optionalFieldOf("color", 0xFFFFFFFF).forGetter(FluidColorOverride::color)
                    ).apply(instance, FluidColorOverride::new)
            );

    public static int getColor(Fluid source) {
        var data = source.builtInRegistryHolder().getData(ModDataMaps.FLUID_COLOR_OVERRIDES);

        if (data == null) {
            return 0xFFFFFFFF;
        }

        return data.color;
    }
}
