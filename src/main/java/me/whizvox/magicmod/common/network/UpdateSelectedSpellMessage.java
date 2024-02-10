package me.whizvox.magicmod.common.network;

import me.whizvox.magicmod.common.api.MagicUser;
import me.whizvox.magicmod.common.lib.MagicUserManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record UpdateSelectedSpellMessage(int selectedSpell) {

  /**
   * From CLIENT to SERVER
   */
  public static final MessageHandler<UpdateSelectedSpellMessage> HANDLER = new MessageHandler<>() {

    @Override
    public Class<UpdateSelectedSpellMessage> getMessageClass() {
      return UpdateSelectedSpellMessage.class;
    }

    @Override
    public void encode(UpdateSelectedSpellMessage msg, FriendlyByteBuf buf) {
      buf.writeByte(msg.selectedSpell);
    }

    @Override
    public UpdateSelectedSpellMessage decode(FriendlyByteBuf buf) {
      return new UpdateSelectedSpellMessage(buf.readByte());
    }

    @Override
    public void handle(NetworkEvent.Context ctx, UpdateSelectedSpellMessage msg) {
      MagicUser magicUser = MagicUserManager.getUser(ctx.getSender());
      magicUser.setSelectedEquippedSpell(msg.selectedSpell);
    }

  };

}
