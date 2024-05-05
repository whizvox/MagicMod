package me.whizvox.magicmod.common.network;

import me.whizvox.magicmod.MagicMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class MMNetwork {

  private static final String PROTOCOL_VERSION = "1";

  private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
      new ResourceLocation(MagicMod.MOD_ID, "main"),
      () -> PROTOCOL_VERSION,
      PROTOCOL_VERSION::equals,
      PROTOCOL_VERSION::equals
  );

  private static <M> void addHandler(int id, MessageHandler<M> handler) {
    INSTANCE.registerMessage(id, handler.getMessageClass(), handler::encode, handler::decode, (msg, ctxSupplier) -> {
      NetworkEvent.Context ctx = ctxSupplier.get();
      ctx.enqueueWork(() -> handler.handle(ctx, msg));
      ctx.setPacketHandled(true);
    });
  }

  public static void register() {
    int id = 1;
    addHandler(id++, UpdateManaMessage.HANDLER);
    addHandler(id++, UpdateKnownSpellsMessage.HANDLER);
    addHandler(id++, UpdateEquippedSpellsMessage.HANDLER);
    addHandler(id++, UpdateSelectedSpellMessage.HANDLER);
    addHandler(id++, SyncMagicUserMessage.HANDLER);
    addHandler(id++, SyncPedestalRecipesMessage.HANDLER);
  }

  public static void sendToClient(ServerPlayer player, Object msg) {
    INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
  }

  public static void sendToServer(Object msg) {
    INSTANCE.sendToServer(msg);
  }

  public static void broadcast(Object msg) {
    INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
  }

}
