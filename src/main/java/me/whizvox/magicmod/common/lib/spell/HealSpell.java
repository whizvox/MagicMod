package me.whizvox.magicmod.common.lib.spell;

import me.whizvox.magicmod.common.api.spell.Spell;
import net.minecraft.world.entity.LivingEntity;

public class HealSpell implements Spell {

  @Override
  public int getCost(int level) {
    return 5 + level * 5;
  }

  @Override
  public int getMaxLevel() {
    return 3;
  }

  @Override
  public boolean activate(int level, LivingEntity caster) {
    if (caster.getHealth() < caster.getMaxHealth()) {
      caster.heal(5.0F * level);
      return true;
    }
    return false;
  }

}
