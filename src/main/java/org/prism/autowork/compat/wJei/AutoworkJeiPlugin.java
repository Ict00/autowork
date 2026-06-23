package org.prism.autowork.compat.wJei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.prism.autowork.Autowork;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.compat.wJei.crushing.CrushingRecipeCategory;
import org.prism.autowork.compat.wJei.enriching.FakeEnrichingRecipe;
import org.prism.autowork.compat.wJei.enriching.FakeEnrichingRecipeCategory;
import org.prism.autowork.compat.wJei.painting.PaintingRecipeCategory;
import org.prism.autowork.compat.wJei.smelting.SmeltingRecipeCategory;
import org.prism.autowork.compat.wJei.spilling.SpillingRecipeCategory;
import org.prism.autowork.other.ModOther;
import org.prism.autowork.other.datamaps.EnrichingMap;
import org.prism.autowork.recipe.BulkSmeltRecipe.BulkSmeltRecipe;
import org.prism.autowork.recipe.CrushingRecipe.CrushingRecipe;
import org.prism.autowork.recipe.ModRecipes;
import org.prism.autowork.recipe.PaintRecipe.PaintRecipe;
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

        registration.addRecipeCategories(new CrushingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new PaintingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new FakeEnrichingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        IModPlugin.super.registerRecipes(registration);

        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<FakeEnrichingRecipe> enrichingRecipes = new ArrayList<>();

        for (Item item : BuiltInRegistries.ITEM) {

            var stack = EnrichingMap.getConversion(item);


            if (!stack.isEmpty()) {
                enrichingRecipes.add(new FakeEnrichingRecipe(item, stack));
            }
        }

        registration.addRecipes(RecipeType.create(Autowork.MODID, "enriching", FakeEnrichingRecipe.class), enrichingRecipes);

        List<CrushingRecipe> crushing_recipes = recipeManager.getAllRecipesFor(ModRecipes.CRUSHING_RECIPE_TYPE.get())
                .stream().map(RecipeHolder::value).toList();

        registration.addRecipes(CrushingRecipeCategory.RTYPE, crushing_recipes);

        List<SpillingRecipe> spilling_recipes = recipeManager.getAllRecipesFor(ModRecipes.SPILLING_RECIPE_TYPE.get())
                .stream().map(RecipeHolder::value).toList();

        registration.addRecipes(SpillingRecipeCategory.SPILLING_RTYPE, spilling_recipes);

        List<BulkSmeltRecipe> smelting_recipes = recipeManager.getAllRecipesFor(ModRecipes.BULK_SMELTING_RECIPE_TYPE.get())
                .stream().map(RecipeHolder::value).toList();

        registration.addRecipes(SmeltingRecipeCategory.SMELTING_RTYPE, smelting_recipes);

        List<PaintRecipe> painting_recipes = recipeManager.getAllRecipesFor(ModRecipes.PAINTING_RECIPE_TYPE.get())
                .stream().map(RecipeHolder::value).toList();

        registration.addRecipes(PaintingRecipeCategory.RTYPE, painting_recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        IModPlugin.super.registerRecipeCatalysts(registration);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SPILLER.asItem()),
                SpillingRecipeCategory.SPILLING_RTYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SMELTER.asItem()),
                SmeltingRecipeCategory.SMELTING_RTYPE);

        var st = new ItemStack(Items.ENCHANTED_BOOK);

        var conn = Minecraft.getInstance().getConnection();

        if (conn != null) {
            var access = conn.registryAccess();
            try {
                var x = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

                var ench = access.holder(ModOther.CRUSHING_ENCHANTMENT);

                x.set(ench.get(), 1);

                st.set(DataComponents.STORED_ENCHANTMENTS, x.toImmutable());
            }
            catch (Exception ignore) { }
        }

        registration.addRecipeCatalyst(st, CrushingRecipeCategory.RTYPE);

        registration.addRecipeCatalyst(ModBlocks.ENRICHER, FakeEnrichingRecipeCategory.RTYPE);
        registration.addRecipeCatalyst(ModBlocks.PAINTER, PaintingRecipeCategory.RTYPE);
        registration.addRecipeCatalyst(Items.DIAMOND_PICKAXE, CrushingRecipeCategory.RTYPE);
        registration.addRecipeCatalyst(Items.NETHERITE_PICKAXE, CrushingRecipeCategory.RTYPE);
        registration.addRecipeCatalyst(Items.IRON_PICKAXE, CrushingRecipeCategory.RTYPE);
    }
}