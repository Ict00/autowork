package org.prism.autowork.other.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.List;

public record CanComponent(List<FoodProperties> food, Item fullItem) {
    public static final Codec<CanComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    FoodProperties.DIRECT_CODEC.listOf().fieldOf("foods").forGetter(CanComponent::food),
                    BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("item", Items.AIR).forGetter(CanComponent::fullItem)
            ).apply(instance, CanComponent::new)
    );

    public @Nullable FoodProperties peek() {
        try {
            return food.getLast();
        }
        catch (Exception ignore) {
            System.out.println(ignore);
            return null;
        }
    }

    public @Nullable FoodProperties pop() {
        try {
            return food.removeLast();
        }
        catch (Exception ignore) {
            System.out.println(ignore);
            return null;
        }
    }
}
