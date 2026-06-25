package org.prism.autowork.other.data.template_card;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record TemplateCardComponent(Map<Direction, List<SlotItemPair>> config, Block apply, List<ItemCountPair> items) {
    public static final Codec<TemplateCardComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(Direction.CODEC, SlotItemPair.CODEC.listOf()).fieldOf("config").forGetter(TemplateCardComponent::config),
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("apply").forGetter(TemplateCardComponent::apply),
                    ItemCountPair.CODEC.listOf().fieldOf("items").forGetter(TemplateCardComponent::items)
            ).apply(instance, TemplateCardComponent::new)
    );

    public static final TemplateCardComponent EMPTY = new TemplateCardComponent(new HashMap<>(), Blocks.BARREL, new ArrayList<>());

    public TemplateCardComponent setBlock(Block block) {
        return new TemplateCardComponent(this.config, block, this.items);
    }

    public TemplateCardComponent addItems(IItemHandler inv, Direction dir) {
        if (this.config.containsKey(dir)) {
            var newMap = new HashMap<>(this.config);
            newMap.remove(dir);

            return new TemplateCardComponent(newMap, apply, items);
        }


        HashMap<Item, Integer> itemCount = new HashMap<>();
        List<SlotItemPair> slots = new ArrayList<>();


        for (int i = 0; i < inv.getSlots(); i++) {
            var stack = inv.getStackInSlot(i);

            if (!stack.isEmpty()) {
                slots.add(new SlotItemPair(i, stack.getCount(), stack.getItem()));

                if (itemCount.containsKey(stack.getItem())) {
                    itemCount.put(stack.getItem(), itemCount.get(stack.getItem())+stack.getCount());
                }
                else {
                    itemCount.put(stack.getItem(), stack.getCount());
                }
            }
        }

        for (var item : this.items) {
            if (itemCount.containsKey(item.item())) {
                itemCount.put(item.item(), itemCount.get(item.item())+item.count());
            }
            else {
                itemCount.put(item.item(), item.count());
            }
        }

        ArrayList<ItemCountPair> countPairs = new ArrayList<>();

        for (var item : itemCount.entrySet()) {
            countPairs.add(new ItemCountPair(item.getValue(), item.getKey()));
        }

        HashMap<Direction, List<SlotItemPair>> newMap = new HashMap<>(this.config);
        newMap.put(dir, slots);

        return new TemplateCardComponent(newMap, this.apply, countPairs);
    }
}
