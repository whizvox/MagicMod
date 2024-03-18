package me.whizvox.magicmod.common.registry;

import me.whizvox.magicmod.MagicMod;
import me.whizvox.magicmod.common.block.PedestalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MMBlocks {

  private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MagicMod.MOD_ID);

  public static void register(IEventBus bus) {
    BLOCKS.register(bus);
  }

  public static final RegistryObject<PedestalBlock> PEDESTAL = BLOCKS.register("pedestal", PedestalBlock::new);

}
