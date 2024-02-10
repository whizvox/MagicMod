package me.whizvox.magicmod.common.lib.spell;

import me.whizvox.magicmod.common.api.spell.Spell;
import me.whizvox.magicmod.common.util.SpellUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.TierSortingRegistry;

public class BreakSpell implements Spell {

  @Override
  public int getMaxLevel() {
    return 4;
  }

  @Override
  public int getCost(int level) {
    return 5 + (level * 8);
  }

  @Override
  public boolean activate(int level, LivingEntity caster) {
    BlockHitResult hit = SpellUtil.lookingAtBlock(caster);
    if (hit.getType() != HitResult.Type.BLOCK) {
      return false;
    }
    Tier tier = switch (level) {
      case 1 -> Tiers.IRON;
      case 2 -> Tiers.DIAMOND;
      case 3 -> Tiers.NETHERITE;
      default -> Tiers.STONE;
    };
    BlockState state = caster.level().getBlockState(hit.getBlockPos());
    if (!TierSortingRegistry.isCorrectTierForDrops(tier, state)) {
      return false;
    }
    caster.level().destroyBlock(hit.getBlockPos(), true, null);
    return true;
  }

}
