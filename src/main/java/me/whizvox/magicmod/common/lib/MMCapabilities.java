package me.whizvox.magicmod.common.lib;

import me.whizvox.magicmod.MagicMod;
import me.whizvox.magicmod.common.api.MagicUser;
import me.whizvox.magicmod.common.capability.MagicUserProvider;
import me.whizvox.magicmod.common.api.ManaStorage;
import me.whizvox.magicmod.common.capability.ManaStorageProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class MMCapabilities {

  public static final Capability<ManaStorage> MANA_STORAGE = CapabilityManager.get(new CapabilityToken<>() {});
  public static final Capability<MagicUser> MAGIC_USER = CapabilityManager.get(new CapabilityToken<MagicUser>() {});

  private static void onRegister(RegisterCapabilitiesEvent event) {
    event.register(ManaStorage.class);
    event.register(MagicUser.class);
  }

  private static void onAttach(AttachCapabilitiesEvent<Entity> event) {
    if (event.getObject() instanceof Player) {
      event.addCapability(new ResourceLocation(MagicMod.MOD_ID, "mana_storage"), new ManaStorageProvider());
      event.addCapability(new ResourceLocation(MagicMod.MOD_ID, "magic_user"), new MagicUserProvider());
    }
  }

  public static void register(IEventBus modBus, IEventBus forgeBus) {
    modBus.addListener(MMCapabilities::onRegister);
    forgeBus.addGenericListener(Entity.class, MMCapabilities::onAttach);
  }

}
