package net.undertaker.furtotemsmod.commands.handle;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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

public class RemoveAllTotemsCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register(){
        return
                Commands.literal("removealltotems")
                                        .requires(source -> source.hasPermission(2))
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .executes(
                                                                context -> {
                                                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                                    return removeTotems(context.getSource(), player);
                                                                }));
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

        source.sendSuccess(
                Component.literal(
                        "Removed " + toRemove.size() + " totems for player: " + player.getName().getString()),
                true);
        return toRemove.size();
    }
}
