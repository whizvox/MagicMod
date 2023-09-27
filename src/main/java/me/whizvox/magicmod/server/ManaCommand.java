package me.whizvox.magicmod.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.whizvox.magicmod.common.api.ManaStorage;
import me.whizvox.magicmod.common.network.MMNetwork;
import me.whizvox.magicmod.common.lib.MMCapabilities;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ManaCommand {

  private static int setMana(CommandSourceStack src, double mana) throws CommandSyntaxException {
    ServerPlayer player = src.getPlayerOrException();
    player.getCapability(MMCapabilities.MANA_STORAGE).ifPresent(manaStorage -> {
      manaStorage.setMana(mana);
      if (manaStorage.isModified()) {
        MMNetwork.updatePlayerMana(player, manaStorage);
      }
      src.sendSuccess(() -> Component.literal("Set mana to " + manaStorage.getMana()), false);
    });
    return 1;
  }

  private static int setMaxMana(CommandSourceStack src, double maxMana) throws CommandSyntaxException {
    ServerPlayer player = src.getPlayerOrException();
    player.getCapability(MMCapabilities.MANA_STORAGE).ifPresent(manaStorage -> {
      manaStorage.setMaxMana(maxMana);
      MMNetwork.updatePlayerMana(player, manaStorage);
      src.sendSuccess(() -> Component.literal("Set max mana to " + manaStorage.getMaxMana()), false);
    });
    return 1;
  }

  private static int setRechargeRate(CommandSourceStack src, double rechargeRate) throws CommandSyntaxException {
    ServerPlayer player = src.getPlayerOrException();
    player.getCapability(MMCapabilities.MANA_STORAGE).ifPresent(manaStorage -> {
      manaStorage.setRechargeRate(rechargeRate);
      MMNetwork.updatePlayerMana(player, manaStorage);
      src.sendSuccess(() -> Component.literal("Set recharge rate to " + manaStorage.getRechargeRate()), false);
    });
    return 1;
  }

  private static int printStats(CommandSourceStack src, ServerPlayer player) throws CommandSyntaxException {
    if (player == null) {
      player = src.getPlayerOrException();
    }
    var cap = player.getCapability(MMCapabilities.MANA_STORAGE);
    if (cap.isPresent()) {
      ManaStorage manaStorage = cap.resolve().get();
      src.sendSystemMessage(Component.literal("Stats for ").append(player.getDisplayName()));
      src.sendSystemMessage(Component.literal("Mana: " + manaStorage.getMana()));
      src.sendSystemMessage(Component.literal("Max: " + manaStorage.getMaxMana()));
      src.sendSystemMessage(Component.literal("Rate: " + manaStorage.getRechargeRate()));
    } else {
      src.sendSystemMessage(Component.literal("No mana!"));
    }
    return 1;
  }

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    var builder = Commands.literal("mana")
        .then(Commands.literal("set")
            .then(Commands.argument("mana", DoubleArgumentType.doubleArg(0, Double.MAX_VALUE))
                .executes(ctx -> setMana(ctx.getSource(), DoubleArgumentType.getDouble(ctx, "mana")))
            )
        )
        .then(Commands.literal("setmax")
            .then(Commands.argument("mana", DoubleArgumentType.doubleArg(0, Double.MAX_VALUE))
                .executes(ctx -> setMaxMana(ctx.getSource(), DoubleArgumentType.getDouble(ctx, "mana")))
            )
        )
        .then(Commands.literal("setrate")
            .then(Commands.argument("rate", DoubleArgumentType.doubleArg(0, Double.MAX_VALUE))
                .executes(ctx -> setRechargeRate(ctx.getSource(), DoubleArgumentType.getDouble(ctx, "rate")))
            )
        )
        .then(Commands.literal("stats")
            .executes(ctx -> printStats(ctx.getSource(), null))
            .then(Commands.argument("player", EntityArgument.player())
                .executes(ctx -> printStats(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))
            )
        );
    dispatcher.register(builder);
  }

}
