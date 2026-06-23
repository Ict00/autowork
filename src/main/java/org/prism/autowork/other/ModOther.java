package org.prism.autowork.other;

import com.mojang.serialization.MapCodec;
import net.mcexpanded.fancytabsections.FancyTabSections;
import net.mcexpanded.fancytabsections.creativetab.ConglomerateOfItems;
import net.mcexpanded.fancytabsections.creativetab.SectionColored;
import net.mcexpanded.fancytabsections.creativetab.SectionTextured;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.TicketType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.prism.autowork.Autowork;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.item.ModItems;
import org.prism.autowork.other.lmod.CrushingModifier;

import java.util.Comparator;
import java.util.function.Supplier;

public class ModOther {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>>
            LOOT_MODIFIERS =
            DeferredRegister.create(
                    NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,
                    Autowork.MODID
            );

    public static final TicketType<ChunkPos> MINECART_TICKET = TicketType.create("minecart_chest_loader", Comparator.comparingLong(ChunkPos::toLong), 100);

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

                    })
                    .build());

    static void addItems() {
        var tabRs = ResourceLocation.fromNamespaceAndPath(Autowork.MODID, "autowork_tab");

        FancyTabSections.addSection(tabRs,
                new SectionTextured(
                        Autowork.loc("misc"),
                        Component.translatable("creativetab.autowork.misc"),
                        Autowork.loc("textures/gui/sections/misc.png"),
                        0xfffcd442,
                        ConglomerateOfItems.create()
                                .add(ModBlocks.HOLDER)
                                .add(ModItems.WRENCH)
                                .add(ModItems.REDSTONE_CHARGE)
                )
        );

        FancyTabSections.addSection(tabRs,
                new SectionTextured(
                        Autowork.loc("base_machines"),
                        Component.translatable("creativetab.autowork.base_machines"),
                        Autowork.loc("textures/gui/sections/base_machines.png"),
                        0xfffcfcfc,
                        ConglomerateOfItems.create()
                                .add(ModBlocks.CHUTE)
                                .add(ModBlocks.FILTER_CHUTE)
                                .add(ModBlocks.EXTRACTOR)
                                .add(ModBlocks.DRILL)
                                .add(ModBlocks.PLACER)
                                .add(ModBlocks.FAN)
                                .add(ModBlocks.BREEZE_COLLECTOR)
                                .add(ModBlocks.BUFFERED_BREEZE_COLLECTOR)
                                .add(ModBlocks.BUFFER)
                                .add(ModBlocks.DISTRIBUTOR)
                                .add(ModBlocks.SCULK_MOVER)
                                .add(ModBlocks.ENRICHER)
                                .add(ModBlocks.PAINTER)
                )
        );

        FancyTabSections.addSection(tabRs,
                new SectionTextured(
                        Autowork.loc("railway"),
                        Component.translatable("creativetab.autowork.railway"),
                        Autowork.loc("textures/gui/sections/railway.png"),
                        0xfffcfcfc,
                        ConglomerateOfItems.create()
                                .add(ModBlocks.RAILWAY_OBSERVER)
                                .add(ModBlocks.CARTLOADER)
                                .add(ModBlocks.CARTUNLOADER)
                                .add(ModBlocks.CARTREFILLER)
                                .add(ModBlocks.BUFFERED_CARTLOADER)
                                .add(ModBlocks.BUFFERED_CARTUNLOADER)
                                .add(ModBlocks.BUFFERED_CARTREFILLER)
                )
        );

        FancyTabSections.addSection(tabRs,
                new SectionTextured(
                        Autowork.loc("fluids"),
                        Component.translatable("creativetab.autowork.fluids"),
                        Autowork.loc("textures/gui/sections/fluids.png"),
                        0xff78c7ff,
                        ConglomerateOfItems.create()
                                .add(ModBlocks.FLUID_EXTRACTOR)
                                .add(ModBlocks.SPILLER)
                                .add(ModBlocks.SMELTER)
                                .add(ModBlocks.FLUID_BARREL)
                                .add(ModBlocks.PUMP)
                )
        );

        FancyTabSections.addSection(tabRs,
                new SectionTextured(
                        Autowork.loc("redstone"),
                        Component.translatable("creativetab.autowork.redstone"),
                        Autowork.loc("textures/gui/sections/redstone.png"),
                        0xfffc5947,
                        ConglomerateOfItems.create()
                                .add(ModBlocks.AND_GATE)
                                .add(ModBlocks.TRANSMITTER)
                                .add(ModBlocks.TOGGLER)
                                .add(ModBlocks.TICKER)
                                .add(ModBlocks.PRECISE_OBSERVER)
                                .add(ModBlocks.REDSTONE_HUB_BLOCK)
                                .add(ModBlocks.REDSTONE_COIL)
                )
        );

        FancyTabSections.addSection(tabRs,
                new SectionTextured(
                        Autowork.loc("ores"),
                        Component.translatable("creativetab.autowork.ores"),
                        Autowork.loc("textures/gui/sections/ores.png"),
                        0xfffcfcfc,
                        ConglomerateOfItems.create()
                                .add(ModBlocks.ENRICHER)
                                .add(ModBlocks.ENRICHED_NETHER_GOLD_ORE)
                                .add(ModBlocks.ENRICHED_NETHER_QUARTZ_ORE)
                                .add(ModBlocks.ENRICHED_COAL_ORE)
                                .add(ModBlocks.ENRICHED_IRON_ORE)
                                .add(ModBlocks.ENRICHED_COPPER_ORE)
                                .add(ModBlocks.ENRICHED_GOLD_ORE)
                                .add(ModBlocks.ENRICHED_LAPIS_ORE)
                                .add(ModBlocks.ENRICHED_REDSTONE_ORE)
                                .add(ModBlocks.ENRICHED_DIAMOND_ORE)
                                .add(ModBlocks.ENRICHED_EMERALD_ORE)
                                .add(ModBlocks.DEEPSLATE_ENRICHED_COAL_ORE)
                                .add(ModBlocks.DEEPSLATE_ENRICHED_IRON_ORE)
                                .add(ModBlocks.DEEPSLATE_ENRICHED_COPPER_ORE)
                                .add(ModBlocks.DEEPSLATE_ENRICHED_GOLD_ORE)
                                .add(ModBlocks.DEEPSLATE_ENRICHED_LAPIS_ORE)
                                .add(ModBlocks.DEEPSLATE_ENRICHED_REDSTONE_ORE)
                                .add(ModBlocks.DEEPSLATE_ENRICHED_DIAMOND_ORE)
                                .add(ModBlocks.DEEPSLATE_ENRICHED_EMERALD_ORE)
                )
        );
    }

    public static final ResourceKey<Enchantment> CRUSHING_ENCHANTMENT = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(Autowork.MODID, "crushing")
    );

    public static final TagKey<Item> TOOL_TAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Autowork.MODID, "tools"));
    public static final TagKey<Block> FIXABLE_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Autowork.MODID, "fixable"));
    public static final TagKey<Block> GLASSES = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "glass_blocks"));

    public static final TagKey<Block> BLOCK_ENTITY_MOVABLE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Autowork.MODID, "block_entity_movable"));
    public static final TagKey<Block> MOVABLE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Autowork.MODID, "movable"));


    public static void register(IEventBus bus) {
        LOOT_MODIFIERS.register(bus);
        CREATIVE_MODE_TABS.register(bus);
        addItems();
    }
}
