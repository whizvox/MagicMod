package me.whizvox.magicmod.common.lib.spell;

import me.whizvox.magicmod.common.api.spell.Spell;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.phys.Vec3;

public class FireballSpell implements Spell {

  @Override
  public int getCost(int level) {
    return 10 + level * 10;
  }

  @Override
  public int getMaxLevel() {
    return 3;
  }

  @Override
  public boolean activate(int level, LivingEntity caster) {
    Vec3 look = caster.getLookAngle();
    SmallFireball fireball = new SmallFireball(caster.level(), caster, look.x, look.y, look.z);
    fireball.setPos(caster.getEyePosition());
    caster.level().addFreshEntity(fireball);
    return true;
  }

}
