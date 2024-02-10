package me.whizvox.magicmod.data;

import me.whizvox.magicmod.MagicMod;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MagicMod.MOD_ID)
public class MMDataGenerator {

  @SubscribeEvent
  public static void onGatherData(GatherDataEvent event) {
    DataGenerator gen = event.getGenerator();
    PackOutput output = gen.getPackOutput();
    ExistingFileHelper fileHelper = event.getExistingFileHelper();
    var lookupProvider = event.getLookupProvider();
    boolean includeClient = event.includeClient();
    boolean includeServer = event.includeServer();


  }

}
