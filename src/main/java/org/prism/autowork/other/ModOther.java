package org.prism.autowork.other;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.prism.autowork.Autowork;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.other.lmod.CrushingModifier;

import java.util.function.Supplier;

public class ModOther {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>>
            LOOT_MODIFIERS =
            DeferredRegister.create(
                    NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,
                    Autowork.MODID
            );

    public static final DeferredHolder<
                MapCodec<? extends IGlobalLootModifier>,
                MapCodec<CrushingModifier>
                > CRUSHING =
            LOOT_MODIFIERS.register(
                    "crushing",
                    () -> CrushingModifier.CODEC
            );

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Autowork.MODID);
    public static final Supplier<CreativeModeTab> AUTOWORK_TAB = CREATIVE_MODE_TABS.register("autowork_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(Items.ITEM_FRAME))
                    .title(Component.translatable("itemGroup.autowork"))
                    .displayItems((idp, output) -> {
                        output.accept(ModBlocks.CHUTE);
                        output.accept(ModBlocks.FILTER_CHUTE);
                        output.accept(ModBlocks.FAN);
                        output.accept(ModBlocks.DRILL);
                        output.accept(ModBlocks.EXTRACTOR);
                        output.accept(ModBlocks.PLACER);
                        output.accept(ModBlocks.BREEZE_COLLECTOR);
                        output.accept(ModBlocks.CARTLOADER);
                        output.accept(ModBlocks.CARTUNLOADER);
                        output.accept(ModBlocks.TICKER);
                        output.accept(ModBlocks.DISTRIBUTOR);
                    })
                    .build());

    public static final ResourceKey<Enchantment> CRUSHING_ENCHANTMENT = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Autowork.MODID, "crushing")
    );

    public static final TagKey<Item> TOOL_TAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Autowork.MODID, "tools"));

    public static void register(IEventBus bus) {
        LOOT_MODIFIERS.register(bus);
        CREATIVE_MODE_TABS.register(bus);
    }
}
