package me.whizvox.magicmod.common.registry;

import me.whizvox.magicmod.common.api.spell.Spell;
import me.whizvox.magicmod.common.lib.spell.BlinkSpell;
import me.whizvox.magicmod.common.lib.spell.FireballSpell;
import me.whizvox.magicmod.common.lib.spell.HarmSpell;
import me.whizvox.magicmod.common.lib.spell.HealSpell;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

import static me.whizvox.magicmod.common.registry.SpellRegistry.SPELLS;

public class MMSpells {

  public static final RegistryObject<Spell>
      HARM = SPELLS.register("harm", HarmSpell::new),
      HEAL = SPELLS.register("heal", HealSpell::new),
      FIREBALL = SPELLS.register("fireball", FireballSpell::new),
      BLINK = SPELLS.register("blink", BlinkSpell::new);

  public static void register(IEventBus bus) {
    SPELLS.register(bus);
  }

}
