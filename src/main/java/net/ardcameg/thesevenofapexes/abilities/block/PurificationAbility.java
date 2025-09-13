package net.ardcameg.thesevenofapexes.abilities.block;

import net.ardcameg.thesevenofapexes.Config;
import net.ardcameg.thesevenofapexes.client.ClientTimerData;
import net.ardcameg.thesevenofapexes.event.AdvancementTriggers;
import net.ardcameg.thesevenofapexes.item.ForbiddenItem;
import net.ardcameg.thesevenofapexes.item.ModItems;
import net.ardcameg.thesevenofapexes.networking.ModMessages;
import net.ardcameg.thesevenofapexes.networking.packet.TimerSyncS2CPacket;
import net.minecraft.nbt.CompoundTag;
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
    public static final String ULTIMATE_ARTIFACT_OBTAINED_TAG = "UltimateArtifactObtained";
    public static final String SURVIVAL_TRIAL_ID = "survival_trial";
    public static final String SURVIVAL_TIMER_TAG = SURVIVAL_TRIAL_ID + "_timer";
    public static final String SURVIVAL_TRIAL_COMPLETED_TAG = "SurvivalTrialCompleted";

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
        } else {
            offering.shrink(amountToConsume);
            int slotToRemove = forbiddenItemSlots.get(RANDOM.nextInt(forbiddenItemSlots.size()));
            ItemStack removedItem = player.getInventory().getItem(slotToRemove).copy();
            player.getInventory().setItem(slotToRemove, ItemStack.EMPTY);
            player.sendSystemMessage(Component.translatable("message.seven_apexes.altar_success", removedItem.getHoverName()));
            player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0f, 2.0f);

            boolean allCursesBanished = true;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (player.getInventory().getItem(i).getItem() instanceof ForbiddenItem) {
                    allCursesBanished = false;
                    break;
                }
            }

            AdvancementTriggers.grantAdvancement(player, "first_step_to_freedom");

            if (allCursesBanished) {
                handleUltimateReward(player);
            }
        }
    }

    private static void handleUltimateReward(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();

        if (!data.getBoolean(ULTIMATE_ARTIFACT_OBTAINED_TAG)) {
            if (data.getInt(AdvancementTriggers.MAX_FORBIDDEN_HELD_TAG) >= Config.ultimateArtifactThreshold.get()) {
                grantUltimateArtifact(player);
                data.putBoolean(ULTIMATE_ARTIFACT_OBTAINED_TAG, true);
            }
        } else {
            if (data.getBoolean(SURVIVAL_TRIAL_COMPLETED_TAG)) {
                grantUltimateArtifact(player);
                data.remove(SURVIVAL_TRIAL_COMPLETED_TAG);
            }
        }
    }

    public static void updateSurvivalTrial(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(SURVIVAL_TIMER_TAG)) return;

        int ticksLeft = data.getInt(SURVIVAL_TIMER_TAG);
        if (ticksLeft > 0) {
            data.putInt(SURVIVAL_TIMER_TAG, ticksLeft - 1);
            if (ticksLeft % 20 == 0) {
                syncSurvivalTrial(player, ticksLeft - 1);
            }
        } else {
            data.remove(SURVIVAL_TIMER_TAG);
            data.putBoolean(SURVIVAL_TRIAL_COMPLETED_TAG, true);
            player.sendSystemMessage(Component.translatable("message.seven_apexes.trial_complete"));
            player.level().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);
            syncSurvivalTrial(player, 0);
        }
    }

    public static void resetSurvivalTrial(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (data.contains(SURVIVAL_TIMER_TAG) || data.contains(SURVIVAL_TRIAL_COMPLETED_TAG)) {
            data.remove(SURVIVAL_TIMER_TAG);
            data.remove(SURVIVAL_TRIAL_COMPLETED_TAG);
            syncSurvivalTrial(player, 0);
        }
    }

    private static void grantUltimateArtifact(ServerPlayer player) {
        ItemStack reward = RANDOM.nextBoolean()
                ? new ItemStack(ModItems.FORBIDDEN_HEART_OF_THE_ABYSS.get())
                : new ItemStack(ModItems.FORBIDDEN_REVERSAL_ARTIFACT.get());
        player.sendSystemMessage(Component.translatable("message.seven_apexes.altar_ultimate_reward", reward.getHoverName()));
        player.level().playSound(null, player.blockPosition(), SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.0f, 1.0f);
        if (!player.getInventory().add(reward)) {
            player.drop(reward, false);
        }
        String advancementId = reward.is(ModItems.FORBIDDEN_HEART_OF_THE_ABYSS.get()) ? "master_of_curses" : "transcender_of_logic";
        AdvancementTriggers.grantAdvancement(player, advancementId);
        player.getPersistentData().putInt(AdvancementTriggers.MAX_FORBIDDEN_HELD_TAG, 0);
    }

    private static void onFailure(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable("message.seven_apexes.altar_invalid_offering"));
        player.level().playSound(null, player.blockPosition(), SoundEvents.LEVER_CLICK, SoundSource.PLAYERS, 0.5f, 0.8f);
    }

    public static void syncSurvivalTrial(ServerPlayer player, int ticksLeft) {
        int maxTicks = Config.finalTrialDurationTicks.get();
        if (ticksLeft > 0) {
            ModMessages.sendToPlayer(new TimerSyncS2CPacket(SURVIVAL_TRIAL_ID, ticksLeft, ClientTimerData.TimerState.COUNTING.ordinal(), 0, 0, 0, maxTicks), player);
        } else {
            ModMessages.sendToPlayer(new TimerSyncS2CPacket(SURVIVAL_TRIAL_ID, 0, ClientTimerData.TimerState.INACTIVE.ordinal(), 0, 0, 0, 0), player);
        }
    }
}