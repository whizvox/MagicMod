package me.whizvox.magicmod.common.lib.spell;

import me.whizvox.magicmod.common.api.spell.ActivationResult;
import me.whizvox.magicmod.common.api.spell.CastType;
import me.whizvox.magicmod.common.api.spell.SpellUsageContext;
import me.whizvox.magicmod.common.api.spell.StatelessSpell;
import me.whizvox.magicmod.common.util.SpellUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class HarmSpell implements StatelessSpell {

  @Override
  public int getCost(int level) {
    return 5;
  }

  @Override
  public int getMaxLevel() {
    return 4;
  }

  @Override
  public CastType getCastType() {
    return CastType.INSTANT;
  }

  @Override
  public ActivationResult activate(int level, SpellUsageContext context) {
    EntityHitResult hit = SpellUtil.lookingAtEntity((Entity) context.caster(), 5.0D, entity -> entity instanceof LivingEntity);
    if (hit.getType() == HitResult.Type.ENTITY) {
      hit.getEntity().hurt(hit.getEntity().damageSources().magic(), 5.0F * level);
      return ActivationResult.SUCCESS;
    } else {
      return ActivationResult.CANCEL;
    }
  }

}
