
package org.prism.autowork.other.datamaps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.prism.autowork.other.ModDataMaps;

public record EnrichingMap(Item to) {
    public static final Codec<EnrichingMap> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            BuiltInRegistries.ITEM.byNameCodec().fieldOf("to").forGetter(EnrichingMap::to)
                    ).apply(instance, EnrichingMap::new)
            );

    public static ItemStack getConversion(Item source) {
        Holder<Item> itemHolder = source.getDefaultInstance().getItemHolder();
        var data = itemHolder.getData(ModDataMaps.ENRICHING_MAP);

        if (data == null) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(data.to());
    }
}
