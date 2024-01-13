package me.whizvox.magicmod.server.lib;

import me.whizvox.magicmod.MagicMod;
import me.whizvox.magicmod.server.command.arguments.SpellArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MMArgumentTypes {

  private static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, MagicMod.MOD_ID);

  public static void register(IEventBus bus) {
    ARGUMENT_TYPES.register(bus);
  }

  public static final RegistryObject<ArgumentTypeInfo<?, ?>>
      SPELL = ARGUMENT_TYPES.register("spell", () -> ArgumentTypeInfos.registerByClass(SpellArgumentType.class, SingletonArgumentInfo.contextFree(SpellArgumentType::new)));

}
