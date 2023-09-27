package me.whizvox.magicmod.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public interface MessageHandler<M> {

  Class<M> getMessageClass();

  void encode(M msg, FriendlyByteBuf buf);

  M decode(FriendlyByteBuf buf);

  void handle(NetworkEvent.Context ctx, M msg);

}
