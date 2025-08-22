package net.ardcameg.thesevenofapexes.abilities.uncommon;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class SpiderWarpAbility {
    private SpiderWarpAbility() {}

    /**
     * "蜘蛛の縦糸"の効果を適用する
     * @param player プレイヤー
     * @param warpCount "蜘蛛の縦糸"の所持数
     * @param prideMultiplier "傲慢"の倍率 (このアビリティでは未使用)
     */
    public static void apply(Player player, int warpCount, int prideMultiplier) {
        if (warpCount <= 0) return;

        // プレイヤーの足元にあるブロックを取得
        BlockPos playerPos = player.blockPosition();
        BlockState blockState = player.level().getBlockState(playerPos);

        // そのブロックが蜘蛛の巣かどうかをチェック
        if (blockState.is(Blocks.COBWEB)) {
            // 蜘蛛の巣を破壊して空気に変える (falseは連鎖的なブロック更新を行わない設定)
            player.level().removeBlock(playerPos, false);

            // 設計書通り、必ず糸を1つドロップさせる
            Block.popResource(player.level(), playerPos, new ItemStack(Items.STRING));

            // 効果が発動したことを示すサウンドを再生
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, playerPos, SoundEvents.TRIPWIRE_DETACH, SoundSource.PLAYERS, 0.5f, 1.2f);
            }
        }
    }
}