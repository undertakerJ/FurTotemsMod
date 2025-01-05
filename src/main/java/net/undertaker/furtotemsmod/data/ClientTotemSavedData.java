package net.undertaker.furtotemsmod.data;

import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class ClientTotemSavedData {
    private static final ClientTotemSavedData INSTANCE = new ClientTotemSavedData();
    private final Map<BlockPos, TotemSavedData.TotemData> totemDataMap = new HashMap<>();

    private ClientTotemSavedData() {}

    public static ClientTotemSavedData get() {
        return INSTANCE;
    }

    public void updateTotemData(Map<BlockPos, TotemSavedData.TotemData> data) {
        this.totemDataMap.clear();
        this.totemDataMap.putAll(data);
    }

    public Map<BlockPos, TotemSavedData.TotemData> getTotemDataMap() {
        return this.totemDataMap;
    }
}
