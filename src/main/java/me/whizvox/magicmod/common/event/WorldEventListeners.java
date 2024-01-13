package me.whizvox.magicmod.common.event;

import me.whizvox.magicmod.common.network.MMNetwork;
import me.whizvox.magicmod.common.lib.MMCapabilities;
import me.whizvox.magicmod.common.network.UpdateEquippedSpellsMessage;
import me.whizvox.magicmod.common.network.UpdateKnownSpellsMessage;
import me.whizvox.magicmod.common.network.UpdateManaMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WorldEventListeners {

  @SubscribeEvent
  public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
    if (event.phase == TickEvent.Phase.START && !event.player.level().isClientSide) {
      event.player.getCapability(MMCapabilities.MANA_STORAGE).ifPresent(manaStorage -> {
        if (manaStorage.getRechargeRate() > 0) {
          manaStorage.attemptRecharge();
          if (manaStorage.isModified()) {
            MMNetwork.sendToClient((ServerPlayer) event.player, new UpdateManaMessage(manaStorage));
          }
        }
      });
    }
  }

  @SubscribeEvent
  public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent e) {
    ServerPlayer player = (ServerPlayer) e.getEntity();
    player.getCapability(MMCapabilities.MANA_STORAGE).ifPresent(manaStorage -> {
      MMNetwork.sendToClient(player, new UpdateManaMessage(manaStorage));
    });
    player.getCapability(MMCapabilities.MAGIC_USER).ifPresent(magicUser -> {
      MMNetwork.sendToClient(player, new UpdateKnownSpellsMessage(magicUser));
      MMNetwork.sendToClient(player, new UpdateEquippedSpellsMessage(magicUser, true));
    });
  }

}
