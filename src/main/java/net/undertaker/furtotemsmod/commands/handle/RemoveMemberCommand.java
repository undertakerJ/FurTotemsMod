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

public class RemoveMemberCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("remove_member")
                .then(
                        Commands.argument("player",  GameProfileArgument.gameProfile())
                                .suggests(
                                        (commandContext, suggestionsBuilder) -> {
                                            for (String name : commandContext.getSource().getOnlinePlayerNames()) {
                                                suggestionsBuilder.suggest(name);
                                            }
                                            return suggestionsBuilder.buildFuture();
                                        })
                                .executes(RemoveMemberCommand::executeRemove));
    }

    private static int executeRemove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException{
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
        if(!data.isPlayerMember(player.getUUID(),targetUUID)){
            source.sendFailure(Component.translatable(  "command.remove_member.failure.not_exist", targetProfile.getName()));
            return 0;
        }

        data.removeMemberFromTotem(player.getUUID(), targetUUID);

        SyncWhitelistPacket packet = new SyncWhitelistPacket(data.getWhitelistPlayers());
        ModNetworking.sendToPlayer(packet, player);
        source.sendSuccess(Component.translatable("command.remove_member.success", targetProfile.getName()), true);
        return 1;
    }
}
