package me.whizvox.magicmod.common.event;

import me.whizvox.magicmod.common.network.MMNetwork;
import me.whizvox.magicmod.common.lib.MMCapabilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WorldEventListeners {

  @SubscribeEvent
  public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
    if (event.phase == TickEvent.Phase.START && !event.player.level().isClientSide) {
      event.player.getCapability(MMCapabilities.MANA_STORAGE).ifPresent(manaStorage -> {
        if (manaStorage.getRechargeRate() > 0) {
          manaStorage.attemptRecharge();
          if (manaStorage.isModified()) {
            MMNetwork.updatePlayerMana((ServerPlayer) event.player, manaStorage);
          }
        }
      });
    }
  }

}
