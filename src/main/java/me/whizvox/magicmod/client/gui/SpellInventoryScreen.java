package me.whizvox.magicmod.client.gui;

import me.whizvox.magicmod.MagicMod;
import me.whizvox.magicmod.common.api.MagicUser;
import me.whizvox.magicmod.common.api.spell.Spell;
import me.whizvox.magicmod.common.api.spell.SpellInstance;
import me.whizvox.magicmod.common.lib.MagicUserManager;
import me.whizvox.magicmod.common.network.MMNetwork;
import me.whizvox.magicmod.common.network.UpdateEquippedSpellsMessage;
import me.whizvox.magicmod.common.registry.SpellRegistry;
import me.whizvox.magicmod.common.util.SpellUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class SpellInventoryScreen extends Screen {

  private static final ResourceLocation TEXTURE = new ResourceLocation(MagicMod.MOD_ID, "textures/gui/spell_inventory.png");

  public static final int
      HOTBAR_SLOTS = 9,
      SEARCH_SLOTS = 54,
      TOTAL_SLOTS = HOTBAR_SLOTS + SEARCH_SLOTS;

  private static int getSlotX(int slot) {
    return 8 + (slot % 9) * 18;
  }

  private static int getSlotY(int slot) {
    if (slot < HOTBAR_SLOTS) {
      return 142;
    }
    return 12 + (slot / 9) * 18;
  }

  private int leftPos, topPos;
  private final SpellSlot[] icons;
  private int scroll;

  private SpellInstance heldSpellInst;

  public SpellInventoryScreen() {
    super(Component.literal("Spells"));
    leftPos = 0;
    topPos = 0;
    icons = new SpellSlot[TOTAL_SLOTS];
    scroll = 0;
    heldSpellInst = null;
  }

  @Override
  protected void init() {
    MagicUser magicUser = MagicUserManager.getUser(minecraft.player);
    leftPos = (width - 194) / 2;
    topPos = (height - 166) / 2;
    for (int i = 0; i < icons.length; i++) {
      SpellInstance spellInst = null;
      if (i < HOTBAR_SLOTS && magicUser != null) {
        spellInst = magicUser.getEquippedSpell(i);
      }
      icons[i] = new SpellSlot(leftPos + getSlotX(i), topPos + getSlotY(i), spellInst, i >= HOTBAR_SLOTS);
      addRenderableWidget(icons[i]);
    }
    updateReadOnlyIcons();
  }

  @Override
  public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
    renderBackground(g);
    g.blit(TEXTURE, leftPos, topPos, 0, 0, 194, 166, 256, 256);
    super.render(g, mouseX, mouseY, partialTick);
    for (SpellSlot icon : icons) {
      if (icon.isHovered()) {
        int x = icon.getX();
        int y = icon.getY();
        g.fillGradient(RenderType.guiOverlay(), x, y, x + 16, y + 16, 0x80FFFFFF, 0x80FFFFFF, 0);
      }
    }
    if (heldSpellInst != null) {
      g.pose().pushPose();
      g.pose().translate(0, 0, 1);
      renderSpellIcon(g, heldSpellInst, mouseX - 8, mouseY - 8);
      g.pose().popPose();
    }
  }

  private void updateReadOnlyIcons() {
    List<SpellInstance> spells = SpellRegistry.getRegistry().getValues().stream()
        .mapMulti((BiConsumer<Spell, Consumer<SpellInstance>>) (spell, consumer) -> {
          for (int level = 0; level < spell.getMaxLevel(); level++) {
            consumer.accept(new SpellInstance(spell, level));
          }
        })
        .sorted(Comparator.comparing((Function<SpellInstance, ResourceLocation>) spellInst -> SpellUtil.getName(spellInst.spell()))
            .thenComparing(SpellInstance::level))
        .skip(scroll * 9L)
        .limit(SEARCH_SLOTS)
        .toList();
    for (int i = 0; i < spells.size(); i++) {
      icons[i + HOTBAR_SLOTS].spellInst = spells.get(i);
      icons[i + HOTBAR_SLOTS].update();
    }
  }

  public void setScroll(int scroll) {
    this.scroll = scroll;
    updateReadOnlyIcons();
  }

  @Override
  public void onClose() {
    MagicUser magicUser = MagicUserManager.getUser(minecraft.player);
    for (int i = 0; i < HOTBAR_SLOTS; i++) {
      SpellInstance spellInst = icons[i].spellInst;
      if (spellInst == null) {
        magicUser.unequipSpell(i);
      } else {
        magicUser.equipSpell(i, spellInst.spell(), spellInst.level());
      }
    }
    MMNetwork.sendToServer(new UpdateEquippedSpellsMessage(magicUser, false));
    super.onClose();
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (keyCode == GLFW.GLFW_KEY_E) {
      onClose();
      return true;
    }
    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  public static void renderSpellIcon(GuiGraphics g, SpellInstance spellInst, int x, int y) {
    ResourceLocation spellIcon = SpellUtil.getSpellIconTexture(spellInst.spell());
    FormattedCharSequence seq = FormattedCharSequence.forward(SpellUtil.formatRomanNumeralsLazy(spellInst.level() + 1), Style.EMPTY);
    int width = Minecraft.getInstance().font.width(seq);
    g.blit(spellIcon, x, y, 0, 0, 0, 16, 16, 16, 16);
    g.drawString(Minecraft.getInstance().font, seq, x + 17 - width, y + 18 - Minecraft.getInstance().font.lineHeight, 0xFFFFFF);
  }

  public class SpellSlot extends AbstractWidget {

    private final boolean isReadOnly;
    private SpellInstance spellInst;
    private ResourceLocation iconTexture;
    private Component levelComp;

    public SpellSlot(int x, int y, SpellInstance spellInst, boolean isReadOnly) {
      super(x, y, 16, 16, Component.empty());
      this.spellInst = spellInst;
      this.isReadOnly = isReadOnly;
      iconTexture = null;
      levelComp = null;
      update();
    }

    public void update() {
      if (spellInst == null) {
        setMessage(Component.empty());
        iconTexture = null;
        levelComp = null;
      } else {
        setMessage(SpellUtil.translateSpellWithLevel(spellInst, false));
        iconTexture = SpellUtil.getSpellIconTexture(spellInst.spell());
        levelComp = Component.literal(SpellUtil.formatRomanNumeralsLazy(spellInst.level() + 1));
      }
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
      if (iconTexture != null) {
        g.blit(iconTexture, getX(), getY(), 0, 0, 0, getWidth(), getHeight(), 16, 16);
        FormattedCharSequence seq = FormattedCharSequence.forward(levelComp.getString(), Style.EMPTY);
        int width = minecraft.font.width(seq);
        g.drawString(minecraft.font, seq, getX() + 17 - width, getY() + 18 - minecraft.font.lineHeight, 0xFFFFFF);
      }
      if (isHovered) {
        setTooltipForNextRenderPass(getMessage());
      }
    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {
      if (isReadOnly) {
        if (SpellInventoryScreen.this.heldSpellInst == null) {
          SpellInventoryScreen.this.heldSpellInst = spellInst;
        } else {
          SpellInventoryScreen.this.heldSpellInst = null;
        }
      } else {
        SpellInstance temp = SpellInventoryScreen.this.heldSpellInst;
        SpellInventoryScreen.this.heldSpellInst = spellInst;
        spellInst = temp;
        update();
      }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
      defaultButtonNarrationText(output);
    }

  }

}
