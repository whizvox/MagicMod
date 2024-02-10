package me.whizvox.magicmod.common.api;

import me.whizvox.magicmod.MagicMod;
import me.whizvox.magicmod.common.api.spell.Spell;
import me.whizvox.magicmod.common.api.spell.SpellInstance;
import me.whizvox.magicmod.common.api.spelldata.SpellData;
import me.whizvox.magicmod.common.api.spelldata.SpellDataManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;
import java.util.stream.Stream;

public class MagicUser implements INBTSerializable<CompoundTag> {

  public static final int
      RECHARGE_COOLDOWN = 20,
      EQUIP_SLOTS = 9;

  private double mana;
  private double maxMana;
  private double rechargeAmount;
  private int rechargeCooldown;

  private final Map<Spell, Integer> knownSpells;
  private final SpellInstance[] equippedSpells;
  private int selectedEquippedSpell;
  private final Map<ResourceLocation, SpellData> spellData;

  private boolean modified;

  public MagicUser() {
    mana = 0;
    rechargeAmount = 0;
    maxMana = 0;
    rechargeCooldown = 0;
    knownSpells = new HashMap<>();
    equippedSpells = new SpellInstance[EQUIP_SLOTS];
    selectedEquippedSpell = 0;
    spellData = new HashMap<>();
  }

  public void markModified() {
    modified = true;
  }

  public boolean hasBeenModified() {
    if (modified) {
      modified = false;
      return true;
    }
    return false;
  }

  public double getMana() {
    return mana;
  }

  public double getMaxMana() {
    return maxMana;
  }

  public double getRechargeAmount() {
    return rechargeAmount;
  }

  public void setMana(double mana) {
    double prevMana = this.mana;
    this.mana = Mth.clamp(mana, 0, maxMana);
    modified = modified || this.mana != prevMana;
  }

  public void changeMana(double amount) {
    setMana(mana + amount);
  }

  public void setMaxMana(double maxMana) {
    this.maxMana = Mth.clamp(maxMana, 0, Double.MAX_VALUE);
    modified = true;
  }

  public void setRechargeAmount(double rechargeAmount) {
    this.rechargeAmount = Mth.clamp(rechargeAmount, 0, Double.MAX_VALUE);
    modified = true;
  }

  public Stream<Map.Entry<Spell, Integer>> knownSpells() {
    return knownSpells.entrySet().stream();
  }

  public int getKnownLevel(Spell spell) {
    return knownSpells.getOrDefault(spell, -1);
  }

  public boolean canUseSpell(Spell spell, int level) {
    return getKnownLevel(spell) >= level;
  }

  public boolean isValidEquipSlot(int slot) {
    return slot >= 0 && slot < EQUIP_SLOTS;
  }

  public SpellInstance[] getEquippedSpells() {
    SpellInstance[] copy = new SpellInstance[equippedSpells.length];
    System.arraycopy(equippedSpells, 0, copy, 0, equippedSpells.length);
    return copy;
  }

  public SpellInstance getEquippedSpell(int slot) {
    return isValidEquipSlot(slot) ? equippedSpells[slot] : null;
  }

  public int getSelectedEquippedSpell() {
    return selectedEquippedSpell;
  }

  public <DATA extends SpellData> DATA getData(ResourceLocation key) {
    //noinspection unchecked
    return (DATA) spellData.get(key);
  }

  public void learnSpell(Spell spell, int level) {
    int currKnownLevel = getKnownLevel(spell);
    if (level > currKnownLevel && level <= spell.getMaxLevel()) {
      knownSpells.put(spell, level);
      markModified();
    }
  }

  public void learnSpell(Spell spell) {
    if (!knownSpells.containsKey(spell)) {
      knownSpells.put(spell, 0);
      markModified();
    }
  }

  public void unlearnSpell(Spell spell) {
    Integer ret = knownSpells.remove(spell);
    if (ret != null) {
      markModified();
    }
  }

  public void equipSpell(int slot, Spell spell, int level) {
    if (isValidEquipSlot(slot) && getKnownLevel(spell) >= level) {
      equippedSpells[slot] = new SpellInstance(spell, level);
      markModified();
    }
  }

  public void unequipSpell(int slot) {
    if (isValidEquipSlot(slot)) {
      equippedSpells[slot] = null;
      markModified();
    }
  }

  public void setSelectedEquippedSpell(int slot) {
    if (isValidEquipSlot(slot)) {
      selectedEquippedSpell = slot;
    }
  }

  public <DATA extends SpellData> DATA addData(ResourceLocation key, DATA data) {
    spellData.put(key, data);
    markModified();
    return data;
  }

  public void removeData(ResourceLocation key) {
    if (spellData.remove(key) != null) {
      markModified();
    }
  }

  public void tickManaCharge() {
    if (rechargeAmount > 0 && mana < maxMana) {
      if (++rechargeCooldown >= RECHARGE_COOLDOWN) {
        rechargeCooldown = 0;
        changeMana(rechargeAmount);
      }
    } else {
      if (rechargeCooldown != 0) {
        rechargeCooldown = 0;
      }
    }
  }

  public void tickSpellData() {
    if (!spellData.isEmpty()) {
      List<ResourceLocation> toRemove = new ArrayList<>();
      spellData.forEach((key, data) -> {
        if (data.shouldRemove()) {
          toRemove.add(key);
        } else {
          double drainAmount = data.tick(mana);
          if (drainAmount != 0) {
            changeMana(-drainAmount);
          }
        }
      });
      toRemove.forEach(spellData::remove);
      markModified();
    }
  }

  public void updateKnownSpells(List<SpellInstance> newKnownSpells) {
    knownSpells.clear();
    newKnownSpells.forEach(spellInst -> {
      knownSpells.put(spellInst.spell(), spellInst.level());
    });
  }

  public void updateEquippedSpells(SpellInstance[] newEquippedSpells) {
    System.arraycopy(newEquippedSpells, 0, equippedSpells, 0, EQUIP_SLOTS);
  }

  @Override
  public CompoundTag serializeNBT() {
    CompoundTag tag = new CompoundTag();
    tag.putDouble("Mana", mana);
    tag.putDouble("MaxMana", maxMana);
    tag.putDouble("RechargeAmount", rechargeAmount);
    ListTag knownSpellsTag = new ListTag();
    knownSpells.forEach((spell, level) -> knownSpellsTag.add(new SpellInstance(spell, level).serializeNBT()));
    tag.put("KnownSpells", knownSpellsTag);
    ListTag equippedSpellsTag = new ListTag();
    for (int i = 0; i < equippedSpells.length; i++) {
      SpellInstance spellInst = equippedSpells[i];
      if (spellInst != null) {
        CompoundTag equippedSpellTag = new CompoundTag();
        equippedSpellTag.putByte("Slot", (byte) i);
        equippedSpellTag.put("SpellInstance", spellInst.serializeNBT());
        equippedSpellsTag.add(equippedSpellTag);
      }
    }
    tag.put("EquippedSpells", equippedSpellsTag);
    tag.put("SpellData", SpellDataManager.INSTANCE.serialize(spellData));
    return tag;
  }

  @Override
  public void deserializeNBT(CompoundTag nbt) {
    knownSpells.clear();
    spellData.clear();
    Arrays.fill(equippedSpells, null);
    mana = nbt.getDouble("Mana");
    maxMana = nbt.getDouble("MaxMana");
    rechargeAmount = nbt.getDouble("RechargeAmount");
    nbt.getList("KnownSpells", Tag.TAG_COMPOUND).forEach(knownSpellTag -> {
      SpellInstance spellInst = SpellInstance.fromTag((CompoundTag) knownSpellTag);
      knownSpells.put(spellInst.spell(), spellInst.level());
    });
    nbt.getList("EquippedSpells", Tag.TAG_COMPOUND).forEach(equippedSpellTagRaw -> {
      CompoundTag equippedSpellTag = (CompoundTag) equippedSpellTagRaw;
      int slot = equippedSpellTag.getByte("Slot");
      SpellInstance spellInst = SpellInstance.fromTag(equippedSpellTag.getCompound("SpellInstance"));
      if (isValidEquipSlot(slot)) {
        equippedSpells[slot] = spellInst;
      } else {
        MagicMod.LOGGER.warn("Invalid equip slot while deserializing MagicUser: {}", slot);
      }
    });
    SpellDataManager.INSTANCE.deserialize(nbt.get("SpellData"), spellData);
  }
}
