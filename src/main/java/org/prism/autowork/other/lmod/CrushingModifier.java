package org.prism.autowork.other.lmod;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.prism.autowork.other.ModOther;
import org.prism.autowork.other.datamaps.CrushingMap;
import org.prism.autowork.recipe.CrushingRecipe.CrushingRecipeInput;
import org.prism.autowork.recipe.ModRecipes;
import org.prism.autowork.recipe.PaintRecipe.PaintRecipeInput;

import java.util.concurrent.atomic.AtomicBoolean;

public class CrushingModifier extends LootModifier {
    public static final MapCodec<CrushingModifier> CODEC =
            RecordCodecBuilder.mapCodec(inst ->
                    codecStart(inst).apply(inst, CrushingModifier::new)
            );

    public CrushingModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generated, LootContext lootContext) {
        ItemStack tool = lootContext.getParamOrNull(LootContextParams.TOOL);

        if (tool != null) {
            AtomicBoolean hasCrushing = new AtomicBoolean(false);
            EnchantmentHelper.runIterationOnItem(tool, (x, y)-> {
                if (x.is(ModOther.CRUSHING_ENCHANTMENT)) {
                    hasCrushing.set(true);
                }
            });

            var level = lootContext.getLevel();

            if (hasCrushing.get()) {
                for (int i = 0; i < generated.size(); i++) {
                    var item = generated.get(i);

                    var input = new CrushingRecipeInput(item);


                    var optionalRecipe = level.getRecipeManager().getRecipeFor(ModRecipes.CRUSHING_RECIPE_TYPE.get(), input, level);

                    if (optionalRecipe.isPresent()) {
                        var actualRecipe = optionalRecipe.get().value();
                        var t = actualRecipe.getResultItem(null);

                        if (!t.isEmpty()) {
                            t.setCount(t.getCount()*item.getCount());
                            generated.set(i, t);
                        }
                    }
                }
            }
        }


        return generated;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
