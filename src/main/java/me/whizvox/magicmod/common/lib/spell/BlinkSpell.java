package me.whizvox.magicmod.common.lib.spell;

import me.whizvox.magicmod.common.api.spell.ActivationResult;
import me.whizvox.magicmod.common.api.spell.CastType;
import me.whizvox.magicmod.common.api.spell.SpellUsageContext;
import me.whizvox.magicmod.common.api.spell.StatelessSpell;
import me.whizvox.magicmod.common.util.SpellUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class BlinkSpell implements StatelessSpell {

  @Override
  public int getCost(int level) {
    return 80;
  }

  @Override
  public int getMaxLevel() {
    return 2;
  }

  @Override
  public CastType getCastType() {
    return CastType.INSTANT;
  }

  @Override
  public ActivationResult activate(int level, SpellUsageContext context) {
    LivingEntity caster = (LivingEntity) context.caster();
    if (caster.level().isClientSide) {
      return ActivationResult.PASS;
    }
    BlockHitResult hit = SpellUtil.lookingAtBlock(caster, 100 + level * 200);
    if (hit.getType() != HitResult.Type.BLOCK) {
      return ActivationResult.CANCEL;
    }
    BlockPos pos = hit.getBlockPos().relative(hit.getDirection());
    caster.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
    return ActivationResult.SUCCESS;
  }

}
