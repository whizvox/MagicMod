package me.whizvox.magicmod.common.api.spelldata;

public interface SpellData {

  double tick(double currentMana);

  boolean shouldRemove();

}
