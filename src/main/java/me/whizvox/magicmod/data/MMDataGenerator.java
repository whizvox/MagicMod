package me.whizvox.magicmod.data;

import me.whizvox.magicmod.MagicMod;
import me.whizvox.magicmod.data.client.MMItemModelProvider;
import me.whizvox.magicmod.data.server.PedestalRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class MMDataGenerator {

  public static void register(IEventBus bus) {
    bus.addListener(MMDataGenerator::onGatherData);
  }

  private static void onGatherData(GatherDataEvent event) {
    DataGenerator gen = event.getGenerator();
    PackOutput output = gen.getPackOutput();
    ExistingFileHelper fileHelper = event.getExistingFileHelper();
    var lookupProvider = event.getLookupProvider();
    boolean includeClient = event.includeClient();
    boolean includeServer = event.includeServer();
    gen.addProvider(includeClient, new MMItemModelProvider(output, fileHelper));
    gen.addProvider(includeServer, new PedestalRecipeProvider(gen, MagicMod.MOD_ID));
  }

}
