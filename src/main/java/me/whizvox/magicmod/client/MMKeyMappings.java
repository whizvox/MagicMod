package me.whizvox.magicmod.client;

import me.whizvox.magicmod.MagicMod;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class MMKeyMappings {

  public static final KeyMapping OPEN_SPELL_INVENTORY = new KeyMapping(
      "key." + MagicMod.MOD_ID + ".openSpellInventory",
      GLFW.GLFW_KEY_P,
      "key." + MagicMod.MOD_ID + ".categories.openSpellInventory"
  );

}
