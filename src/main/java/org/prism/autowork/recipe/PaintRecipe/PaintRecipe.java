package org.prism.autowork.recipe.PaintRecipe;

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

public record PaintRecipe(Ingredient dye, Ingredient input, ItemStack result)  implements Recipe<PaintRecipeInput> {
    @Override
    public boolean matches(PaintRecipeInput input, Level level) {
        return this.input.test(input.stack()) && this.dye.test(input.dye());
    }

    @Override
    public ItemStack assemble(PaintRecipeInput input, HolderLookup.Provider provider) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> res = NonNullList.create();
        res.add(input);
        res.add(dye);
        return res;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return result.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.PAINTING_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.PAINTING_RECIPE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<PaintRecipe> {
        public static final MapCodec<PaintRecipe> CODEC =
                RecordCodecBuilder.mapCodec(inst ->
                        inst.group(
                                Ingredient.CODEC_NONEMPTY.fieldOf("dye").forGetter(PaintRecipe::dye),
                                Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(PaintRecipe::input),
                                ItemStack.CODEC.fieldOf("result").forGetter(PaintRecipe::result)
                        ).apply(inst, PaintRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, PaintRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, PaintRecipe::dye,
                        Ingredient.CONTENTS_STREAM_CODEC, PaintRecipe::input,
                        ItemStack.STREAM_CODEC, PaintRecipe::result,
                        PaintRecipe::new
                );

        @Override
        public MapCodec<PaintRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PaintRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
