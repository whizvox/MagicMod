package me.whizvox.magicmod.common.lib.spell;

import me.whizvox.magicmod.common.api.spell.ActivationResult;
import me.whizvox.magicmod.common.api.spell.CastType;
import me.whizvox.magicmod.common.api.spell.SpellUsageContext;
import me.whizvox.magicmod.common.api.spell.StatelessSpell;
import net.minecraft.world.entity.LivingEntity;

public class HealSpell implements StatelessSpell {

  @Override
  public int getCost(int level) {
    return 5 + level * 5;
  }

  @Override
  public int getMaxLevel() {
    return 3;
  }

  @Override
  public CastType getCastType() {
    return CastType.INSTANT;
  }

  @Override
  public ActivationResult activate(int level, SpellUsageContext context) {
    LivingEntity caster = (LivingEntity) context.caster();
    if (caster.getHealth() < caster.getMaxHealth()) {
      caster.heal(5.0F * level);
      return ActivationResult.SUCCESS;
    }
    return ActivationResult.CANCEL;
  }

}
