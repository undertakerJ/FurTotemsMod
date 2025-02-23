package net.undertaker.furtotemsmod.commands.handle;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.undertaker.furtotemsmod.FurConfig;

import java.util.ArrayList;
import java.util.List;

public class AddToSmallTotemCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("add_to_small_totem")
                .requires(source -> source.hasPermission(2))
                .then(
                        Commands.argument("blockid", ResourceLocationArgument.id())
                                .suggests(
                                        (commandContext, suggestionsBuilder) -> {
                                            for (ResourceLocation resourceLocation : ForgeRegistries.BLOCKS.getKeys()) {
                                                suggestionsBuilder.suggest(resourceLocation.toString());
                                            }
                                            return suggestionsBuilder.buildFuture();
                                        })
                                .executes(AddToSmallTotemCommand::executeAdd));
    }

    private static int executeAdd(CommandContext<CommandSourceStack> context) {
        ResourceLocation blockid = ResourceLocationArgument.getId(context, "blockid");
        String blockidString = blockid.toString();

        List<String> currentList = new ArrayList<>(FurConfig.ALLOWED_NEAR_SMALL_TOTEM.get());
        boolean exists = currentList.stream().anyMatch(s -> s.startsWith(blockidString));
        if (exists) {
            context
                    .getSource()
                    .sendFailure(Component.translatable("command.add_to_small_totem.failure.exist", blockidString));

            return 0;
        }

        currentList.add(blockidString.toLowerCase());
        FurConfig.ALLOWED_NEAR_SMALL_TOTEM.set(currentList);
        FurConfig.getConfig().save();
        context
                .getSource()
                .sendSuccess(Component.translatable("command.add_to_small_totem.success", blockidString), true);
        return 1;
    }
}
