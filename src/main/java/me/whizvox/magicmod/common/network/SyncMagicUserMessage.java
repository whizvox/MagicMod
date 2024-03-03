package me.whizvox.magicmod.common.network;

import me.whizvox.magicmod.common.api.MagicUser;
import me.whizvox.magicmod.common.lib.MagicUserManager;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record SyncMagicUserMessage(CompoundTag nbt) {

  public SyncMagicUserMessage(MagicUser magicUser) {
    this(magicUser.serializeNBT());
  }

  public static final MessageHandler<SyncMagicUserMessage> HANDLER = new MessageHandler<>() {

    @Override
    public Class<SyncMagicUserMessage> getMessageClass() {
      return SyncMagicUserMessage.class;
    }

    @Override
    public void encode(SyncMagicUserMessage msg, FriendlyByteBuf buf) {
      buf.writeNbt(msg.nbt);
    }

    @Override
    public SyncMagicUserMessage decode(FriendlyByteBuf buf) {
      return new SyncMagicUserMessage(buf.readNbt());
    }

    @Override
    public void handle(NetworkEvent.Context ctx, SyncMagicUserMessage msg) {
      MagicUser magicUser = new MagicUser();
      magicUser.deserializeNBT(msg.nbt);
      MagicUserManager.sync(Minecraft.getInstance().player.getUUID(), magicUser);
    }

  };

}
