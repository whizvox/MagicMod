package me.whizvox.magicmod.common.network;

import it.unimi.dsi.fastutil.Pair;
import me.whizvox.magicmod.common.api.MagicUser;
import me.whizvox.magicmod.common.api.spell.SpellInstance;
import me.whizvox.magicmod.common.lib.MMCapabilities;
import me.whizvox.magicmod.common.util.SpellUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;

public record UpdateEquippedSpellsMessage(SpellInstance[] equippedSpells, boolean toClient) {

  public UpdateEquippedSpellsMessage(MagicUser magicUser, boolean toClient) {
    this(magicUser.getEquippedSpells(), toClient);
  }

  public static final MessageHandler<UpdateEquippedSpellsMessage> HANDLER = new MessageHandler<>() {

    @Override
    public Class<UpdateEquippedSpellsMessage> getMessageClass() {
      return UpdateEquippedSpellsMessage.class;
    }

    @Override
    public void encode(UpdateEquippedSpellsMessage msg, FriendlyByteBuf buf) {
      List<Pair<Integer, SpellInstance>> equippedSpells = new ArrayList<>();
      for (int i = 0; i < msg.equippedSpells.length; i++) {
        SpellInstance spellInst = msg.equippedSpells[i];
        if (spellInst != null) {
          equippedSpells.add(Pair.of(i, spellInst));
        }
      }
      buf.writeByte(equippedSpells.size());
      equippedSpells.forEach(pair -> {
        buf.writeByte(pair.first());
        SpellUtil.encodeSpellInstance(pair.second(), buf);
      });
      buf.writeBoolean(msg.toClient);
    }

    @Override
    public UpdateEquippedSpellsMessage decode(FriendlyByteBuf buf) {
      int count = buf.readByte();
      SpellInstance[] equippedSpells = new SpellInstance[MagicUser.EQUIP_SLOTS];
      for (int i = 0; i < count; i++) {
        int slot = buf.readByte();
        SpellInstance spellInst = SpellUtil.decodeSpellInstance(buf, true);
        if (spellInst.spell() != null) {
          equippedSpells[slot] = spellInst;
        }
      }
      boolean forClient = buf.readBoolean();
      return new UpdateEquippedSpellsMessage(equippedSpells, forClient);
    }

    @Override
    public void handle(NetworkEvent.Context ctx, UpdateEquippedSpellsMessage msg) {
      Player player;
      if (msg.toClient) {
        player = Minecraft.getInstance().player;
      } else {
        player = ctx.getSender();
      }
      player.getCapability(MMCapabilities.MAGIC_USER).ifPresent(magicUser -> {
        magicUser.updateEquippedSpells(msg.equippedSpells);
      });
    }

  };

}
