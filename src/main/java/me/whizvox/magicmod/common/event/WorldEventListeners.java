package me.whizvox.magicmod.common.event;

import me.whizvox.magicmod.MagicMod;
import me.whizvox.magicmod.common.lib.spell.ShieldSpell;
import me.whizvox.magicmod.common.lib.spelldata.ShieldSpellData;
import me.whizvox.magicmod.common.network.MMNetwork;
import me.whizvox.magicmod.common.lib.MMCapabilities;
import me.whizvox.magicmod.common.network.UpdateEquippedSpellsMessage;
import me.whizvox.magicmod.common.network.UpdateKnownSpellsMessage;
import me.whizvox.magicmod.common.network.UpdateManaMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
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
        event.player.getCapability(MMCapabilities.MAGIC_USER).ifPresent(magicUser ->
            magicUser.tickData(manaStorage::getMana, drainAmount -> manaStorage.changeMana(-drainAmount))
        );
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

  @SubscribeEvent
  public static void onEntityHurt(LivingHurtEvent event) {
    if (event.getEntity() instanceof Player player) {
      player.getCapability(MMCapabilities.MAGIC_USER).ifPresent(magicUser -> {
        ShieldSpellData shieldSpellData = magicUser.getData(ShieldSpell.SHIELD_DATA_KEY);
        if (shieldSpellData != null) {
          double leftover = shieldSpellData.changeDurability(-event.getAmount());
          MagicMod.LOGGER.info("total={}, leftover={}", event.getAmount(), leftover);
          if (leftover > 0) {
            event.setAmount((float) leftover);
          } else {
            event.setCanceled(true);
          }
        }
      });
    }
  }

}
