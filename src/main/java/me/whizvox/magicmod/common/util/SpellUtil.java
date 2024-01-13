package me.whizvox.magicmod.common.util;

import it.unimi.dsi.fastutil.Pair;
import me.whizvox.magicmod.MagicMod;
import me.whizvox.magicmod.common.api.MagicUser;
import me.whizvox.magicmod.common.api.ManaStorage;
import me.whizvox.magicmod.common.api.spell.Spell;
import me.whizvox.magicmod.common.api.spell.SpellInstance;
import me.whizvox.magicmod.common.lib.MMCapabilities;
import me.whizvox.magicmod.common.registry.SpellRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import java.util.function.Predicate;

public class SpellUtil {

  private static final ResourceLocation
      UNKNOWN_SPELL_ICON_TEXTURE = new ResourceLocation(MagicMod.MOD_ID, "textures/gui/unknown_spell_icon.png");
  private static final MutableComponent
      UNKNOWN_SPELL_COMPONENT = Component.translatable("spell.magicmod.unknown_spell.name");

  public static String formatRomanNumeralsLazy(int n) {
    return switch (n) {
      case 1 -> "I";
      case 2 -> "II";
      case 3 -> "III";
      case 4 -> "IV";
      case 5 -> "V";
      case 6 -> "VI";
      case 7 -> "VII";
      case 8 -> "VIII";
      case 9 -> "IX";
      case 10 -> "X";
      default -> Integer.toString(n);
    };
  }

  public static ResourceLocation getSpellIconTexture(ResourceLocation spellName) {
    return new ResourceLocation(spellName.getNamespace(), "textures/gui/spellicons/" + spellName.getPath() + ".png");
  }

  public static ResourceLocation getSpellIconTexture(Spell spell) {
    ResourceLocation spellName = getName(spell);
    if (spellName == null) {
      return UNKNOWN_SPELL_ICON_TEXTURE;
    }
    return getSpellIconTexture(spellName);
  }

  public static Pair<ManaStorage, MagicUser> getMagicCapabilities(ICapabilityProvider caster) {
    ManaStorage manaStorage = caster.getCapability(MMCapabilities.MANA_STORAGE).orElse(null);
    MagicUser magicUser = caster.getCapability(MMCapabilities.MAGIC_USER).orElse(null);
    return Pair.of(manaStorage, magicUser);
  }

  public static BlockHitResult lookingAtBlock(Entity entity, double range) {
    Vec3 from = entity.getEyePosition();
    Vec3 to = from.add(entity.getLookAngle().multiply(range, range, range));
    return entity.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
  }

  public static BlockHitResult lookingAtBlock(Entity entity) {
    return lookingAtBlock(entity, 5);
  }

  public static EntityHitResult lookingAtEntity(Entity entity, double range, Predicate<Entity> filter) {
    Vec3 from = entity.getEyePosition();
    Vec3 to = from.add(entity.getLookAngle().multiply(range, range, range));
    return ProjectileUtil.getEntityHitResult(entity, from, to, new AABB(from, to), filter, range);
  }

  public static ResourceLocation getName(Spell spell) {
    return SpellRegistry.getRegistry().getKey(spell);
  }

  public static Spell fromName(ResourceLocation name) {
    return SpellRegistry.getRegistry().getValue(name);
  }

  public static MutableComponent translateSpell(ResourceLocation name, boolean withHover) {
    MutableComponent comp;
    if (name == null) {
      comp = Component.translatable("spell.magicmod.unknown_spell.name");
    } else {
      comp = Component.translatable("spell.%s.%s.name".formatted(name.getNamespace(), name.getPath()));
    }
    if (withHover) {
      comp.withStyle(Style.EMPTY.withUnderlined(true).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(String.valueOf(name)))));
    }
    return comp;
  }

  public static MutableComponent translateSpell(ResourceLocation name) {
    return translateSpell(name, false);
  }

  public static MutableComponent translateSpell(Spell spell) {
    return translateSpell(getName(spell));
  }

  public static MutableComponent translateSpellWithLevel(ResourceLocation spellName, int level, boolean withHover) {
    return Component.translatable("tooltip.magicmod.spell.instance", translateSpell(spellName, withHover), formatRomanNumeralsLazy(level + 1));
  }

  public static MutableComponent translateSpellWithLevel(ResourceLocation spellName, int level) {
    return translateSpellWithLevel(spellName, level, false);
  }

  public static MutableComponent translateSpellWithLevel(SpellInstance spellInst, boolean withHover) {
    ResourceLocation spellName = getName(spellInst.spell());
    if (spellName == null) {
      return UNKNOWN_SPELL_COMPONENT.append(" " + formatRomanNumeralsLazy(spellInst.level() + 1));
    }
    return translateSpellWithLevel(spellName, spellInst.level(), withHover);
  }

  public static void encodeSpellInstance(SpellInstance spellInst, FriendlyByteBuf buf) {
    buf.writeResourceLocation(getName(spellInst.spell()));
    buf.writeByte(spellInst.level());
  }

  public static SpellInstance decodeSpellInstance(FriendlyByteBuf buf) {
    return decodeSpellInstance(buf, false);
  }

  public static SpellInstance decodeSpellInstance(FriendlyByteBuf buf, boolean logUnknownSpell) {
    ResourceLocation name = buf.readResourceLocation();
    Spell spell = fromName(name);
    if (spell == null && logUnknownSpell) {
      MagicMod.LOGGER.warn("Unknown spell while decoding: {}", name);
    }
    int level = buf.readByte();
    return new SpellInstance(spell, level);
  }

}
