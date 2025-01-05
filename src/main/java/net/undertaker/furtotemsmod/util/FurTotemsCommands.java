package net.undertaker.furtotemsmod.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.undertaker.furtotemsmod.data.TotemSavedData;

public class FurTotemsCommands {
  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    dispatcher.register(
        Commands.literal("furtotems")
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
                                })))
            .then(
                Commands.literal("blacklistadd")
                    .requires(source -> source.hasPermission(0))
                    .then(
                        Commands.argument("target", EntityArgument.player())
                            .executes(
                                context -> {
                                  ServerPlayer target = EntityArgument.getPlayer(context, "target");
                                  ServerPlayer sender = context.getSource().getPlayer();

                                  TotemSavedData data = TotemSavedData.get(sender.getLevel());
                                  if (data != null) {
                                    data.addToBlacklist(sender.getUUID() ,target.getUUID());
                                    context
                                        .getSource()
                                        .sendSuccess(
                                            Component.literal(
                                                target.getName().getString()
                                                    + " добавлен в чёрный список."),
                                            true);
                                  } else {
                                    context
                                        .getSource()
                                        .sendFailure(
                                            Component.literal("У вас нет рядом активного тотема."));
                                  }
                                  return 1;
                                })))
            .then(
                Commands.literal("blacklistremove")
                    .requires(source -> source.hasPermission(0))
                    .then(
                        Commands.argument("target", EntityArgument.player())
                            .executes(
                                context -> {
                                  ServerPlayer target = EntityArgument.getPlayer(context, "target");
                                  ServerPlayer sender = context.getSource().getPlayer();

                                  TotemSavedData data = TotemSavedData.get(sender.getLevel());
                                  if (data != null) {
                                    data.removeFromBlacklist(sender.getUUID(), target.getUUID());
                                    context
                                        .getSource()
                                        .sendSuccess(
                                            Component.literal(
                                                target.getName().getString()
                                                    + " удалён из чёрного списка."),
                                            true);
                                  } else {
                                    context
                                        .getSource()
                                        .sendFailure(
                                            Component.literal("У вас нет рядом активного тотема."));
                                  }
                                  return 1;
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
        .sendSuccess(
            Component.literal(
                "Игрок "
                    + member.getName().getString()
                    + " добавлен в список членов тотемов игрока "
                    + owner.getName().getString()),
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
        .sendSuccess(
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

    // Collect totems owned by the player
    List<BlockPos> toRemove =
        data.getTotemDataMap().entrySet().stream()
            .filter(entry -> entry.getValue().getOwner().equals(playerUUID))
            .map(Map.Entry::getKey)
            .toList();

    // Remove the totems from the world and data storage
    toRemove.forEach(
        pos -> {
          level.destroyBlock(pos, false);
          data.removeTotem(pos);
        });

    // Send feedback to the command source
    source.sendSuccess(
        Component.literal(
            "Removed " + toRemove.size() + " totems for player: " + player.getName().getString()),
        true);
    return toRemove.size();
  }
}
