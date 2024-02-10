package me.whizvox.magicmod.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.whizvox.magicmod.common.api.MagicUser;
import me.whizvox.magicmod.common.lib.MagicUserManager;
import me.whizvox.magicmod.common.network.MMNetwork;
import me.whizvox.magicmod.common.network.UpdateManaMessage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ManaCommand {

  private static int setMana(CommandSourceStack src, double mana) throws CommandSyntaxException {
    ServerPlayer player = src.getPlayerOrException();
    MagicUser magicUser = MagicUserManager.getUser(player);
    magicUser.setMana(mana);
    if (magicUser.hasBeenModified()) {
      MMNetwork.sendToClient(player, new UpdateManaMessage(magicUser));
    }
    src.sendSuccess(() -> Component.literal("Set mana to " + magicUser.getMana()), true);
    return 1;
  }

  private static int setMaxMana(CommandSourceStack src, double maxMana) throws CommandSyntaxException {
    ServerPlayer player = src.getPlayerOrException();
    MagicUser magicUser = MagicUserManager.getUser(player);
    magicUser.setMaxMana(maxMana);
    MMNetwork.sendToClient(player, new UpdateManaMessage(magicUser));
    src.sendSuccess(() -> Component.literal("Set max mana to " + magicUser.getMaxMana()), true);
    return 1;
  }

  private static int setRechargeAmount(CommandSourceStack src, double rechargeAmount) throws CommandSyntaxException {
    ServerPlayer player = src.getPlayerOrException();
    MagicUser magicUser = MagicUserManager.getUser(player);
    magicUser.setRechargeAmount(rechargeAmount);
    MMNetwork.sendToClient(player, new UpdateManaMessage(magicUser));
    src.sendSuccess(() -> Component.literal("Set recharge rate to " + magicUser.getRechargeAmount()), true);
    return 1;
  }

  private static int printStats(CommandSourceStack src, ServerPlayer player) throws CommandSyntaxException {
    if (player == null) {
      player = src.getPlayerOrException();
    }
    MagicUser magicUser = MagicUserManager.getUser(player);
    src.sendSystemMessage(Component.literal("Stats for ").append(player.getDisplayName()));
    src.sendSystemMessage(Component.literal("Mana: " + magicUser.getMana()));
    src.sendSystemMessage(Component.literal("Max: " + magicUser.getMaxMana()));
    src.sendSystemMessage(Component.literal("Rate: " + magicUser.getRechargeAmount()));
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
            .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0, Double.MAX_VALUE))
                .executes(ctx -> setRechargeAmount(ctx.getSource(), DoubleArgumentType.getDouble(ctx, "amount")))
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
