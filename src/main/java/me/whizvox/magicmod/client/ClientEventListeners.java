package me.whizvox.magicmod.client;

import me.whizvox.magicmod.MagicMod;
import me.whizvox.magicmod.common.lib.MMCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEventListeners {

  private static final ResourceLocation MANA_BAR_GUI = new ResourceLocation(MagicMod.MOD_ID, "textures/gui/manabar.png");

  @SubscribeEvent
  public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
    if (event.getOverlay() == VanillaGuiOverlay.PLAYER_HEALTH.type()) {
      Minecraft.getInstance().player.getCapability(MMCapabilities.MANA_STORAGE).ifPresent(manaStorage -> {
        GuiGraphics g = event.getGuiGraphics();
        int startX = 15;
        int startY = event.getWindow().getGuiScaledHeight() - 26;
        g.blit(MANA_BAR_GUI, startX, startY, 50, 0, 0, 64, 16, 64, 32);
        g.blit(MANA_BAR_GUI, startX, startY, 50, 0, 16, (int) ((manaStorage.getMana() / manaStorage.getMaxMana()) * 64), 16, 64, 32);
        g.drawCenteredString(Minecraft.getInstance().font, "%d".formatted((int) manaStorage.getMana()), startX + 32, startY + 4, 0xFFFFFF);
      });
    }
  }

}
