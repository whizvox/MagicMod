package me.whizvox.magicmod.common.api;

import it.unimi.dsi.fastutil.Pair;
import me.whizvox.magicmod.MagicMod;
import me.whizvox.magicmod.common.api.spell.Spell;
import me.whizvox.magicmod.common.api.spell.SpellInstance;
import me.whizvox.magicmod.common.api.spell.SpellState;
import me.whizvox.magicmod.common.util.SpellUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;

public class MagicUser implements INBTSerializable<CompoundTag> {

  public static int EQUIP_SLOTS = 9;

  private int level;
  private int experience;
  private final Map<Spell, Integer> knownSpells;
  private final SpellInstance[] equippedSpells;
  private final SpellState[] spellStates;
  private int selectedEquippedSpell;

  private int currLevelXp;
  private int nextLevelXp;
  private boolean modified;

  public MagicUser() {
    level = 0;
    experience = 0;
    knownSpells = new HashMap<>();
    equippedSpells = new SpellInstance[EQUIP_SLOTS];
    spellStates = new SpellState[EQUIP_SLOTS];
    selectedEquippedSpell = 0;

    currLevelXp = 0;
    nextLevelXp = 0;
    modified = false;
  }

  private void clearEquippedSpells() {
    for (int i = 0; i < EQUIP_SLOTS; i++) {
      equippedSpells[i] = null;
      spellStates[i] = null;
    }
  }

  private void markModified() {
    modified = true;
  }

  public int getLevel() {
    return level;
  }

  public int getExperience() {
    return experience;
  }

  public <S extends SpellState> S getSpellState(int slot, Class<S> stateClass) {
    if (isValidEquipSlot(slot)) {
      SpellState state = spellStates[slot];
      if (state == null) {
        return null;
      }
      if (stateClass.isAssignableFrom(state.getClass())) {
        return stateClass.cast(state);
      }
    }
    return null;
  }

  public Set<Map.Entry<Spell, Integer>> allKnownSpells() {
    return Collections.unmodifiableSet(knownSpells.entrySet());
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

  public List<Pair<Integer, SpellInstance>> getEquippedSpells() {
    List<Pair<Integer, SpellInstance>> list = new ArrayList<>();
    for (int i = 0; i < equippedSpells.length; i++) {
      SpellInstance spellInst = equippedSpells[i];
      if (spellInst != null) {
        list.add(Pair.of(i, spellInst));
      }
    }
    return Collections.unmodifiableList(list);
  }

  public int getSelectedEquippedSpell() {
    return selectedEquippedSpell;
  }

  public void increaseExperience(int amount) {
    experience += amount;
    if (experience < 0) {
      experience = 0;
    }
    while (experience >= nextLevelXp) {
      level++;
      currLevelXp = nextLevelXp;
      nextLevelXp = calculateXpNeeded(level + 1);
    }
    while (experience < currLevelXp) {
      level--;
      nextLevelXp = currLevelXp;
      currLevelXp = calculateXpNeeded(level);
    }
    markModified();
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

  public void equipSpell(SpellInstance spellInst, int slot) {
    if (isValidEquipSlot(slot) && getKnownLevel(spellInst.spell()) >= spellInst.level()) {
      equippedSpells[slot] = spellInst;
      markModified();
    }
  }

  public void unequipSpell(int slot) {
    if (isValidEquipSlot(slot)) {
      equippedSpells[slot] = null;
      markModified();
    }
  }

  public boolean hasBeenModified(boolean unsetModifiedFlag) {
    if (unsetModifiedFlag) {
      if (modified) {
        modified = false;
        return true;
      }
      return false;
    }
    return modified;
  }

  public boolean hasBeenModified() {
    return hasBeenModified(true);
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

    // possible to unlearn spells that are equipped
    List<Integer> equippedSpellsToRemove = new ArrayList<>();
    for (int i = 0; i < EQUIP_SLOTS; i++) {
      SpellInstance spellInst = equippedSpells[i];
      if (getKnownLevel(spellInst.spell()) < spellInst.level()) {
        equippedSpellsToRemove.add(i);
      }
    }
    equippedSpellsToRemove.forEach(this::unequipSpell);
  }

  public void updateEquippedSpells(List<Pair<Integer, SpellInstance>> newEquippedSpells) {
    clearEquippedSpells();
    newEquippedSpells.forEach(pair -> {
      int slot = pair.first();
      SpellInstance spellInst = pair.second();
      if (isValidEquipSlot(slot)) {
        if (spellInst.level() <= getKnownLevel(spellInst.spell())) {
          equippedSpells[slot] = spellInst;
        } else {
          MagicMod.LOGGER.warn("Could not equip spell as its level is too high: {} lvl {} (max {})",
              SpellUtil.getName(spellInst.spell()), spellInst.level(), getKnownLevel(spellInst.spell()));
        }
      } else {
        MagicMod.LOGGER.warn("Invalid equip slot while updating equipped spells: {}", slot);
      }
    });
  }

  @Override
  public CompoundTag serializeNBT() {
    CompoundTag tag = new CompoundTag();
    tag.putShort("Level", (short) level);
    tag.putInt("Experience", experience);
    ListTag learnedSpellsTag = new ListTag();
    knownSpells.forEach((spell, level) -> learnedSpellsTag.add(new SpellInstance(spell, level).serializeNBT()));
    tag.put("KnownSpells", learnedSpellsTag);
    ListTag equippedSpellsTag = new ListTag();
    for (int i = 0; i < equippedSpells.length; i++) {
      SpellInstance spellInst = equippedSpells[i];
      if (spellInst != null) {
        CompoundTag equippedSpellTag = new CompoundTag();
        equippedSpellTag.putByte("Slot", (byte) i);
        equippedSpellTag.put("SpellInstance", spellInst.serializeNBT());
        SpellState state = spellStates[i];
        if (state != null) {
          equippedSpellTag.put("State", state.serializeNBT());
        }
      }
    }
    tag.put("EquippedSpells", equippedSpellsTag);
    return tag;
  }

  @Override
  public void deserializeNBT(CompoundTag tag) {
    knownSpells.clear();
    clearEquippedSpells();
    level = tag.getShort("Level");
    experience = tag.getInt("Experience");
    tag.getList("KnownSpells", Tag.TAG_COMPOUND).forEach(learnedSpellTag -> {
      SpellInstance spellInst = SpellInstance.fromTag((CompoundTag) learnedSpellTag);
      knownSpells.put(spellInst.spell(), spellInst.level());
    });
    tag.getList("EquippedSpells", Tag.TAG_COMPOUND).forEach(equippedSpellTagRaw -> {
      CompoundTag equippedSpellTag = (CompoundTag) equippedSpellTagRaw;
      int slot = equippedSpellTag.getByte("Slot");
      SpellInstance spellInst = SpellInstance.fromTag(equippedSpellTag.getCompound("SpellInstance"));
      if (isValidEquipSlot(slot)) {
        equippedSpells[slot] = spellInst;
        if (spellInst.spell().hasState()) {
          spellStates[slot] = spellInst.spell().createState();
          if (equippedSpellTag.contains("State")) {
            spellStates[slot].deserializeNBT(equippedSpellTag.getCompound("State"));
          }
        }
      } else {
        MagicMod.LOGGER.warn("Invalid equip slot while deserializing MagicUser: {}", slot);
      }
    });

    currLevelXp = calculateXpNeeded(level);
    nextLevelXp = calculateXpNeeded(level + 1);
  }

  public static boolean isValidEquipSlot(int index) {
    return index >= 0 && index < EQUIP_SLOTS;
  }

  public static int calculateXpNeeded(int level) {
    return (int) (100.0 * Math.pow(level, 2.6));
  }

}
