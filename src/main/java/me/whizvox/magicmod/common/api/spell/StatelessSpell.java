package me.whizvox.magicmod.common.api.spell;

public interface StatelessSpell extends Spell {

  @Override
  default boolean hasState() {
    return false;
  }

  @Override
  default SpellState createState() {
    return null;
  }

}
