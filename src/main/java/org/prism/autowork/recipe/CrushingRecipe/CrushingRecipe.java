package org.prism.autowork.recipe.CrushingRecipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.prism.autowork.recipe.ModRecipes;

public record CrushingRecipe(Ingredient inputItem, ItemStack result)  implements Recipe<CrushingRecipeInput> {
    @Override
    public boolean matches(CrushingRecipeInput recipeInput, Level level) {
        return inputItem.test(recipeInput.block());
    }

    @Override
    public ItemStack assemble(CrushingRecipeInput recipeInput, HolderLookup.Provider provider) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> res = NonNullList.create();
        res.add(inputItem);
        return res;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return result.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CRUSHING_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.CRUSHING_RECIPE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<CrushingRecipe> {
        public static final MapCodec<CrushingRecipe> CODEC =
                RecordCodecBuilder.mapCodec(inst ->
                        inst.group(
                                Ingredient.CODEC_NONEMPTY.fieldOf("block").forGetter(CrushingRecipe::inputItem),
                                ItemStack.CODEC.fieldOf("result").forGetter(CrushingRecipe::result)
                        ).apply(inst, CrushingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CrushingRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, CrushingRecipe::inputItem,
                        ItemStack.STREAM_CODEC, CrushingRecipe::result,
                        CrushingRecipe::new
                );

        @Override
        public MapCodec<CrushingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CrushingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
