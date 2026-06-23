package org.prism.autowork.recipe.CrushingRecipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record CrushingRecipeInput(ItemStack block) implements RecipeInput {
    @Override
    public ItemStack getItem(int i) {
        return block;
    }

    @Override
    public int size() {
        return 1;
    }
}
