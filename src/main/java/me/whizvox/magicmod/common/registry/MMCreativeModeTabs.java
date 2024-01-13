package me.whizvox.magicmod.common.registry;

import me.whizvox.magicmod.MagicMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MMCreativeModeTabs {

  private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), MagicMod.MOD_ID);

  public static void register(IEventBus bus) {
    TABS.register(bus);
  }

  public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
      .title(Component.translatable("itemGroup." + MagicMod.MOD_ID + ".main"))
      .icon(() -> new ItemStack(MMItems.WAND.get()))
      .displayItems((parameters, output) -> {
        output.accept(MMItems.WAND.get());
      })
      .build()
  );

}
