package me.whizvox.magicmod.common.item;

import me.whizvox.magicmod.common.api.MagicUser;
import me.whizvox.magicmod.common.api.spell.Spell;
import me.whizvox.magicmod.common.api.spell.SpellInstance;
import me.whizvox.magicmod.common.lib.MagicUserManager;
import me.whizvox.magicmod.common.registry.SpellRegistry;
import me.whizvox.magicmod.common.util.SpellUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class KnowledgeScrollItem extends Item {

  public KnowledgeScrollItem() {
    super(new Item.Properties().stacksTo(1));
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    if (!level.isClientSide) {
      SpellInstance spell = readSpell(player.getItemInHand(hand));
      if (spell != null) {
        MagicUser magicUser = MagicUserManager.getUser(player);
        magicUser.learnSpell(spell.spell(), spell.level());
        player.displayClientMessage(Component.translatable("message.magicmod.knowledge_scroll.learned", SpellUtil.translateSpellWithLevel(spell, false)), true);
        player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 1.0F, 1.0F);
        player.setItemInHand(hand, ItemStack.EMPTY);
      }
    }
    return super.use(level, player, hand);
  }

  @Override
  public Component getName(ItemStack stack) {
    SpellInstance spell = readSpell(stack);
    if (spell != null) {
      return Component.translatable("item.magicmod.knowledge_scroll.with_spell", SpellUtil.translateSpellWithLevel(spell, false));
    }
    return super.getName(stack);
  }

  public static SpellInstance readSpell(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    if (tag != null) {
      ResourceLocation key = new ResourceLocation(tag.getString("SpellKey"));
      int level = tag.getByte("SpellLevel");
      Spell spell = SpellRegistry.getRegistry().getValue(key);
      return new SpellInstance(spell, level);
    }
    return null;
  }

  public static void writeSpell(ItemStack stack, SpellInstance spell) {
    CompoundTag tag = stack.getOrCreateTag();
    ResourceLocation key = SpellRegistry.getRegistry().getKey(spell.spell());
    tag.putString("SpellKey", key.toString());
    tag.putByte("SpellLevel", (byte) spell.level());
  }

}
