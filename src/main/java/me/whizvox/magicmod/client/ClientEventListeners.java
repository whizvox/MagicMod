package me.whizvox.magicmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.whizvox.magicmod.MagicMod;
import me.whizvox.magicmod.client.gui.SpellInventoryScreen;
import me.whizvox.magicmod.common.api.MagicUser;
import me.whizvox.magicmod.common.api.spell.SpellInstance;
import me.whizvox.magicmod.common.lib.MagicUserManager;
import me.whizvox.magicmod.common.network.MMNetwork;
import me.whizvox.magicmod.common.network.UpdateSelectedSpellMessage;
import me.whizvox.magicmod.common.registry.MMItems;
import me.whizvox.magicmod.common.util.SpellUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEventListeners {

  private static final ResourceLocation
      MANA_BAR_GUI = new ResourceLocation(MagicMod.MOD_ID, "textures/gui/manabar.png"),
      SPELL_WIDGETS = new ResourceLocation(MagicMod.MOD_ID, "textures/gui/spell_widgets.png"),
      UNKNOWN_SPELL_ICON = new ResourceLocation(MagicMod.MOD_ID, "textures/gui/unknown_spell_icon.png");

  private static ResourceLocation getSpellIconLocation(ResourceLocation spellName) {
    return new ResourceLocation(spellName.getNamespace(), "textures/gui/spellicons/" + spellName.getPath() + ".png");
  }

  private static int renderEquippedSpellNameTimer = 0;
  private static Component equippedSpellTooltip = null;

  @SubscribeEvent
  public static void onRenderGui(RenderGuiOverlayEvent.Pre event) {
    if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type()) {
      Player player = Minecraft.getInstance().player;
      MagicUser magicUser = MagicUserManager.getUser(player);
      GuiGraphics g = event.getGuiGraphics();
      int manaBarStartX = 15;
      int manaBarStartY = event.getWindow().getGuiScaledHeight() - 26;
      g.blit(MANA_BAR_GUI, manaBarStartX, manaBarStartY, 50, 0, 0, 64, 16, 64, 32);
      g.blit(MANA_BAR_GUI, manaBarStartX, manaBarStartY, 50, 0, 16, (int) ((magicUser.getMana() / magicUser.getMaxMana()) * 64), 16, 64, 32);
      g.drawCenteredString(Minecraft.getInstance().font, "%d".formatted((int) magicUser.getMana()), manaBarStartX + 32, manaBarStartY + 4, 0xFFFFFF);
      if (player.isShiftKeyDown()) {
        if (player.getMainHandItem().is(MMItems.WAND.get()) || player.getOffhandItem().is(MMItems.WAND.get())) {
          event.setCanceled(true);
          int hotbarStartX = g.guiWidth() / 2 - 91;
          int hotbarStartY = g.guiHeight() - 22;
          g.blit(SPELL_WIDGETS, hotbarStartX, hotbarStartY, 0, 0, 182, 22);
          g.blit(SPELL_WIDGETS, hotbarStartX - 1 + magicUser.getSelectedEquippedSpell() * 20, hotbarStartY - 1, 0, 22, 24, 24);
          SpellInstance[] equippedSpells = magicUser.getEquippedSpells();
          for (int i = 0; i < equippedSpells.length; i++) {
            SpellInstance spellInst = equippedSpells[i];
            if (spellInst != null) {
              SpellInventoryScreen.renderSpellIcon(g, spellInst, hotbarStartX + 3 + i * 20, hotbarStartY + 3);
            }
          }
        }
      }
      if (renderEquippedSpellNameTimer > 0 && equippedSpellTooltip != null) {
        Minecraft mc = Minecraft.getInstance();
        int width = mc.font.width(equippedSpellTooltip);
        int x = (g.guiWidth() - width) / 2;
        int y = g.guiHeight() - 59;
        if (mc.gameMode.canHurtPlayer()) {
          y -= 14;
        }
        int alpha = (int) ((float) renderEquippedSpellNameTimer * 256.0F / 40.0F);
        if (alpha > 255) {
          alpha = 255;
        }
        if (alpha > 0) {
          g.fill(x - 2, y - 2, x + width + 2, y + mc.font.lineHeight + 2, mc.options.getBackgroundColor(0));
          g.drawString(mc.font, equippedSpellTooltip, x, y, 0xFFFFFF | (alpha << 24));
        }
        renderEquippedSpellNameTimer--;
      }
    }
  }

  @SubscribeEvent
  public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
    Player player = Minecraft.getInstance().player;
    if (player.isShiftKeyDown() && (player.getMainHandItem().is(MMItems.WAND.get()) || player.getOffhandItem().is(MMItems.WAND.get()))) {
      MagicUser magicUser = MagicUserManager.getUser(player);
      event.setCanceled(true);
      int slot = magicUser.getSelectedEquippedSpell();
      if (event.getScrollDelta() > 0) {
        slot--;
      } else {
        slot++;
      }
      if (slot < 0) {
        slot = MagicUser.EQUIP_SLOTS - 1;
      } else if (slot >= MagicUser.EQUIP_SLOTS) {
        slot = 0;
      }
      magicUser.setSelectedEquippedSpell(slot);
      SpellInstance selectedSpell = magicUser.getEquippedSpell(slot);
      if (selectedSpell == null) {
        equippedSpellTooltip = null;
        renderEquippedSpellNameTimer = 0;
      } else {
        equippedSpellTooltip = SpellUtil.translateSpellWithLevel(magicUser.getEquippedSpell(slot), false);
        renderEquippedSpellNameTimer = (int) (Minecraft.getInstance().options.notificationDisplayTime().get() * 160);
      }
      MMNetwork.sendToServer(new UpdateSelectedSpellMessage(slot));
    }
  }

  @SubscribeEvent
  public static void onKey(InputEvent.Key event) {
    InputConstants.Key key = InputConstants.getKey(event.getKey(), event.getScanCode());
    if (MMKeyMappings.OPEN_SPELL_INVENTORY.isActiveAndMatches(key)) {
      if (Minecraft.getInstance().screen == null) {
        Minecraft.getInstance().setScreen(new SpellInventoryScreen());
      }
    }
  }

}
