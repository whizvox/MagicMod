package me.whizvox.magicmod.common.network;

import me.whizvox.magicmod.common.api.MagicUser;
import me.whizvox.magicmod.common.api.spell.SpellInstance;
import me.whizvox.magicmod.common.lib.MMCapabilities;
import me.whizvox.magicmod.common.util.SpellUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record UpdateKnownSpellsMessage(List<SpellInstance> knownSpells) {

  public UpdateKnownSpellsMessage(MagicUser magicUser) {
    this(magicUser.allKnownSpells().map(entry -> new SpellInstance(entry.getKey(), entry.getValue())).toList());
  }

  public static final MessageHandler<UpdateKnownSpellsMessage> HANDLER = new MessageHandler<>() {

    @Override
    public Class<UpdateKnownSpellsMessage> getMessageClass() {
      return UpdateKnownSpellsMessage.class;
    }

    @Override
    public void encode(UpdateKnownSpellsMessage msg, FriendlyByteBuf buf) {
      buf.writeShort(msg.knownSpells.size());
      msg.knownSpells.forEach(spellInst -> SpellUtil.encodeSpellInstance(spellInst, buf));
    }

    @Override
    public UpdateKnownSpellsMessage decode(FriendlyByteBuf buf) {
      List<SpellInstance> knownSpells = new ArrayList<>();
      int count = buf.readShort();
      for (int i = 0; i < count; i++) {
        SpellInstance spellInst = SpellUtil.decodeSpellInstance(buf, true);
        if (spellInst.spell() != null) {
          knownSpells.add(spellInst);
        }
      }
      return new UpdateKnownSpellsMessage(Collections.unmodifiableList(knownSpells));
    }

    @Override
    public void handle(NetworkEvent.Context ctx, UpdateKnownSpellsMessage msg) {
      Minecraft.getInstance().player.getCapability(MMCapabilities.MAGIC_USER).ifPresent(magicUser -> {
        magicUser.updateKnownSpells(msg.knownSpells);
      });
    }

  };

}
