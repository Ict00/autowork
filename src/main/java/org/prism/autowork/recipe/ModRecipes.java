package org.prism.autowork.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.prism.autowork.Autowork;
import org.prism.autowork.recipe.BulkSmeltRecipe.BulkSmeltRecipe;
import org.prism.autowork.recipe.SpillingRecipe.SpillingRecipe;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Autowork.MODID);

    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, Autowork.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<SpillingRecipe>> SPILLING_RECIPE_TYPE =
            TYPES.register("spilling", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return "spilling";
                }
            });

    public static final DeferredHolder<RecipeType<?>, RecipeType<BulkSmeltRecipe>> BULK_SMELTING_RECIPE_TYPE =
            TYPES.register("bulk_smelting", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return "bulk_smelting";
                }
            });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BulkSmeltRecipe>> BULK_SMELTING_RECIPE_SERIALIZER =
            SERIALIZERS.register("bulk_smelting", BulkSmeltRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SpillingRecipe>> SPILLING_RECIPE_SERIALIZER =
            SERIALIZERS.register("spilling", SpillingRecipe.Serializer::new);
    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
        TYPES.register(bus);
    }
}
