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
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.stream.Stream;

public class MagicUser implements INBTSerializable<CompoundTag> {

  public static final int EQUIP_SLOTS = 9;

  private final Map<Spell, Integer> knownSpells;
  private final SpellInstance[] equippedSpells;
  private int selectedEquippedSpell;
  private final Map<ResourceLocation, SpellData> spellData;

  private boolean modified;

  public MagicUser() {
    knownSpells = new HashMap<>();
    equippedSpells = new SpellInstance[EQUIP_SLOTS];
    selectedEquippedSpell = 0;
    spellData = new HashMap<>();

    modified = false;
  }

  private void clearEquippedSpells() {
    for (int i = 0; i < EQUIP_SLOTS; i++) {
      equippedSpells[i] = null;
    }
  }

  public void markModified() {
    modified = true;
  }

  public Stream<Map.Entry<Spell, Integer>> allKnownSpells() {
    return knownSpells.entrySet().stream();
  }

  /**
   * Gets the learned level of a potentially known spell.
   * @param spell The spell to check
   * @return The learned level of the spell (zero-indexed), -1 if not known
   */
  public int getKnownLevel(Spell spell) {
    return knownSpells.getOrDefault(spell, -1);
  }

  public boolean canUseSpell(Spell spell, int level) {
    return getKnownLevel(spell) >= level;
  }

  public SpellInstance getEquippedSpell(int slot) {
    if (isValidEquipSlot(slot)) {
      return equippedSpells[slot];
    }
    return null;
  }

  public SpellInstance[] getEquippedSpells() {
    SpellInstance[] copy = new SpellInstance[EQUIP_SLOTS];
    System.arraycopy(equippedSpells, 0, copy, 0, EQUIP_SLOTS);
    return copy;
  }

  public int getSelectedEquippedSpell() {
    return selectedEquippedSpell;
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

  public void downgradeLearnedSpell(Spell spell, int newLevel) {
    if (newLevel < 0) {
      unlearnSpell(spell);
    } else {
      int level = getKnownLevel(spell);
      if (newLevel < level) {
        knownSpells.put(spell, newLevel);
      }
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

  public boolean hasBeenModified() {
    if (modified) {
      modified = false;
      return true;
    }
    return false;
  }

  public void setSelectedEquippedSpell(int slot) {
    if (isValidEquipSlot(slot)) {
      selectedEquippedSpell = slot;
    }
  }

  public <DATA extends SpellData> DATA getData(ResourceLocation key) {
    //noinspection unchecked
    return (DATA) spellData.get(key);
  }

  public <DATA extends SpellData> DATA addData(ResourceLocation key, DATA data) {
    spellData.put(key, data);
    markModified();
    return data;
  }

  public void tickData(DoubleSupplier manaSupplier, Consumer<Double> manaDrainConsumer) {
    if (!spellData.isEmpty()) {
      List<ResourceLocation> toRemove = new ArrayList<>();
      spellData.forEach((key, data) -> {
        if (data.shouldRemove()) {
          toRemove.add(key);
        } else {
          double drainAmount = data.tick(manaSupplier.getAsDouble());
          if (drainAmount != 0) {
            manaDrainConsumer.accept(drainAmount);
          }
        }
      });
      toRemove.forEach(spellData::remove);
      markModified();
    }
  }

  public void removeData(ResourceLocation key) {
    if (spellData.remove(key) != null) {
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
  public void deserializeNBT(CompoundTag tag) {
    knownSpells.clear();
    spellData.clear();
    Arrays.fill(equippedSpells, null);
    tag.getList("KnownSpells", Tag.TAG_COMPOUND).forEach(knownSpellTag -> {
      SpellInstance spellInst = SpellInstance.fromTag((CompoundTag) knownSpellTag);
      knownSpells.put(spellInst.spell(), spellInst.level());
    });
    tag.getList("EquippedSpells", Tag.TAG_COMPOUND).forEach(equippedSpellTagRaw -> {
      CompoundTag equippedSpellTag = (CompoundTag) equippedSpellTagRaw;
      int slot = equippedSpellTag.getByte("Slot");
      SpellInstance spellInst = SpellInstance.fromTag(equippedSpellTag.getCompound("SpellInstance"));
      if (isValidEquipSlot(slot)) {
        equippedSpells[slot] = spellInst;
      } else {
        MagicMod.LOGGER.warn("Invalid equip slot while deserializing MagicUser: {}", slot);
      }
    });
    SpellDataManager.INSTANCE.deserialize(tag.get("SpellData"), spellData);
  }

  public static boolean isValidEquipSlot(int index) {
    return index >= 0 && index < EQUIP_SLOTS;
  }

}
