package me.whizvox.magicmod.common.network;

import me.whizvox.magicmod.common.api.ManaStorage;
import me.whizvox.magicmod.common.lib.MMCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record UpdateManaMessage(double mana, double maxMana, double rechargeRate) {

  public UpdateManaMessage(ManaStorage manaStorage) {
    this(manaStorage.getMana(), manaStorage.getMaxMana(), manaStorage.getRechargeRate());
  }

  public static final MessageHandler<UpdateManaMessage> HANDLER = new MessageHandler<>() {

    @Override
    public Class<UpdateManaMessage> getMessageClass() {
      return UpdateManaMessage.class;
    }

    @Override
    public void encode(UpdateManaMessage msg, FriendlyByteBuf buf) {
      buf.writeDouble(msg.mana);
      buf.writeDouble(msg.maxMana);
      buf.writeDouble(msg.rechargeRate);
    }

    @Override
    public UpdateManaMessage decode(FriendlyByteBuf buf) {
      return new UpdateManaMessage(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    @Override
    public void handle(NetworkEvent.Context ctx, UpdateManaMessage msg) {
      Minecraft.getInstance().player.getCapability(MMCapabilities.MANA_STORAGE).ifPresent(manaStorage -> {
        manaStorage.setMaxMana(msg.maxMana);
        manaStorage.setRechargeRate(msg.rechargeRate);
        manaStorage.setMana(msg.mana);
      });
    }

  };

}

