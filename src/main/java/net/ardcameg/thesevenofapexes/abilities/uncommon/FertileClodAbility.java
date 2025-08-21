package net.ardcameg.thesevenofapexes.abilities.uncommon;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public final class FertileClodAbility {
    private FertileClodAbility() {}
    private static final Random RANDOM = new Random();

    /**
     * 任務：骨粉使用時に、確率で追加の成長を促す
     */
    public static void tryExtraBonemeal(ServerLevel level, BlockPos pos, int clodCount, int prideMultiplier) {
        if (clodCount <= 0) return;

        int finalCount = clodCount * prideMultiplier;

        // --- 1. 発動確率を計算 ---
        // 基本10%、追加1個あたり2.5%上昇
        float procChance = 0.10f + (finalCount - 1) * 0.025f;

        if (RANDOM.nextFloat() < procChance) {
            // --- 2. 追加の成長を試みる ---
            BlockState targetState = level.getBlockState(pos);
            if (targetState.getBlock() instanceof BonemealableBlock bonemealable) {
                // 成長可能か（骨粉を与えられるか）をチェック
                if (bonemealable.isValidBonemealTarget(level, pos, targetState)) {
                    // バニラの骨粉と同じ成長ロジックを、もう一度呼び出す
                    bonemealable.performBonemeal(level, level.random, pos.immutable(), targetState);

                    // 特別な成功音を鳴らす
                    level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.5f);
                }
            }
        }
    }
}