package me.whizvox.magicmod.server.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.whizvox.magicmod.common.api.spell.Spell;
import me.whizvox.magicmod.common.registry.SpellRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class SpellArgument implements ArgumentType<Spell> {

  private static final DynamicCommandExceptionType ERROR_UNKNOWN_SPELL = new DynamicCommandExceptionType(spellName ->
      Component.translatable("command.magicmod.spell.unknown_spell", spellName)
  );

  public static SpellArgument spell() {
    return new SpellArgument();
  }

  @Override
  public Spell parse(StringReader reader) throws CommandSyntaxException {
    ResourceLocation spellName = ResourceLocation.read(reader);
    Spell spell = SpellRegistry.getRegistry().getValue(spellName);
    if (spell == null) {
      throw ERROR_UNKNOWN_SPELL.create(spellName);
    }
    return spell;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
    return SharedSuggestionProvider.suggestResource(SpellRegistry.getRegistry().getKeys(), builder);
  }

  public static Spell getSpell(CommandContext<CommandSourceStack> ctx, String argumentName) {
    return ctx.getArgument(argumentName, Spell.class);
  }

}
