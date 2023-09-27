package me.whizvox.magicmod.common.lib.spell;

import me.whizvox.magicmod.common.api.spell.ActivationResult;
import me.whizvox.magicmod.common.api.spell.CastType;
import me.whizvox.magicmod.common.api.spell.SpellUsageContext;
import me.whizvox.magicmod.common.api.spell.StatelessSpell;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class FireballSpell implements StatelessSpell {

  @Override
  public int getCost(int level) {
    return 10;
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
    return ActivationResult.PASS;
  }


}
