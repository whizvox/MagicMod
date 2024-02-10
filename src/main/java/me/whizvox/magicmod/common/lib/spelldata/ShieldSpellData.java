package me.whizvox.magicmod.common.lib.spelldata;

import me.whizvox.magicmod.common.api.spelldata.SpellData;
import me.whizvox.magicmod.common.api.spelldata.SpellDataSerializer;
import net.minecraft.nbt.CompoundTag;

public class ShieldSpellData implements SpellData {

  public final int level;
  private double durability;
  private int duration;

  private ShieldSpellData(int level, double durability, int duration) {
    this.level = level;
    this.durability = durability;
    this.duration = duration;
  }

  public ShieldSpellData(int level) {
    this(level, 20.0 * Math.pow(2, level), (level + 1) * 1200);
  }

  public double getDurability() {
    return durability;
  }

  public int getDuration() {
    return duration;
  }

  public double changeDurability(double amount) {
    durability += amount;
    if (durability < 0) {
      return -durability;
    }
    return 0;
  }

  @Override
  public double tick(double currentMana) {
    int manaCost = level;
    if (currentMana < manaCost) {
      return 0;
    }
    duration--;
    return manaCost;
  }

  @Override
  public boolean shouldRemove() {
    return durability <= 0 || duration <= 0;
  }

  public static final SpellDataSerializer<ShieldSpellData, CompoundTag> SERIALIZER = new SpellDataSerializer<>() {

    @Override
    public ShieldSpellData deserialize(CompoundTag nbt) {
      int level = nbt.getByte("Level");
      double durability = nbt.getDouble("Durability");
      int duration = nbt.getShort("Duration");
      return new ShieldSpellData(level, durability, duration);
    }

    @Override
    public CompoundTag serialize(ShieldSpellData data) {
      CompoundTag nbt = new CompoundTag();
      nbt.putByte("Level", (byte) data.level);
      nbt.putDouble("Durability", data.durability);
      nbt.putShort("Duration", (short) data.duration);
      return nbt;
    }

  };

}
