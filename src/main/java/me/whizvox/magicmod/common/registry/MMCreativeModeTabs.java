package me.whizvox.magicmod.common.registry;

import me.whizvox.magicmod.MagicMod;
import me.whizvox.magicmod.common.api.spell.SpellInstance;
import me.whizvox.magicmod.common.item.KnowledgeScrollItem;
import me.whizvox.magicmod.common.util.SpellUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Comparator;

public class MMCreativeModeTabs {

  private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), MagicMod.MOD_ID);

  public static void register(IEventBus bus) {
    TABS.register(bus);
  }

  public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
      .title(Component.translatable("itemGroup." + MagicMod.MOD_ID + ".main"))
      .icon(() -> new ItemStack(MMItems.WAND.get()))
      .displayItems((parameters, output) -> {
        output.accept(MMItems.PEDESTAL.get());
        output.accept(MMItems.WAND.get());
        output.accept(MMItems.KNOWLEDGE_FRAGMENT.get());
        SpellRegistry.getRegistry().getValues().stream()
            .sorted(Comparator.comparing(spell -> SpellUtil.translateSpell(spell).toString()))
            .forEach(spell -> {
              for (int level = 0; level < spell.getMaxLevel(); level++) {
                ItemStack stack = new ItemStack(MMItems.KNOWLEDGE_SCROLL.get());
                KnowledgeScrollItem.writeSpell(stack, new SpellInstance(spell, level));
                output.accept(stack);
              }
            });
      })
      .build()
  );

}
