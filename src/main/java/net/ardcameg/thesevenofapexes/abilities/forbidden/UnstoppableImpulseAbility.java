package net.ardcameg.thesevenofapexes.abilities.forbidden;

import net.ardcameg.thesevenofapexes.Config;
import net.ardcameg.thesevenofapexes.client.ClientTimerData;
import net.ardcameg.thesevenofapexes.item.ModItems;
import net.ardcameg.thesevenofapexes.networking.ModMessages;
import net.ardcameg.thesevenofapexes.networking.packet.TimerSyncS2CPacket;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class UnstoppableImpulseAbility {
    public static final String ID = "unstoppable_impulse";
    private static final String STATE_TAG = ID + "_state";
    private static final String TIMER_TAG = ID + "_timer";
    private static final String ANIMATION_TICKS_TAG = ID + "_anim_ticks";
    private static final int ANIM_DURATION_START = 20;
    private static final int ANIM_DURATION_RESET = 10;

    public static void update(ServerPlayer player) {
        if (!player.isAlive()) return;
        int itemCount = BuffItemUtils.countAllItemsForPlayer(player, ModItems.FORBIDDEN_UNSTOPPABLE_IMPULSE.get());
        CompoundTag data = player.getPersistentData();
        ClientTimerData.TimerState currentState = ClientTimerData.TimerState.values()[data.getInt(STATE_TAG)];

        if (itemCount > 0) {
            if (currentState == ClientTimerData.TimerState.INACTIVE) {
                start(player);
                return;
            }
        } else {
            if (currentState != ClientTimerData.TimerState.INACTIVE
                    && !(player.isCreative() || player.isSpectator())) {
                clear(player);
            }
            return;
        }

        if (currentState == ClientTimerData.TimerState.ANIMATING_IN || currentState == ClientTimerData.TimerState.ANIMATING_RESET) {
            int animTicks = data.getInt(ANIMATION_TICKS_TAG);
            if (animTicks > 0) {
                data.putInt(ANIMATION_TICKS_TAG, animTicks - 1);
            } else {
                data.putInt(STATE_TAG, ClientTimerData.TimerState.COUNTING.ordinal());
                data.remove(ANIMATION_TICKS_TAG);
                int targetTicks = (currentState == ClientTimerData.TimerState.ANIMATING_IN)
                        ? Config.unstoppableImpulseBaseTicks.get()
                        : Config.unstoppableImpulseShortResetTicks.get();
                data.putInt(TIMER_TAG, targetTicks);
            }
        } else if (currentState == ClientTimerData.TimerState.COUNTING) {
            int ticksLeft = data.getInt(TIMER_TAG);
            if (ticksLeft > 0) {
                data.putInt(TIMER_TAG, ticksLeft - 1);
            } else {
                explode(player);
                return;
            }
        } else if (currentState == ClientTimerData.TimerState.PAUSED) {
            checkForReactivation(player);
            return; // PAUSED状態では、以下のsyncは不要
        }

        sync(player);

    }

    // このメソッドは「Mobをキルした時」専用
    public static void onKill(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(STATE_TAG)) return;
        ClientTimerData.TimerState currentState = ClientTimerData.TimerState.values()[data.getInt(STATE_TAG)];
        if (currentState == ClientTimerData.TimerState.COUNTING) {

            int ticksLeft = data.getInt(TIMER_TAG);
            int shortResetTicks = Config.unstoppableImpulseShortResetTicks.get();
            if(ticksLeft < shortResetTicks) {
                data.putInt(STATE_TAG, ClientTimerData.TimerState.ANIMATING_RESET.ordinal());
                data.putInt(ANIMATION_TICKS_TAG, ANIM_DURATION_RESET);
                sync(player);
            }
        }
    }

    public static void pause(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (BuffItemUtils.countAllItemsForPlayer(player, ModItems.FORBIDDEN_UNSTOPPABLE_IMPULSE.get()) > 0) {
            data.putInt(STATE_TAG, ClientTimerData.TimerState.PAUSED.ordinal());
            data.remove(TIMER_TAG);
            sync(player);
        }
    }

    private static void start(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        data.putInt(STATE_TAG, ClientTimerData.TimerState.ANIMATING_IN.ordinal());
        data.putInt(ANIMATION_TICKS_TAG, ANIM_DURATION_START);
        player.level().playSound(null, player.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.0f, 1.0f);
        sync(player);
    }

    // このメソッドは「リスポーン後の再起動」専用
    private static void checkForReactivation(ServerPlayer player) {
        Level level = player.level();
        AABB infiniteRange = new AABB(player.blockPosition()).inflate(128.0); // 無限は負荷が高いので、128ブロックに制限
        List<Mob> allMobs = level.getEntitiesOfClass(Mob.class, infiniteRange);
        for (Mob mob : allMobs) {
            if (mob.getTarget() == player) {
                start(player);
                return;
            }
        }
    }

    private static void explode(ServerPlayer player) {
        Level level = player.level();
        float power = Config.unstoppableImpulseExplosionPower.get().floatValue();
        level.explode(null, player.getX(), player.getY(), player.getZ(), power, Level.ExplosionInteraction.BLOCK);
        clear(player);
    }

    private static void clear(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        data.remove(STATE_TAG);
        data.remove(TIMER_TAG);
        data.remove(ANIMATION_TICKS_TAG);
        sync(player);
    }

    private static void sync(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        int state = data.getInt(STATE_TAG);
        int ticksLeft = data.getInt(TIMER_TAG);
        int animTicks = data.getInt(ANIMATION_TICKS_TAG);
        int animDuration = 0, animTarget = 0, maxTicks = 0;

        if (state == ClientTimerData.TimerState.ANIMATING_IN.ordinal()){
            animDuration = ANIM_DURATION_START;
            animTarget = Config.unstoppableImpulseBaseTicks.get();
            maxTicks = animTarget;
        } else if(state == ClientTimerData.TimerState.ANIMATING_RESET.ordinal()){
            animDuration = ANIM_DURATION_RESET;
            animTarget = Config.unstoppableImpulseShortResetTicks.get();
            maxTicks = Config.unstoppableImpulseBaseTicks.get();
        } else if (state == ClientTimerData.TimerState.COUNTING.ordinal()) {
            maxTicks = Config.unstoppableImpulseBaseTicks.get();
        }

        ModMessages.sendToPlayer(new TimerSyncS2CPacket(ID, ticksLeft, state, animTicks, animDuration, animTarget, maxTicks), player);
    }
}