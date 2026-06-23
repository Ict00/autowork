package org.prism.autowork.compat.wJei.enriching;

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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.Autowork;
import org.prism.autowork.block.ModBlocks;

public class FakeEnrichingRecipeCategory implements IRecipeCategory<FakeEnrichingRecipe>{
    private final IDrawable icon;
    public static final RecipeType<FakeEnrichingRecipe> RTYPE =  RecipeType.create(Autowork.MODID, "enriching", FakeEnrichingRecipe.class);

    private final IDrawable staticBackground;

    public FakeEnrichingRecipeCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.ENRICHER));
        this.staticBackground = helper.createDrawable(
                ResourceLocation.fromNamespaceAndPath(Autowork.MODID, "textures/gui/enriching_gui.png"),
                0, 0,
                90,
                40
        );
    }

    @Override
    public void draw(FakeEnrichingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);

        staticBackground.draw(guiGraphics, 0, 0);
    }

    @Override
    public RecipeType<FakeEnrichingRecipe> getRecipeType() {
        return RTYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("autowork.recipe.enriching");
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
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
    public void setRecipe(IRecipeLayoutBuilder builder, FakeEnrichingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 7, 12).addItemLike(recipe.input());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 72, 12).addItemStack(recipe.output());

    }
}