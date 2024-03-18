package me.whizvox.magicmod.common.registry;

import me.whizvox.magicmod.MagicMod;
import me.whizvox.magicmod.common.block.entity.PedestalBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;
import java.util.function.Supplier;

public class MMBlockEntities {

  private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MagicMod.MOD_ID);

  private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String name, BlockEntityType.BlockEntitySupplier<T> supplier, Supplier<Block>... validBlocks) {
    return BLOCK_ENTITIES.register(name, () -> BlockEntityType.Builder.of(supplier, Arrays.stream(validBlocks).map(Supplier::get).toArray(Block[]::new)).build(null));
  }

  public static void register(IEventBus bus) {
    BLOCK_ENTITIES.register(bus);
  }

  public static final RegistryObject<BlockEntityType<PedestalBlockEntity>> PEDESTAL = register("pedestal", PedestalBlockEntity::new, MMBlocks.PEDESTAL::get);

}
