package net.undertaker.furtotemsmod.util;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RemoveTotemsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("removetotems")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> removeTotems(context.getSource(), EntityArgument.getPlayer(context, "player")))));
    }

    private static int removeTotems(CommandSourceStack source, Player player) {
        ServerLevel level = source.getLevel();
        TotemSavedData data = TotemSavedData.get(level);
        UUID playerUUID = player.getUUID();

        List<BlockPos> toRemove = new ArrayList<>();
        for (Map.Entry<BlockPos, TotemSavedData.TotemData> entry : data.getTotemDataMap().entrySet()) {
            if (entry.getValue().getOwner().equals(playerUUID)) {
                toRemove.add(entry.getKey());
            }
        }

        for (BlockPos pos : toRemove) {
            level.destroyBlock(pos, false);
            data.removeTotem(pos);
        }

        source.sendSuccess(Component.literal("Удалено " + toRemove.size() + " тотемов для игрока " + player.getName().getString()), true);
        return toRemove.size();
    }
}
