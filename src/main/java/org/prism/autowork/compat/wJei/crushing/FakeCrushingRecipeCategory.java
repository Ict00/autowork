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
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.Autowork;

public class FakeCrushingRecipeCategory implements IRecipeCategory<FakeCrushingRecipe> {
    private final IDrawable icon;

    public FakeCrushingRecipeCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.ENCHANTED_BOOK));
    }


    @Override
    public RecipeType<FakeCrushingRecipe> getRecipeType() {
        return RecipeType.create(Autowork.MODID, "crushing", FakeCrushingRecipe.class);
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
    public void draw(FakeCrushingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.translatable("autowork.crushing_description"),
                1,
                1,
                0x404040,
                false
        );
    }

    @Override
    public int getHeight() {
        return 40;
    }

    @Override
    public int getWidth() {
        return 180;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FakeCrushingRecipe recipe, IFocusGroup focuses) {
        var st = new ItemStack(Items.ENCHANTED_BOOK);

        builder.addSlot(RecipeIngredientRole.INPUT, 0+55, 15).addItemLike(recipe.input());
        builder.addSlot(RecipeIngredientRole.INPUT, 20+55, 15)
                .addItemStack(st);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 50+55, 15).addItemStack(recipe.output());
    }
}
