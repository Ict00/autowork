package org.prism.autowork.compat.wJei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.prism.autowork.Autowork;
import org.prism.autowork.other.datamaps.CrushingMap;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class AutoworkJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Autowork.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IModPlugin.super.registerCategories(registration);

        registration.addRecipeCategories(new FakeCrushingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        IModPlugin.super.registerRecipes(registration);

        List<FakeCrushingRecipe> recipes = new ArrayList<>();

        for (Item item : BuiltInRegistries.ITEM) {

            var stack = CrushingMap.getConversion(item);


            if (!stack.isEmpty()) {
                recipes.add(new FakeCrushingRecipe(item, stack));
            }
        }

        registration.addRecipes(RecipeType.create(Autowork.MODID, "crushing", FakeCrushingRecipe.class), recipes);
    }
}