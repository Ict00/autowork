package org.prism.autowork.other.data.template_card;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import org.prism.autowork.other.data.FluidStackComponent;

public record SlotItemPair(int slot, int count, Item item) {
    public static final Codec<SlotItemPair> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("slot").forGetter(SlotItemPair::slot),
                    Codec.INT.fieldOf("count").forGetter(SlotItemPair::count),
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(SlotItemPair::item)
            ).apply(instance, SlotItemPair::new)
    );


}
