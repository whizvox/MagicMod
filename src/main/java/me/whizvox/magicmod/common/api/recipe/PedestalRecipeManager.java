package me.whizvox.magicmod.common.api.recipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import me.whizvox.magicmod.MagicMod;
import me.whizvox.magicmod.common.network.MMNetwork;
import me.whizvox.magicmod.common.network.SyncPedestalRecipesMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.stream.Collectors;

public class PedestalRecipeManager extends SimpleJsonResourceReloadListener {

  private static final Gson GSON = new GsonBuilder()
      .registerTypeAdapter(PedestalRecipe.class, new PedestalRecipe.Serializer())
      .create();

  private final Map<ResourceLocation, PedestalRecipe> recipes;
  private final Map<PedestalRecipe, ResourceLocation> reverseLookup;

  public PedestalRecipeManager() {
    super(GSON, "pedestal_recipes");
    recipes = new HashMap<>();
    reverseLookup = new HashMap<>();
  }

  private void add(ResourceLocation key, PedestalRecipe recipe) {
    recipes.put(key, recipe);
    reverseLookup.put(recipe, key);
  }

  public PedestalRecipe getRecipe(ResourceLocation key) {
    return recipes.get(key);
  }

  public ResourceLocation getKey(PedestalRecipe recipe) {
    return reverseLookup.get(recipe);
  }

  public List<PedestalRecipeMatch> match(ItemStack centerItem, List<ItemStack> outerItems) {
    List<PedestalRecipeMatch> matches = new ArrayList<>();
    PedestalCraftItems input = new PedestalCraftItems(centerItem, outerItems);
    recipes.values().forEach(recipe -> {
      PedestalCraftItems result = recipe.test(input);
      if (result != null) {
        matches.add(new PedestalRecipeMatch(recipe, result));
      }
    });
    return matches;
  }

  public Map<ResourceLocation, PedestalRecipe> getRecipes() {
    return Collections.unmodifiableMap(recipes);
  }

  public void sync(SyncPedestalRecipesMessage msg) {
    clear();
    msg.recipes().forEach(this::add);
  }

  public void clear() {
    recipes.clear();
    reverseLookup.clear();
  }

  @Override
  protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager manager, ProfilerFiller profiler) {
    clear();
    entries.forEach((location, json) -> {
      try {
        PedestalRecipe recipe = GSON.fromJson(json, PedestalRecipe.class);
        recipe.setId(location);
        add(location, recipe);
        MagicMod.LOGGER.debug("Loaded pedestal recipe: {}", location);
      } catch (JsonParseException e) {
        MagicMod.LOGGER.warn("Could not deserialize pedestal recipe: " + location, e);
      }
    });
    MagicMod.LOGGER.info("Loaded {} pedestal recipes", recipes.size());
    if (ServerLifecycleHooks.getCurrentServer() != null && !FMLEnvironment.dist.isClient()) {
      MMNetwork.broadcast(new SyncPedestalRecipesMessage());
    }
  }

  public static final PedestalRecipeManager INSTANCE = new PedestalRecipeManager();

}
