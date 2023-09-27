package me.whizvox.magicmod.common.api.spell;

public interface Spell {

  int getCost(int level);

  int getMaxLevel();

  CastType getCastType();

  boolean hasState();

  SpellState createState();

  ActivationResult activate(int level, SpellUsageContext context);

}
