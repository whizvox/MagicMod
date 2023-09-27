package me.whizvox.magicmod.common.registry;

import me.whizvox.magicmod.MagicMod;
import me.whizvox.magicmod.common.api.spell.Spell;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class SpellRegistry {

  public static final ResourceLocation NAME = new ResourceLocation(MagicMod.MOD_ID, "spells");
  static final DeferredRegister<Spell> SPELLS = DeferredRegister.create(SpellRegistry.NAME, MagicMod.MOD_ID);
  private static Supplier<IForgeRegistry<Spell>> REGISTRY = SPELLS.makeRegistry(RegistryBuilder::new);

  public static IForgeRegistry<Spell> getRegistry() {
    return REGISTRY.get();
  }

  public static void register(IEventBus bus) {
    MMSpells.init();
    SPELLS.register(bus);
  }

}
