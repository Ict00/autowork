package org.prism.autowork.other.data.template_card;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public record ItemCountPair(int count, Item item) {
    public static final Codec<ItemCountPair> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("count").forGetter(ItemCountPair::count),
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(ItemCountPair::item)
            ).apply(instance, ItemCountPair::new)
    );
}
