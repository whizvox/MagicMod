package me.whizvox.magicmod.common.lib.spell;

import me.whizvox.magicmod.MagicMod;
import me.whizvox.magicmod.common.api.spell.Spell;
import me.whizvox.magicmod.common.lib.MMCapabilities;
import me.whizvox.magicmod.common.lib.spelldata.ShieldSpellData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class ShieldSpell implements Spell {

  public static final ResourceLocation SHIELD_DATA_KEY = new ResourceLocation(MagicMod.MOD_ID, "shield");

  @Override
  public int getMaxLevel() {
    return 4;
  }

  @Override
  public int getCost(int level) {
    return 10 + (level * 10);
  }

  @Override
  public boolean activate(int level, LivingEntity caster) {
    return caster.getCapability(MMCapabilities.MAGIC_USER).map(magicUser -> {
      magicUser.addData(SHIELD_DATA_KEY, new ShieldSpellData(level));
      caster.sendSystemMessage(Component.translatable("message.magicmod.spell.shield.cast"));
      return true;
    }).orElse(false);
  }

}
