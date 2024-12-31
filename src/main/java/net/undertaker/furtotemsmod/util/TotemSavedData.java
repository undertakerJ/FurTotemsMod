package net.undertaker.furtotemsmod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.undertaker.furtotemsmod.FurTotemsMod;

import java.util.*;
import java.util.stream.Stream;

public class TotemSavedData extends SavedData {
    private final Map<BlockPos, TotemData> totemDataMap = new HashMap<>();
    private final Map<UUID, TotemCount> totemsPerPlayer = new HashMap<>();

    public static class TotemData {
        private final UUID owner;
        private final int radius;
        private final String type;
        private final Set<UUID> members;

        public TotemData(UUID owner, int radius, String type) {
            this.owner = owner;
            this.radius = radius;
            this.type = type;
            this.members = new HashSet<>();
        }

        public UUID getOwner() {
            return owner;
        }

        public int getRadius() {
            return radius;
        }

        public String getType() {
            return type;
        }
        public Set<UUID> getMembers() {
            return members;
        }

        public void addMember(UUID memberUUID) {
            members.add(memberUUID);
        }

        public void removeMember(UUID memberUUID) {
            members.remove(memberUUID);
        }

        public boolean isMember(UUID memberUUID) {
            return owner.equals(memberUUID) || members.contains(memberUUID);
        }

        private final List<UUID> blacklistedPlayers = new ArrayList<>();

        public boolean isBlacklisted(UUID playerUUID) {
            return blacklistedPlayers.contains(playerUUID);
        }

        public void addToBlacklist(UUID playerUUID) {
            if (!blacklistedPlayers.contains(playerUUID)) {
                blacklistedPlayers.add(playerUUID);
            }
        }

        public void removeFromBlacklist(UUID playerUUID) {
            blacklistedPlayers.remove(playerUUID);
        }

        public List<UUID> getBlacklistedPlayers() {
            return Collections.unmodifiableList(blacklistedPlayers);
        }

    }

    public static class TotemCount {
        private int smallTotems = 0;
        private int bigTotems = 0;

        public int getSmallTotems() {
            return smallTotems;
        }

        public int getBigTotems() {
            return bigTotems;
        }

        public void incrementSmallTotems() {
            smallTotems++;
        }

        public void incrementBigTotems() {
            bigTotems++;
        }

        public void decrementSmallTotems() {
            smallTotems = Math.max(0, smallTotems - 1);
        }

        public void decrementBigTotems() {
            bigTotems = Math.max(0, bigTotems - 1);
        }
    }

    public void addToBlacklist(UUID ownerUUID, UUID playerUUID) {
        totemDataMap.values().stream()
                .filter(totem -> totem.getOwner().equals(ownerUUID))
                .forEach(totem -> totem.addToBlacklist(playerUUID));
        setDirty();
    }

    public void removeFromBlacklist(UUID ownerUUID, UUID playerUUID) {
        totemDataMap.values().stream()
                .filter(totem -> totem.getOwner().equals(ownerUUID))
                .forEach(totem -> totem.removeFromBlacklist(playerUUID));
        setDirty();
    }

    public boolean isBlacklisted(UUID ownerUUID, UUID playerUUID) {
        return totemDataMap.values().stream()
                .filter(totem -> totem.getOwner().equals(ownerUUID))
                .anyMatch(totem -> totem.isBlacklisted(playerUUID));
    }

    public void addMemberToTotem(UUID ownerUUID, UUID memberUUID) {
        totemDataMap.values().stream()
                .filter(totem -> totem.getOwner().equals(ownerUUID))
                .forEach(totem -> totem.addMember(memberUUID));
        setDirty();
    }

    public void removeMemberFromTotem(UUID ownerUUID, UUID memberUUID) {
        totemDataMap.values().stream()
                .filter(totem -> totem.getOwner().equals(ownerUUID))
                .forEach(totem -> totem.removeMember(memberUUID));
        setDirty();
    }

    public boolean isPlayerMember(UUID ownerUUID, UUID playerUUID) {
        return totemDataMap.values().stream()
                .filter(totem -> totem.getOwner().equals(ownerUUID))
                .anyMatch(totem -> totem.isMember(playerUUID));
    }


    public TotemCount getPlayerTotemCount(UUID playerUUID) {
        return totemsPerPlayer.computeIfAbsent(playerUUID, uuid -> new TotemCount());
    }

    public void addTotem(BlockPos pos, UUID owner, int radius, String type) {
        totemDataMap.put(pos, new TotemData(owner, radius, type));
        TotemCount count = getPlayerTotemCount(owner);
        if ("Small".equals(type)) {
            count.incrementSmallTotems();
        } else if ("Upgradable".equals(type)) {
            count.incrementBigTotems();
        }
        setDirty();
    }

    public void removeTotem(BlockPos pos) {
        TotemData data = totemDataMap.remove(pos);
        if (data != null) {
            TotemCount count = getPlayerTotemCount(data.getOwner());
            if ("Small".equals(data.getType())) {
                count.decrementSmallTotems();
            } else if ("Upgradable".equals(data.getType())) {
                count.decrementBigTotems();
            }
        }
        setDirty();
    }

    public TotemData getTotemData(BlockPos pos) {
        return totemDataMap.get(pos);
    }

    public void updateTotem(BlockPos pos, UUID owner, int radius, String type) {
        TotemData existingTotem = totemDataMap.get(pos);
        if (existingTotem != null) {
            totemDataMap.put(pos, new TotemData(owner, radius, type));
            setDirty();
            FurTotemsMod.LOGGER.info("Тотем обновлён: " + pos + ", радиус: " + radius + ", тип: " + type);
        } else {
            FurTotemsMod.LOGGER.warn("Не удалось обновить тотем на позиции: " + pos);
        }
    }

    public Map<BlockPos, TotemData> getTotemDataMap() {
        return totemDataMap;
    }

    public boolean isOverlapping(BlockPos pos, double radius, UUID ownerUUID) {
        for (Map.Entry<BlockPos, TotemData> entry : totemDataMap.entrySet()) {
            BlockPos existingPos = entry.getKey();
            TotemData totemData = entry.getValue();


            if (totemData.getOwner().equals(ownerUUID)) {
                return false;
            }

            double existingRadius = totemData.getRadius();

            if (existingPos.equals(pos)) {
                continue;
            }

            if (existingPos.distSqr(pos) <= Math.pow(radius + existingRadius, 2)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPositionProtected(BlockPos pos, UUID playerUUID) {
        BlockPos nearestTotem = getNearestTotem(pos);
        if (nearestTotem == null) return false;

        TotemData totemData = getTotemData(nearestTotem);
        if (totemData == null) return false;

        return nearestTotem.distSqr(pos) <= Math.pow(totemData.getRadius(), 2)
                && !totemData.getOwner().equals(playerUUID);
    }


    public BlockPos getNearestTotem(BlockPos targetPos) {
        return totemDataMap.entrySet().stream()
                .filter(entry -> entry.getKey().distSqr(targetPos) <= entry.getValue().getRadius() * entry.getValue().getRadius())
                .min(Comparator.comparingDouble(entry -> entry.getKey().distSqr(targetPos)))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public static TotemSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TotemSavedData::load, TotemSavedData::new, "totem_positions");
    }

    public static TotemSavedData load(CompoundTag tag) {
        TotemSavedData data = new TotemSavedData();
        ListTag totemList = tag.getList("Totems", Tag.TAG_COMPOUND);
        for (Tag entry : totemList) {
            CompoundTag compound = (CompoundTag) entry;
            BlockPos pos = new BlockPos(compound.getInt("X"), compound.getInt("Y"), compound.getInt("Z"));
            UUID owner = compound.getUUID("Owner");
            int radius = compound.getInt("Radius");
            String type = compound.getString("Type");
            data.totemDataMap.put(pos, new TotemData(owner, radius, type));
        }

        CompoundTag playerCounts = tag.getCompound("PlayerCounts");
        for (String key : playerCounts.getAllKeys()) {
            UUID player = UUID.fromString(key);
            CompoundTag countsTag = playerCounts.getCompound(key);
            TotemCount count = new TotemCount();
            count.smallTotems = countsTag.getInt("SmallTotems");
            count.bigTotems = countsTag.getInt("BigTotems");
            data.totemsPerPlayer.put(player, count);
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag totemList = new ListTag();
        for (Map.Entry<BlockPos, TotemData> entry : totemDataMap.entrySet()) {
            CompoundTag compound = new CompoundTag();
            BlockPos pos = entry.getKey();
            TotemData data = entry.getValue();
            compound.putInt("X", pos.getX());
            compound.putInt("Y", pos.getY());
            compound.putInt("Z", pos.getZ());
            compound.putUUID("Owner", data.getOwner());
            compound.putInt("Radius", data.getRadius());
            compound.putString("Type", data.getType());
            totemList.add(compound);
        }
        tag.put("Totems", totemList);

        CompoundTag playerCounts = new CompoundTag();
        for (Map.Entry<UUID, TotemCount> entry : totemsPerPlayer.entrySet()) {
            UUID player = entry.getKey();
            TotemCount counts = entry.getValue();
            CompoundTag countsTag = new CompoundTag();
            countsTag.putInt("SmallTotems", counts.getSmallTotems());
            countsTag.putInt("BigTotems", counts.getBigTotems());
            playerCounts.put(player.toString(), countsTag);
        }
        tag.put("PlayerCounts", playerCounts);

        return tag;
    }
}

