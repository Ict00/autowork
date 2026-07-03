package org.prism.autowork.compat.wJei.painting;

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
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.Autowork;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.item.ModItems;
import org.prism.autowork.other.ModData;
import org.prism.autowork.other.data.CartridgeComponent;
import org.prism.autowork.recipe.PaintRecipe.PaintRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class PaintingRecipeCategory implements IRecipeCategory<PaintRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Autowork.MODID, "painting_recipe");
    public static final RecipeType<PaintRecipe> RTYPE =
            new RecipeType<>(UID, PaintRecipe.class);

    private final IDrawable icon;
    private final IDrawable staticBackground;

    public PaintingRecipeCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.PAINTER.get()));
        this.staticBackground = helper.createDrawable(
                ResourceLocation.fromNamespaceAndPath(Autowork.MODID, "textures/gui/painting_gui.png"),
                0, 0,
                90,
                40
        );
    }

    @Override
    public RecipeType<PaintRecipe> getRecipeType() {
        return RTYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("autowork.recipe.painting");
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
    public void draw(PaintRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);

        staticBackground.draw(guiGraphics, 0, 0);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PaintRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 7, 12).addIngredients(recipe.getIngredients().getFirst());

        Ingredient target;
        var dye = recipe.getIngredients().get(1);
        var items = dye.getItems();
        ArrayList<ItemStack> newItems = new ArrayList<>();
        for (var item : items) {
            if (item.getItem() instanceof DyeItem dyeItem) {
                var newStack = new ItemStack(ModItems.DYE_CARTRIDGE.get());
                newStack.set(ModData.CARTRIDGE, new CartridgeComponent(dyeItem, item.getCount()*8, dyeItem.getDyeColor().getTextureDiffuseColor()));
                newItems.add(newStack);
            }
        }

        if (newItems.isEmpty()) {
            target = dye;
        }
        else {
            target = Ingredient.of(Stream.concat(Arrays.stream(items), Arrays.stream(newItems.toArray(new ItemStack[0]))));
        }

        builder.addSlot(RecipeIngredientRole.INPUT, 40, 12).addIngredients(target);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 72, 12).addItemStack(recipe.getResultItem(null));
    }
}
