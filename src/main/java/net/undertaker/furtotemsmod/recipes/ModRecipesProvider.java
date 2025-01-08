package net.undertaker.furtotemsmod.recipes;

import java.util.function.Consumer;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.UpgradeRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.undertaker.furtotemsmod.blocks.ModBlocks;
import net.undertaker.furtotemsmod.items.ModItems;

public class ModRecipesProvider extends RecipeProvider implements IConditionBuilder {
  public ModRecipesProvider(DataGenerator pGenerator) {
    super(pGenerator);
  }

  @Override
  protected void buildCraftingRecipes(Consumer<FinishedRecipe> recipeConsumer) {
    ShapedRecipeBuilder.shaped(ModItems.COPPER_STAFF_ITEM.get())
        .pattern("###")
        .pattern("# #")
        .pattern("I#I")
        .define('#', Blocks.COPPER_BLOCK)
        .define('I', Items.COPPER_INGOT)
        .unlockedBy(getHasName(Blocks.COPPER_BLOCK), has(Blocks.COPPER_BLOCK))
        .save(recipeConsumer);
    ShapedRecipeBuilder.shaped(ModItems.SMALL_TOTEM.get())
        .pattern("SF")
        .pattern("FS")
        .define('F', Items.FLINT)
        .define('S', Items.STICK)
        .unlockedBy(getHasName(Items.FLINT), has(Items.FLINT))
        .save(recipeConsumer);

    ShapedRecipeBuilder.shaped(ModItems.IRON_TOTEMIC_EMPOWERMENT.get())
        .pattern("IBI")
        .pattern("BSB")
        .pattern("IBI")
        .define('I', Items.IRON_INGOT)
        .define('B', Blocks.IRON_BLOCK)
        .define('S', Blocks.STONE)
        .unlockedBy(
            getHasName(ModItems.COPPER_STAFF_ITEM.get()), has(ModItems.COPPER_STAFF_ITEM.get()))
        .save(recipeConsumer);
    ShapedRecipeBuilder.shaped(ModItems.GOLD_TOTEMIC_EMPOWERMENT.get())
        .pattern("IBI")
        .pattern("BSB")
        .pattern("IBI")
        .define('I', Items.GOLD_INGOT)
        .define('B', Blocks.GOLD_BLOCK)
        .define('S', Blocks.STONE)
        .unlockedBy(getHasName(ModItems.IRON_STAFF_ITEM.get()), has(ModItems.IRON_STAFF_ITEM.get()))
        .save(recipeConsumer);
    ShapedRecipeBuilder.shaped(ModItems.DIAMOND_TOTEMIC_EMPOWERMENT.get())
        .pattern("IBI")
        .pattern("BSB")
        .pattern("IBI")
        .define('I', Items.DIAMOND)
        .define('B', Blocks.DIAMOND_BLOCK)
        .define('S', Blocks.CRYING_OBSIDIAN)
        .unlockedBy(getHasName(ModItems.GOLD_STAFF_ITEM.get()), has(ModItems.GOLD_STAFF_ITEM.get()))
        .save(recipeConsumer);
    ShapedRecipeBuilder.shaped(ModItems.NETHERITE_TOTEMIC_EMPOWERMENT.get())
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
        ModItems.IRON_STAFF_ITEM.get(),
        "totem_staff_iron_upgrade",
        "has_iron_ingot");

    generateSmithingRecipe(
        recipeConsumer,
        ModItems.IRON_STAFF_ITEM.get(),
        ModItems.GOLD_TOTEMIC_EMPOWERMENT.get(),
        ModItems.GOLD_STAFF_ITEM.get(),
        "totem_staff_gold_upgrade",
        "has_gold_ingot");

    generateSmithingRecipe(
        recipeConsumer,
        ModItems.GOLD_STAFF_ITEM.get(),
        ModItems.DIAMOND_TOTEMIC_EMPOWERMENT.get(),
        ModItems.DIAMOND_STAFF_ITEM.get(),
        "totem_staff_diamond_upgrade",
        "has_diamond");

    generateSmithingRecipe(
        recipeConsumer,
        ModItems.DIAMOND_STAFF_ITEM.get(),
        ModItems.NETHERITE_TOTEMIC_EMPOWERMENT.get(),
        ModItems.NETHERITE_STAFF_ITEM.get(),
        "totem_staff_netherite_upgrade",
        "has_netherite_ingot");
  }

  protected static void generateSmithingRecipe(
      Consumer<FinishedRecipe> recipeConsumer,
      Item baseItem,
      Item upgradeMaterial,
      Item resultItem,
      String recipeName,
      String unlockCondition) {
    UpgradeRecipeBuilder.smithing(
            Ingredient.of(baseItem), Ingredient.of(upgradeMaterial), resultItem)
        .unlocks(unlockCondition, has(upgradeMaterial))
        .save(recipeConsumer, recipeName);
  }
}
