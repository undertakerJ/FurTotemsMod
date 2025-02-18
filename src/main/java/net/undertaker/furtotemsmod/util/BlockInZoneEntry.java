package net.undertaker.furtotemsmod.util;

import net.undertaker.furtotemsmod.data.TotemSavedData;

public class BlockInZoneEntry {
    public final TotemSavedData.TotemData totemData;
    public final long placedTime;

    public BlockInZoneEntry(TotemSavedData.TotemData totemData, long placedTime) {
        this.totemData = totemData;
        this.placedTime = placedTime;
    }
}
