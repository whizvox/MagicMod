package me.whizvox.magicmod.common.api.spell;

import net.minecraft.world.entity.LivingEntity;

public interface Spell {

  int getMaxLevel();

  int getCost(int level);

  boolean activate(int level, LivingEntity caster);

}
