package me.whizvox.magicmod.common.api.spelldata;

import net.minecraft.nbt.Tag;

public interface SpellDataSerializer<DATA extends SpellData, TAG extends Tag> {

  DATA deserialize(TAG nbt);

  TAG serialize(DATA data);

}
