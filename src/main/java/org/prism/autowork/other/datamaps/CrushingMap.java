package org.prism.autowork.other.datamaps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.prism.autowork.other.ModDataMaps;

import java.util.ArrayList;

public record CrushingMap(Item to, int count) {
    public static final Codec<CrushingMap> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            BuiltInRegistries.ITEM.byNameCodec().fieldOf("to").forGetter(CrushingMap::to),
                            Codec.INT.optionalFieldOf("count", 1).forGetter(CrushingMap::count)
                    ).apply(instance, CrushingMap::new)
            );

    public static ItemStack getConversion(Item source) {
        Holder<Item> itemHolder = source.getDefaultInstance().getItemHolder();
        var data = itemHolder.getData(ModDataMaps.CRUSHING_MAP);

        if (data == null) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(data.to(), data.count());
    }
}
