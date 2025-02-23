package net.undertaker.furtotemsmod;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = FurTotemsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FurConfig {
  public static final ForgeConfigSpec SERVER_CONFIG;

  public static final ForgeConfigSpec.BooleanValue ENABLE_BLOCK_BREAK_EVENT;
  public static final ForgeConfigSpec.BooleanValue ENABLE_PLAYER_RESTRICT;
  public static final ForgeConfigSpec.BooleanValue ENABLE_BLOCK_PLACE_EVENT;
  public static final ForgeConfigSpec.BooleanValue DISABLE_EXPLOSION_BLOCKS;
  public static final ForgeConfigSpec.BooleanValue DISABLE_PLAYER_PVP;

  public static final ForgeConfigSpec.BooleanValue DISABLE_BLOCK_INTERACTION;
  public static final ForgeConfigSpec.BooleanValue DISABLE_ENTITY_INTERACTION;
  public static final ForgeConfigSpec.BooleanValue DISABLE_PISTON;
  public static final ForgeConfigSpec.BooleanValue DISABLE_FALLING_ENTITIES;
  public static final ForgeConfigSpec.BooleanValue PREVENT_MOB_SPAWN;
  public static final ForgeConfigSpec.BooleanValue BREAKING_HANGING_ENTITIES;
  public static final ForgeConfigSpec.BooleanValue DISABLE_FIRE_SPREAD;
  public static final ForgeConfigSpec.BooleanValue DISABLE_MOB_DAMAGING;
  public static final ForgeConfigSpec.BooleanValue DISABLE_MOB_GRIEF;
  public static final ForgeConfigSpec.BooleanValue DISABLE_ITEM_PICKUP;
  public static final ForgeConfigSpec.BooleanValue DISABLE_ITEM_TOSS;
  public static final ForgeConfigSpec.BooleanValue PLAYER_TOTEM_DEBUFFS;
  public static final ForgeConfigSpec.BooleanValue CREATIVE_IGNORE_TOTEMS;
  public static final ForgeConfigSpec.BooleanValue KEEP_BLOCKS_AFTER_RESTART;

  public static final ForgeConfigSpec.BooleanValue PREVENT_TOTEM_NEAR_SPAWNER;
  public static final ForgeConfigSpec.IntValue SPAWNER_CHECK_RADIUS;

  public static final ForgeConfigSpec.IntValue SMALL_TOTEM_RADIUS;
  public static final ForgeConfigSpec.BooleanValue BLOCK_DESTROY_IN_ZONE;

  public static final ForgeConfigSpec.IntValue DELAY_BLOCK_DESTROY_IN_ZONE;

  public static final ForgeConfigSpec.IntValue UPGRADEABLE_TOTEM_COPPER_RADIUS;
  public static final ForgeConfigSpec.IntValue UPGRADEABLE_TOTEM_IRON_RADIUS;
  public static final ForgeConfigSpec.IntValue UPGRADEABLE_TOTEM_GOLD_RADIUS;
  public static final ForgeConfigSpec.IntValue UPGRADEABLE_TOTEM_DIAMOND_RADIUS;
  public static final ForgeConfigSpec.IntValue UPGRADEABLE_TOTEM_NETHERITE_RADIUS;

  public static final ForgeConfigSpec.IntValue BLOCK_BREAK_DELAY;

  public static final ForgeConfigSpec.ConfigValue<String> TOTEM_CONSUMED_BLOCK;
  public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ALLOWED_NEAR_SMALL_TOTEM;
  public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLIST_DECAY_BLOCKS;
  public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DISABLE_BLOCK_BREAK_DIMENSIONS;

  public static ForgeConfigSpec getConfig() {
    return SERVER_CONFIG;
  }

  public static void load() {


  }




  static {
    ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    builder.push("events");

    TOTEM_CONSUMED_BLOCK =
        builder
            .comment("The block ID required to place a Totem. Example: \"minecraft:copper_block\"")
            .define("totemConsumedBlock", "minecraft:copper_block");

    DISABLE_BLOCK_BREAK_DIMENSIONS = builder
            .comment("List of dimension IDs that ignore block breaking. Example: \"minecraft:nether\"")
            .defineList("disableBlockBreakDimensions", List.of(), obj -> obj instanceof String);
    BLACKLIST_DECAY_BLOCKS =
        builder
            .comment(
                "List of block IDs that can be placed without block decay. Example: \"minecraft:stone\"")
            .defineList(
                "blacklistDecayBlocks",
                List.of("minecraft:torch", "minecraft:spawner"),
                obj -> obj instanceof String);
    ALLOWED_NEAR_SMALL_TOTEM =
        builder
            .comment(
                "List of block IDs that can be placed near small totems. Example: \"minecraft:stone\"")
            .defineList(
                "allowedNearSmallTotem",
                List.of("minecraft:crafting_table", "minecraft:furnace", "minecraft:campfire"),
                obj -> obj instanceof String);
    PREVENT_TOTEM_NEAR_SPAWNER = builder
            .comment("Prevent placing Totems near Spawners (default - true)")
            .define("preventTotemNearSpawner", true);
    ENABLE_BLOCK_BREAK_EVENT =
        builder
            .comment("Enable block breaking protection near totems(default - true)")
            .define("enableBlockBreakEvent", true);
    PLAYER_TOTEM_DEBUFFS =
        builder
            .comment("Enable debuffs on player(mining fatigue and weakness) for 60 seconds after placing a totem(default - false)")
            .define("playerTotemDebuffs", false);
    BLOCK_DESTROY_IN_ZONE =
        builder
            .comment("Enable destroying all placed block in totem's zone, after totem is destroyed(default - true)")
            .define("blockDestroyInZone", true);
    CREATIVE_IGNORE_TOTEMS =
        builder
            .comment("Creative players can ignore totem zone(default - false)")
            .define("creativeIgnoreTotems", false);
    ENABLE_BLOCK_PLACE_EVENT =
        builder
            .comment("Enable block placing protection near totems(default - true)")
            .define("enableBlockPlaceEvent", true);
    ENABLE_PLAYER_RESTRICT =
        builder
            .comment("Enable player restriction in totem zones(default - true)")
            .define("enablePlayerRestrict", true);
    DISABLE_EXPLOSION_BLOCKS =
        builder
            .comment("Disable explosion breaking blocks in zones(default - true)")
            .define("disableExplosionBlocks", true);
    DISABLE_MOB_DAMAGING =
        builder
            .comment("Disable mob taking damage in zone(default - true)")
            .define("disableMobDamage", true);
    DISABLE_PLAYER_PVP =
        builder
            .comment("Disable players can PvP in zone(default - true)")
            .define("disablePlayerPvp", true);
    DISABLE_BLOCK_INTERACTION =
        builder
            .comment("Disable interaction with blocks(default - true)")
            .define("disableBlockInteraction", true);
    DISABLE_ENTITY_INTERACTION =
        builder
            .comment("Disable interaction with entities(default - true)")
            .define("disableEntityInteraction", true);
    DISABLE_PISTON =
        builder
            .comment("Disable piston moves block in zone(default - true)")
            .define("disablePiston", true);
    DISABLE_FALLING_ENTITIES =
        builder
            .comment("Removes every falling blocks in zone(default - true)")
            .define("disableFallingEntities", true);
    PREVENT_MOB_SPAWN =
        builder
            .comment("Prevent mob spawning in zone(default - true)")
            .define("preventMobSpawn", true);
    BREAKING_HANGING_ENTITIES =
        builder
            .comment("Disable players can break paintings/item frames with punch(default - true)")
            .define("breakingHangingEntities", true);
    DISABLE_FIRE_SPREAD =
        builder.comment("Disable fire spread(default - true)").define("disableFireSpread", true);
    DISABLE_MOB_GRIEF =
        builder
            .comment("Disable mob griefing in zone(default - true)")
            .comment("You should disable explosion protection as well.")
            .define("disableMobGrief", true);
    DISABLE_ITEM_TOSS =
        builder
            .comment("Disable players can toss items in zone(default - true)")
            .define("disableItemToss", true);
    DISABLE_ITEM_PICKUP =
        builder
            .comment("Disable players can pickup items in zone(default - true)")
            .define("disableItemPickup", true);
    KEEP_BLOCKS_AFTER_RESTART =
            builder.comment("If true keeps blocks placed in zone without breaking if totem was destroyed.")
                            .define("keepBlocksAfterRestart", false);

    builder.pop();

    builder.push("totems");

    SMALL_TOTEM_RADIUS =
        builder
            .comment("Radius of small totems(default - 5)")
            .defineInRange("smallTotemRadius", 5, 1, 128);

    UPGRADEABLE_TOTEM_COPPER_RADIUS =
        builder
            .comment("Radius of copper totem(default - 8)")
            .defineInRange("upgradeableCopperTotemRadius", 8, 1, 128);
    UPGRADEABLE_TOTEM_IRON_RADIUS =
        builder
            .comment("Radius of iron totem(default - 16)")
            .defineInRange("upgradeableIronTotemRadius", 16, 1, 128);
    UPGRADEABLE_TOTEM_GOLD_RADIUS =
        builder
            .comment("Radius of gold totem(default - 24)")
            .defineInRange("upgradeableGoldTotemRadius", 24, 1, 128);
    UPGRADEABLE_TOTEM_DIAMOND_RADIUS =
        builder
            .comment("Radius of diamond totem(default - 32)")
            .defineInRange("upgradeableDiamondTotemRadius", 32, 1, 128);
    UPGRADEABLE_TOTEM_NETHERITE_RADIUS =
        builder
            .comment("Radius of netherite totem(default - 40)")
            .defineInRange("upgradeableNetheriteTotemRadius", 40, 1, 128);
    BLOCK_BREAK_DELAY =
        builder
            .comment(
                "Time in seconds, after blocks in no-zone will be destroyed(default - 5)")
            .defineInRange("blockBreakDelay", 5, 1, 300);
    DELAY_BLOCK_DESTROY_IN_ZONE =
        builder
            .comment(
                "Time in seconds, after blocks that was in totem zone will be destroyed(default - 60)")
            .defineInRange("blockDestroyInZoneDelay", 60, 1, 600);
    SPAWNER_CHECK_RADIUS = builder
            .comment("Radius to check for Spawners around the Totem placement(default - 48)")
            .defineInRange("spawnerCheckRadius", 48, 16, 128);
    builder.pop();

    SERVER_CONFIG = builder.build();
  }
}
