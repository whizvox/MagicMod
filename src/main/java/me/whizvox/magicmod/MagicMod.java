package me.whizvox.magicmod;

import me.whizvox.magicmod.client.ClientEventListeners;
import me.whizvox.magicmod.common.event.WorldEventListeners;
import me.whizvox.magicmod.common.lib.MMCapabilities;
import me.whizvox.magicmod.common.registry.MMSpells;
import me.whizvox.magicmod.common.network.MMNetwork;
import me.whizvox.magicmod.common.registry.MMItems;
import me.whizvox.magicmod.common.registry.SpellRegistry;
import me.whizvox.magicmod.server.ManaCommand;
import me.whizvox.magicmod.server.SpellCommand;
import me.whizvox.magicmod.server.lib.MMArgumentTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MagicMod.MOD_ID)
public class MagicMod {

    public static final String MOD_ID = "magicmod";

    public static final Logger LOGGER = LoggerFactory.getLogger(MagicMod.class);

    public MagicMod() {
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        modBus.register(SpellRegistry.class);
        MMNetwork.register();
        MMItems.register(modBus);
        SpellRegistry.register(modBus);
        MMCapabilities.register(modBus, forgeBus);
        MMArgumentTypes.register(modBus);
        forgeBus.register(ClientEventListeners.class);
        forgeBus.register(WorldEventListeners.class);
        forgeBus.addListener(this::onRegisterCommand);
    }

    private void onRegisterCommand(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        ManaCommand.register(dispatcher);
        SpellCommand.register(dispatcher);
    }

}
