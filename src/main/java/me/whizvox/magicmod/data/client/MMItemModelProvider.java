package me.whizvox.magicmod.data.client;

import me.whizvox.magicmod.MagicMod;
import me.whizvox.magicmod.common.registry.MMItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class MMItemModelProvider extends ItemModelProvider {

  public MMItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
    super(output, MagicMod.MOD_ID, existingFileHelper);
  }

  @Override
  protected void registerModels() {
    basicItem(MMItems.KNOWLEDGE_SCROLL.get());
  }

}
