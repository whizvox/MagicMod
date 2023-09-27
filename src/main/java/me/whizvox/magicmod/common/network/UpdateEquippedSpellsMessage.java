package me.whizvox.magicmod.common.network;

import it.unimi.dsi.fastutil.Pair;
import me.whizvox.magicmod.common.api.MagicUser;
import me.whizvox.magicmod.common.api.spell.SpellInstance;
import me.whizvox.magicmod.common.lib.MMCapabilities;
import me.whizvox.magicmod.common.util.SpellUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;

public record UpdateEquippedSpellsMessage(List<Pair<Integer, SpellInstance>> equippedSpells) {

  public UpdateEquippedSpellsMessage(MagicUser magicUser) {
    this(magicUser.getEquippedSpells());
  }

  public static final MessageHandler<UpdateEquippedSpellsMessage> HANDLER = new MessageHandler<>() {

    @Override
    public Class<UpdateEquippedSpellsMessage> getMessageClass() {
      return UpdateEquippedSpellsMessage.class;
    }

    @Override
    public void encode(UpdateEquippedSpellsMessage msg, FriendlyByteBuf buf) {
      buf.writeByte(msg.equippedSpells.size());
      msg.equippedSpells.forEach(pair -> {
        buf.writeByte(pair.first());
        SpellUtil.encodeSpellInstance(pair.second(), buf);
      });
    }

    @Override
    public UpdateEquippedSpellsMessage decode(FriendlyByteBuf buf) {
      int count = buf.readByte();
      List<Pair<Integer, SpellInstance>> equippedSpells = new ArrayList<>();
      for (int i = 0; i < count; i++) {
        int slot = buf.readByte();
        SpellInstance spellInst = SpellUtil.decodeSpellInstance(buf, true);
        if (spellInst.spell() != null) {
          equippedSpells.add(Pair.of(slot, spellInst));
        }
      }
      return new UpdateEquippedSpellsMessage(Collections.unmodifiableList(equippedSpells));
    }

    @Override
    public void handle(NetworkEvent.Context ctx, UpdateEquippedSpellsMessage msg) {
      Minecraft.getInstance().player.getCapability(MMCapabilities.MAGIC_USER).ifPresent(magicUser -> {
        magicUser.updateEquippedSpells(msg.equippedSpells);
      });
    }

  };

}
