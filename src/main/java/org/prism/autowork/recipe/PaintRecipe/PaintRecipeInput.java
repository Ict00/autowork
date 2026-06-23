package org.prism.autowork.recipe.PaintRecipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

public record PaintRecipeInput(ItemStack dye, ItemStack stack) implements RecipeInput {
    @Override
    public ItemStack getItem(int i) {
        return stack;
    }

    @Override
    public int size() {
        return 1;
    }
}
