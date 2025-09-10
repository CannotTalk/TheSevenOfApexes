package net.ardcameg.thesevenofapexes.abilities.epic;

import net.ardcameg.thesevenofapexes.Config;
import net.ardcameg.thesevenofapexes.item.ForbiddenItem;
import net.ardcameg.thesevenofapexes.item.ModItems;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import java.util.HashMap;
import java.util.Map;

public final class FerrymansBargeAbility {
    private FerrymansBargeAbility() {}

    public static final String BARGE_TICKS_TAG = "FerrymansBargeTicks";
    private static final String BARGE_GAMEMODE_TAG = "FerrymansBargeOriginalGameMode";

    public static boolean startFerry(ServerPlayer player, int bargeCount, int prideMultiplier) {
        int originalGameModeId = player.gameMode.getGameModeForPlayer().getId();
        player.getPersistentData().putInt(BARGE_GAMEMODE_TAG, originalGameModeId);

        Map<Integer, ItemStack> bargesToKeep = new HashMap<>();
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(ModItems.EPIC_FERRYMANS_BARGE.get()) || stack.getItem() instanceof ForbiddenItem) {
                bargesToKeep.put(i, stack.copy());
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }

        inventory.dropAll();

        bargesToKeep.forEach((slot, stack) -> {
            inventory.setItem(slot, stack);
        });

        player.setGameMode(GameType.SPECTATOR);

        // この時点での bargeCount を使って計算する
        int finalBargeCount = bargeCount * prideMultiplier;
        int baseTicks = Config.ferrymanBargeSpectatorBaseTicks.getAsInt();
        // 1個目は5秒、2個目から+1秒
        int bonusSeconds = finalBargeCount - 1;
        int totalTicks = baseTicks + bonusSeconds * 20;
        player.getPersistentData().putInt(BARGE_TICKS_TAG, totalTicks);

        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_DEATH, SoundSource.PLAYERS, 1.0f, 0.5f);
        player.level().playSound(null, player.blockPosition(), SoundEvents.CONDUIT_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 1.0f);

        return true;
    }

    public static void endFerry(ServerPlayer player, int bargeCount, int prideMultiplier) {
        int originalGameModeId = player.getPersistentData().getInt(BARGE_GAMEMODE_TAG);
        GameType originalGameType = GameType.byId(originalGameModeId);
        player.setGameMode(originalGameType);

        // 1. まず、HP回復量を計算する (この時点ではアイテムは消費されていない)
        int finalBargeCount = bargeCount * prideMultiplier;
        float baseRegenerate = Config.ferrymanBargeBaseRegenerate.get().floatValue();
        // 1個目は基礎値(2)、2個目から+1
        float healthToRestore = baseRegenerate + (finalBargeCount - 1);
        player.setHealth(healthToRestore);

        // 2. HP回復の恩恵を受けた後に、アイテムを1つ消費する
        BuffItemUtils.consumeItemFromBuffRow(player, ModItems.EPIC_FERRYMANS_BARGE.get());

        player.getFoodData().setFoodLevel(20);
        player.clearFire();

        player.getPersistentData().remove(BARGE_TICKS_TAG);
        player.getPersistentData().remove(BARGE_GAMEMODE_TAG);

        player.level().playSound(null, player.blockPosition(), SoundEvents.CONDUIT_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.0f);
        player.teleportTo(player.getX(), player.getY(), player.getZ());
    }
}