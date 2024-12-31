package net.undertaker.furtotemsmod.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
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
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.level.PistonEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.undertaker.furtotemsmod.Config;
import net.undertaker.furtotemsmod.FurTotemsMod;
import net.undertaker.furtotemsmod.attributes.ModAttributes;
import net.undertaker.furtotemsmod.blocks.blockentity.UpgradableTotemBlockEntity;
import net.undertaker.furtotemsmod.blocks.custom.SmallTotemBlock;
import net.undertaker.furtotemsmod.blocks.custom.UpgradableTotemBlock;

@Mod.EventBusSubscriber(modid = FurTotemsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TotemEventHandlers {

  @SubscribeEvent
  public static void onBlockBreakNearTotem(BlockEvent.BreakEvent event) {
    if (event.getLevel().isClientSide()) return;
    if (!Config.ENABLE_BLOCK_BREAK_EVENT.get()) return;

    ServerLevel serverLevel = (ServerLevel) event.getLevel();
    TotemSavedData data = TotemSavedData.get(serverLevel);
    BlockPos pos = event.getPos();
    BlockPos nearestTotem = data.getNearestTotem(pos);

    if (nearestTotem != null) {
      TotemSavedData.TotemData totemData = data.getTotemData(nearestTotem);

      if (totemData != null
          && !totemData.isMember(event.getPlayer().getUUID())
          && nearestTotem.distSqr(pos) <= Math.pow(totemData.getRadius(), 2)) {

        event.setCanceled(true);
        event
            .getPlayer()
            .displayClientMessage(Component.literal("Этот блок защищён тотемом!"), true);
      }
    }
  }

  @SubscribeEvent
  public static void onBlockPlaceNearTotem(BlockEvent.EntityPlaceEvent event) {
    if (event.getEntity().getLevel().isClientSide()) return;
    if (!Config.ENABLE_BLOCK_PLACE_EVENT.get()) return;

    ServerLevel serverLevel = (ServerLevel) event.getEntity().getLevel();
    TotemSavedData data = TotemSavedData.get(serverLevel);
    BlockPos pos = event.getPos();
    BlockPos nearestTotem = data.getNearestTotem(pos);

    if (nearestTotem != null) {
      TotemSavedData.TotemData totemData = data.getTotemData(nearestTotem);

      if (totemData != null
          && !(event.getEntity() instanceof Player player
              && player.getUUID().equals(totemData.getOwner()))
          && nearestTotem.distSqr(pos) <= Math.pow(totemData.getRadius(), 2)) {
        event.setCanceled(true);
        if (event.getEntity() instanceof Player player) {
          player.displayClientMessage(Component.literal("Этот блок защищён тотемом!"), true);
        }
      }
    }
  }

  @SubscribeEvent
  public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
    if (Config.DISABLE_BLOCK_INTERACTION.get() == false) return;
    if (event.getLevel().isClientSide()) return;
    ServerLevel level = (ServerLevel) event.getLevel();
    Player player = event.getEntity();
    BlockPos pos = event.getPos();

    TotemSavedData data = TotemSavedData.get(level);

    if (data.isPositionProtected(pos, player.getUUID())) {
      event.setCanceled(true);
      player.displayClientMessage(Component.literal("Этот блок защищён тотемом!"), true);
    }
  }

  @SubscribeEvent
  public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
    if (Config.DISABLE_ENTITY_INTERACTION.get() == false) return;
    if (event.getLevel().isClientSide()) return;
    ServerLevel level = (ServerLevel) event.getEntity().getLevel();
    Player player = event.getEntity();
    BlockPos pos = event.getPos();

    TotemSavedData data = TotemSavedData.get(level);

    if (data.isPositionProtected(pos, player.getUUID())) {
      event.setCanceled(true);
      player.displayClientMessage(Component.literal("Этот энтити защищён тотемом!"), true);
    }
  }

  @SubscribeEvent
  public static void onPistonExtend(PistonEvent.Pre event) {
    if (Config.DISABLE_PISTON.get() == false) return;
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
    if (Config.DISABLE_FALLING_ENTITIES.get() == false) return;
    if (event.getLevel().isClientSide) return;
    if (event.getEntity() instanceof FallingBlockEntity fallingBlock) {
      fallingBlocks.add(fallingBlock);
    }
  }

  @SubscribeEvent(priority = EventPriority.LOW)
  public static void onWorldTick(TickEvent.LevelTickEvent event) {
    if (Config.DISABLE_FALLING_ENTITIES.get() == false) return;
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
  public static void mobSpawnEvent(EntityJoinLevelEvent event) {
    if (Config.PREVENT_MOB_SPAWN.get() == false) return;
    if (event.getLevel().isClientSide) return;
    if (!(event.getEntity() instanceof Monster monster)) return;

    ServerLevel level = (ServerLevel) event.getLevel();
    BlockPos pos = monster.blockPosition();
    TotemSavedData data = TotemSavedData.get(level);
    BlockPos nearestTotem = data.getNearestTotem(pos);

    if (nearestTotem != null) {
      TotemSavedData.TotemData totemData = data.getTotemData(nearestTotem);
      if (totemData != null && nearestTotem.distSqr(pos) <= Math.pow(totemData.getRadius(), 2)) {
        event.setCanceled(true);
      }
    }
  }

  @SubscribeEvent
  public static void onHangingEntityAttack(AttackEntityEvent event) {
    if (Config.BREAKING_HANGING_ENTITIES.get() == false) return;
    if (event.getEntity().level.isClientSide()) return;

    if (event.getTarget() instanceof HangingEntity) {
      ServerLevel level = (ServerLevel) event.getEntity().getLevel();
      Player player = event.getEntity();
      BlockPos pos = event.getTarget().blockPosition();

      TotemSavedData data = TotemSavedData.get(level);

      if (data.isPositionProtected(pos, player.getUUID())) {
        event.setCanceled(true);
        player.displayClientMessage(Component.literal("Этот энтити защищён тотемом!"), true);
      }
    }
  }

  @SubscribeEvent
  public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
    if (Config.DISABLE_FIRE_SPREAD.get() == false) return;
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
          System.out.println("Fire removed within totem radius: " + pos);
        }
      }
    }
  }

  private static void protectLivingEntity(LivingEvent event, UUID attackerUUID) {
    if (Config.DISABLE_MOB_DAMAGING.get() == false) return;
    if (event.getEntity().level.isClientSide()) return;

    ServerLevel level = (ServerLevel) event.getEntity().getLevel();
    BlockPos pos = event.getEntity().blockPosition();
    TotemSavedData data = TotemSavedData.get(level);

    if (data.isPositionProtected(pos, attackerUUID)) {
      event.setCanceled(true);
      if (attackerUUID != null) {
        Player attacker = level.getPlayerByUUID(attackerUUID);
        if (attacker != null) {
          attacker.displayClientMessage(Component.literal("Этот энтити защищён тотемом!"), true);
        }
      }
    }
  }

  @SubscribeEvent
  public static void onLivingAttack(LivingAttackEvent event) {
    if (event.getSource().getEntity() instanceof Player attacker) {
      if (event.getEntity() instanceof Monster || event.getEntity() instanceof Player) return;
      protectLivingEntity(event, attacker.getUUID());
    }
  }

  @SubscribeEvent
  public static void onLivingDamage(LivingDamageEvent event) {
    if (event.getSource().getEntity() instanceof Player attacker) {
      if (event.getEntity() instanceof Monster || event.getEntity() instanceof Player) return;
      protectLivingEntity(event, attacker.getUUID());
    }
  }

  @SubscribeEvent
  public void onMobGrief(EntityMobGriefingEvent event) {
    if (Config.DISABLE_MOB_GRIEF.get() == false) return;
    if (event.getEntity().getLevel().isClientSide()) return;
    Entity entity = event.getEntity();

    if (entity instanceof WitherBoss || entity instanceof EnderMan || entity instanceof Creeper) {
      ServerLevel level = (ServerLevel) entity.getLevel();
      BlockPos pos = entity.blockPosition();

      TotemSavedData data = TotemSavedData.get(level);

      if (data.isPositionProtected(pos, null)) {
        event.setResult(Event.Result.DENY);
      }
    }
  }

  @SubscribeEvent
  public static void onItemPickup(EntityItemPickupEvent event) {
    if (Config.DISABLE_ITEM_PICKUP.get() == false) return;
    if (event.getEntity().level.isClientSide()) return;

    Player player = event.getEntity();
    BlockPos itemPos = event.getItem().blockPosition();

    ServerLevel level = (ServerLevel) player.getLevel();
    TotemSavedData data = TotemSavedData.get(level);

    BlockPos nearestTotem = data.getNearestTotem(itemPos);
    if (nearestTotem != null) {
      TotemSavedData.TotemData totemData = data.getTotemData(nearestTotem);
      if (totemData != null
          && nearestTotem.distSqr(itemPos) <= Math.pow(totemData.getRadius(), 2)
          && !player.getUUID().equals(totemData.getOwner())) {
        event.setCanceled(true);
        player.displayClientMessage(Component.literal("Этот предмет защищён тотемом!"), true);
      }
    }
  }

  @SubscribeEvent
  public static void onItemToss(ItemTossEvent event) {
    if (Config.DISABLE_ITEM_TOSS.get() == false) return;
    if (event.getPlayer().level.isClientSide()) return;

    Player player = event.getPlayer();
    BlockPos dropPos = event.getEntity().blockPosition();

    ServerLevel level = (ServerLevel) player.getLevel();
    TotemSavedData data = TotemSavedData.get(level);

    BlockPos nearestTotem = data.getNearestTotem(dropPos);
    if (nearestTotem != null) {
      TotemSavedData.TotemData totemData = data.getTotemData(nearestTotem);
      if (totemData != null
          && nearestTotem.distSqr(dropPos) <= Math.pow(totemData.getRadius(), 2)
          && !player.getUUID().equals(totemData.getOwner())) {
        event.setCanceled(true);
        player.displayClientMessage(
            Component.literal("Нельзя выбрасывать предметы в зоне тотема!"), true);
      }
    }
  }

  @SubscribeEvent
  public static void explosion(ExplosionEvent.Detonate event) {
    if (Config.DISABLE_EXPLOSION_BLOCKS.get() == false) return;
    ServerLevel level = (ServerLevel) event.getLevel();
    TotemSavedData data = TotemSavedData.get(level);

    List<BlockPos> explosionBlocks = event.getExplosion().getToBlow();
    explosionBlocks.removeIf(pos -> data.getNearestTotem(pos) != null);
  }

  @SubscribeEvent
  public static void livingTick(TickEvent.PlayerTickEvent event) {
    if (event.player.getLevel().isClientSide()) return;
    if (event.phase != TickEvent.Phase.END) return;
    if (!Config.ENABLE_PLAYER_RESTRICT.get()) return;

    Player player = event.player;
    ServerLevel level = (ServerLevel) player.getLevel();
    TotemSavedData data = TotemSavedData.get(level);

    BlockPos playerPos = player.blockPosition();
    BlockPos nearestTotem = data.getNearestTotem(playerPos);

    if (nearestTotem != null) {
      TotemSavedData.TotemData totemData = data.getTotemData(nearestTotem);
      if (totemData == null) return;

      double radius = totemData.getRadius();


      if (player.getUUID().equals(totemData.getOwner()) || totemData.isMember(player.getUUID())) return;


      if (totemData.isBlacklisted(player.getUUID())) {
        teleportPlayerOutOfRadius(player, nearestTotem, radius);
        player.displayClientMessage(Component.literal("Вы находитесь в чёрном списке!"), true);
      }
    }
  }

  private static void teleportPlayerOutOfRadius(Player player, BlockPos totemPos, double radius) {
    BlockPos playerPos = player.blockPosition();
    double angle = Math.atan2(playerPos.getZ() - totemPos.getZ(), playerPos.getX() - totemPos.getX());
    double safeX = totemPos.getX() + (radius + 1) * Math.cos(angle);
    double safeZ = totemPos.getZ() + (radius + 1) * Math.sin(angle);
    BlockPos safePos = new BlockPos(safeX, playerPos.getY(), safeZ);

    player.teleportTo(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
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

      double maxSmallTotems = player.getAttributeValue(ModAttributes.SMALL_TOTEM_COUNT.get());
      double maxBigTotems = player.getAttributeValue(ModAttributes.BIG_TOTEM_COUNT.get());

      int radius = 0;
      if (placedBlock.getBlock() instanceof SmallTotemBlock) {
        radius = Config.SMALL_TOTEM_RADIUS.get();
      } else if (placedBlock.getBlock() instanceof UpgradableTotemBlock) {
        radius = UpgradableTotemBlockEntity.MaterialType.COPPER.getRadius();
      }

      if (data.isOverlapping(pos, radius, player.getUUID())) {
        event.setCanceled(true);
        player.displayClientMessage(
            Component.literal("Этот тотем пересекается с другой зоной!"), true);
        return;
      }

      if (placedBlock.getBlock() instanceof SmallTotemBlock) {
        int currentSmallTotems = data.getPlayerTotemCount(player.getUUID()).getSmallTotems();
        if (currentSmallTotems >= maxSmallTotems) {
          event.setCanceled(true);
          player.displayClientMessage(
              Component.literal("Вы достигли лимита маленьких тотемов!"), true);
          return;
        }
        data.addTotem(pos, player.getUUID(), radius, "Small");
      } else if (placedBlock.getBlock() instanceof UpgradableTotemBlock) {
        int currentBigTotems = data.getPlayerTotemCount(player.getUUID()).getBigTotems();
        if (currentBigTotems >= maxBigTotems) {
          event.setCanceled(true);
          player.displayClientMessage(
              Component.literal("Вы достигли лимита больших тотемов!"), true);
          return;
        }

        UpgradableTotemBlockEntity totemEntity =
            (UpgradableTotemBlockEntity) serverLevel.getBlockEntity(pos);
        if (totemEntity != null) {
          data.addTotem(pos, player.getUUID(), totemEntity.getRadius(), "Upgradable");
        }
      }
    } else if (isCorrectBlock(placedBlock.getBlock().defaultBlockState())) {
      PlacedBlockManager.addBlock((Level) event.getLevel(), pos);
    }
  }

  private static boolean isCorrectBlock(BlockState state) {
    return !state.is(BlockTags.FIRE)
        && !state.is(Blocks.BEEHIVE)
        && !state.is(Blocks.BEE_NEST)
        && !state.is(Blocks.COMMAND_BLOCK)
        && !state.is(Blocks.CHAIN_COMMAND_BLOCK)
        && !state.is(Blocks.REPEATING_COMMAND_BLOCK)
        && !state.is(Blocks.STRUCTURE_BLOCK)
        && !state.is(Blocks.JIGSAW)
        && !state.is(Blocks.SPAWNER)
        && !state.is(BlockTags.SAPLINGS)
        && !state.is(Blocks.BEDROCK)
        && !state.is(Blocks.END_PORTAL_FRAME)
        && !state.is(Blocks.END_GATEWAY)
        && !state.is(Blocks.BARRIER)
        && !state.is(BlockTags.FLOWERS)
        && !state.is(Blocks.TORCH)
        && !state.is(Blocks.SOUL_TORCH)
        && !state.is(Blocks.WALL_TORCH)
        && !state.is(Blocks.LANTERN)
        && !state.is(Blocks.SOUL_WALL_TORCH)
        && !state.is(Blocks.GRASS_BLOCK)
        && !state.is(Blocks.TALL_GRASS)
        && !state.is(Blocks.WATER)
        && !state.is(Blocks.LAVA)
        && !state.is(Blocks.FARMLAND)
        && !state.is(BlockTags.CORAL_PLANTS)
        && !state.is(Blocks.FERN)
        && !state.is(Blocks.LARGE_FERN)
        && !state.is(Blocks.VINE)
        && !state.is(Blocks.DEAD_BUSH)
        && !state.is(Blocks.CACTUS)
        && !state.is(Blocks.SUGAR_CANE)
        && !state.is(Blocks.BAMBOO)
        && !state.is(Blocks.WHEAT)
        && !state.is(Blocks.CARROTS)
        && !state.is(Blocks.POTATOES)
        && !state.is(Blocks.BEETROOTS)
        && !state.is(Blocks.MELON_STEM)
        && !state.is(Blocks.PUMPKIN_STEM)
        && !state.is(Blocks.SWEET_BERRY_BUSH)
        && !state.is(Blocks.CAVE_VINES_PLANT)
        && !state.is(Blocks.CAVE_VINES)
        && !state.is(Blocks.KELP)
        && !state.is(Blocks.SEAGRASS)
        && !state.is(Blocks.TALL_SEAGRASS)
        && !state.is(Blocks.MUSHROOM_STEM)
        && !state.is(Blocks.BROWN_MUSHROOM)
        && !state.is(Blocks.RED_MUSHROOM);
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

        if ("Small".equals(totemData.getType())) {
          data.getPlayerTotemCount(owner).decrementSmallTotems();
        } else if ("Upgradable".equals(totemData.getType())) {
          data.getPlayerTotemCount(owner).decrementBigTotems();
        }

        data.removeTotem(pos);

        ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(owner);
        if (player != null) {
          player.displayClientMessage(Component.literal("Ваш тотем был уничтожен!"), true);
        }
      }
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public static void onServerStarting(RegisterCommandsEvent event) {
    FurTotemsCommands.register(event.getDispatcher());
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public static void worldLoad(LevelEvent.Load event) {
    if (event.getLevel() instanceof ServerLevel serverLevel) {
      TotemSavedData data = TotemSavedData.get(serverLevel);
      data.getTotemDataMap()
          .forEach(
              (pos, totemData) -> {
                FurTotemsMod.LOGGER.info(
                    "Загружен тотем на позиции {} с владельцем {}", pos, totemData.getOwner());
              });
    }
  }
}
