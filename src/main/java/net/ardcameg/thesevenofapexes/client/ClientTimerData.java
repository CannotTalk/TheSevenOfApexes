package net.ardcameg.thesevenofapexes.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientTimerData {
    public enum TimerState { INACTIVE, ANIMATING_IN, ANIMATING_RESET, COUNTING, PAUSED }
    public static class TimerInfo {
        public int ticksLeft;
        public int timerState;
        public int animationTicks;
        public int animationDuration;
        public int animationTargetTicks;
        public final int maxTicks;

        public TimerInfo(int ticksLeft, int timerState, int animationTicks, int animationDuration, int animationTargetTicks, int maxTicks) {
            this.ticksLeft = ticksLeft;
            this.timerState = timerState;
            this.animationTicks = animationTicks;
            this.animationDuration = animationDuration;
            this.animationTargetTicks = animationTargetTicks;
            this.maxTicks = maxTicks;
        }
    }
    private static final Map<String, TimerInfo> activeTimers = new ConcurrentHashMap<>();

    public static void updateTimer(String timerId, int ticksLeft, int timerState, int animTicks, int animDuration, int animTarget, int maxTicks) {
        if (timerState == TimerState.INACTIVE.ordinal()) {
            activeTimers.remove(timerId);
        } else {
            activeTimers.put(timerId, new TimerInfo(ticksLeft, timerState, animTicks, animDuration, animTarget, maxTicks));
        }
    }

    // クライアントサイドでのカウントダウン処理を復活
    public static void tick() {
        for (TimerInfo timer : activeTimers.values()) {
            if (timer.timerState == TimerState.COUNTING.ordinal() && timer.ticksLeft > 0) {
                timer.ticksLeft--;
            } else if (timer.timerState == TimerState.ANIMATING_IN.ordinal() || timer.timerState == TimerState.ANIMATING_RESET.ordinal()) {
                if (timer.animationTicks > 0) {
                    timer.animationTicks--;
                } else {
                    timer.timerState = TimerState.COUNTING.ordinal();
                    timer.ticksLeft = timer.animationTargetTicks;
                }
            }
        }
    }

    public static Map<String, TimerInfo> getActiveTimers() {
        return activeTimers;
    }
}