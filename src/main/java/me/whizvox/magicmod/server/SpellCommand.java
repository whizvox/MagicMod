package me.whizvox.magicmod.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.whizvox.magicmod.common.api.MagicUser;
import me.whizvox.magicmod.common.api.spell.Spell;
import me.whizvox.magicmod.common.api.spell.SpellInstance;
import me.whizvox.magicmod.common.lib.MMCapabilities;
import me.whizvox.magicmod.common.network.MMNetwork;
import me.whizvox.magicmod.common.network.UpdateEquippedSpellsMessage;
import me.whizvox.magicmod.common.network.UpdateKnownSpellsMessage;
import me.whizvox.magicmod.common.util.SpellUtil;
import me.whizvox.magicmod.server.command.arguments.SpellArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Comparator;

public class SpellCommand {

  private static int list(CommandSourceStack src) throws CommandSyntaxException {
    ServerPlayer player = src.getPlayerOrException();
    player.getCapability(MMCapabilities.MAGIC_USER).ifPresent(magicUser -> {
      var knownSpells = new ArrayList<>(magicUser.allKnownSpells());
      if (knownSpells.isEmpty()) {
        src.sendSuccess(() -> Component.translatable("command.magicmod.spell.list.no_known_spells"), false);
      } else {
        knownSpells.sort(Comparator.comparing(entry -> SpellUtil.translateSpell(entry.getKey()).getString()));
        MutableComponent comp = Component.translatable("command.magicmod.spell.list.known_header", knownSpells.size());
        knownSpells.forEach(entry -> {
          comp.append("\n- ").append(SpellUtil.translateSpellWithLevel(SpellUtil.getName(entry.getKey()), entry.getValue(), true));
        });
        src.sendSuccess(() -> comp, false);
      }
      var equippedSpells = magicUser.getEquippedSpells();
      if (equippedSpells.isEmpty()) {
        src.sendSuccess(() -> Component.translatable("command.magicmod.spell.list.no_equipped_spells"), false);
      } else {
        MutableComponent comp = Component.translatable("command.magicmod.spell.list.equipped_header", equippedSpells.size());
        equippedSpells.forEach(pair -> {
          comp.append("\n- ")
              .append(Component.literal(pair.first() + ": "))
              .append(SpellUtil.translateSpellWithLevel(SpellUtil.getName(pair.second().spell()), pair.second().level(), true));
        });
        src.sendSuccess(() -> comp, false);
      }
    });
    return 1;
  }

  private static int learn(CommandSourceStack src, Spell spell, int level) throws CommandSyntaxException {
    ServerPlayer player = src.getPlayerOrException();
    player.getCapability(MMCapabilities.MAGIC_USER).ifPresent(magicUser -> {
      if (level < 0) {
        magicUser.learnSpell(spell);
      } else {
        magicUser.learnSpell(spell, level - 1);
      }
      if (magicUser.hasBeenModified()) {
        src.sendSuccess(() -> Component.translatable(
            "command.magicmod.spell.learn.success",
            player.getName(),
            SpellUtil.translateSpellWithLevel(SpellUtil.getName(spell), magicUser.getKnownLevel(spell), true)
        ), true);
        MMNetwork.sendToClient(player, new UpdateKnownSpellsMessage(magicUser));
      } else {
        src.sendFailure(Component.translatable("command.magicmod.spell.learn.fail"));
      }
    });
    return 1;
  }

  private static int unlearn(CommandSourceStack src, Spell spell, int level) throws CommandSyntaxException {
    ServerPlayer player = src.getPlayerOrException();
    player.getCapability(MMCapabilities.MAGIC_USER).ifPresent(magicUser -> {
      magicUser.downgradeLearnedSpell(spell, level - 1);
      if (magicUser.hasBeenModified()) {
        int currLevel = magicUser.getKnownLevel(spell);
        ResourceLocation name = SpellUtil.getName(spell);
        if (currLevel >= 0) {
          src.sendSuccess(() -> Component.translatable(
              "command.magicmod.spell.unlearn.success_downgrade",
              player.getName(),
              SpellUtil.translateSpell(name, true),
              currLevel + 1
          ), true);
        } else {
          src.sendSuccess(() -> Component.translatable(
              "command.magicmod.spell.unlearn.success",
              player.getName(),
              SpellUtil.translateSpell(name, true)
          ), true);
        }
        MMNetwork.sendToClient(player, new UpdateKnownSpellsMessage(magicUser));
      } else {
        src.sendFailure(Component.translatable("command.magicmod.spell.unlearn.fail"));
      }
    });
    return 1;
  }

  public static int equip(CommandSourceStack src, Spell spell, int level, int slot) throws CommandSyntaxException {
    ServerPlayer player = src.getPlayerOrException();
    player.getCapability(MMCapabilities.MAGIC_USER).ifPresent(magicUser -> {
      magicUser.equipSpell(new SpellInstance(spell, level), slot);
      if (magicUser.hasBeenModified()) {
        src.sendSuccess(() -> Component.translatable(
            "command.magicmod.spell.equip.success",
            player.getName(),
            SpellUtil.translateSpellWithLevel(SpellUtil.getName(spell), level, true),
            slot
        ), true);
        MMNetwork.sendToClient(player, new UpdateEquippedSpellsMessage(magicUser));
      } else {
        src.sendFailure(Component.translatable("command.magicmod.spell.equip.fail"));
      }
    });
    return 1;
  }

  public static int unequip(CommandSourceStack src, int slot) throws CommandSyntaxException {
    ServerPlayer player = src.getPlayerOrException();
    player.getCapability(MMCapabilities.MAGIC_USER).ifPresent(magicUser -> {
      magicUser.unequipSpell(slot);
      if (magicUser.hasBeenModified()) {
        src.sendSuccess(() -> Component.translatable(
            "command.magicmod.spell.unequip.success",
            player.getName(),
            slot
        ), true);
      } else {
        src.sendFailure(Component.translatable("command.magicmod.spell.unequip.fail"));
      }
    });
    return 1;
  }

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    var builder = Commands.literal("spell")
        .then(Commands.literal("list")
            .executes(ctx -> list(ctx.getSource()))
        )
        .then(Commands.literal("learn")
            .then(Commands.argument("spell", SpellArgument.spell())
                .executes(ctx -> learn(ctx.getSource(), SpellArgument.getSpell(ctx, "spell"), -1))
                .then(Commands.argument("level", IntegerArgumentType.integer(1, 127))
                    .executes(ctx -> learn(ctx.getSource(), SpellArgument.getSpell(ctx, "spell"), IntegerArgumentType.getInteger(ctx, "level") - 1))
                )
            )
        )
        .then(Commands.literal("unlearn")
            .then(Commands.argument("spell", SpellArgument.spell())
                .executes(ctx -> unlearn(ctx.getSource(), SpellArgument.getSpell(ctx, "spell"), -1))
                .then(Commands.argument("level", IntegerArgumentType.integer(1, 127))
                    .executes(ctx -> unlearn(ctx.getSource(), SpellArgument.getSpell(ctx, "spell"), IntegerArgumentType.getInteger(ctx, "level") - 1))
                )
            )
        )
        .then(Commands.literal("equip")
            .then(Commands.argument("spell", SpellArgument.spell())
                .then(Commands.argument("level", IntegerArgumentType.integer(1, 127))
                    .then(Commands.argument("slot", IntegerArgumentType.integer(0, MagicUser.EQUIP_SLOTS))
                        .executes(ctx -> equip(ctx.getSource(), SpellArgument.getSpell(ctx, "spell"), IntegerArgumentType.getInteger(ctx, "level") - 1, IntegerArgumentType.getInteger(ctx, "slot")))
                    )
                )
            )
        )
        .then(Commands.literal("unequip")
            .then(Commands.argument("slot", IntegerArgumentType.integer(0, MagicUser.EQUIP_SLOTS))
                .executes(ctx -> unequip(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "slot")))
            )
        );
    dispatcher.register(builder);
  }

}
