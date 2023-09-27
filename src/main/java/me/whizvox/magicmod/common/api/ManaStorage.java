package me.whizvox.magicmod.common.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraftforge.common.util.INBTSerializable;

public class ManaStorage implements INBTSerializable<CompoundTag> {

  private double mana;
  private double maxMana;
  private double rechargeRate;
  private int rechargeCooldown;

  private boolean modified;

  public ManaStorage() {
    mana = 0;
    maxMana = 0;
    rechargeRate = 0;
    rechargeCooldown = 0;

    modified = false;
  }

  public double getMana() {
    return mana;
  }

  public double getMaxMana() {
    return maxMana;
  }

  public double getRechargeRate() {
    return rechargeRate;
  }

  public boolean isModified() {
    if (modified) {
      modified = false;
      return true;
    }
    return false;
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

  public void setRechargeRate(double rechargeRate) {
    this.rechargeRate = Mth.clamp(rechargeRate, 0, Double.MAX_VALUE);
    modified = true;
  }

  public void attemptRecharge() {
    if (rechargeRate > 0 && mana < maxMana) {
      if (++rechargeCooldown >= 20) {
        rechargeCooldown = 0;
        changeMana(rechargeRate);
      }
    } else {
      if (rechargeCooldown != 0) {
        rechargeCooldown = 0;
      }
    }
  }

  @Override
  public CompoundTag serializeNBT() {
    CompoundTag tag = new CompoundTag();
    tag.putDouble("Mana", mana);
    tag.putDouble("MaxMana", maxMana);
    tag.putDouble("RechargeRate", rechargeRate);
    tag.putByte("RechargeCooldown", (byte) rechargeCooldown);
    return tag;
  }

  @Override
  public void deserializeNBT(CompoundTag tag) {
    mana = tag.getDouble("Mana");
    maxMana = tag.getDouble("MaxMana");
    rechargeRate = tag.getDouble("RechargeRate");
    rechargeCooldown = tag.getByte("RechargeCooldown");
  }

}

