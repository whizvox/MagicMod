package me.whizvox.magicmod.common.api;

import me.whizvox.magicmod.MagicMod;
import me.whizvox.magicmod.common.api.spell.Spell;
import me.whizvox.magicmod.common.api.spell.SpellInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MagicUser implements INBTSerializable<CompoundTag> {

  public static final int EQUIP_SLOTS = 9;

  private final Map<Spell, Integer> knownSpells;
  private final SpellInstance[] equippedSpells;
  private int selectedEquippedSpell;

  private boolean modified;

  public MagicUser() {
    knownSpells = new HashMap<>();
    equippedSpells = new SpellInstance[EQUIP_SLOTS];
    selectedEquippedSpell = 0;

    modified = false;
  }

  private void clearEquippedSpells() {
    for (int i = 0; i < EQUIP_SLOTS; i++) {
      equippedSpells[i] = null;
    }
  }

  private void markModified() {
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
    return tag;
  }

  @Override
  public void deserializeNBT(CompoundTag tag) {
    knownSpells.clear();
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
  }

  public static boolean isValidEquipSlot(int index) {
    return index >= 0 && index < EQUIP_SLOTS;
  }

}
