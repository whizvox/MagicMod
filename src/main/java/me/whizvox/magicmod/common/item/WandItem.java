package me.whizvox.magicmod.common.item;

import me.whizvox.magicmod.common.api.MagicUser;
import me.whizvox.magicmod.common.api.spell.Spell;
import me.whizvox.magicmod.common.api.spell.SpellInstance;
import me.whizvox.magicmod.common.block.entity.PedestalBlockEntity;
import me.whizvox.magicmod.common.lib.MagicUserManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class WandItem extends Item {

  public WandItem() {
    super(new Item.Properties().stacksTo(1));
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (!level.isClientSide) {
      MagicUser magicUser = MagicUserManager.getUser(player);
      int selected = magicUser.getSelectedEquippedSpell();
      SpellInstance selectedSpell = magicUser.getEquippedSpell(selected);
      if (selectedSpell != null) {
        Spell spell = selectedSpell.spell();
        int spellLevel = selectedSpell.level();
        double cost = spell.getCost(spellLevel);
        if (cost <= magicUser.getMana()) {
          boolean res = spell.activate(spellLevel, player);
          if (res) {
            magicUser.changeMana(-cost);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
          }
        }
      }
    }
    return InteractionResultHolder.pass(stack);
  }

}
