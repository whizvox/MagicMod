package me.whizvox.magicmod.data.server;

import me.whizvox.magicmod.common.api.recipe.PedestalRecipe;
import me.whizvox.magicmod.common.api.spell.Spell;
import me.whizvox.magicmod.common.api.spell.SpellInstance;
import me.whizvox.magicmod.common.item.KnowledgeScrollItem;
import me.whizvox.magicmod.common.registry.MMItems;
import me.whizvox.magicmod.common.registry.MMSpells;
import me.whizvox.magicmod.common.util.SpellUtil;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.PartialNBTIngredient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class PedestalRecipeProvider implements DataProvider {

  private final DataGenerator gen;
  private final String modId;

  public PedestalRecipeProvider(DataGenerator gen, String modId) {
    this.gen = gen;
    this.modId = modId;
  }

  @Override
  public String getName() {
    return "PedestalRecipes";
  }

  @Override
  public CompletableFuture<?> run(CachedOutput output) {
    PackOutput.PathProvider pathProvider = gen.getPackOutput().createPathProvider(PackOutput.Target.DATA_PACK, "pedestal_recipes");
    List<CompletableFuture<?>> futures = new ArrayList<>();
    Set<ResourceLocation> keys = new HashSet<>();
    buildRecipes(recipe -> {
      ResourceLocation location = recipe.getId();
      if (!keys.add(location)) {
        throw new IllegalStateException("Duplicate recipes: " + recipe.getId());
      }
      futures.add(DataProvider.saveStable(output, recipe.toJson(), pathProvider.json(location)));
    });
    return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
  }

  protected Ingredient scrollStack(Spell spell, int level) {
    ItemStack stack = new ItemStack(MMItems.KNOWLEDGE_SCROLL.get());
    KnowledgeScrollItem.writeSpell(stack, new SpellInstance(spell, level));
    return PartialNBTIngredient.of(MMItems.KNOWLEDGE_SCROLL.get(), stack.getTag());
  }

  protected PedestalRecipe.Builder scroll(Spell spell, int level) {
    ItemStack result = new ItemStack(MMItems.KNOWLEDGE_SCROLL.get());
    KnowledgeScrollItem.writeSpell(result, new SpellInstance(spell, level));
    return PedestalRecipe.builder()
        .id(new ResourceLocation(modId, "scroll_" + SpellUtil.getName(spell).getPath() + "_" + level))
        .mana(50)
        .center(Items.PAPER)
        .result(result);
  }

  public void buildRecipes(Consumer<PedestalRecipe> output) {
    scroll(MMSpells.BLINK.get(), 0)
        .outer(Items.ENDER_PEARL)
        .outer(Items.CHORUS_FRUIT)
        .save(output);
    scroll(MMSpells.BLINK.get(), 1)
        .outer(scrollStack(MMSpells.BLINK.get(), 0))
        .outer(Items.SCULK)
        .outer(Items.DRAGON_BREATH)
        .save(output);
    scroll(MMSpells.HARM.get(), 0)
        .outer(Items.GLISTERING_MELON_SLICE)
        .outer(Items.FERMENTED_SPIDER_EYE)
        .outer(Items.STONE_SWORD)
        .save(output);
    scroll(MMSpells.HARM.get(), 1)
        .outer(scrollStack(MMSpells.HARM.get(), 0))
        .outer(Items.GOLDEN_SWORD)
        .save(output);
    scroll(MMSpells.HARM.get(), 2)
        .outer(scrollStack(MMSpells.HARM.get(), 1))
        .outer(Items.IRON_SWORD)
        .save(output);
    scroll(MMSpells.HARM.get(), 3)
        .outer(scrollStack(MMSpells.HARM.get(), 2))
        .outer(Items.DIAMOND_SWORD)
        .save(output);
    scroll(MMSpells.HARM.get(), 4)
        .outer(scrollStack(MMSpells.HARM.get(), 3))
        .outer(Items.NETHERITE_SWORD)
        .save(output);

  }

}
