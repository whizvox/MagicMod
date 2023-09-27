package me.whizvox.magicmod.common.item;

import me.whizvox.magicmod.common.api.MagicUser;
import me.whizvox.magicmod.common.api.ManaStorage;
import me.whizvox.magicmod.common.api.spell.*;
import me.whizvox.magicmod.common.util.SpellUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WandItem extends Item {

  public WandItem() {
    super(new Item.Properties().stacksTo(1));
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (!level.isClientSide) {
      var caps = SpellUtil.getMagicCapabilities(player);
      ManaStorage manaStorage = caps.left();
      MagicUser magicUser = caps.right();
      int selected = magicUser.getSelectedEquippedSpell();
      SpellInstance selectedSpell = magicUser.getEquippedSpell(selected);
      if (selectedSpell != null) {
        Spell spell = selectedSpell.spell();
        int spellLevel = selectedSpell.level();
        double cost = spell.getCost(spellLevel);
        if (cost <= manaStorage.getMana()) {
          if (spell.getCastType() == CastType.INSTANT) {
            ActivationResult res = spell.activate(spellLevel, new SpellUsageContext(player, magicUser.getSpellState(selected, SpellState.class), magicUser, manaStorage));
            switch (res) {
              case SUCCESS -> {
                manaStorage.changeMana(-cost);
              }
            }
          }
        }
      }
    }
    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
  }

}
