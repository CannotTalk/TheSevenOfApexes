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

        checkForTrialStart(player, uniqueForbiddenItems);
    }

    private static void checkForTrialStart(ServerPlayer player, Set<Item> currentForbidden) {
        CompoundTag data = player.getPersistentData();
        boolean hasObtainedUltimate = data.getBoolean(PurificationAbility.ULTIMATE_ARTIFACT_OBTAINED_TAG);
        boolean isTimerRunning = data.contains(PurificationAbility.SURVIVAL_TIMER_TAG);
        boolean isTrialCompleted = data.getBoolean(PurificationAbility.SURVIVAL_TRIAL_COMPLETED_TAG);

        if (hasObtainedUltimate && !isTimerRunning && !isTrialCompleted) {
            // --- ここからデバッグログ ---
            // このメッセージがコンソールに出力されれば、最初の3条件はクリアしています。
            System.out.println("[TRIAL DEBUG] Player " + player.getName().getString() + " is eligible to check for trial start.");

            if (isReadyForSecondTrial(currentForbidden)) {
                // このメッセージが出力されれば、全ての条件がクリアされ、試練が開始されるはずです。
                System.out.println("[TRIAL DEBUG] All conditions met! Starting trial.");

                int trialDuration = Config.finalTrialDurationTicks.get(); // Configから取得するように修正
                data.putInt(PurificationAbility.SURVIVAL_TIMER_TAG, trialDuration);
                PurificationAbility.syncSurvivalTrial(player, trialDuration);

                // 試練開始の進捗を付与
                grantAdvancement(player, "trial_of_survival");

            }
        }
    }

    private static boolean isReadyForSecondTrial(Set<Item> currentForbidden) {
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

        // --- ここからデバッグログ ---
        // このログは、条件4の成否を詳細に教えてくれます。
        if (!readyForTrial) {
            System.out.println("[TRIAL DEBUG] isReadyForSecondTrial FAILED.");
            List<Item> missingItems = new ArrayList<>(allRequiredForbidden);
            missingItems.removeAll(currentForbidden);
            System.out.println(" >>>> Required (" + allRequiredForbidden.size() + "): " + allRequiredForbidden.stream().map(i -> i.toString()).collect(Collectors.joining(", ")));
            System.out.println(" >>>> Player Has (" + currentForbidden.size() + "): " + currentForbidden.stream().map(i -> i.toString()).collect(Collectors.joining(", ")));
            System.out.println(" >>>> MISSING ("+ missingItems.size() +"): " + missingItems.stream().map(i -> i.toString()).collect(Collectors.joining(", ")));
        }
        // --- デバッグログここまで ---

        return readyForTrial;
    }

    public static void grantAdvancement(ServerPlayer player, String advancementId) {
        AdvancementHolder advancement = player.getServer().getAdvancements().get(ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "main/" + advancementId));
        if (advancement != null) {
            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
            if (!progress.isDone()) {
                String firstCriterion = advancement.value().criteria().keySet().stream().findFirst().orElse(null);
                if (firstCriterion != null) {
                    player.getAdvancements().award(advancement, firstCriterion);
                }
            }
        }
    }
}