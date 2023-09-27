package me.whizvox.magicmod.common.network;

import me.whizvox.magicmod.MagicMod;
import me.whizvox.magicmod.common.api.ManaStorage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

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
  }

  public static void sendToClient(ServerPlayer player, Object msg) {
    INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
  }

  public static void updatePlayerMana(ServerPlayer player, ManaStorage manaStorage) {
    sendToClient(player, new UpdateManaMessage(manaStorage));
  }

}
