package net.undertaker.furtotemsmod.items.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.undertaker.furtotemsmod.FurTotemsMod;
import net.undertaker.furtotemsmod.attributes.ModAttributes;
import net.undertaker.furtotemsmod.blocks.ModBlocks;
import net.undertaker.furtotemsmod.blocks.blockentity.UpgradableTotemBlockEntity;
import net.undertaker.furtotemsmod.items.ModItems;
import net.undertaker.furtotemsmod.networking.ModNetworking;
import net.undertaker.furtotemsmod.networking.packets.ChangeModePacket;
import net.undertaker.furtotemsmod.util.TotemSavedData;

@Mod.EventBusSubscriber(modid = FurTotemsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TotemItem extends Item {
    public TotemItem(Properties pProperties) {
        super(pProperties);
    }
    private static final String MODE_KEY = "Mode";

    public enum StaffMode {
        PLACE_TOTEM("Установка тотема"),
        REMOVE_TOTEM("Удаление тотема"),
        ADD_MEMBER("Добавление участника"),
        REMOVE_MEMBER("Удаление участника"),
        ADD_BLACKLIST("Добавление в черный список"),
        REMOVE_BLACKLIST("Удаление из черного списка");

        private final String displayName;

        StaffMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack pStack, Player player, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
        Level level = player.getLevel();
        BlockPos pos = player.blockPosition();

        if (level.isClientSide || player == null) return InteractionResult.FAIL;
        ItemStack heldItem = player.getMainHandItem();
        CompoundTag tag = heldItem.getOrCreateTag();
        StaffMode currentMode = getModeFromTag(tag);
        return switch (currentMode) {
            case ADD_MEMBER -> handleAddMember(level, pos, player, pInteractionTarget);
            case REMOVE_MEMBER -> handleRemoveMember(level, pos, player, pInteractionTarget);
            case ADD_BLACKLIST -> handleAddToBlacklist(level, pos, player, pInteractionTarget);
            case REMOVE_BLACKLIST -> handleRemoveFromBlacklist(level, pos, player, pInteractionTarget);
            default -> InteractionResult.FAIL;
        };
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (level.isClientSide || player == null) return InteractionResult.FAIL;

        ItemStack heldItem = context.getItemInHand();
        CompoundTag tag = heldItem.getOrCreateTag();
        StaffMode currentMode = getModeFromTag(tag);

        return switch (currentMode) {
            case PLACE_TOTEM -> handlePlaceTotem(context, level, pos, player);
            case REMOVE_TOTEM -> handleRemoveTotem(level, pos, player);
            default -> InteractionResult.FAIL;
        };
    }
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && player.isShiftKeyDown()) {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof TotemItem) {
                CompoundTag tag = stack.getOrCreateTag();

                StaffMode currentMode;
                try {
                    currentMode = StaffMode.valueOf(tag.getString("Mode"));
                } catch (IllegalArgumentException | NullPointerException e) {
                    currentMode = StaffMode.PLACE_TOTEM;
                }

                int newIndex = (currentMode.ordinal() + (event.getScrollDelta() > 0 ? 1 : -1) + StaffMode.values().length) % StaffMode.values().length;
                StaffMode newMode = StaffMode.values()[newIndex];

                // Update the tag and notify the server
                tag.putString("Mode", newMode.name());
                stack.setHoverName(Component.literal("Посох тотемов (" + newMode.getDisplayName() + ")"));
                ModNetworking.sendToServer(new ChangeModePacket(newIndex));

                // Provide player feedback
                player.displayClientMessage(Component.literal("Режим изменён на: " + newMode.getDisplayName()), true);

                event.setCanceled(true);
            }
        }
    }


    private static StaffMode getModeFromTag(CompoundTag tag) {
        if (tag.contains(MODE_KEY)) {
            try {
                return StaffMode.valueOf(tag.getString(MODE_KEY));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return StaffMode.PLACE_TOTEM;
    }

    private InteractionResult handlePlaceTotem(UseOnContext context, Level level, BlockPos pos, Player player) {
        if (!player.isShiftKeyDown()) return InteractionResult.FAIL;
        ItemStack heldItem = context.getItemInHand();
        Item heldItemType = heldItem.getItem();

        UpgradableTotemBlockEntity.MaterialType materialType = null;

        if (heldItemType == ModItems.COPPER_STAFF_ITEM.get()) {
            materialType = UpgradableTotemBlockEntity.MaterialType.COPPER;
        } else if (heldItemType == ModItems.IRON_STAFF_ITEM.get()) {
            materialType = UpgradableTotemBlockEntity.MaterialType.IRON;
        } else if (heldItemType == ModItems.GOLD_STAFF_ITEM.get()) {
            materialType = UpgradableTotemBlockEntity.MaterialType.GOLD;
        } else if (heldItemType == ModItems.DIAMOND_STAFF_ITEM.get()) {
            materialType = UpgradableTotemBlockEntity.MaterialType.DIAMOND;
        } else if (heldItemType == ModItems.NETHERITE_STAFF_ITEM.get()) {
            materialType = UpgradableTotemBlockEntity.MaterialType.NETHERITE;
        }

        TotemSavedData data = TotemSavedData.get((ServerLevel) level);
        double maxBigTotems = player.getAttributeValue(ModAttributes.BIG_TOTEM_COUNT.get());
        int currentBigTotems = data.getPlayerTotemCount(player.getUUID()).getBigTotems();
        if (currentBigTotems >= maxBigTotems){
            player.displayClientMessage(
                    Component.literal("Вы достигли лимита больших тотемов!"), true);
            return InteractionResult.FAIL;
        }

        if (materialType == null) {
            player.displayClientMessage(Component.literal("Посох не соответствует ни одному материалу!"), true);
            return InteractionResult.FAIL;
        }

        if (!player.getInventory().contains(new ItemStack(materialType.getRequiredBlock()))) {
            player.displayClientMessage(Component.literal("У вас нет нужного блока: " + materialType.name()), true);
            return InteractionResult.FAIL;
        }

        BlockState totemBlockState = ModBlocks.UPGRADABLE_TOTEM.get().defaultBlockState();
        level.setBlock(pos.above(), totemBlockState, 3);

        if (level.getBlockEntity(pos.above()) instanceof UpgradableTotemBlockEntity totemEntity) {

            totemEntity.setOwner(player.getUUID());
            totemEntity.upgrade(materialType);

            removeItemFromInventory(player, materialType.getRequiredBlock().asItem(), 1);
            player.displayClientMessage(Component.literal("Тотем установлен с уровнем: " + materialType.name()), true);
            return InteractionResult.SUCCESS;
        }

        player.displayClientMessage(Component.literal("Не удалось установить тотем!"), true);
        return InteractionResult.FAIL;
    }

    private InteractionResult handleRemoveTotem(Level level, BlockPos pos, Player player) {
        ServerLevel serverLevel = (ServerLevel) level;
        TotemSavedData data = TotemSavedData.get(serverLevel);
        if(!player.isShiftKeyDown()) return InteractionResult.FAIL;
        TotemSavedData.TotemData totemData = data.getTotemData(pos);
        if (totemData == null) {
            player.displayClientMessage(Component.literal("Здесь нет тотема для удаления!"), true);
            return InteractionResult.FAIL;
        }

        if (!totemData.getOwner().equals(player.getUUID())) {
            player.displayClientMessage(Component.literal("Вы не являетесь владельцем этого тотема!"), true);
            return InteractionResult.FAIL;
        }

        level.destroyBlock(pos, false);

        data.removeTotem(pos);

        player.displayClientMessage(Component.literal("Тотем успешно удалён!"), true);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleAddMember(Level level, BlockPos pos, Player player, LivingEntity pInteractionTarget) {
        ServerLevel serverLevel = (ServerLevel) level;
        TotemSavedData data = TotemSavedData.get(serverLevel);
        if(!player.isShiftKeyDown()) return InteractionResult.FAIL;
    if (!(pInteractionTarget instanceof Player)) {
        player.displayClientMessage(Component.literal("Это не игрок!"), true);
        return InteractionResult.FAIL;
    }
    if (data.isPlayerMember(player.getUUID(), pInteractionTarget.getUUID())) {
        player.displayClientMessage(Component.literal("Игрок уже является союзником!"), true);
        return InteractionResult.FAIL;
    }

        data.addMemberToTotem(player.getUUID(), pInteractionTarget.getUUID());

        player.displayClientMessage(Component.literal("Добавляем участника..."), true);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleRemoveMember(Level level, BlockPos pos, Player player, LivingEntity pInteractionTarget) {
        ServerLevel serverLevel = (ServerLevel) level;
        TotemSavedData data = TotemSavedData.get(serverLevel);
        if(!player.isShiftKeyDown()) return InteractionResult.FAIL;
        if (!(pInteractionTarget instanceof Player)) {
            player.displayClientMessage(Component.literal("Это не игрок!"), true);
            return InteractionResult.FAIL;
        }
        if (!data.isPlayerMember(player.getUUID(), pInteractionTarget.getUUID())) {
            player.displayClientMessage(Component.literal("Игрок не является союзником!"), true);
            return InteractionResult.FAIL;
        }

        data.removeMemberFromTotem(player.getUUID(), pInteractionTarget.getUUID());

        player.displayClientMessage(Component.literal("Удаляем участника..."), true);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleAddToBlacklist(Level level, BlockPos pos, Player player, LivingEntity pInteractionTarget) {
        ServerLevel serverLevel = (ServerLevel) level;
        TotemSavedData data = TotemSavedData.get(serverLevel);
        if(!player.isShiftKeyDown()) return InteractionResult.FAIL;
        if (!(pInteractionTarget instanceof Player)) {
            player.displayClientMessage(Component.literal("Это не игрок!"), true);
            return InteractionResult.FAIL;
        }
        if (data.isBlacklisted(player.getUUID(), pInteractionTarget.getUUID())) {
            player.displayClientMessage(Component.literal("Игрок уже в черном списке!"), true);
            return InteractionResult.FAIL;
        }

        data.addToBlacklist(player.getUUID(), pInteractionTarget.getUUID());

        player.displayClientMessage(Component.literal("Добавляем в черный список..."), true);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleRemoveFromBlacklist(Level level, BlockPos pos, Player player, LivingEntity pInteractionTarget) {
        ServerLevel serverLevel = (ServerLevel) level;
        TotemSavedData data = TotemSavedData.get(serverLevel);
        if(!player.isShiftKeyDown()) return InteractionResult.FAIL;
        if (!(pInteractionTarget instanceof Player)) {
            player.displayClientMessage(Component.literal("Это не игрок!"), true);
            return InteractionResult.FAIL;
        }
        if (!data.isBlacklisted(player.getUUID(), pInteractionTarget.getUUID())) {
            player.displayClientMessage(Component.literal("Игрок не в черном списке!"), true);
            return InteractionResult.FAIL;
        }
        data.removeFromBlacklist(player.getUUID(), pInteractionTarget.getUUID());

        player.displayClientMessage(Component.literal("Удаляем из черного списка..."), true);
        return InteractionResult.SUCCESS;
    }

    private void removeItemFromInventory(Player player, Item item, int count) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                stack.shrink(count);
                if (stack.isEmpty()) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
                break;
            }
        }
    }



}
