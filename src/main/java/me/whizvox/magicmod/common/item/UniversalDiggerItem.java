package me.whizvox.magicmod.common.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

// used for the break spell
// have to inherit Item and not DiggerItem because the latter only accepts a single tag
public class UniversalDiggerItem extends Item {

  private final Set<TagKey<Block>> miningTags;
  public final Tier tier;

  public UniversalDiggerItem(Tier tier) {
    super(new Properties().durability(1));
    this.tier = tier;
    miningTags = Set.of(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.MINEABLE_WITH_AXE, BlockTags.MINEABLE_WITH_HOE, BlockTags.MINEABLE_WITH_SHOVEL);
  }

  private boolean isHarvestable(BlockState state) {
    return miningTags.stream().anyMatch(state::is);
  }

  public boolean isCorrectToolForDrops(BlockState state) {
    if (net.minecraftforge.common.TierSortingRegistry.isTierSorted(tier)) {
      return net.minecraftforge.common.TierSortingRegistry.isCorrectTierForDrops(tier, state) && isHarvestable(state);
    }
    int i = tier.getLevel();
    if (i < 3 && state.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
      return false;
    } else if (i < 2 && state.is(BlockTags.NEEDS_IRON_TOOL)) {
      return false;
    } else {
      return i < 1 && state.is(BlockTags.NEEDS_STONE_TOOL) ? false : isHarvestable(state);
    }
  }

  @Override
  public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
    return isHarvestable(state) && net.minecraftforge.common.TierSortingRegistry.isCorrectTierForDrops(tier, state);
  }

}
