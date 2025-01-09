package net.undertaker.furtotemsmod.recipes;

import java.util.function.Consumer;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.undertaker.furtotemsmod.items.ModItems;

public class ModRecipesProvider extends RecipeProvider implements IConditionBuilder {
  public ModRecipesProvider(PackOutput packOutput) {
    super(packOutput);
  }

  @Override
  protected void buildRecipes(Consumer<FinishedRecipe> recipeConsumer) {
    ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS,ModItems.COPPER_STAFF_ITEM.get())
        .pattern("###")
        .pattern("# #")
        .pattern("I#I")
        .define('#', Blocks.COPPER_BLOCK)
        .define('I', Items.COPPER_INGOT)
        .unlockedBy(getHasName(Blocks.COPPER_BLOCK), has(Blocks.COPPER_BLOCK))
        .save(recipeConsumer);
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS ,ModItems.SMALL_TOTEM.get())
        .pattern("SF")
        .pattern("FS")
        .define('F', Items.FLINT)
        .define('S', Items.STICK)
        .unlockedBy(getHasName(Items.FLINT), has(Items.FLINT))
        .save(recipeConsumer);

    ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModItems.IRON_TOTEMIC_EMPOWERMENT.get())
        .pattern("IBI")
        .pattern("BSB")
        .pattern("IBI")
        .define('I', Items.IRON_INGOT)
        .define('B', Blocks.IRON_BLOCK)
        .define('S', Blocks.STONE)
        .unlockedBy(
            getHasName(ModItems.COPPER_STAFF_ITEM.get()), has(ModItems.COPPER_STAFF_ITEM.get()))
        .save(recipeConsumer);
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GOLD_TOTEMIC_EMPOWERMENT.get())
        .pattern("IBI")
        .pattern("BSB")
        .pattern("IBI")
        .define('I', Items.GOLD_INGOT)
        .define('B', Blocks.GOLD_BLOCK)
        .define('S', Blocks.STONE)
        .unlockedBy(getHasName(ModItems.IRON_STAFF_ITEM.get()), has(ModItems.IRON_STAFF_ITEM.get()))
        .save(recipeConsumer);
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DIAMOND_TOTEMIC_EMPOWERMENT.get())
        .pattern("IBI")
        .pattern("BSB")
        .pattern("IBI")
        .define('I', Items.DIAMOND)
        .define('B', Blocks.DIAMOND_BLOCK)
        .define('S', Blocks.CRYING_OBSIDIAN)
        .unlockedBy(getHasName(ModItems.GOLD_STAFF_ITEM.get()), has(ModItems.GOLD_STAFF_ITEM.get()))
        .save(recipeConsumer);
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.NETHERITE_TOTEMIC_EMPOWERMENT.get())
        .pattern("IBI")
        .pattern("BSB")
        .pattern("IBI")
        .define('I', Items.NETHERITE_INGOT)
        .define('B', Blocks.NETHERITE_BLOCK)
        .define('S', Items.WITHER_SKELETON_SKULL)
        .unlockedBy(
            getHasName(ModItems.DIAMOND_STAFF_ITEM.get()), has(ModItems.DIAMOND_STAFF_ITEM.get()))
        .save(recipeConsumer);

    generateSmithingRecipe(
        recipeConsumer,
        ModItems.COPPER_STAFF_ITEM.get(),
        ModItems.IRON_TOTEMIC_EMPOWERMENT.get(),
        RecipeCategory.TOOLS,
        ModItems.IRON_STAFF_ITEM.get());

    generateSmithingRecipe(
        recipeConsumer,
        ModItems.IRON_STAFF_ITEM.get(),
        ModItems.GOLD_TOTEMIC_EMPOWERMENT.get(),
            RecipeCategory.TOOLS,
        ModItems.GOLD_STAFF_ITEM.get());

    generateSmithingRecipe(
        recipeConsumer,
        ModItems.GOLD_STAFF_ITEM.get(),
        ModItems.DIAMOND_TOTEMIC_EMPOWERMENT.get(),
            RecipeCategory.TOOLS,
        ModItems.DIAMOND_STAFF_ITEM.get());

    generateSmithingRecipe(
        recipeConsumer,
        ModItems.DIAMOND_STAFF_ITEM.get(),
        ModItems.NETHERITE_TOTEMIC_EMPOWERMENT.get(),
            RecipeCategory.TOOLS,
        ModItems.NETHERITE_STAFF_ITEM.get());
  }

  protected static void generateSmithingRecipe(
          Consumer<FinishedRecipe> recipeOutput,
          Item pIngredientItem,
          Item pIngridientItem2,
          RecipeCategory pCategory,
          Item pResultItem) {
    SmithingTransformRecipeBuilder.smithing(
                    Ingredient.of(Items.AMETHYST_BLOCK),
                    Ingredient.of(pIngredientItem),
                    Ingredient.of(pIngridientItem2),
                    pCategory,
                    pResultItem)
            .unlocks("has_totem", has(ModItems.COPPER_STAFF_ITEM.get()))
            .save(recipeOutput, getItemName(pResultItem) + "_smithing");
  }
}
