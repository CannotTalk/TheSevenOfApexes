package net.ardcameg.thesevenofapexes.abilities.uncommon;

import net.ardcameg.thesevenofapexes.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public final class ScentOfCompostAbility {
    private ScentOfCompostAbility() {}
    private static final Random RANDOM = new Random();

    /**
     * 任務：コンポスターの堆肥レベルを追加で上昇させる
     */
    public static void tryBoostComposter(ServerLevel level, BlockPos pos, int scentCount, int prideMultiplier) {
        if (scentCount <= 0) return;

        int finalCount = scentCount * prideMultiplier;

        // --- 1. 発動確率を計算 ---
        float chanceModifier = Config.scentOfCompostBoostComposterChance.get().floatValue();
        float procChance = chanceModifier + (finalCount - 1) * (chanceModifier / 2);

        if (RANDOM.nextFloat() < procChance) {
            // --- 2. 堆肥レベルを1上昇させる ---
            BlockState currentState = level.getBlockState(pos);
            int currentLevel = currentState.getValue(ComposterBlock.LEVEL);

            // 堆肥レベルが最大(7)になる一歩手前までしか効果はない
            if (currentLevel < 7) {
                // 成功音を鳴らし、レベルを1上げる
                level.playSound(null, pos, SoundEvents.COMPOSTER_FILL_SUCCESS, SoundSource.BLOCKS, 1.0f, 1.0f);
                level.setBlock(pos, currentState.setValue(ComposterBlock.LEVEL, currentLevel + 1), 3);
            }
        }
    }
}