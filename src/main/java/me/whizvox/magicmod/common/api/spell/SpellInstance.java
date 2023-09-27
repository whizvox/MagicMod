package me.whizvox.magicmod.common.api.spell;

import me.whizvox.magicmod.common.util.SpellUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record SpellInstance(Spell spell, int level) {

  public SpellInstance(Map.Entry<Spell, Integer> mapEntry) {
    this(mapEntry.getKey(), mapEntry.getValue());
  }

  public CompoundTag serializeNBT() {
    CompoundTag tag = new CompoundTag();
    tag.putString("Spell", SpellUtil.getName(spell).toString());
    tag.putByte("Level", (byte) level);
    return tag;
  }

  public static SpellInstance fromTag(CompoundTag tag) {
    ResourceLocation spellName = new ResourceLocation(tag.getString("Spell"));
    Spell spell = SpellUtil.fromName(spellName);
    if (spell == null) {
      throw new IllegalArgumentException("Unknown spell: " + spellName);
    }
    int level = tag.getByte("Level");
    return new SpellInstance(spell, level);
  }

}
