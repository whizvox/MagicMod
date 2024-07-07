package me.whizvox.magicmod.common.lib.spell;

import me.whizvox.magicmod.common.api.spell.Spell;
import me.whizvox.magicmod.common.block.entity.PedestalBlockEntity;
import me.whizvox.magicmod.common.util.SpellUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class CraftSpell implements Spell {

  @Override
  public int getMaxLevel() {
    return 1;
  }

  @Override
  public int getCost(int level) {
    return 50;
  }

  @Override
  public boolean activate(int level, LivingEntity caster) {
    BlockHitResult hit = SpellUtil.lookingAtBlock(caster);
    return hit.getType() == HitResult.Type.BLOCK &&
        caster.level().getBlockEntity(hit.getBlockPos()) instanceof PedestalBlockEntity pedestal &&
        pedestal.attemptCraft();
  }

}
