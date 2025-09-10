package net.ardcameg.thesevenofapexes.abilities.forbidden;

import net.ardcameg.thesevenofapexes.Config;
import net.ardcameg.thesevenofapexes.client.ClientTimerData;
import net.ardcameg.thesevenofapexes.event.ModEvents;
import net.ardcameg.thesevenofapexes.item.ModItems;
import net.ardcameg.thesevenofapexes.networking.ModMessages;
import net.ardcameg.thesevenofapexes.networking.packet.TimerSyncS2CPacket;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class WhispersOfTheVoidAbility {
    public static final String ID = "whispers_of_the_void";
    public static final String GRACE_ID = "void_grace_period";
    private static final String GAUGE_TAG = ID + "_gauge";
    private static final String GRACE_TIMER_TAG = GRACE_ID + "_timer";

    public static void update(ServerPlayer player) {
        if (!player.isAlive()) return;
        if (BuffItemUtils.countAllItemsForPlayer(player, ModItems.FORBIDDEN_WHISPERS_OF_THE_VOID.get()) <= 0) {
            if (player.getPersistentData().contains(GAUGE_TAG) || player.getPersistentData().contains(GRACE_TIMER_TAG)) {
                reset(player);
                clearGrace(player);
            }
            return;
        }

        CompoundTag data = player.getPersistentData();
        if (data.contains(GRACE_TIMER_TAG)) {
            int graceTicks = data.getInt(GRACE_TIMER_TAG);
            if (graceTicks > 0) {
                data.putInt(GRACE_TIMER_TAG, graceTicks - 1);
                syncGrace(player, graceTicks - 1);
                return;
            } else {
                clearGrace(player);
            }
        }

        if (player.isCreative() || player.isSpectator()) {
            reset(player);
            return;
        }

        Vec3 currentPos = player.position();
        Vec3 lastPos = ModEvents.PLAYER_LAST_POSITION.get(player.getUUID());

        if (lastPos != null && currentPos.distanceToSqr(lastPos) < 1.0E-7) {
            int currentGauge = data.getInt(GAUGE_TAG) + 1;
            int requiredGauge = Config.whispersOfTheVoidRequiredTicks.get();
            if (currentGauge >= requiredGauge) {
                player.kill();
                reset(player);
            } else {
                data.putInt(GAUGE_TAG, currentGauge);
                syncGauge(player, currentGauge, requiredGauge);
            }
        } else {
            reset(player);
        }
    }

    public static void pause(ServerPlayer player) {
        if (BuffItemUtils.countAllItemsForPlayer(player, ModItems.FORBIDDEN_WHISPERS_OF_THE_VOID.get()) > 0) {
            reset(player); // まず古いゲージを確実に消す
            int gracePeriod = Config.whispersOfTheVoidGracePeriodTicks.get();
            player.getPersistentData().putInt(GRACE_TIMER_TAG, gracePeriod);
            syncGrace(player, gracePeriod);
        }
    }

    public static void reset(ServerPlayer player) {
        if (player.getPersistentData().contains(GAUGE_TAG)) {
            player.getPersistentData().remove(GAUGE_TAG);
            ModMessages.sendToPlayer(new TimerSyncS2CPacket(ID, 0, ClientTimerData.TimerState.INACTIVE.ordinal(), 0, 0, 0, 0), player);
        }
    }

    private static void clearGrace(ServerPlayer player) {
        player.getPersistentData().remove(GRACE_TIMER_TAG);
        syncGrace(player, 0);
    }

    private static void syncGauge(ServerPlayer player, int currentGauge, int maxGauge) {
        int ticksLeft = maxGauge - currentGauge;
        ModMessages.sendToPlayer(new TimerSyncS2CPacket(ID, ticksLeft, ClientTimerData.TimerState.COUNTING.ordinal(), 0, 0, 0, maxGauge), player);
    }

    private static void syncGrace(ServerPlayer player, int ticksLeft) {
        int maxTicks = Config.whispersOfTheVoidGracePeriodTicks.get();
        if (ticksLeft > 0) {
            ModMessages.sendToPlayer(new TimerSyncS2CPacket(GRACE_ID, ticksLeft, ClientTimerData.TimerState.COUNTING.ordinal(), 0, 0, 0, maxTicks), player);
        } else {
            ModMessages.sendToPlayer(new TimerSyncS2CPacket(GRACE_ID, 0, ClientTimerData.TimerState.INACTIVE.ordinal(), 0, 0, 0, 0), player);
        }
    }
}