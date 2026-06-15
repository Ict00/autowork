package org.prism.autowork.compat.wJei.smelting;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.Autowork;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.recipe.BulkSmeltRecipe.BulkSmeltRecipe;
import org.prism.autowork.recipe.SpillingRecipe.SpillingRecipe;

public class SmeltingRecipeCategory implements IRecipeCategory<BulkSmeltRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Autowork.MODID, "smelting_recipe");
    public static final RecipeType<BulkSmeltRecipe> SMELTING_RTYPE =
            new RecipeType<>(UID, BulkSmeltRecipe.class);

    private final IDrawable icon;
    private final IDrawable staticBackground;

    public SmeltingRecipeCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.SMELTER.get()));
        this.staticBackground = helper.createDrawable(
                ResourceLocation.fromNamespaceAndPath(Autowork.MODID, "textures/gui/smelting_gui.png"),
                0, 0,
                90,
                40
        );
    }

    @Override
    public RecipeType<BulkSmeltRecipe> getRecipeType() {
        return SMELTING_RTYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("autowork.recipe.smelting");
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
    public void getTooltip(ITooltipBuilder tooltip, BulkSmeltRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        IRecipeCategory.super.getTooltip(tooltip, recipe, recipeSlotsView, mouseX, mouseY);

        if (mouseX >= 39 && mouseX < 39 + 14 &&
                mouseY >= 11 && mouseY < 11 + 14) {
            tooltip.add(Component.translatable("autowork.recipe.required", recipe.amount()));
        }
    }

    @Override
    public void draw(BulkSmeltRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);

        staticBackground.draw(guiGraphics, 0, 0);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BulkSmeltRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 7, 12).addIngredients(recipe.getIngredients().getFirst());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 72, 12).addItemStack(recipe.getResultItem(null));
    }
}
