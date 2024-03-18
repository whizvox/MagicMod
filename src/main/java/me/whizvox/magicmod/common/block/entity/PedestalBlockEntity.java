package me.whizvox.magicmod.common.block.entity;

import me.whizvox.magicmod.common.registry.MMBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PedestalBlockEntity extends BlockEntity {

  private ItemStack storedStack;
  private final float rotationOffset;

  public PedestalBlockEntity(BlockPos pos, BlockState state) {
    super(MMBlockEntities.PEDESTAL.get(), pos, state);
    storedStack = ItemStack.EMPTY;
    rotationOffset = (float) (Math.random() * Math.PI * 2.0);
  }

  public boolean hasItem() {
    return !storedStack.isEmpty();
  }

  public ItemStack getItem() {
    return storedStack;
  }

  public float getItemRotation(float partialTicks) {
    return (level.getGameTime() + partialTicks) / 20.0F + rotationOffset;
  }

  public void setItem(ItemStack stack) {
    storedStack = stack;
    setChanged();
  }

  public ItemStack removeItem() {
    return storedStack.copyAndClear();
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    tag.put("Item", storedStack.save(new CompoundTag()));
    super.saveAdditional(tag);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    storedStack = ItemStack.of(tag.getCompound("Item"));
  }

  @Override
  public CompoundTag getUpdateTag() {
    CompoundTag tag = new CompoundTag();
    saveAdditional(tag);
    return tag;
  }

  @Nullable
  @Override
  public Packet<ClientGamePacketListener> getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  // only ticks on the client
  public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {

  }

}
