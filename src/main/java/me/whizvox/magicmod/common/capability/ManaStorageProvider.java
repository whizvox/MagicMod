package me.whizvox.magicmod.common.capability;

import me.whizvox.magicmod.common.api.ManaStorage;
import me.whizvox.magicmod.common.lib.MMCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ManaStorageProvider implements ICapabilitySerializable<CompoundTag> {

  private final ManaStorage storage;
  private final LazyOptional<ManaStorage> lazyOp;

  public ManaStorageProvider() {
    storage = new ManaStorage();
    lazyOp = LazyOptional.of(() -> storage);
  }

  @NotNull
  @Override
  public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
    return cap == MMCapabilities.MANA_STORAGE ? lazyOp.cast() : LazyOptional.empty();
  }

  @Override
  public CompoundTag serializeNBT() {
    return storage.serializeNBT();
  }

  @Override
  public void deserializeNBT(CompoundTag tag) {
    storage.deserializeNBT(tag);
  }

}

