package me.whizvox.magicmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.whizvox.magicmod.MagicMod;
import me.whizvox.magicmod.client.gui.SpellInventoryScreen;
import me.whizvox.magicmod.common.api.MagicUser;
import me.whizvox.magicmod.common.api.spell.SpellInstance;
import me.whizvox.magicmod.common.lib.MMCapabilities;
import me.whizvox.magicmod.common.network.MMNetwork;
import me.whizvox.magicmod.common.network.UpdateSelectedSpellMessage;
import me.whizvox.magicmod.common.registry.MMItems;
import me.whizvox.magicmod.common.util.SpellUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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

  @SubscribeEvent
  public static void onRenderGui(RenderGuiOverlayEvent.Pre event) {
    if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type()) {
      Player player = Minecraft.getInstance().player;
      player.getCapability(MMCapabilities.MANA_STORAGE).ifPresent(manaStorage -> {
        GuiGraphics g = event.getGuiGraphics();
        int startX = 15;
        int startY = event.getWindow().getGuiScaledHeight() - 26;
        g.blit(MANA_BAR_GUI, startX, startY, 50, 0, 0, 64, 16, 64, 32);
        g.blit(MANA_BAR_GUI, startX, startY, 50, 0, 16, (int) ((manaStorage.getMana() / manaStorage.getMaxMana()) * 64), 16, 64, 32);
        g.drawCenteredString(Minecraft.getInstance().font, "%d".formatted((int) manaStorage.getMana()), startX + 32, startY + 4, 0xFFFFFF);
      });
      if (player.isShiftKeyDown()) {
        if (player.getMainHandItem().is(MMItems.WAND.get()) || player.getOffhandItem().is(MMItems.WAND.get())) {
          player.getCapability(MMCapabilities.MAGIC_USER).ifPresent(magicUser -> {
            event.setCanceled(true);
            GuiGraphics g = event.getGuiGraphics();
            int startX = g.guiWidth() / 2 - 91;
            int startY = g.guiHeight() - 22;
            g.blit(SPELL_WIDGETS, startX, startY, 0, 0, 182, 22);
            g.blit(SPELL_WIDGETS, startX - 1 + magicUser.getSelectedEquippedSpell() * 20, startY - 1, 0, 22, 24, 24);
            SpellInstance[] equippedSpells = magicUser.getEquippedSpells();
            for (int i = 0; i < equippedSpells.length; i++) {
              SpellInstance spellInst = equippedSpells[i];
              if (spellInst != null) {
                SpellInventoryScreen.renderSpellIcon(g, spellInst, startX + 3 + i * 20, startY + 3);
              }
            }
          });
        }
      }
    }
  }

  @SubscribeEvent
  public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
    Player player = Minecraft.getInstance().player;
    if (player.isShiftKeyDown() && (player.getMainHandItem().is(MMItems.WAND.get()) || player.getOffhandItem().is(MMItems.WAND.get()))) {
      player.getCapability(MMCapabilities.MAGIC_USER).ifPresent(magicUser -> {
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
        MMNetwork.sendToServer(new UpdateSelectedSpellMessage(slot));
      });
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
