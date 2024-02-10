package me.whizvox.magicmod.common.lib.spell;

import me.whizvox.magicmod.MagicMod;
import me.whizvox.magicmod.common.api.MagicUser;
import me.whizvox.magicmod.common.api.spell.Spell;
import me.whizvox.magicmod.common.lib.MagicUserManager;
import me.whizvox.magicmod.common.lib.spelldata.ShieldSpellData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class ShieldSpell implements Spell {

  public static final ResourceLocation SHIELD_DATA_KEY = new ResourceLocation(MagicMod.MOD_ID, "shield");

  @Override
  public int getMaxLevel() {
    return 4;
  }

  @Override
  public int getCost(int level) {
    return 10 + (level * 10);
  }

  @Override
  public boolean activate(int level, LivingEntity caster) {
    MagicUser magicUser = MagicUserManager.getUser(caster.getUUID());
    magicUser.addData(SHIELD_DATA_KEY, new ShieldSpellData(level));
    ((Player) caster).displayClientMessage(Component.translatable("message.magicmod.spell.shield.cast"), true);
    return true;
  }

}
