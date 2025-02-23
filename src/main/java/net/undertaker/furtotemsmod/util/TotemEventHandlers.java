package net.undertaker.furtotemsmod.util;

import java.util.*;
import java.util.stream.Collectors;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.level.PistonEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.undertaker.furtotemsmod.FurConfig;
import net.undertaker.furtotemsmod.FurTotemsMod;
import net.undertaker.furtotemsmod.attributes.ModAttributes;
import net.undertaker.furtotemsmod.blocks.ModBlocks;
import net.undertaker.furtotemsmod.blocks.blockentity.UpgradableTotemBlockEntity;
import net.undertaker.furtotemsmod.blocks.custom.SmallTotemBlock;
import net.undertaker.furtotemsmod.blocks.custom.UpgradableTotemBlock;
import net.undertaker.furtotemsmod.data.TotemSavedData;
import net.undertaker.furtotemsmod.items.ModItems;
import net.undertaker.furtotemsmod.networking.ModNetworking;
import net.undertaker.furtotemsmod.networking.packets.SyncBlacklistPacket;
import net.undertaker.furtotemsmod.networking.packets.SyncTotemsPacket;
import net.undertaker.furtotemsmod.networking.packets.SyncWhitelistPacket;

@Mod.EventBusSubscriber(modid = FurTotemsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TotemEventHandlers {

  @SubscribeEvent
  public static void onBlockBreakNearTotem(BlockEvent.BreakEvent event) {
    if (event.getLevel().isClientSide()) return;
    if (!FurConfig.ENABLE_BLOCK_BREAK_EVENT.get()) return;
    if (event.getPlayer().isCreative() && FurConfig.CREATIVE_IGNORE_TOTEMS.get()) return;
    ServerLevel serverLevel = (ServerLevel) event.getLevel();
    TotemSavedData data = TotemSavedData.get(serverLevel);
    BlockPos pos = event.getPos();
    BlockPos nearestTotem = data.getNearestTotem(pos);

    if (nearestTotem != null) {
      TotemSavedData.TotemData totemData = data.getTotemData(nearestTotem);

      if (totemData != null
              && !data.isPlayerMember(totemData.getOwner(), event.getPlayer().getUUID())
              && nearestTotem.distSqr(pos) <= Math.pow(totemData.getRadius(), 2)) {

        event.setCanceled(true);
        event
            .getPlayer()
            .displayClientMessage(
                Component.translatable("message.furtotemsmod.block_protected_by_totem"), true);
      }
    }
  }

  @SubscribeEvent
  public static void onBlockPlaceNearTotem(BlockEvent.EntityPlaceEvent event) {
    if (event.getEntity().level().isClientSide()) return;
    if (!FurConfig.ENABLE_BLOCK_PLACE_EVENT.get()) return;
    if (!(event.getEntity() instanceof Player player)) return;

    if (player.isCreative() && FurConfig.CREATIVE_IGNORE_TOTEMS.get()) return;
    ServerLevel serverLevel = (ServerLevel) event.getEntity().level();
    TotemSavedData data = TotemSavedData.get(serverLevel);
    BlockPos pos = event.getPos();
    BlockPos nearestTotem = data.getNearestTotem(pos);

    if (nearestTotem != null) {
      TotemSavedData.TotemData totemData = data.getTotemData(nearestTotem);

      if (totemData != null
          && !(event.getEntity() instanceof Player && data.isPlayerMember(totemData.getOwner(), player.getUUID()))
          && nearestTotem.distSqr(pos) <= Math.pow(totemData.getRadius(), 2)) {
        event.setCanceled(true);
        if (event.getEntity() instanceof Player) {
          player.displayClientMessage(
              Component.translatable("message.furtotemsmod.block_protected_by_totem"), true);
        }
      }
    }
  }

  @SubscribeEvent
  public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
    if (FurConfig.DISABLE_BLOCK_INTERACTION.get() == false) return;
    if (event.getLevel().isClientSide()) return;
    if (event.getEntity().isCreative() && FurConfig.CREATIVE_IGNORE_TOTEMS.get()) return;
    ServerLevel level = (ServerLevel) event.getLevel();
    Player player = event.getEntity();
    BlockPos pos = event.getPos();

    TotemSavedData data = TotemSavedData.get(level);

    if (data.isPositionProtected(pos, player.getUUID())) {
      event.setCanceled(true);
      player.displayClientMessage(
          Component.translatable("message.furtotemsmod.block_protected_by_totem"), true);
    }
  }

  @SubscribeEvent
  public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
    if (FurConfig.DISABLE_ENTITY_INTERACTION.get() == false) return;
    if (event.getLevel().isClientSide()) return;
    if (event.getEntity().isCreative() && FurConfig.CREATIVE_IGNORE_TOTEMS.get()) return;
    ServerLevel level = (ServerLevel) event.getEntity().level();
    Player player = event.getEntity();
    BlockPos pos = event.getPos();

    TotemSavedData data = TotemSavedData.get(level);

    if (data.isPositionProtected(pos, player.getUUID())) {
      event.setCanceled(true);
      player.displayClientMessage(
          Component.translatable("message.furtotemsmod.entity_protected_by_totem"), true);
    }
  }

  @SubscribeEvent
  public static void onPistonExtend(PistonEvent.Pre event) {
    if (FurConfig.DISABLE_PISTON.get() == false) return;
    if(event.getLevel().isClientSide()) return;
    PistonStructureResolver resolver = event.getStructureHelper();
    if (resolver == null) return;

    resolver.resolve();
    List<BlockPos> movedBlocks = resolver.getToPush();
    if (movedBlocks.isEmpty()) return;

    ServerLevel level = (ServerLevel) event.getLevel();
    TotemSavedData data = TotemSavedData.get(level);

    for (BlockPos blockPos : movedBlocks) {
      Direction direction = event.getDirection();
      BlockPos targetPos = blockPos.relative(direction);

      BlockPos nearestTotem = data.getNearestTotem(targetPos);
      if (nearestTotem == null) continue;

      TotemSavedData.TotemData totemData = data.getTotemData(nearestTotem);
      if (totemData != null
          && nearestTotem.distSqr(targetPos) <= Math.pow(totemData.getRadius(), 2)) {
        event.setCanceled(true);
        return;
      }
    }
  }

  private static final List<FallingBlockEntity> fallingBlocks = new ArrayList<>();

  @SubscribeEvent
  public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
    if (FurConfig.DISABLE_FALLING_ENTITIES.get() == false) return;
    if (event.getLevel().isClientSide) return;
    if (event.getEntity() instanceof FallingBlockEntity fallingBlock) {
      fallingBlocks.add(fallingBlock);
    }
  }

  @SubscribeEvent(priority = EventPriority.LOW)
  public static void onWorldTick(TickEvent.LevelTickEvent event) {
    if (FurConfig.DISABLE_FALLING_ENTITIES.get() == false) return;
    if (event.level.isClientSide || event.phase != TickEvent.Phase.END) return;
    if (event.level.getServer().getTickCount() % 10 != 0) return;
    ServerLevel level = (ServerLevel) event.level;
    TotemSavedData data = TotemSavedData.get(level);

    List<FallingBlockEntity> toRemove = new ArrayList<>();

    for (FallingBlockEntity fallingBlock : fallingBlocks) {
      if (!fallingBlock.isAlive()) {
        toRemove.add(fallingBlock);
        continue;
      }

      BlockPos entityPos = fallingBlock.blockPosition();
      BlockPos nearestTotem = data.getNearestTotem(entityPos);

      if (nearestTotem != null) {
        TotemSavedData.TotemData totemData = data.getTotemData(nearestTotem);
        if (totemData != null
            && nearestTotem.distSqr(entityPos) <= Math.pow(totemData.getRadius(), 2)) {
          fallingBlock.discard();
          toRemove.add(fallingBlock);
        }
      }
    }

    fallingBlocks.removeAll(toRemove);
  }

  @SubscribeEvent
  public static void mobSpawnEvent(MobSpawnEvent.SpawnPlacementCheck event) {
    if (FurConfig.PREVENT_MOB_SPAWN.get() == false) return;
    if (event.getLevel().isClientSide()) return;
    if (event.getSpawnType() == MobSpawnType.SPAWNER) return;

    ServerLevel level = event.getLevel().getLevel();
    BlockPos pos = event.getPos();
    TotemSavedData data = TotemSavedData.get(level);
    BlockPos nearestTotem = data.getNearestTotem(pos);

    if (nearestTotem != null) {
      TotemSavedData.TotemData totemData = data.getTotemData(nearestTotem);
      if (totemData != null && nearestTotem.distSqr(pos) <= Math.pow(totemData.getRadius(), 2)) {
        event.setResult(Event.Result.DENY);
      }
    }
  }

  @SubscribeEvent
  public static void onHangingEntityAttack(AttackEntityEvent event) {
    if (FurConfig.BREAKING_HANGING_ENTITIES.get() == false) return;
    if (event.getEntity().level().isClientSide()) return;
    if (event.getEntity().isCreative() && FurConfig.CREATIVE_IGNORE_TOTEMS.get()) return;
    if (event.getTarget() instanceof HangingEntity) {
      ServerLevel level = (ServerLevel) event.getEntity().level();
      Player player = event.getEntity();
      BlockPos pos = event.getTarget().blockPosition();

      TotemSavedData data = TotemSavedData.get(level);

      if (data.isPositionProtected(pos, player.getUUID())) {
        event.setCanceled(true);
        player.displayClientMessage(
            Component.translatable("message.furtotemsmod.entity_protected_by_totem"), true);
      }
    }
  }

  @SubscribeEvent
  public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
    if (FurConfig.DISABLE_FIRE_SPREAD.get() == false) return;
    if (event.getLevel().isClientSide()) return;

    ServerLevel level = (ServerLevel) event.getLevel();
    BlockPos pos = event.getPos();

    if (level.getBlockState(pos).getBlock() == Blocks.FIRE) {
      TotemSavedData data = TotemSavedData.get(level);
      BlockPos nearestTotem = data.getNearestTotem(pos);

      if (nearestTotem != null) {
        TotemSavedData.TotemData totemData = data.getTotemData(nearestTotem);
        if (totemData != null && nearestTotem.distSqr(pos) <= Math.pow(totemData.getRadius(), 2)) {
          level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
      }
    }
  }

  private static void disablePvP(LivingEvent event, UUID attackerUUID) {
    if (!FurConfig.DISABLE_PLAYER_PVP.get()) return;
    if (event.getEntity().level().isClientSide()) return;
    if (!(event.getEntity() instanceof Player player)) return;
    if (player.isCreative() && FurConfig.CREATIVE_IGNORE_TOTEMS.get()) return;
    ServerLevel level = (ServerLevel) event.getEntity().level();
    BlockPos pos = event.getEntity().blockPosition();
    TotemSavedData data = TotemSavedData.get(level);

    if (data.isPositionProtected(pos, attackerUUID)) {
      event.setCanceled(true);
      if (attackerUUID != null) {
        Player attacker = level.getPlayerByUUID(attackerUUID);
        if (attacker != null) {
          attacker.displayClientMessage(
              Component.translatable("message.furtotemsmod.player_protected_by_totem"), true);
        }
      }
    }
  }

  private static void protectLivingEntity(LivingEvent event, UUID attackerUUID) {
    if (FurConfig.DISABLE_MOB_DAMAGING.get() == false) return;
    if (event.getEntity().level().isClientSide()) return;
    if (event.getEntity() instanceof Player player) return;
    ServerLevel level = (ServerLevel) event.getEntity().level();
    BlockPos pos = event.getEntity().blockPosition();
    TotemSavedData data = TotemSavedData.get(level);

    if (data.isPositionProtected(pos, attackerUUID)) {
      event.setCanceled(true);
      if (attackerUUID != null) {
        Player attacker = level.getPlayerByUUID(attackerUUID);
        if (attacker != null) {
          attacker.displayClientMessage(
              Component.translatable("message.furtotemsmod.entity_protected_by_totem"), true);
        }
      }
    }
  }

  @SubscribeEvent
  public static void onLivingAttack(LivingAttackEvent event) {
    if (event.getSource().getEntity() instanceof Player attacker) {
      if(event.getEntity().level().isClientSide) return;

      if (attacker.isCreative() && FurConfig.CREATIVE_IGNORE_TOTEMS.get()) return;
      if (event.getEntity() instanceof Monster) return;
      protectLivingEntity(event, attacker.getUUID());
      disablePvP(event, attacker.getUUID());
    }
  }

  @SubscribeEvent
  public static void onLivingDamage(LivingDamageEvent event) {
    if (event.getSource().getEntity() instanceof Player attacker) {
      if(event.getEntity().level().isClientSide) return;
      if (attacker.isCreative() && FurConfig.CREATIVE_IGNORE_TOTEMS.get()) return;
      if (event.getEntity() instanceof Monster) return;
      protectLivingEntity(event, attacker.getUUID());
      disablePvP(event, attacker.getUUID());
    }
  }

  @SubscribeEvent
  public void onMobGrief(EntityMobGriefingEvent event) {
    if (FurConfig.DISABLE_MOB_GRIEF.get() == false) return;
    if (event.getEntity().level().isClientSide()) return;
    Entity entity = event.getEntity();

    if (entity instanceof WitherBoss || entity instanceof EnderMan || entity instanceof Creeper) {
      ServerLevel level = (ServerLevel) entity.level();
      BlockPos pos = entity.blockPosition();

      TotemSavedData data = TotemSavedData.get(level);

      if (data.isPositionProtected(pos, null)) {
        event.setResult(Event.Result.DENY);
      }
    }
  }

  @SubscribeEvent
  public static void onItemPickup(EntityItemPickupEvent event) {
    if (FurConfig.DISABLE_ITEM_PICKUP.get() == false) return;
    if (event.getEntity().level().isClientSide()) return;

    Player player = event.getEntity();
    BlockPos itemPos = event.getItem().blockPosition();
    if (player.isCreative() && FurConfig.CREATIVE_IGNORE_TOTEMS.get()) return;
    ServerLevel level = (ServerLevel) player.level();
    TotemSavedData data = TotemSavedData.get(level);

    BlockPos nearestTotem = data.getNearestTotem(itemPos);
    if (nearestTotem != null) {
      TotemSavedData.TotemData totemData = data.getTotemData(nearestTotem);
      if (totemData != null
          && nearestTotem.distSqr(itemPos) <= Math.pow(totemData.getRadius(), 2)
          && !player.getUUID().equals(totemData.getOwner())) {
        event.setCanceled(true);
        player.displayClientMessage(
            Component.translatable("message.furtotemsmod.item_protected_by_totem"), true);
      }
    }
  }

  @SubscribeEvent
  public static void onItemToss(ItemTossEvent event) {
    if (FurConfig.DISABLE_ITEM_TOSS.get() == false) return;
    if (event.getPlayer().level().isClientSide()) return;

    Player player = event.getPlayer();
    BlockPos dropPos = event.getEntity().blockPosition();

    if (player.isCreative() && FurConfig.CREATIVE_IGNORE_TOTEMS.get()) return;
    ServerLevel level = (ServerLevel) player.level();
    TotemSavedData data = TotemSavedData.get(level);

    BlockPos nearestTotem = data.getNearestTotem(dropPos);
    if (nearestTotem != null) {
      TotemSavedData.TotemData totemData = data.getTotemData(nearestTotem);
      if (totemData != null
          && nearestTotem.distSqr(dropPos) <= Math.pow(totemData.getRadius(), 2)
          && !player.getUUID().equals(totemData.getOwner())) {
        event.setCanceled(true);
        player.displayClientMessage(
            Component.translatable("message.furtotemsmod.cannot_throw_item_in_totem_zone"), true);
      }
    }
  }

  @SubscribeEvent
  public static void explosion(ExplosionEvent.Detonate event) {
    if (FurConfig.DISABLE_EXPLOSION_BLOCKS.get() == false) return;
    if(event.getLevel().isClientSide()) return;
    ServerLevel level = (ServerLevel) event.getLevel();
    TotemSavedData data = TotemSavedData.get(level);

    List<BlockPos> explosionBlocks = event.getExplosion().getToBlow();
    explosionBlocks.removeIf(pos -> data.getNearestTotem(pos) != null);
  }

  @SubscribeEvent
  public static void livingTick(TickEvent.PlayerTickEvent event) {
    if (event.player.level().isClientSide()) return;
    if (event.phase != TickEvent.Phase.END) return;
    if (!FurConfig.ENABLE_PLAYER_RESTRICT.get()) return;

    Player player = event.player;
    if (player.isCreative() && FurConfig.CREATIVE_IGNORE_TOTEMS.get()) return;
    ServerLevel level = (ServerLevel) player.level();
    TotemSavedData data = TotemSavedData.get(level);

    BlockPos playerPos = player.blockPosition();
    BlockPos nearestTotem = data.getNearestTotem(playerPos);

    if (nearestTotem != null) {
      TotemSavedData.TotemData totemData = data.getTotemData(nearestTotem);
      if (totemData == null) return;

      double radius = totemData.getRadius();

      if (player.getUUID().equals(totemData.getOwner()) || data.isPlayerMember(totemData.getOwner(), player.getUUID()))
        return;

      if (data.isBlacklisted(totemData.getOwner(), player.getUUID()) && !player.isCreative() && !player.isSpectator())  {
        teleportPlayerOutOfRadius(player, nearestTotem, radius);
        player.displayClientMessage(
                Component.translatable("message.furtotemsmod.in_blacklist"), true);
      }
    }
  }

  private static void teleportPlayerOutOfRadius(Player player, BlockPos totemPos, double radius) {
    BlockPos playerPos = player.blockPosition();
    double angle =
        Math.atan2(playerPos.getZ() - totemPos.getZ(), playerPos.getX() - totemPos.getX());
    double safeX = totemPos.getX() + (radius + 1) * Math.cos(angle);
    double safeZ = totemPos.getZ() + (radius + 1) * Math.sin(angle);
    BlockPos safePos = new BlockPos((int) safeX, playerPos.getY(), (int) safeZ);

    player.teleportTo(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
  }

  @SubscribeEvent
  public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
    if(event.player.level().isClientSide()) return;
    if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player))
      return;
    if (player.level().getServer() == null) return;
    if (player.level().getServer().getTickCount() % 20 != 0) return;
    ServerLevel serverLevel = (ServerLevel) player.level();
    TotemSavedData data = TotemSavedData.get(serverLevel);
    ModNetworking.sendToPlayer(new SyncTotemsPacket(data.getTotemDataMap()), player);

  }

  @SubscribeEvent
  public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
    if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;
    if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
    TotemSavedData data = TotemSavedData.get(serverLevel);
    ModNetworking.sendToPlayer(new SyncTotemsPacket(data.getTotemDataMap()), serverPlayer);
    ModNetworking.sendToPlayer(new SyncWhitelistPacket(data.getWhitelistPlayers()), serverPlayer);
    ModNetworking.sendToPlayer(new SyncBlacklistPacket(data.getBlacklistPlayers()), serverPlayer);
  }

  private static final Map<UUID, Map<String, Set<BlockPos>>> playerTotemState = new HashMap<>();

  @SubscribeEvent
  public static void onPlayerTick1(TickEvent.PlayerTickEvent event) {
    if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

    Player player = event.player;
    ServerLevel level = (ServerLevel) player.level();
    TotemSavedData data = TotemSavedData.get(level);

    Map<String, Set<BlockPos>> currentZonesByOwner =
        groupZonesByOwner(
            data.getTotemsInRadius(player.blockPosition(), player.getUUID()), data, level);

    Map<String, Set<BlockPos>> previousZonesByOwner =
        playerTotemState.getOrDefault(player.getUUID(), new HashMap<>());

    for (Map.Entry<String, Set<BlockPos>> entry : currentZonesByOwner.entrySet()) {
      String owner = entry.getKey();
      Set<BlockPos> currentPositions = entry.getValue();
      Set<BlockPos> previousPositions = previousZonesByOwner.getOrDefault(owner, new HashSet<>());

      Set<BlockPos> enteredPositions = new HashSet<>(currentPositions);
      enteredPositions.removeAll(previousPositions);

      if (!enteredPositions.isEmpty() && !isInAnyZoneOfOwner(previousZonesByOwner, owner)) {
        player.displayClientMessage(
            Component.literal(
                    Component.translatable("message.furtotemsmod.enter_protection").getString()
                        + owner)
                .withStyle(ChatFormatting.GREEN),
            true);
      }
    }

    for (Map.Entry<String, Set<BlockPos>> entry : previousZonesByOwner.entrySet()) {
      String owner = entry.getKey();
      Set<BlockPos> previousPositions = entry.getValue();
      Set<BlockPos> currentPositions = currentZonesByOwner.getOrDefault(owner, new HashSet<>());

      Set<BlockPos> exitedPositions = new HashSet<>(previousPositions);
      exitedPositions.removeAll(currentPositions);

      if (!exitedPositions.isEmpty() && !isInAnyZoneOfOwner(currentZonesByOwner, owner)) {
        player.displayClientMessage(
            Component.literal(
                    Component.translatable("message.furtotemsmod.leave_protection").getString()
                        + owner)
                .withStyle(ChatFormatting.YELLOW),
            true);
      }
    }

    playerTotemState.put(player.getUUID(), currentZonesByOwner);
  }

  private static Map<String, Set<BlockPos>> groupZonesByOwner(
      Set<BlockPos> positions, TotemSavedData data, ServerLevel level) {
    Map<String, Set<BlockPos>> zonesByOwner = new HashMap<>();
    for (BlockPos pos : positions) {
      TotemSavedData.TotemData totemData = data.getTotemData(pos);
      String owner = totemData != null ? totemData.getOwnerName(totemData.getOwner()) : "None";
      zonesByOwner.computeIfAbsent(owner, k -> new HashSet<>()).add(pos);
    }
    return zonesByOwner;
  }

  private static boolean isInAnyZoneOfOwner(Map<String, Set<BlockPos>> zonesByOwner, String owner) {
    Set<BlockPos> ownerZones = zonesByOwner.getOrDefault(owner, new HashSet<>());
    return !ownerZones.isEmpty();
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
    if (event.getLevel().isClientSide()) return;
    if (!(event.getEntity() instanceof Player player)) return;

    ServerLevel serverLevel = (ServerLevel) event.getLevel();
    TotemSavedData data = TotemSavedData.get(serverLevel);

    BlockState placedBlock = event.getPlacedBlock();
    BlockPos pos = event.getPos();
    if (placedBlock.getBlock() instanceof SmallTotemBlock
        || placedBlock.getBlock() instanceof UpgradableTotemBlock) {
      if (FurConfig.PREVENT_TOTEM_NEAR_SPAWNER.get()) {
        int radius = FurConfig.SPAWNER_CHECK_RADIUS.get();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
          for (int dy = -radius; dy <= radius; dy++) {
            for (int dz = -radius; dz <= radius; dz++) {
              mutablePos.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
              if (serverLevel.getBlockState(mutablePos).getBlock() instanceof SpawnerBlock) {
                player.displayClientMessage(
                        Component.translatable("message.furtotemsmod.nearby_spawner"), true);
                event.setCanceled(true);
                return;
              }
            }
          }
        }
      }
      double maxSmallTotems = player.getAttributeValue(ModAttributes.SMALL_TOTEM_COUNT.get());
      double maxBigTotems = player.getAttributeValue(ModAttributes.BIG_TOTEM_COUNT.get());

      int radius = 0;
      if (placedBlock.getBlock() instanceof SmallTotemBlock) {
        radius = FurConfig.SMALL_TOTEM_RADIUS.get();
      } else if (placedBlock.getBlock() instanceof UpgradableTotemBlock) {
        radius = UpgradableTotemBlockEntity.MaterialType.COPPER_BLOCK.getRadius();
      }
      if (data.isOverlapping(pos, radius, player.getUUID())) {
        event.setCanceled(true);
        player.displayClientMessage(
            Component.translatable("message.furtotemsmod.totem_overlaps_another_zone"), true);
        return;
      }

      if (placedBlock.getBlock() instanceof SmallTotemBlock) {
        data.addSmallTotemWithLimit(serverLevel, pos, serverLevel.getGameTime(), (int) maxSmallTotems);
        data.addTotem(serverLevel, pos, player.getUUID(), radius, "Small");

      }  else if (placedBlock.getBlock() instanceof UpgradableTotemBlock) {
        int currentBigTotems = data.getPlayerTotemCount(player.getUUID()).getBigTotems();
        if (currentBigTotems > maxBigTotems) {
          event.setCanceled(true);
          player.displayClientMessage(
              Component.translatable("message.furtotemsmod.too_many_large_totems"), true);
          return;
        }
      }
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public static void onBlockDestroy(BlockEvent.BreakEvent event) {
    if (event.getLevel().isClientSide()) return;

    ServerLevel serverLevel = (ServerLevel) event.getLevel();
    TotemSavedData data = TotemSavedData.get(serverLevel);
    BlockPos pos = event.getPos();
    BlockState state = event.getState();

    if (state.getBlock() instanceof SmallTotemBlock
        || state.getBlock() instanceof UpgradableTotemBlock) {
      TotemSavedData.TotemData totemData = data.getTotemData(pos);

      if (totemData != null) {
        UUID owner = totemData.getOwner();
        ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(owner);

        if (!event.getPlayer().getUUID().equals(owner)) {
          event.setCanceled(true);
          event
              .getPlayer()
              .displayClientMessage(
                  Component.translatable("message.furtotemsmod.cannot_destroy_totem"), true);
          return;
        }
        data.removeTotem(serverLevel, pos);
        if (player != null) {
          player.displayClientMessage(
              Component.translatable("message.furtotemsmod.totem_destroyed"), true);
        }
      }
    }
  }

  public static Set<Block> getAllowedBlocksNearSmallTotem() {
    Set<Block> allowedBlocks = FurConfig.ALLOWED_NEAR_SMALL_TOTEM.get().stream()
            .map(blockId -> ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId)))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    allowedBlocks.add(ModBlocks.SMALL_TOTEM.get());
    allowedBlocks.add(ModBlocks.UPGRADABLE_TOTEM.get());
    allowedBlocks.add(ModBlocks.FOX_PLUSHIE.get());

    return allowedBlocks;
  }

  @SubscribeEvent
  public static void onBlockPlaceNearSmallTotem(BlockEvent.EntityPlaceEvent event) {
    if (event.getLevel().isClientSide()) return;

    Level level = (Level) event.getLevel();
    BlockPos placedPos = event.getPos();
    Block placedBlock = event.getPlacedBlock().getBlock();

    Set<Block> allowedBlocks = getAllowedBlocksNearSmallTotem();

    if (isNearTotem(level, placedPos)) {
      if (!allowedBlocks.contains(placedBlock)) {
        event.setCanceled(true);
        if (event.getEntity() instanceof Player player) {
          player.displayClientMessage(Component.translatable("message.furtotemsmod.invalid_block_near_totem"), true);
        }
      }
    }
  }

  private static boolean isNearTotem(Level level, BlockPos pos) {
    if (level instanceof ServerLevel serverLevel) {
      TotemSavedData data = TotemSavedData.get(serverLevel);

      for (Map.Entry<BlockPos, TotemSavedData.TotemData> entry : data.getTotemDataMap().entrySet()) {
        BlockPos totemPos = entry.getKey();
        TotemSavedData.TotemData totemData = entry.getValue();

        if ("Small".equals(totemData.getType())) {
          int radius = totemData.getRadius();
          if (totemPos.distSqr(pos) <= Math.pow(radius, 2)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public static void worldLoad(ServerStartedEvent event) {
    for(ServerLevel level : event.getServer().getAllLevels()){
      ServerLevelAccessor.setServerLevel(level);
      PlacedBlockManager.restoreDelayedTasks(level);
    }
  }

  @SubscribeEvent
  public static void worldStop(ServerStoppingEvent event){
    if(FurConfig.KEEP_BLOCKS_AFTER_RESTART.get()){
      for(ServerLevel level : event.getServer().getAllLevels()){
        TotemSavedData data = TotemSavedData.get(level);
        data.placedBlocksInZone.clear();
      }
    }
  }
}
