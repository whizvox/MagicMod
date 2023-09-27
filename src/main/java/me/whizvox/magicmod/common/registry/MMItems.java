package me.whizvox.magicmod.common.registry;

import me.whizvox.magicmod.MagicMod;
import me.whizvox.magicmod.common.item.UniversalDiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MMItems {

  private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MagicMod.MOD_ID);

  public static void register(IEventBus bus) {
    ITEMS.register(bus);
  }

  public static final RegistryObject<Item>
      UNIVERSAL_STONE_DIGGER = ITEMS.register("universal_stone_digger", () -> new UniversalDiggerItem(Tiers.STONE)),
      UNIVERSAL_IRON_DIGGER = ITEMS.register("universal_iron_digger", () -> new UniversalDiggerItem(Tiers.IRON)),
      UNIVERSAL_DIAMOND_DIGGER = ITEMS.register("universal_diamond_digger", () -> new UniversalDiggerItem(Tiers.DIAMOND)),
      UNIVERSAL_NETHERITE_DIGGER = ITEMS.register("universal_netherite_digger", () -> new UniversalDiggerItem(Tiers.NETHERITE));

}
