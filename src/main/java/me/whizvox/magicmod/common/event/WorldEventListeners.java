package me.whizvox.magicmod.common.event;

import me.whizvox.magicmod.common.api.MagicUser;
import me.whizvox.magicmod.common.lib.MagicUserManager;
import me.whizvox.magicmod.common.lib.spell.ShieldSpell;
import me.whizvox.magicmod.common.lib.spelldata.ShieldSpellData;
import me.whizvox.magicmod.common.network.MMNetwork;
import me.whizvox.magicmod.common.network.UpdateManaMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WorldEventListeners {

  @SubscribeEvent
  public static void onServerTick(TickEvent.ServerTickEvent event) {
    if (event.phase == TickEvent.Phase.START) {
      MagicUserManager.forEach((playerId, magicUser) -> {
        ServerPlayer player = event.getServer().getPlayerList().getPlayer(playerId);
        if (player != null) {
          magicUser.tickManaCharge();
          magicUser.tickSpellData();
          if (magicUser.hasBeenModified()) {
            MMNetwork.sendToClient(player, new UpdateManaMessage(magicUser));
          }
        }
      });
    }
  }

  @SubscribeEvent
  public static void onEntityHurt(LivingHurtEvent event) {
    if (event.getEntity() instanceof Player player) {
      MagicUser magicUser = MagicUserManager.getUser(player);
      ShieldSpellData shieldSpellData = magicUser.getData(ShieldSpell.SHIELD_DATA_KEY);
      if (shieldSpellData != null) {
        double leftover = shieldSpellData.changeDurability(-event.getAmount());
        if (leftover > 0) {
          event.setAmount((float) leftover);
        } else {
          event.setCanceled(true);
        }
      }
    }
  }

}
