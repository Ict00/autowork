package org.prism.autowork.recipe.DyeCartridge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.prism.autowork.item.ModItems;
import org.prism.autowork.item.custom.DyeCartridge;
import org.prism.autowork.other.ModData;
import org.prism.autowork.other.data.CartridgeComponent;
import org.prism.autowork.recipe.ModRecipes;

public class DyeCartridgeRecipe extends ShapelessRecipe {
    final ItemStack result;

    public DyeCartridgeRecipe(String p_249640_, CraftingBookCategory p_249390_, ItemStack p_252071_, NonNullList<Ingredient> p_250689_) {
        super(p_249640_, p_249390_, p_252071_, p_250689_);

        result = p_252071_;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        var items = input.items();
        ItemStack cartridge = ItemStack.EMPTY;
        DyeItem dye = null;
        int dyeCount = 0;

        for (var item : items) {
            if (item.is(ModItems.DYE_CARTRIDGE)) {
                if (cartridge.isEmpty()) {
                    cartridge = item;
                }
                else {
                    return false;
                }
            }
            else if (item.getItem() instanceof DyeItem dyeItem) {
                if (dye == null) {
                    dye = dyeItem;
                    dyeCount++;
                }
                else {
                    if (item.is(dye)) {
                        dyeCount++;
                    }
                    else {
                        return false;
                    }
                }
            }
        }

        var component = cartridge.get(ModData.CARTRIDGE);

        if (component == null) {
            return dyeCount > 0 && !cartridge.isEmpty();
        }

        if (component.currentUses() >= 64 * 8) {
            return false;
        }

        var dyeOfCartridge = ((DyeItem) component.dye());

        return dyeOfCartridge.equals(dye);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.DYING_CARTRIDGE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider prov) {
        ItemStack cartridge = ItemStack.EMPTY;
        DyeItem dye = null;
        int dyes = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof DyeCartridge) {
                cartridge = stack;
            } else if (stack.getItem() instanceof DyeItem d) {
                if (dye == null) {
                    dye = d;
                }

                dyes++;
            }
        }

        if (cartridge.isEmpty() || dye == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = cartridge.copy();

        var component = result.get(ModData.CARTRIDGE);

        int addedUses = dyes * 8;

        if (component == null) {
            result.set(ModData.CARTRIDGE, new CartridgeComponent(dye, Math.min(addedUses, 64 * 8), dye.getDyeColor().getTextureDiffuseColor()));
        } else {
            result.set(ModData.CARTRIDGE, new CartridgeComponent(component.dye(), Math.min(component.currentUses() + addedUses, 64 * 8), component.color()));
        }

        return result;
    }

    // Copied shamelessly from ShapelessRecipe.Serializer

    public static class MySerializer implements RecipeSerializer<DyeCartridgeRecipe> {
        private static final MapCodec<DyeCartridgeRecipe> CODEC = RecordCodecBuilder.mapCodec((p_340779_) -> p_340779_.group(Codec.STRING.optionalFieldOf("group", "").forGetter((p_301127_) -> p_301127_.getGroup()), CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter((p_301133_) -> p_301133_.category()), ItemStack.STRICT_CODEC.fieldOf("result").forGetter((p_301142_) -> p_301142_.result), Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap((p_301021_) -> {
            Ingredient[] aingredient = p_301021_.toArray((x$0) -> new Ingredient[x$0]);
            if (aingredient.length == 0) {
                return DataResult.error(() -> "No ingredients for shapeless recipe");
            } else {
                return aingredient.length > ShapedRecipePattern.getMaxHeight() * ShapedRecipePattern.getMaxWidth() ? DataResult.error(() -> "Too many ingredients for shapeless recipe. The maximum is: %s".formatted(ShapedRecipePattern.getMaxHeight() * ShapedRecipePattern.getMaxWidth())) : DataResult.success(NonNullList.of(Ingredient.EMPTY, aingredient));
            }
        }, DataResult::success).forGetter((p_300975_) -> p_300975_.getIngredients())).apply(p_340779_, DyeCartridgeRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, DyeCartridgeRecipe> STREAM_CODEC = StreamCodec.of(DyeCartridgeRecipe.MySerializer::toNetwork, DyeCartridgeRecipe.MySerializer::fromNetwork);

        public MySerializer() {
        }

        public MapCodec<DyeCartridgeRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, DyeCartridgeRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static DyeCartridgeRecipe fromNetwork(RegistryFriendlyByteBuf p_319905_) {
            String s = p_319905_.readUtf();
            CraftingBookCategory craftingbookcategory = (CraftingBookCategory)p_319905_.readEnum(CraftingBookCategory.class);
            int i = p_319905_.readVarInt();
            NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i, Ingredient.EMPTY);
            nonnulllist.replaceAll((p_319735_) -> (Ingredient)Ingredient.CONTENTS_STREAM_CODEC.decode(p_319905_));
            ItemStack itemstack = (ItemStack)ItemStack.STREAM_CODEC.decode(p_319905_);
            return new DyeCartridgeRecipe(s, craftingbookcategory, itemstack, nonnulllist);
        }

        private static void toNetwork(RegistryFriendlyByteBuf p_320371_, DyeCartridgeRecipe p_320323_) {
            p_320371_.writeUtf(p_320323_.getGroup());
            p_320371_.writeEnum(p_320323_.category());
            p_320371_.writeVarInt(p_320323_.getIngredients().size());

            for(Ingredient ingredient : p_320323_.getIngredients()) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(p_320371_, ingredient);
            }

            ItemStack.STREAM_CODEC.encode(p_320371_, p_320323_.result);
        }
    }
}
