package me.whizvox.magicmod.common.network;

import me.whizvox.magicmod.common.api.recipe.PedestalRecipe;
import me.whizvox.magicmod.common.api.recipe.PedestalRecipeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;

public record SyncPedestalRecipesMessage(Map<ResourceLocation, PedestalRecipe> recipes) {

  public SyncPedestalRecipesMessage() {
    this(PedestalRecipeManager.INSTANCE.getRecipes());
  }

  public static final MessageHandler<SyncPedestalRecipesMessage> HANDLER = new MessageHandler<>() {

    @Override
    public Class<SyncPedestalRecipesMessage> getMessageClass() {
      return SyncPedestalRecipesMessage.class;
    }

    @Override
    public void encode(SyncPedestalRecipesMessage msg, FriendlyByteBuf buf) {
      buf.writeShort(msg.recipes.size());
      msg.recipes.forEach((location, recipe) -> {
        buf.writeResourceLocation(location);
        recipe.toNetwork(buf);
      });
    }

    @Override
    public SyncPedestalRecipesMessage decode(FriendlyByteBuf buf) {
      Map<ResourceLocation, PedestalRecipe> recipes = new HashMap<>();
      int count = buf.readShort();
      for (int i = 0; i < count; i++) {
        ResourceLocation key = buf.readResourceLocation();
        PedestalRecipe recipe = PedestalRecipe.fromNetwork(buf);
        recipes.put(key, recipe);
      }
      return new SyncPedestalRecipesMessage(recipes);
    }

    @Override
    public void handle(NetworkEvent.Context ctx, SyncPedestalRecipesMessage msg) {
      PedestalRecipeManager.INSTANCE.sync(msg);
    }

  };

}
