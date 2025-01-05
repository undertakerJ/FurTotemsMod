package net.undertaker.furtotemsmod.util;

import net.minecraft.server.level.ServerLevel;

public class ServerLevelAccessor {
    private static ServerLevel serverLevel;

    public static void setServerLevel(ServerLevel level) {
        serverLevel = level;
    }

    public static ServerLevel getServerLevel() {
        return serverLevel;
    }
}
