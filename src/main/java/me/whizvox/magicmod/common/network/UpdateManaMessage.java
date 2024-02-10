package me.whizvox.magicmod.common.network;

import me.whizvox.magicmod.common.api.MagicUser;
import me.whizvox.magicmod.common.lib.MagicUserManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record UpdateManaMessage(double mana) {

  public UpdateManaMessage(MagicUser magicUser) {
    this(magicUser.getMana());
  }

  public static final MessageHandler<UpdateManaMessage> HANDLER = new MessageHandler<>() {

    @Override
    public Class<UpdateManaMessage> getMessageClass() {
      return UpdateManaMessage.class;
    }

    @Override
    public void encode(UpdateManaMessage msg, FriendlyByteBuf buf) {
      buf.writeDouble(msg.mana);
    }

    @Override
    public UpdateManaMessage decode(FriendlyByteBuf buf) {
      return new UpdateManaMessage(buf.readDouble());
    }

    @Override
    public void handle(NetworkEvent.Context ctx, UpdateManaMessage msg) {
      MagicUser magicUser = MagicUserManager.getUser(Minecraft.getInstance().player);
      magicUser.setMana(msg.mana);
    }

  };

}

