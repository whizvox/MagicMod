package me.whizvox.magicmod.common.capability;

import me.whizvox.magicmod.common.api.MagicUser;
import me.whizvox.magicmod.common.lib.MMCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MagicUserProvider implements ICapabilitySerializable<CompoundTag> {

  private final MagicUser magicUser;
  private final LazyOptional<MagicUser> lazyOp;

  public MagicUserProvider() {
    magicUser = new MagicUser();
    lazyOp = LazyOptional.of(() -> magicUser);
  }

  @Override
  public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
    return cap == MMCapabilities.MAGIC_USER ? lazyOp.cast() : LazyOptional.empty();
  }

  @Override
  public CompoundTag serializeNBT() {
    return magicUser.serializeNBT();
  }

  @Override
  public void deserializeNBT(CompoundTag tag) {
    magicUser.deserializeNBT(tag);
  }

}
