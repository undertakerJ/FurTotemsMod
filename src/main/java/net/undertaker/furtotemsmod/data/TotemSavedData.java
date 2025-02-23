package net.undertaker.furtotemsmod.data;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.UsernameCache;
import net.undertaker.furtotemsmod.util.PlacedBlockManager;

public class TotemSavedData extends SavedData {
  private final Map<BlockPos, TotemData> totemDataMap = new HashMap<>();

  private final Map<UUID, TotemCount> totemsPerPlayer = new HashMap<>();

  private final Map<BlockPos, Long> placedSmallTotems = new ConcurrentHashMap<>();

  public final Map<BlockPos, Long> placedBlocks = new HashMap<>();
  public final Map<BlockPos, BlockInZoneEntry> placedBlocksInZone = new ConcurrentHashMap<>();
  private final Map<BlockPos, Long> placedBlocksOutZone = new ConcurrentHashMap<>();

  private final Map<UUID, Set<UUID>> whitelistPlayers = new HashMap<>();
  private final Map<UUID, Set<UUID>> blacklistPlayers = new HashMap<>();

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

    public String getOwnerName(UUID uuid) {
      String cachedName = UsernameCache.getLastKnownUsername(uuid);
      if(cachedName == null){
        return uuid.toString();
      }
      return cachedName;
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

  }

  public static class TotemCount {
    private int smallTotems = 0;
    private int bigTotems = 0;

    public void setSmallTotems(int smallTotems) {
      this.smallTotems = Math.max(0, smallTotems);
    }

    public void setBigTotems(int bigTotems) {
      this.bigTotems = Math.max(0, bigTotems);
    }

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
  public static class BlockInZoneEntry {
    public final TotemData totemData;
    public final long placedTime;
    public BlockInZoneEntry(TotemData totemData, long placedTime) {
      this.totemData = totemData;
      this.placedTime = placedTime;
    }
  }

  public void removeSmallTotems(BlockPos pos, Long time){
    placedSmallTotems.remove(pos);
    setDirty();
  }

  public void addSmallTotemWithLimit(ServerLevel level, BlockPos newTotemPos, long time, int maxSmallTotems) {
    // Если лимит превышен, ищем самый старый тотем
    if (placedSmallTotems.size() >= maxSmallTotems) {
      BlockPos oldestTotem = placedSmallTotems.entrySet().stream()
              .min(Comparator.comparingLong(Map.Entry::getValue))
              .map(Map.Entry::getKey)
              .orElse(null);

      if (oldestTotem != null) {
        level.destroyBlock(oldestTotem, true);
        placedSmallTotems.remove(oldestTotem);
        totemDataMap.remove(oldestTotem);
        setDirty();
      }
    }
    // Добавляем новый тотем
    placedSmallTotems.put(newTotemPos, time);
    setDirty();
  }

  public Map<BlockPos, Long> getPlacedSmallTotems() {
    return placedSmallTotems;
  }

  public void addToBlacklist(UUID ownerUUID, UUID playerUUID) {
    blacklistPlayers.computeIfAbsent(ownerUUID, k -> new HashSet<>()).add(playerUUID);
    setDirty();
  }

  public void removeFromBlacklist(UUID ownerUUID, UUID playerUUID) {
    blacklistPlayers.getOrDefault(ownerUUID, Collections.emptySet()).remove(playerUUID);
    setDirty();
  }

  public boolean isBlacklisted(UUID ownerUUID, UUID playerUUID) {
    return blacklistPlayers.getOrDefault(ownerUUID, Collections.emptySet()).contains(playerUUID);
  }

  public void addMemberToTotem(UUID ownerUUID, UUID memberUUID) {
    whitelistPlayers.computeIfAbsent(ownerUUID, k -> new HashSet<>()).add(memberUUID);
    setDirty();
  }

  public void removeMemberFromTotem(UUID ownerUUID, UUID memberUUID) {
    whitelistPlayers.getOrDefault(ownerUUID, Collections.emptySet()).remove(memberUUID);
    setDirty();
  }

  public boolean isPlayerMember(UUID ownerUUID, UUID playerUUID) {
    return ownerUUID.equals(playerUUID) ||
            whitelistPlayers.getOrDefault(ownerUUID, Collections.emptySet()).contains(playerUUID);
  }

  public Map<UUID, Set<UUID>> getWhitelistPlayers() {
    return whitelistPlayers;
  }

  public Map<UUID, Set<UUID>> getBlacklistPlayers() {
    return blacklistPlayers;
  }

  public void addBlockInZone(BlockPos pos, BlockInZoneEntry entry) {
    this.placedBlocksInZone.put(pos, entry);
  }

  public void addBlock(BlockPos pos, long time) {
    this.placedBlocks.put(pos, time);
  }

  public Map<BlockPos, Long> getPlacedBlocks() {
    return placedBlocks;
  }

  public Map<BlockPos, BlockInZoneEntry> getPlacedBlocksInZone() {
    return placedBlocksInZone;
  }

  public static String getOwnerName(ServerLevel level, UUID ownerUUID) {
    ServerPlayer player = level.getServer().getPlayerList().getPlayer(ownerUUID);
    if (player != null) {
      return player.getName().getString();
    } else {
      return "Unknown";
    }
  }

  public Set<BlockPos> getTotemsInRadius(BlockPos playerPos, UUID playerUUID) {
    Set<BlockPos> affectingTotems = new HashSet<>();

    for (Map.Entry<BlockPos, TotemData> entry : totemDataMap.entrySet()) {
      BlockPos totemPos = entry.getKey();
      TotemData totem = entry.getValue();

      double radius = totem.getRadius();

      if (totemPos.distSqr(playerPos) <= radius * radius) {
        affectingTotems.add(totemPos);
      }
    }

    return affectingTotems;
  }


  public Set<BlockPos> getSmallTotemPositions() {
    Set<BlockPos> smallTotemPositions = new HashSet<>();
    for (Map.Entry<BlockPos, TotemData> entry : totemDataMap.entrySet()) {
      if ("Small".equals(entry.getValue().getType())) {
        smallTotemPositions.add(entry.getKey());
      }
    }
    return smallTotemPositions;
  }

  public TotemCount getPlayerTotemCount(UUID playerUUID) {
    return totemsPerPlayer.computeIfAbsent(playerUUID, uuid -> new TotemCount());
  }

  public void addTotem(ServerLevel level, BlockPos pos, UUID owner, int radius, String type) {
    if(level.isClientSide()) return;
    TotemData newTotem = new TotemData(owner, radius, type);
    totemDataMap.put(pos, newTotem);
    TotemCount count = getPlayerTotemCount(owner);
    if ("Small".equals(type)) {
      count.incrementSmallTotems();
    } else if ("Upgradable".equals(type)) {
      count.incrementBigTotems();
    }
    for (BlockPos blockPos : placedBlocksInZone.keySet()) {
      if (blockPos.distSqr(pos) <= Math.pow(radius, 2)) {
        addBlockInZone(blockPos, new BlockInZoneEntry(newTotem, level.getGameTime()));
      }
    }
    setDirty();
  }

  public void removeTotem(ServerLevel level, BlockPos pos) {
    TotemData removedTotem = totemDataMap.remove(pos);
    if (removedTotem != null) {
      UUID owner = removedTotem.getOwner();
      TotemCount count = getPlayerTotemCount(owner);
      if ("Small".equals(removedTotem.getType())) {
        count.decrementSmallTotems();
      } else if ("Upgradable".equals(removedTotem.getType())) {
        count.decrementBigTotems();
      }
      PlacedBlockManager.onTotemDestroyed(level ,removedTotem);
      setDirty();
    }
  }



  public List<BlockPos> getAllTotemsOwnedBy(UUID ownerUUID) {
    List<BlockPos> ownedTotems = new ArrayList<>();
    for (Map.Entry<BlockPos, TotemData> entry : totemDataMap.entrySet()) {
      if (entry.getValue().getOwner().equals(ownerUUID)) {
        ownedTotems.add(entry.getKey());
      }
    }
    return ownedTotems;
  }

  public TotemData getTotemData(BlockPos pos) {
    return totemDataMap.get(pos);
  }

  public void updateTotem(BlockPos pos, UUID owner, int radius, String type) {
    TotemData existingTotem = totemDataMap.get(pos);
    if (existingTotem != null) {
      totemDataMap.put(pos, new TotemData(owner, radius, type));
      setDirty();
    }
  }

  public Map<BlockPos, TotemData> getTotemDataMap() {
    return totemDataMap;
  }

  public boolean isOverlapping(BlockPos pos, double radius, UUID ownerUUID) {
    for (Map.Entry<BlockPos, TotemData> entry : totemDataMap.entrySet()) {
      BlockPos existingPos = entry.getKey();
      TotemData totemData = entry.getValue();
      if (existingPos.equals(pos)) {
        continue;
      }
      if (totemData.getOwner().equals(ownerUUID)) {
        continue;
      }
      double existingRadius = totemData.getRadius();
      if (existingPos.distSqr(pos) <= Math.pow(radius + existingRadius, 2)) {
        return true;
      }
    }
    return false;
  }

  public Map<BlockPos, Long> getPlacedBlocksOutZone() {
    return placedBlocksOutZone;
  }

  public boolean isPositionProtected(BlockPos pos, UUID playerUUID) {
    BlockPos nearestTotem = getNearestTotem(pos);
    if (nearestTotem == null) return false;

    TotemData totemData = getTotemData(nearestTotem);
    if (totemData == null) return false;

    return nearestTotem.distSqr(pos) <= Math.pow(totemData.getRadius(), 2)
        && !isPlayerMember(totemData.getOwner(), playerUUID);
  }

    public static TotemSavedData.TotemData getNearestTotemData(ServerLevel level, BlockPos pos) {
    TotemSavedData data = TotemSavedData.get(level);
    BlockPos nearestTotem = data.getNearestTotem(pos);
    if (nearestTotem != null) {
      double radius = data.getTotemData(nearestTotem).getRadius();
      if (nearestTotem.distSqr(pos) <= Math.pow(radius, 2)) {
        return data.getTotemData(nearestTotem);
      }
    }
    return null;
  }


  public BlockPos getNearestTotem(BlockPos targetPos) {
    return totemDataMap.entrySet().stream()
        .filter(
            entry ->
                entry.getKey().distSqr(targetPos)
                    <= entry.getValue().getRadius() * entry.getValue().getRadius())
        .min(Comparator.comparingDouble(entry -> entry.getKey().distSqr(targetPos)))
        .map(Map.Entry::getKey)
        .orElse(null);
  }

  public static TotemSavedData get(ServerLevel level) {
    return level
        .getDataStorage()
        .computeIfAbsent(TotemSavedData::load, TotemSavedData::new, "totem_positions");
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

    ListTag placedList = tag.getList("PlacedBlocks", Tag.TAG_COMPOUND);
    for (int i = 0; i < placedList.size(); i++) {
      CompoundTag blockTag = placedList.getCompound(i);
      BlockPos pos = new BlockPos(blockTag.getInt("X"), blockTag.getInt("Y"), blockTag.getInt("Z"));
      long time = blockTag.getLong("Time");
      data.placedBlocks.put(pos, time);
    }

    ListTag placedInZoneList = tag.getList("PlacedBlocksInZone", Tag.TAG_COMPOUND);
    for (int i = 0; i < placedInZoneList.size(); i++) {
      CompoundTag blockTag = placedInZoneList.getCompound(i);
      BlockPos pos = new BlockPos(blockTag.getInt("X"), blockTag.getInt("Y"), blockTag.getInt("Z"));
      UUID owner = blockTag.getUUID("Owner");
      int radius = blockTag.getInt("Radius");
      String type = blockTag.getString("Type");
      long time = blockTag.getLong("Time");
      data.placedBlocksInZone.put(pos, new BlockInZoneEntry(new TotemData(owner, radius, type), time));
    }

    ListTag outZoneList = tag.getList("PlacedBlocksOutZone", Tag.TAG_COMPOUND);
    for (Tag t : outZoneList) {
      CompoundTag entryTag = (CompoundTag) t;
      BlockPos pos = new BlockPos(
              entryTag.getInt("X"),
              entryTag.getInt("Y"),
              entryTag.getInt("Z")
      );
      long time = entryTag.getLong("Time");
      data.placedBlocksOutZone.put(pos, time);
    }

    ListTag whitelistTag = tag.getList("Whitelist", Tag.TAG_COMPOUND);
    for (int i = 0; i < whitelistTag.size(); i++) {
      CompoundTag entryTag = whitelistTag.getCompound(i);
      UUID owner = entryTag.getUUID("Owner");
      ListTag membersTag = entryTag.getList("Members", Tag.TAG_INT_ARRAY);
      Set<UUID> members = new HashSet<>();
      membersTag.forEach(memberTag -> members.add(NbtUtils.loadUUID(memberTag)));
      data.whitelistPlayers.put(owner, members);
    }

    ListTag blacklistTag = tag.getList("Blacklist", Tag.TAG_COMPOUND);
    for (int i = 0; i < blacklistTag.size(); i++) {
      CompoundTag entryTag = blacklistTag.getCompound(i);
      UUID owner = entryTag.getUUID("Owner");
      ListTag blockedTag = entryTag.getList("Blacklist", Tag.TAG_INT_ARRAY);
      Set<UUID> blocked = new HashSet<>();
      blockedTag.forEach(blockedMemberTag -> blocked.add(NbtUtils.loadUUID(blockedMemberTag)));
      data.blacklistPlayers.put(owner, blocked);
    }

    data.placedSmallTotems.clear();
    ListTag smallTotemsList = tag.getList("PlacedSmallTotems", 10);
    for (int i = 0; i < smallTotemsList.size(); i++) {
      CompoundTag totemTag = smallTotemsList.getCompound(i);
      BlockPos pos = BlockPos.of(totemTag.getLong("pos"));
      long time = totemTag.getLong("time");
      data.placedSmallTotems.put(pos, time);
    }

    return data;
  }

  @Override
  public CompoundTag save(CompoundTag tag) {
    ListTag totemList = new ListTag();
    for (Map.Entry<BlockPos, TotemData> entry : totemDataMap.entrySet()) {
      CompoundTag totemTag = new CompoundTag();
      BlockPos pos = entry.getKey();
      TotemData data = entry.getValue();

      totemTag.putInt("X", pos.getX());
      totemTag.putInt("Y", pos.getY());
      totemTag.putInt("Z", pos.getZ());

      totemTag.putUUID("Owner", data.getOwner());
      totemTag.putInt("Radius", data.getRadius());
      totemTag.putString("Type", data.getType());
      totemList.add(totemTag);

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

    ListTag placedList = new ListTag();
    for (Map.Entry<BlockPos, Long> entry : placedBlocks.entrySet()) {
      CompoundTag blockTag = new CompoundTag();
      BlockPos pos = entry.getKey();
      blockTag.putInt("X", pos.getX());
      blockTag.putInt("Y", pos.getY());
      blockTag.putInt("Z", pos.getZ());
      blockTag.putLong("Time", entry.getValue());
      placedList.add(blockTag);
    }
    tag.put("PlacedBlocks", placedList);

    ListTag placedInZoneList = new ListTag();
    for (Map.Entry<BlockPos, BlockInZoneEntry> entry : placedBlocksInZone.entrySet()) {
      CompoundTag blockTag = new CompoundTag();
      BlockPos pos = entry.getKey();
      BlockInZoneEntry bze = entry.getValue();
      blockTag.putInt("X", pos.getX());
      blockTag.putInt("Y", pos.getY());
      blockTag.putInt("Z", pos.getZ());
      blockTag.putUUID("Owner", bze.totemData.getOwner());
      blockTag.putInt("Radius", bze.totemData.getRadius());
      blockTag.putString("Type", bze.totemData.getType());
      blockTag.putLong("Time", bze.placedTime);
      placedInZoneList.add(blockTag);
    }
    tag.put("PlacedBlocksInZone", placedInZoneList);

    ListTag outZoneList = new ListTag();
    for (Map.Entry<BlockPos, Long> entry : placedBlocksOutZone.entrySet()) {
      CompoundTag entryTag = new CompoundTag();
      BlockPos pos = entry.getKey();
      entryTag.putInt("X", pos.getX());
      entryTag.putInt("Y", pos.getY());
      entryTag.putInt("Z", pos.getZ());
      entryTag.putLong("Time", entry.getValue());
      outZoneList.add(entryTag);
    }
    tag.put("PlacedBlocksOutZone", outZoneList);

    ListTag whitelistTag = new ListTag();
    whitelistPlayers.forEach((owner, members) -> {
      CompoundTag entryTag = new CompoundTag();
      entryTag.putUUID("Owner", owner);
      ListTag membersTag = new ListTag();
      members.forEach(uuid -> membersTag.add(NbtUtils.createUUID(uuid)));
      entryTag.put("Members", membersTag);
      whitelistTag.add(entryTag);
    });
    tag.put("Whitelist", whitelistTag);

    ListTag blacklistTag = new ListTag();
    blacklistPlayers.forEach((owner, blocked) -> {
      CompoundTag entryTag = new CompoundTag();
      entryTag.putUUID("Owner", owner);
      ListTag blockedTag = new ListTag();
      blocked.forEach(uuid -> blockedTag.add(NbtUtils.createUUID(uuid)));
      entryTag.put("Blacklist", blockedTag);
      blacklistTag.add(entryTag);
    });
    tag.put("Blacklist", blacklistTag);

    ListTag smallTotemsList = new ListTag();
    for (Map.Entry<BlockPos, Long> entry : placedSmallTotems.entrySet()) {
      CompoundTag totemTag = new CompoundTag();
      totemTag.putLong("pos", entry.getKey().asLong());
      totemTag.putLong("time", entry.getValue());
      smallTotemsList.add(totemTag);
    }
    tag.put("PlacedSmallTotems", smallTotemsList);
    return tag;

  }

}
