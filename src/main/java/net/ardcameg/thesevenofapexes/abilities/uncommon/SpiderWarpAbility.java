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

import java.util.List;

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

        // チェックしたい座標のリストを作成
        BlockPos basePos = player.blockPosition();
        List<BlockPos> positionsToCheck = List.of(basePos, basePos.above());

        // 各座標をチェックして、蜘蛛の巣なら処理を実行
        for (BlockPos pos : positionsToCheck) {
            if (player.level().getBlockState(pos).is(Blocks.COBWEB)) {
                destroyCobweb(player, pos);
            }
        }
    }

    /**
     * 指定された位置の蜘蛛の巣を破壊し、アイテムと音を生成するヘルパーメソッド
     */
    private static void destroyCobweb(Player player, BlockPos pos) {
        player.level().removeBlock(pos, false);
        Block.popResource(player.level(), pos, new ItemStack(Items.STRING));

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, pos, SoundEvents.TRIPWIRE_DETACH, SoundSource.PLAYERS, 0.5f, 1.2f);
        }
    }
}