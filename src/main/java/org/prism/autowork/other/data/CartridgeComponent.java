package org.prism.autowork.other.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public record CartridgeComponent(Item dye, int currentUses, int color) {
    public static final Codec<CartridgeComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("dye").forGetter(CartridgeComponent::dye),
                    Codec.INT.fieldOf("uses").forGetter(CartridgeComponent::currentUses),
                    Codec.INT.fieldOf("color").forGetter(CartridgeComponent::color)
            ).apply(instance, CartridgeComponent::new)
    );

}
