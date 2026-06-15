package org.prism.autowork.compat.wJei.spilling;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
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
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.Autowork;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.recipe.SpillingRecipe.SpillingRecipe;

public class SpillingRecipeCategory implements IRecipeCategory<SpillingRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Autowork.MODID, "spilling_recipe");
    public static final RecipeType<SpillingRecipe> SPILLING_RTYPE =
            new RecipeType<>(UID, SpillingRecipe.class);

    private final IDrawable icon;
    private final IDrawable staticBackground;

    public SpillingRecipeCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.SPILLER.get()));
        this.staticBackground = helper.createDrawable(
                ResourceLocation.fromNamespaceAndPath(Autowork.MODID, "textures/gui/spilling_gui.png"),
                0, 0,
                90,
                40
        );
    }

    @Override
    public RecipeType<SpillingRecipe> getRecipeType() {
        return SPILLING_RTYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("autowork.recipe.spilling");
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
    public void draw(SpillingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);

        staticBackground.draw(guiGraphics, 0, 0);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SpillingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 7, 12).addIngredients(recipe.getIngredients().getFirst());
        builder.addSlot(RecipeIngredientRole.INPUT, 40, 12).addFluidStack(recipe.inputFluid().getFluid(), recipe.inputFluid().getAmount());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 72, 12).addItemStack(recipe.getResultItem(null));
    }
}
