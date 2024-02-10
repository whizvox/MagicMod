package me.whizvox.magicmod;

import me.whizvox.magicmod.client.ClientEventListeners;
import me.whizvox.magicmod.client.MMKeyMappings;
import me.whizvox.magicmod.common.api.spelldata.SpellDataManager;
import me.whizvox.magicmod.common.api.user.MagicUserManager;
import me.whizvox.magicmod.common.event.WorldEventListeners;
import me.whizvox.magicmod.common.lib.MMCapabilities;
import me.whizvox.magicmod.common.lib.spell.ShieldSpell;
import me.whizvox.magicmod.common.lib.spelldata.ShieldSpellData;
import me.whizvox.magicmod.common.network.MMNetwork;
import me.whizvox.magicmod.common.registry.MMCreativeModeTabs;
import me.whizvox.magicmod.common.registry.MMItems;
import me.whizvox.magicmod.common.registry.MMSpells;
import me.whizvox.magicmod.common.registry.SpellRegistry;
import me.whizvox.magicmod.server.ManaCommand;
import me.whizvox.magicmod.server.SpellCommand;
import me.whizvox.magicmod.server.lib.MMArgumentTypes;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MagicMod.MOD_ID)
public class MagicMod {

  private static final String MAGIC_USERS_FILE = "MagicUserData.nbt";

  public static final String MOD_ID = "magicmod";

  public static final Logger LOGGER = LoggerFactory.getLogger(MagicMod.class);
  public static final MagicUserManager MAGIC_USERS = new MagicUserManager();

  public MagicMod() {
    IEventBus forgeBus = MinecraftForge.EVENT_BUS;
    IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

    modBus.register(SpellRegistry.class);
    modBus.addListener(this::onRegisterKeyMappings);
    MMNetwork.register();
    MMItems.register(modBus);
    MMCreativeModeTabs.register(modBus);
    MMSpells.register(modBus);
    MMCapabilities.register(modBus, forgeBus);
    MMArgumentTypes.register(modBus);

    forgeBus.register(ClientEventListeners.class);
    forgeBus.register(WorldEventListeners.class);
    forgeBus.addListener(this::onRegisterCommand);
    MagicUserManager.register(forgeBus);

    SpellDataManager.INSTANCE.addSerializer(ShieldSpell.SHIELD_DATA_KEY, ShieldSpellData.SERIALIZER);
  }

  private void onRegisterCommand(RegisterCommandsEvent event) {
    var dispatcher = event.getDispatcher();
    ManaCommand.register(dispatcher);
    SpellCommand.register(dispatcher);
  }

  private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
    event.register(MMKeyMappings.OPEN_SPELL_INVENTORY);
  }

}
