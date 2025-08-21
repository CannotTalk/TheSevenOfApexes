package net.ardcameg.thesevenofapexes.abilities.rare;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.Random;

public final class BountyTotemAbility {
    private BountyTotemAbility() {}
    private static final Random RANDOM = new Random();

    /**
     * 任務1：作物の成長を促進する
     */
    public static void accelerateGrowth(ServerLevel level, BlockPos cropPos, int totemCount, int prideMultiplier) {
        // この処理は、ランダムなtickで呼び出される
        int finalCount = totemCount * prideMultiplier;

        // 成長速度+50%は、50%の確率で追加の成長判定を行うことで再現
        float chance = 0.5f * finalCount;

        if (RANDOM.nextFloat() < chance) {
            BlockState cropState = level.getBlockState(cropPos);
            Block cropBlock = cropState.getBlock();

            // 対象が成長可能な作物か確認
            if (cropBlock instanceof BonemealableBlock bonemealable) {
                if (bonemealable.isValidBonemealTarget(level, cropPos, cropState)) {
                    bonemealable.performBonemeal(level, level.random, cropPos.immutable(), cropState);
                }
            }
        }
    }

    /**
     * 任務2：収穫時に確率で耕地を劣化させる
     */
    public static void degradeFarmland(BlockEvent.BreakEvent event, int totemCount, int prideMultiplier) {
        int finalCount = totemCount * prideMultiplier;

        // 1個で20%、追加ごとに5%上昇
        float chance = 0.2f + (finalCount - 1) * 0.05f;

        if (RANDOM.nextFloat() < chance) {
            BlockPos cropPos = event.getPos();
            BlockPos farmlandPos = cropPos.below();

            // 下のブロックが耕地か確認
            if (event.getLevel().getBlockState(farmlandPos).is(Blocks.FARMLAND)) {
                // 耕地をただの土ブロックに戻す
                event.getLevel().setBlock(farmlandPos, Blocks.DIRT.defaultBlockState(), 3);
            }
        }
    }
}