package net.undertaker.furtotemsmod.data;

import net.minecraft.core.BlockPos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClientTotemSavedData {
    private static ClientTotemSavedData instance;

    private final Map<BlockPos, TotemSavedData.TotemData> totemDataMap = new HashMap<>();

    private final Map<UUID, Set<UUID>> whitelistPlayers = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> blacklistPlayers = new ConcurrentHashMap<>();
    private ClientTotemSavedData() {}

    public static ClientTotemSavedData get() {
        if (instance == null) {
            instance = new ClientTotemSavedData();
        }
        return instance;
    }

    public Map<UUID, Set<UUID>> getWhitelistPlayers() {
        return this.whitelistPlayers;
    }

    public Map<UUID, Set<UUID>> getBlacklistPlayers() {
        return this.blacklistPlayers;
    }

    public Map<BlockPos, TotemSavedData.TotemData> getTotemDataMap() {
        return this.totemDataMap;
    }

    public void updateTotemData(Map<BlockPos, TotemSavedData.TotemData> newData) {
        this.totemDataMap.clear();
        this.totemDataMap.putAll(newData);
    }

    public void updateWhitelistPlayers(Map<UUID, Set<UUID>> newWhitelist){
        this.whitelistPlayers.clear();
        this.whitelistPlayers.putAll(newWhitelist);
    }

    public void updateBlacklistPlayers(Map<UUID, Set<UUID>> newBlacklist){
        this.blacklistPlayers.clear();
        this.blacklistPlayers.putAll(newBlacklist);
    }
}
