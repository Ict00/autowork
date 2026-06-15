package org.prism.autowork.recipe.BulkSmeltRecipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record BulkSmeltRecipeInput(int amount, ItemStack stack) implements RecipeInput {
    @Override
    public ItemStack getItem(int i) {
        return stack;
    }

    @Override
    public int size() {
        return 1;
    }
}
