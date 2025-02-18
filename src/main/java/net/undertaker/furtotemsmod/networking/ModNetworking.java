package net.undertaker.furtotemsmod.networking;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.undertaker.furtotemsmod.FurTotemsMod;
import net.undertaker.furtotemsmod.networking.packets.ChangeModePacket;
import net.undertaker.furtotemsmod.networking.packets.SyncTotemMaterialPacket;
import net.undertaker.furtotemsmod.networking.packets.SyncTotemsPacket;

public class ModNetworking {
  private static SimpleChannel INSTANCE;

  private static int packetId = 0;

  private static int id() {
    return packetId++;
  }

  public static void register() {
    SimpleChannel net =
        NetworkRegistry.ChannelBuilder.named(new ResourceLocation(FurTotemsMod.MOD_ID, "messages"))
            .networkProtocolVersion(() -> "1.3")
            .clientAcceptedVersions(s -> true)
            .serverAcceptedVersions(s -> true)
            .simpleChannel();

    INSTANCE = net;

    INSTANCE
        .messageBuilder(ChangeModePacket.class, 1)
        .encoder(ChangeModePacket::encode)
        .decoder(ChangeModePacket::decode)
        .consumerMainThread(ChangeModePacket::handle)
        .add();
    INSTANCE
        .messageBuilder(SyncTotemsPacket.class, 2)
        .encoder(SyncTotemsPacket::encode)
        .decoder(SyncTotemsPacket::new)
        .consumerMainThread(SyncTotemsPacket::handle)
        .add();
    INSTANCE
        .messageBuilder(SyncTotemMaterialPacket.class, 3)
        .encoder(SyncTotemMaterialPacket::encode)
        .decoder(SyncTotemMaterialPacket::decode)
        .consumerMainThread(SyncTotemMaterialPacket::receive)
        .add();

  }

  public static <MSG> void sendToServer(MSG message) {
    INSTANCE.sendToServer(message);
  }

  public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
    INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
  }

  public static void sendToAllPlayers(Object message, ServerLevel level) {
    level.players().forEach(player -> {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);

    });
  }
}
