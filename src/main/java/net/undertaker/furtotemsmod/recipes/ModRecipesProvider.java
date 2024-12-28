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
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.undertaker.furtotemsmod.items.ModItems;

public class ModRecipesProvider extends RecipeProvider implements IConditionBuilder {
  public ModRecipesProvider(DataGenerator pGenerator) {
    super(pGenerator);
  }

  @Override
  protected void buildCraftingRecipes(Consumer<FinishedRecipe> recipeConsumer) {

    generateSmithingRecipe(
        recipeConsumer,
        ModItems.COPPER_STAFF_ITEM.get(),
        Items.IRON_INGOT,
        ModItems.IRON_STAFF_ITEM.get(),
        "totem_staff_iron_upgrade",
        "has_iron_ingot");

    generateSmithingRecipe(
        recipeConsumer,
        ModItems.IRON_STAFF_ITEM.get(),
        Items.GOLD_INGOT,
        ModItems.GOLD_STAFF_ITEM.get(),
        "totem_staff_gold_upgrade",
        "has_gold_ingot");

    generateSmithingRecipe(
        recipeConsumer,
        ModItems.GOLD_STAFF_ITEM.get(),
        Items.DIAMOND,
        ModItems.DIAMOND_STAFF_ITEM.get(),
        "totem_staff_diamond_upgrade",
        "has_diamond");

    generateSmithingRecipe(
        recipeConsumer,
        ModItems.DIAMOND_STAFF_ITEM.get(),
        Items.NETHERITE_INGOT,
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
