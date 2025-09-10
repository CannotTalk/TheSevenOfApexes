package net.ardcameg.thesevenofapexes.abilities.block;

import net.ardcameg.thesevenofapexes.item.ForbiddenItem;
import net.ardcameg.thesevenofapexes.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class PurificationAbility {
    private static final Random RANDOM = new Random();

    public static void performRitual(ServerPlayer player) {
        ItemStack offHandStack = player.getOffhandItem();
        int requiredEpics = 7;

        if (offHandStack.is(ModItems.LEGENDARY_PACK.get())) {
            onSuccess(player, offHandStack, 1);
        } else if (offHandStack.is(ModItems.EPIC_PACK.get()) && offHandStack.getCount() >= requiredEpics) {
            onSuccess(player, offHandStack, requiredEpics);
        } else {
            onFailure(player);
        }
    }

    private static void onSuccess(ServerPlayer player, ItemStack offering, int amountToConsume) {
        List<Integer> forbiddenItemSlots = new ArrayList<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).getItem() instanceof ForbiddenItem) {
                forbiddenItemSlots.add(i);
            }
        }

        if (forbiddenItemSlots.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.seven_apexes.altar_no_curse"));
            player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 1.5f);
            // 供物は消費しない
        } else {
            // 供物を消費
            offering.shrink(amountToConsume);

            int slotToRemove = forbiddenItemSlots.get(RANDOM.nextInt(forbiddenItemSlots.size()));
            ItemStack removedItem = player.getInventory().getItem(slotToRemove).copy();
            player.getInventory().setItem(slotToRemove, ItemStack.EMPTY);

            player.sendSystemMessage(Component.translatable("message.seven_apexes.altar_success", removedItem.getHoverName()));
            player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0f, 2.0f);
            player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 0.5f);
        }
    }

    private static void onFailure(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable("message.seven_apexes.altar_invalid_offering"));
        player.level().playSound(null, player.blockPosition(), SoundEvents.LEVER_CLICK, SoundSource.PLAYERS, 0.5f, 0.8f);
    }
}