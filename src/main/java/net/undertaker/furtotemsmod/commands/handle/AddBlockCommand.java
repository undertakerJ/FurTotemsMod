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

public class AddBlockCommand {
  public static LiteralArgumentBuilder<CommandSourceStack> register() {
    return Commands.literal("add_block")
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
                .executes(AddBlockCommand::executeAdd));
  }

  private static int executeAdd(CommandContext<CommandSourceStack> context) {
    ResourceLocation blockid = ResourceLocationArgument.getId(context, "blockid");
    String blockidString = blockid.toString();

    List<String> currentList = new ArrayList<>(FurConfig.BLACKLIST_DECAY_BLOCKS.get());
    boolean exists = currentList.stream().anyMatch(s -> s.startsWith(blockidString));
    if (exists) {
      context
          .getSource()
          .sendFailure(Component.translatable("command.add_block.failure.exist", blockidString));

      return 0;
    }

    currentList.add(blockidString.toLowerCase());
    FurConfig.BLACKLIST_DECAY_BLOCKS.set(currentList);
    FurConfig.getConfig().save();
    context
        .getSource()
        .sendSuccess(Component.translatable("command.add_block.success", blockidString), true);
    return 1;
  }
}
