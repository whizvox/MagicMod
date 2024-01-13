package me.whizvox.magicmod.common.lib.spell;

import me.whizvox.magicmod.common.api.spell.Spell;
import me.whizvox.magicmod.common.util.SpellUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class BlinkSpell implements Spell {

  @Override
  public int getCost(int level) {
    return 80;
  }

  @Override
  public int getMaxLevel() {
    return 2;
  }

  @Override
  public boolean activate(int level, LivingEntity caster) {
    if (caster.level().isClientSide) {
      return false;
    }
    BlockHitResult hit = SpellUtil.lookingAtBlock(caster, (level + 1) * 70);
    if (hit.getType() != HitResult.Type.BLOCK) {
      return false;
    }
    BlockPos pos = hit.getBlockPos().relative(hit.getDirection());
    caster.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
    return true;
  }

}
