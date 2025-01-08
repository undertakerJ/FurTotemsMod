package net.undertaker.furtotemsmod.data;

import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class ClientTotemSavedData {
    private static ClientTotemSavedData instance;

    private final Map<BlockPos, TotemSavedData.TotemData> totemDataMap = new HashMap<>();

    private ClientTotemSavedData() {}

    public static ClientTotemSavedData get() {
        if (instance == null) {
            instance = new ClientTotemSavedData();
        }
        return instance;
    }

    public Map<BlockPos, TotemSavedData.TotemData> getTotemDataMap() {
        return this.totemDataMap;
    }

    public TotemSavedData.TotemData getTotemData(BlockPos pos) {
        return totemDataMap.get(pos);
    }

    public void updateTotemData(Map<BlockPos, TotemSavedData.TotemData> newData) {
        this.totemDataMap.clear();
        this.totemDataMap.putAll(newData);
    }

}
