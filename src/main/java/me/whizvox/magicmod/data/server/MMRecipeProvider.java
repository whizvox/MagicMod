package me.whizvox.magicmod.data.server;

import me.whizvox.magicmod.common.registry.MMItems;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class MMRecipeProvider extends RecipeProvider {

  public MMRecipeProvider(PackOutput output) {
    super(output);
  }

  @Override
  protected void buildRecipes(Consumer<FinishedRecipe> writer) {
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MMItems.KNOWLEDGE_SCROLL.get())
        .pattern(" F ")
        .pattern("FCF")
        .pattern(" F ")
        .define('F', MMItems.KNOWLEDGE_FRAGMENT.get())
        .define('C', Items.FIRE_CHARGE)
        .unlockedBy("has_fragment", InventoryChangeTrigger.TriggerInstance.hasItems(MMItems.KNOWLEDGE_FRAGMENT.get()))
        .save(writer);
  }

}
