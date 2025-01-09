package net.undertaker.furtotemsmod.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.undertaker.furtotemsmod.data.ClientTotemCountData;
import net.undertaker.furtotemsmod.data.TotemSavedData;

public class FurTotemsCommands {
  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    dispatcher.register(
        Commands.literal("furtotems")
            .then(
                Commands.literal("totemcount")
                    .requires(source -> source.hasPermission(2)) // Admin permission level
                    .then(
                        Commands.literal("set")
                            .then(
                                Commands.argument("player", EntityArgument.player())
                                    .then(
                                        Commands.argument("totemType", StringArgumentType.word())
                                            .suggests(
                                                (context, builder) -> {
                                                  return SharedSuggestionProvider.suggest(
                                                      new String[] {"small", "big"}, builder);
                                                })
                                            .then(
                                                Commands.argument(
                                                        "amount", IntegerArgumentType.integer(0))
                                                    .executes(
                                                        context ->
                                                            setTotemCount(
                                                                context,
                                                                EntityArgument.getPlayer(
                                                                    context, "player"),
                                                                StringArgumentType.getString(
                                                                    context, "totemType"),
                                                                IntegerArgumentType.getInteger(
                                                                    context, "amount"))))))))
            .then(
                Commands.literal("addmember")
                    .requires(source -> source.hasPermission(0))
                    .then(
                        Commands.argument("player", EntityArgument.player())
                            .then(
                                Commands.argument("member", EntityArgument.player())
                                    .executes(context -> addMember(context)))))
            .then(
                Commands.literal("removemember")
                    .requires(source -> source.hasPermission(0))
                    .then(
                        Commands.argument("player", EntityArgument.player())
                            .then(
                                Commands.argument("member", EntityArgument.player())
                                    .executes(context -> removeMember(context)))))
            .then(
                Commands.literal("removealltotems")
                    .requires(source -> source.hasPermission(2))
                    .then(
                        Commands.argument("player", EntityArgument.player())
                            .executes(
                                context -> {
                                  ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                  return removeTotems(context.getSource(), player);
                                }))));
  }

  private static int addMember(CommandContext<CommandSourceStack> context)
      throws CommandSyntaxException {
    ServerPlayer owner = EntityArgument.getPlayer(context, "player");
    ServerPlayer member = EntityArgument.getPlayer(context, "member");

    UUID ownerUUID = owner.getUUID();
    UUID memberUUID = member.getUUID();

    TotemSavedData data = TotemSavedData.get(context.getSource().getLevel());
    data.addMemberToTotem(ownerUUID, memberUUID);

    context
        .getSource()
        .sendSuccess(() ->
            Component.literal(
                "Player "
                    + member.getName().getString()
                    + " added to member of totem "
                    + owner.getName().getString()),
            true);
    return 1;
  }

  private static int setTotemCount(
      CommandContext<CommandSourceStack> context,
      ServerPlayer player,
      String totemType,
      int amount) {
    TotemSavedData data = TotemSavedData.get(player.serverLevel());
    TotemSavedData.TotemCount count = data.getPlayerTotemCount(player.getUUID());

    if (totemType.equalsIgnoreCase("small")) {
      count.setSmallTotems(amount);
    } else if (totemType.equalsIgnoreCase("big")) {
      count.setBigTotems(amount);
    } else {
      context
          .getSource()
          .sendFailure(Component.translatable("message.furtotemsmod.invalid_totem_type"));
      return 0;
    }
    data.setDirty();
    ClientTotemCountData.updateCounts(count.getSmallTotems(), count.getBigTotems());

    context
        .getSource()
        .sendSuccess(() ->
            Component.literal(
                "Set "
                    + totemType
                    + " totems for "
                    + player.getName().getString()
                    + " to "
                    + amount),
            true);
    return 1;
  }

  private static int removeMember(CommandContext<CommandSourceStack> context)
      throws CommandSyntaxException {
    ServerPlayer owner = EntityArgument.getPlayer(context, "player");
    ServerPlayer member = EntityArgument.getPlayer(context, "member");

    UUID ownerUUID = owner.getUUID();
    UUID memberUUID = member.getUUID();

    TotemSavedData data = TotemSavedData.get(context.getSource().getLevel());
    data.removeMemberFromTotem(ownerUUID, memberUUID);

    context
        .getSource()
        .sendSuccess(() ->
            Component.literal(
                "Игрок "
                    + member.getName().getString()
                    + " удалён из списка членов тотемов игрока "
                    + owner.getName().getString()),
            true);
    return 1;
  }

  private static int removeTotems(CommandSourceStack source, ServerPlayer player) {
    ServerLevel level = source.getLevel();
    TotemSavedData data = TotemSavedData.get(level);
    UUID playerUUID = player.getUUID();

    List<BlockPos> toRemove =
        data.getTotemDataMap().entrySet().stream()
            .filter(entry -> entry.getValue().getOwner().equals(playerUUID))
            .map(Map.Entry::getKey)
            .toList();

    toRemove.forEach(
        pos -> {
          level.destroyBlock(pos, false);
          data.removeTotem(level, pos);
        });

    source.sendSuccess(() ->
        Component.literal(
            "Removed " + toRemove.size() + " totems for player: " + player.getName().getString()),
        true);
    return toRemove.size();
  }
}
