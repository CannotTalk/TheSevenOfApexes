// net/ardcameg/thesevenofapexes/abilities/uncommon/LuckyFlintAbility.java

package net.ardcameg.thesevenofapexes.abilities.uncommon;

import net.ardcameg.thesevenofapexes.Config;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class LuckyFlintAbility {
    private LuckyFlintAbility() {}

    /**
     * "幸運の火打石"の効果を適用する
     * @param event ブロック破壊イベント
     * @param flintCount "幸運の火打石"の所持数
     * @param prideMultiplier "傲慢"による効果倍率
     */
    public static void apply(BlockEvent.BreakEvent event, int flintCount, int prideMultiplier) {
        if (flintCount <= 0) return;

        Player player = event.getPlayer();
        // プレイヤーがクリエイティブモードの場合は何もしない
        if (player.isCreative()) return;

        // 破壊されたブロックが砂利かどうかをチェック
        if (event.getState().is(net.minecraft.world.level.block.Blocks.GRAVEL)) {
            int finalCount = flintCount * prideMultiplier;
            float chanceModifier = Config.luckyFlintFlintDropAdditionalChance.get().floatValue();
            float chance = chanceModifier * finalCount;

            // 確率判定
            if (player.level().random.nextFloat() < chance) {
                Block.popResource(
                        (Level) player.level(), // ワールド
                        event.getPos(), // ブロックの位置
                        new ItemStack(Items.FLINT) // ドロップさせるアイテム
                );
            }
        }
    }
}