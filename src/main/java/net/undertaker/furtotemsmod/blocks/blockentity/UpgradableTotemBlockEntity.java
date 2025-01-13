package net.undertaker.furtotemsmod.blocks.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.undertaker.furtotemsmod.FurConfig;
import net.undertaker.furtotemsmod.blocks.ModBlockEntities;
import net.undertaker.furtotemsmod.data.TotemSavedData;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;

public class UpgradableTotemBlockEntity extends BlockEntity {
  private UUID ownerUUID;
  private int radius;
  private MaterialType materialType;

  public enum MaterialType {
    COPPER_BLOCK(
            () -> FurConfig.UPGRADEABLE_TOTEM_COPPER_RADIUS.get(),
            "textures/totem/copper_totem_block.png"),
    IRON_BLOCK(
            () -> FurConfig.UPGRADEABLE_TOTEM_IRON_RADIUS.get(),
            "textures/totem/iron_totem_block.png"),
    GOLD_BLOCK(
            () -> FurConfig.UPGRADEABLE_TOTEM_GOLD_RADIUS.get(),
            "textures/totem/gold_totem_block.png"),
    DIAMOND_BLOCK(
            () -> FurConfig.UPGRADEABLE_TOTEM_DIAMOND_RADIUS.get(),
            "textures/totem/diamond_totem_block.png"),
    NETHERITE_BLOCK(
            () -> FurConfig.UPGRADEABLE_TOTEM_NETHERITE_RADIUS.get(),
            "textures/totem/netherite_totem_block.png");

    private final Supplier<Integer> radiusSupplier;
    private final String texture;

    MaterialType(Supplier<Integer> radiusSupplier, String texture) {
      this.radiusSupplier = radiusSupplier;
      this.texture = texture;

    }

    public int getRadius() {
      return radiusSupplier.get();
    }

    public String getTexture() {
      return texture;
    }

  }

  public UpgradableTotemBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.UPGRADABLE_TOTEM.get(), pos, state);
    this.materialType = MaterialType.COPPER_BLOCK;
    this.radius = materialType.getRadius();
  }

  public UUID getOwner() {
    return ownerUUID;
  }

  public void setOwner(UUID owner) {
    this.ownerUUID = owner;
  }

  public int getRadius() {
    return radius;
  }

  public MaterialType getMaterialType() {
    return materialType;
  }

  public void setMaterialType(MaterialType materialType) {
    this.materialType = materialType;
    setChanged();
  }

  public void upgrade(MaterialType newType) {
    this.materialType = newType;
    this.radius = newType.getRadius();
    setChanged();

    if (level instanceof ServerLevel serverLevel) {
      TotemSavedData data = TotemSavedData.get(serverLevel);
      data.updateTotem(this.getBlockPos(), this.ownerUUID, this.radius, "Upgradable");
    }
  }
  @Override
  public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
    return super.getUpdatePacket();
  }

  @Override
  public CompoundTag getUpdateTag() {
    CompoundTag tag = super.getUpdateTag();
    saveAdditional(tag);
    return tag;
  }
  @Override
  public void handleUpdateTag(CompoundTag tag) {
    load(tag);
  }

  @Override
  public void onLoad() {
    super.onLoad();
    if (level != null && level.isClientSide) {
      level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
  }

  @Override
  public void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    if (ownerUUID != null) {
      tag.putUUID("Owner", ownerUUID);
    }
    tag.putString("MaterialType", materialType.name());
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    if (tag.hasUUID("Owner")) {
      ownerUUID = tag.getUUID("Owner");
    }
    materialType = MaterialType.valueOf(tag.getString("MaterialType"));
    if (tag.contains("MaterialType")) {
      try {
        materialType = MaterialType.valueOf(tag.getString("MaterialType"));
      } catch (IllegalArgumentException e) {
        materialType = MaterialType.COPPER_BLOCK;
      }
    } else {
      materialType = MaterialType.COPPER_BLOCK;
    }
    radius = materialType.getRadius();
  }
}