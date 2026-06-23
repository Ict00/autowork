package org.prism.autowork.compat.wJei.crushing;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.Autowork;
import org.prism.autowork.other.ModOther;
import org.prism.autowork.recipe.CrushingRecipe.CrushingRecipe;

public class CrushingRecipeCategory implements IRecipeCategory<CrushingRecipe> {
    private final IDrawable icon;
    private final IDrawable staticBackground;


    public static final RecipeType<CrushingRecipe> RTYPE =  RecipeType.create(Autowork.MODID, "crushing", CrushingRecipe.class);

    public CrushingRecipeCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.ENCHANTED_BOOK));

        this.staticBackground = helper.createDrawable(
                ResourceLocation.fromNamespaceAndPath(Autowork.MODID, "textures/gui/crushing_gui.png"),
                0, 0,
                90,
                40
        );
    }


    @Override
    public RecipeType<CrushingRecipe> getRecipeType() {
        return RTYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("enchantment.autowork.crushing");
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(CrushingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        staticBackground.draw(guiGraphics, 0, 0);
    }

    @Override
    public int getHeight() {
        return 40;
    }

    @Override
    public int getWidth() {
        return 100;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CrushingRecipe recipe, IFocusGroup focuses) {
        var st = new ItemStack(Items.ENCHANTED_BOOK);

        var conn = Minecraft.getInstance().getConnection();

        if (conn != null) {
            var access = conn.registryAccess();
            try {
                var x = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

                var ench = access.holder(ModOther.CRUSHING_ENCHANTMENT);

                x.set(ench.get(), 1);

                st.set(DataComponents.STORED_ENCHANTMENTS, x.toImmutable());
            }
            catch (Exception ignore) { }
        }

        builder.addSlot(RecipeIngredientRole.INPUT, 7, 13).addIngredients(recipe.getIngredients().getFirst());
        builder.addSlot(RecipeIngredientRole.INPUT, 39, 4)
                .addItemStack(st);
        builder.addSlot(RecipeIngredientRole.INPUT, 39, 22)
                .addIngredients(Ingredient.of(ModOther.TOOL_TAG));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 70, 13).addItemStack(recipe.result());
    }
}
