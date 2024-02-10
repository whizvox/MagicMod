package me.whizvox.magicmod.common.api.spelldata;

import me.whizvox.magicmod.MagicMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class SpellDataManager {

  private final Map<ResourceLocation, SpellDataSerializer<?, ?>> serializers;

  public SpellDataManager() {
    serializers = new HashMap<>();
  }

  private static <DATA extends SpellData, TAG extends Tag> TAG serialize(SpellDataSerializer<?, ?> serializer, SpellData data) {
    //noinspection unchecked
    return ((SpellDataSerializer<DATA, TAG>) serializer).serialize((DATA) data);
  }

  private static <DATA extends SpellData, TAG extends Tag> DATA deserialize(SpellDataSerializer<?, ?> serializer, Tag nbt) {
    //noinspection unchecked
    return ((SpellDataSerializer<DATA, TAG>) serializer).deserialize((TAG) nbt);
  }

  public boolean hasSerializer(ResourceLocation key) {
    return serializers.containsKey(key);
  }

  public <DATA extends SpellData, TAG extends Tag> void addSerializer(ResourceLocation key, SpellDataSerializer<DATA, TAG> serializer, boolean replace) {
    if (!serializers.containsKey(key) || replace) {
      serializers.put(key, serializer);
    } else {
      MagicMod.LOGGER.warn("Attempted to add pre-existing serializer without replacing it: " + key);
    }
  }

  public <DATA extends SpellData, TAG extends Tag> void addSerializer(ResourceLocation key, SpellDataSerializer<DATA, TAG> serializer) {
    addSerializer(key, serializer, false);
  }

  public Tag serialize(Map<ResourceLocation, SpellData> input) {
    ListTag listTag = new ListTag();
    input.forEach((key, data) -> {
      var serializer = serializers.get(key);
      if (serializer != null) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Key", key.toString());
        tag.put("Data", serialize(serializer, data));
      }
    });
    return listTag;
  }

  public void deserialize(Tag nbt, Map<ResourceLocation, SpellData> output) {
    if (nbt instanceof ListTag listTag) {
      listTag.forEach(dataTagRaw -> {
        if (dataTagRaw instanceof CompoundTag dataTag) {
          ResourceLocation key = new ResourceLocation(dataTag.getString("Key"));
          SpellDataSerializer<?, ?> serializer = serializers.get(key);
          if (serializer != null) {
            SpellData data = deserialize(serializer, dataTag.get("Data"));
            output.put(key, data);
          }
        }
      });
    }
  }

  public static final SpellDataManager INSTANCE = new SpellDataManager();

}
