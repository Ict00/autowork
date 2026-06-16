package org.prism.autowork.compat.wJei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.prism.autowork.Autowork;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.compat.wJei.crushing.FakeCrushingRecipe;
import org.prism.autowork.compat.wJei.crushing.FakeCrushingRecipeCategory;
import org.prism.autowork.compat.wJei.smelting.SmeltingRecipeCategory;
import org.prism.autowork.compat.wJei.spilling.SpillingRecipeCategory;
import org.prism.autowork.other.ModOther;
import org.prism.autowork.other.datamaps.CrushingMap;
import org.prism.autowork.recipe.BulkSmeltRecipe.BulkSmeltRecipe;
import org.prism.autowork.recipe.ModRecipes;
import org.prism.autowork.recipe.SpillingRecipe.SpillingRecipe;

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

        registration.addRecipeCategories(new SpillingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new SmeltingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new FakeCrushingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        IModPlugin.super.registerRecipes(registration);

        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<FakeCrushingRecipe> recipes = new ArrayList<>();

        for (Item item : BuiltInRegistries.ITEM) {

            var stack = CrushingMap.getConversion(item);


            if (!stack.isEmpty()) {
                recipes.add(new FakeCrushingRecipe(item, stack));
            }
        }

        registration.addRecipes(RecipeType.create(Autowork.MODID, "crushing", FakeCrushingRecipe.class), recipes);

        List<SpillingRecipe> spilling_recipes = recipeManager.getAllRecipesFor(ModRecipes.SPILLING_RECIPE_TYPE.get())
                .stream().map(RecipeHolder::value).toList();

        registration.addRecipes(SpillingRecipeCategory.SPILLING_RTYPE, spilling_recipes);

        List<BulkSmeltRecipe> smelting_recipes = recipeManager.getAllRecipesFor(ModRecipes.BULK_SMELTING_RECIPE_TYPE.get())
                .stream().map(RecipeHolder::value).toList();

        registration.addRecipes(SmeltingRecipeCategory.SMELTING_RTYPE, smelting_recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        IModPlugin.super.registerRecipeCatalysts(registration);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SPILLER.asItem()),
                SpillingRecipeCategory.SPILLING_RTYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SMELTER.asItem()),
                SmeltingRecipeCategory.SMELTING_RTYPE);

        registration.addRecipeCatalyst(Items.DIAMOND_PICKAXE, FakeCrushingRecipeCategory.RTYPE);
        registration.addRecipeCatalyst(Items.NETHERITE_PICKAXE, FakeCrushingRecipeCategory.RTYPE);
        registration.addRecipeCatalyst(Items.IRON_PICKAXE, FakeCrushingRecipeCategory.RTYPE);
    }
}