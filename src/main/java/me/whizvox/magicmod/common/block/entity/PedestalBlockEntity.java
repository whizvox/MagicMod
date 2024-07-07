package me.whizvox.magicmod.common.block.entity;

import me.whizvox.magicmod.common.api.recipe.PedestalRecipeManager;
import me.whizvox.magicmod.common.api.recipe.PedestalRecipeMatch;
import me.whizvox.magicmod.common.registry.MMBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class PedestalBlockEntity extends BlockEntity {

  private static final Vec3i[] OFFSETS = new Vec3i[] {
      new Vec3i(-1, 0, -2),
      new Vec3i(1, 0, -2),
      new Vec3i(2, 0, -1),
      new Vec3i(2, 0, 1),
      new Vec3i(1, 0, 2),
      new Vec3i(-1, 0, 2),
      new Vec3i(-2, 0, 1),
      new Vec3i(-2, 0, -1)
  };

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
    ItemStack item = storedStack.copyAndClear();
    setChanged();
    return item;
  }

  public boolean attemptCraft() {
    List<PedestalBlockEntity> pedestals = Arrays.stream(OFFSETS)
        .map(offset -> {
          if (level.getBlockEntity(worldPosition.offset(offset)) instanceof PedestalBlockEntity pedestal && !pedestal.storedStack.isEmpty()) {
            return pedestal;
          }
          return null;
        })
        .filter(pedestal -> pedestal != null && pedestal.hasItem())
        .toList();
    List<ItemStack> stacks = pedestals.stream().map(pedestal -> pedestal.storedStack).toList();
    List<PedestalRecipeMatch> matchingRecipes = PedestalRecipeManager.INSTANCE.match(storedStack, stacks);
    if (matchingRecipes.isEmpty()) {
      return false;
    }
    PedestalRecipeMatch match = matchingRecipes.get(0);
    setItem(match.output().center());
    for (int i = 0; i < match.output().outer().size(); i++) {
      PedestalBlockEntity pedestal = pedestals.get(i);
      pedestal.setItem(match.output().outer().get(i));
      level.sendBlockUpdated(pedestal.worldPosition, pedestal.getBlockState(), pedestal.getBlockState(), Block.UPDATE_ALL);
    }
    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    if (level instanceof ServerLevel serverLevel) {
      serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, worldPosition.getX() + 0.5, worldPosition.getY() + 1.1, worldPosition.getZ() + 0.5, 5, 0.2, 0.2, 0.2, 0.2);
    }
    return true;
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

}
