package me.whizvox.magicmod.common.api.recipe;

public record PedestalRecipeMatch(PedestalRecipe recipe, PedestalCraftItems output) {

  public boolean isEmpty() {
    return recipe == null;
  }

  public static final PedestalRecipeMatch EMPTY = new PedestalRecipeMatch(null, null);

}
