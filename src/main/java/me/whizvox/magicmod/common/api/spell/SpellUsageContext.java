package me.whizvox.magicmod.common.api.spell;

import me.whizvox.magicmod.common.api.MagicUser;
import me.whizvox.magicmod.common.api.ManaStorage;
import me.whizvox.magicmod.common.lib.MMCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public record SpellUsageContext(
    ICapabilityProvider caster,
    SpellState state,
    MagicUser user,
    ManaStorage mana) {

  public boolean hasState() {
    return state != null;
  }

  public boolean hasUser() {
    return user != null;
  }

  public boolean hasMana() {
    return mana != null;
  }

  public static SpellUsageContext from(ICapabilityProvider caster, int slot, Class<SpellState> stateClass) {
    MagicUser magicUser = caster.getCapability(MMCapabilities.MAGIC_USER)
        .orElseThrow(() -> new IllegalArgumentException("Caster of type " + caster.getClass() + " does not have a MagicUser"));
    ManaStorage manaStorage = caster.getCapability(MMCapabilities.MANA_STORAGE).orElse(null);
    SpellState state = magicUser.getSpellState(slot, stateClass);
    return new SpellUsageContext(caster, state, magicUser, manaStorage);
  }

  public static SpellUsageContext from(ICapabilityProvider caster, int slot) {
    return from(caster, slot, SpellState.class);
  }

}
