package org.prism.autowork.recipe.BulkSmeltRecipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.prism.autowork.recipe.ModRecipes;
import org.prism.autowork.recipe.SpillingRecipe.SpillingRecipe;

public record BulkSmeltRecipe(int amount, Ingredient inputItem, ItemStack result)  implements Recipe<BulkSmeltRecipeInput> {
    @Override
    public boolean matches(BulkSmeltRecipeInput bulkSmeltRecipeInput, Level level) {
        return bulkSmeltRecipeInput.amount() >= amount && inputItem.test(bulkSmeltRecipeInput.stack());
    }

    @Override
    public ItemStack assemble(BulkSmeltRecipeInput bulkSmeltRecipeInput, HolderLookup.Provider provider) {
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
        return ModRecipes.BULK_SMELTING_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.BULK_SMELTING_RECIPE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<BulkSmeltRecipe> {
        public static final MapCodec<BulkSmeltRecipe> CODEC =
                RecordCodecBuilder.mapCodec(inst ->
                        inst.group(
                                Codec.INT.fieldOf("amount").forGetter(BulkSmeltRecipe::amount),
                                Ingredient.CODEC_NONEMPTY.fieldOf("block").forGetter(BulkSmeltRecipe::inputItem),
                                ItemStack.CODEC.fieldOf("result").forGetter(BulkSmeltRecipe::result)
                        ).apply(inst, BulkSmeltRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, BulkSmeltRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.fromCodec(Codec.INT), BulkSmeltRecipe::amount,
                        Ingredient.CONTENTS_STREAM_CODEC, BulkSmeltRecipe::inputItem,
                        ItemStack.STREAM_CODEC, BulkSmeltRecipe::result,
                        BulkSmeltRecipe::new
                );

        @Override
        public MapCodec<BulkSmeltRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BulkSmeltRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
