package me.whizvox.magicmod.common.lib.spell;

import me.whizvox.magicmod.common.api.spell.Spell;
import me.whizvox.magicmod.common.util.SpellUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class HarmSpell implements Spell {

  @Override
  public int getCost(int level) {
    return 5 + level * 5;
  }

  @Override
  public int getMaxLevel() {
    return 4;
  }

  @Override
  public boolean activate(int level, LivingEntity caster) {
    EntityHitResult hit = SpellUtil.lookingAtEntity(caster, 5.0D, entity -> entity instanceof LivingEntity);
    if (hit.getType() == HitResult.Type.ENTITY) {
      hit.getEntity().hurt(hit.getEntity().damageSources().indirectMagic(caster, hit.getEntity()), 5.0F * (level + 1));
      return true;
    } else {
      return false;
    }
  }

}
