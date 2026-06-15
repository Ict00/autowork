package org.prism.autowork.recipe.SpillingRecipe;

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
import net.neoforged.neoforge.fluids.FluidStack;
import org.prism.autowork.recipe.ModRecipes;

public record SpillingRecipe(FluidStack inputFluid, Ingredient inputItem, ItemStack result) implements Recipe<SpillingRecipeInput> {
    @Override
    public boolean matches(SpillingRecipeInput input, Level level) {
        if (level.isClientSide) return false;

        return this.inputFluid.is(input.fluidStack().getFluid()) && this.inputItem.test(input.getItem(0)) &&
                inputFluid.getAmount() <= input.fluidStack().getAmount();
    }

    @Override
    public ItemStack assemble(SpillingRecipeInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> res = NonNullList.create();
        res.add(inputItem);
        return res;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SPILLING_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.SPILLING_RECIPE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<SpillingRecipe> {
        public static final MapCodec<SpillingRecipe> CODEC =
                RecordCodecBuilder.mapCodec(inst ->
                        inst.group(
                                FluidStack.CODEC.fieldOf("fluid").forGetter(SpillingRecipe::inputFluid),
                                Ingredient.CODEC_NONEMPTY.fieldOf("block").forGetter(SpillingRecipe::inputItem),
                                ItemStack.CODEC.fieldOf("result").forGetter(SpillingRecipe::result)
                        ).apply(inst, SpillingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, SpillingRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        FluidStack.STREAM_CODEC, SpillingRecipe::inputFluid,
                        Ingredient.CONTENTS_STREAM_CODEC, SpillingRecipe::inputItem,
                        ItemStack.STREAM_CODEC, SpillingRecipe::result,
                        SpillingRecipe::new
                );

        @Override
        public MapCodec<SpillingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SpillingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
