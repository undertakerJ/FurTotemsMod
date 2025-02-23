package net.undertaker.furtotemsmod.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.undertaker.furtotemsmod.commands.handle.*;

public class FurTotemsCommands {

  @SubscribeEvent
  public void onRegisterCommands(RegisterCommandsEvent event){
    event.getDispatcher()
            .register(LiteralArgumentBuilder.<CommandSourceStack>literal("furtotems")
                    .then(AddMemberCommand.register())
                    .then(RemoveMemberCommand.register())
                    .then(AddBlacklistCommand.register())
                    .then(RemoveBlacklistCommand.register())
                    .then(RemoveAllTotemsCommand.register())
                    .then(AddBlockCommand.register())
                    .then(RemoveBlockCommand.register())
                    .then(AddToSmallTotemCommand.register())
                    .then(RemoveFromSmallTotemCommand.register()));
  }
}
