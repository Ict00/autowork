package org.prism.autowork.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.prism.autowork.Autowork;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.block.fluidbarrel.FluidBarrelItem;
import org.prism.autowork.block.sculk_cell.SculkCellItem;
import org.prism.autowork.item.custom.DyeCartridge;
import org.prism.autowork.item.custom.RedstoneChargeItem;
import org.prism.autowork.item.custom.TemplateCardItem;
import org.prism.autowork.item.custom.WrenchItem;
import org.prism.autowork.other.ModData;
import org.prism.autowork.other.ModOther;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Autowork.MODID);
    public static final DeferredItem<Item> REDSTONE_CHARGE = ITEMS.register("redstone_charge", () -> new RedstoneChargeItem(new Item.Properties()));
    public static final DeferredItem<Item> WRENCH = ITEMS.register("wrench", () -> new WrenchItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> TEMPLATE_CARD = ITEMS.register("template_card", () -> new TemplateCardItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> FLUID_BARREL_ITEM = ITEMS.register("fluid_barrel", () -> new FluidBarrelItem(ModBlocks.FLUID_BARREL.get(), new Item.Properties()));
    public static final DeferredItem<Item> SCULK_CELL_ITEM = ITEMS.register("sculk_cell", () -> new SculkCellItem(ModBlocks.SCULK_CELL.get(), new Item.Properties()));
    public static final DeferredItem<Item> DYE_CARTRIDGE = ITEMS.register("dye_cartridge", () -> new DyeCartridge(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
