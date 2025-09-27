package net.ardcameg.thesevenofapexes.event;

import net.ardcameg.thesevenofapexes.Config;
import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.ardcameg.thesevenofapexes.abilities.block.PurificationAbility;
import net.ardcameg.thesevenofapexes.item.ForbiddenItem;
import net.ardcameg.thesevenofapexes.item.ModItems;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = TheSevenOfApexes.MOD_ID)
public class AdvancementTriggers {
    private static final String HAS_HELD_FORBIDDEN_TAG = "HasHeldForbidden";
    public static final String MAX_FORBIDDEN_HELD_TAG = "MaxForbiddenHeld";

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.level().getGameTime() % 20 != 0) {
            return;
        }

        CompoundTag data = player.getPersistentData();

        Set<Item> uniqueForbiddenItems = new HashSet<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof ForbiddenItem) {
                uniqueForbiddenItems.add(stack.getItem());
            }
        }
        int currentForbiddenCount = uniqueForbiddenItems.size();

        if (currentForbiddenCount > 0 && !data.getBoolean(HAS_HELD_FORBIDDEN_TAG)) {
            grantAdvancement(player, "the_cursed_one");
            data.putBoolean(HAS_HELD_FORBIDDEN_TAG, true);
        }

        int maxHeld = data.getInt(MAX_FORBIDDEN_HELD_TAG);
        if (currentForbiddenCount > maxHeld) {
            data.putInt(MAX_FORBIDDEN_HELD_TAG, currentForbiddenCount);
            if (maxHeld < Config.ultimateArtifactThreshold.get() && currentForbiddenCount >= Config.ultimateArtifactThreshold.get()) {
                grantAdvancement(player, "seeker_of_the_abyss");
            }
        }

        // 伝説級をすべて持ち、全て列にセットされているか？
        AdvancementHolder apexAdvancement = player.getServer().getAdvancements().get(ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "main/apex_of_the_seven"));
        if (apexAdvancement != null && !player.getAdvancements().getOrStartProgress(apexAdvancement).isDone()) {

            // 1. 必要な全ての伝説級アイテムのリストを定義
            Set<Item> requiredLegendaries = Set.of(
                    ModItems.LEGENDARY_PRIDE.get(),
                    ModItems.LEGENDARY_ENVY.get(),
                    ModItems.LEGENDARY_WRATH.get(),
                    ModItems.LEGENDARY_SLOTH.get(),
                    ModItems.LEGENDARY_GREED.get(),
                    ModItems.LEGENDARY_GLUTTONY.get(),
                    ModItems.LEGENDARY_LUST.get(),
                    ModItems.LEGENDARY_SUNLIGHT_SACRED_SEAL.get(),
                    ModItems.LEGENDARY_MOONLIGHT_SACRED_SEAL.get()
            );

            // 2. 現在のバフ列にあるアイテムのセットを作成
            Set<Item> itemsInBuffRow = new HashSet<>();
            for (int i = 9; i <= 17; i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty()) {
                    itemsInBuffRow.add(stack.getItem());
                }
            }

            // 3. バフ列のセットが必要なアイテムを全て含んでいるか確認
            if (itemsInBuffRow.containsAll(requiredLegendaries)) {
                grantAdvancement(player, "apex_of_the_seven");
            }
        }


        checkForTrialStart(player, uniqueForbiddenItems);
    }

    private static void checkForTrialStart(ServerPlayer player, Set<Item> currentForbidden) {
        CompoundTag data = player.getPersistentData();
        boolean hasObtainedUltimate = data.getBoolean(PurificationAbility.ULTIMATE_ARTIFACT_OBTAINED_TAG);
        boolean isTimerRunning = data.contains(PurificationAbility.SURVIVAL_TIMER_TAG);
        boolean isTrialCompleted = data.getBoolean(PurificationAbility.SURVIVAL_TRIAL_COMPLETED_TAG);

        if (hasObtainedUltimate && !isTimerRunning && !isTrialCompleted) {
            if (isReadyForSecondTrial(currentForbidden)) {

                PurificationAbility.startSurvivalTrial(player);

                // 試練開始の進捗を付与
                grantAdvancement(player, "trial_of_survival");

            }
        }
    }

    public static boolean isReadyForSecondTrial(Set<Item> currentForbidden) {
        List<Item> excludedItems = List.of(
                ModItems.FORBIDDEN_HEART_OF_THE_ABYSS.get(),
                ModItems.FORBIDDEN_REVERSAL_ARTIFACT.get(),
                ModItems.FORBIDDEN_DEATHS_PREMONITION.get()
        );
        List<Item> allRequiredForbidden = ModItems.ITEMS.getEntries().stream()
                .map(Supplier::get)
                .filter(item -> item instanceof ForbiddenItem)
                .filter(item -> !excludedItems.contains(item))
                .collect(Collectors.toList());

        boolean readyForTrial = currentForbidden.containsAll(allRequiredForbidden);

        return readyForTrial;
    }

    /**
     * 指定したプレイヤーに指定した進捗を達成させる
     * この進捗が要求する全ての条件を達成させることで、進捗を完了させる
     * @param player 対象プレイヤー
     * @param advancementId 進捗のID
     */
    public static void grantAdvancement(ServerPlayer player, String advancementId) {
        AdvancementHolder advancement = player.getServer().getAdvancements().get(ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "main/" + advancementId));
        if (advancement != null) {
            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
            if (!progress.isDone()) {
                // この進捗が持つ全ての条件(criteria)を取得し、
                // 「まだ達成されていないものだけ」をawardする
                for (String criterion : progress.getRemainingCriteria()) {
                    player.getAdvancements().award(advancement, criterion);
                }
            }
        }
    }

    /**
     * 指定された進捗の、指定された単一の条件（Criterion）を達成させる
     * @param player 対象プレイヤー
     * @param advancementId 進捗のID
     * @param criterionId 条件のID
     */
    public static void grantCriterion(ServerPlayer player, String advancementId, String criterionId) {
        AdvancementHolder advancement = player.getServer().getAdvancements().get(ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "main/" + advancementId));
        if (advancement != null) {
            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
            if (!progress.isDone()) {
                // 指定されたcriterionを達成させる
                progress.grantProgress(criterionId);
            }
        }
    }
}