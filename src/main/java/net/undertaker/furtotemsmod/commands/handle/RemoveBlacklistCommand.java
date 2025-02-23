package net.undertaker.furtotemsmod.commands.handle;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.undertaker.furtotemsmod.data.TotemSavedData;
import net.undertaker.furtotemsmod.networking.ModNetworking;
import net.undertaker.furtotemsmod.networking.packets.SyncWhitelistPacket;

public class RemoveBlacklistCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("remove_blacklist")
                .then(
                        Commands.argument("player",  GameProfileArgument.gameProfile())
                                .suggests(
                                        (commandContext, suggestionsBuilder) -> {
                                            for (String name : commandContext.getSource().getOnlinePlayerNames()) {
                                                suggestionsBuilder.suggest(name);
                                            }
                                            return suggestionsBuilder.buildFuture();
                                        })
                                .executes(RemoveBlacklistCommand::executeAdd));
    }

    private static int executeAdd(CommandContext<CommandSourceStack> context) throws CommandSyntaxException{
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        Collection<GameProfile> gameProfiles = GameProfileArgument.getGameProfiles(context, "player");

        if (gameProfiles.isEmpty()) {
            source.sendFailure(Component.literal( "command.error.player_not_found"));
            return 0;
        }
        GameProfile targetProfile = gameProfiles.iterator().next();
        UUID targetUUID = targetProfile.getId();

        ServerLevel level = source.getLevel();
        TotemSavedData data = TotemSavedData.get(level);
        if(!data.isBlacklisted(player.getUUID(),targetUUID)){
            source.sendFailure(Component.translatable( "command.remove_blacklist.failure.not_exist", targetProfile.getName()));
            return 0;
        }

        data.removeFromBlacklist(player.getUUID(), targetUUID);

        SyncWhitelistPacket packet = new SyncWhitelistPacket(data.getWhitelistPlayers());
        ModNetworking.sendToPlayer(packet, player);
        source.sendSuccess(Component.translatable("command.remove_blacklist.success", targetProfile.getName()), true);
        return 1;
    }
}
